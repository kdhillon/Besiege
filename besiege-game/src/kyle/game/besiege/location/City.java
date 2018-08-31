/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.location;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Random;
import kyle.game.besiege.army.*;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.voronoi.Biomes;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

public class City extends Location {
	private final static float CITY_WEALTH_FACTOR = 0.6f; // arbitrary, this times pop = wealth
	private final static float INITIAL_WEALTH_VARIANCE_MIN = 0.6f;
	private final static float INITIAL_WEALTH_VARIANCE_MAX = 2f;
	
	private final static float SCALE = 7;
	private final static int MAX_PATROLS = 3;
	private static final int MAX_FARMERS = 4;
    private static final int MAX_HUNTERS = 4;
    //	private static int CITY_UPPER_VALUE = Assets.cityNames.size; // highest of cities possible
	private static double MERCHANT_COST_FACTOR = .98;

	private final static float closeCityDistance = 500; // cities within this distance are considered "close" for trading, raiding, etc
	private final static double MERCHANT_GAIN = .008; // calculates merchant's wealth which goes to other cities.
//	private final int patrolCost;
//	private final int raiderCost;
//	private final int merchantCost;
	
	private Array<Merchant> merchants;
	public Array<Noble> nobles;
	private boolean[] merchantExists;
	private Array<RaidingParty> raiders;
	private boolean[] raiderExists;

	public enum Size {
	    TOWN, CITY, LARGE_CITY
    }

	public City() {}
	
	public City(Kingdom kingdom, String name, int index, Faction faction, float posX,
			float posY, Center center, Corner corner) {
		super(kingdom, name, index, faction, posX, posY, PartyType.Type.CITY_GARRISON, center, corner);
		this.type = LocationType.CITY;
						
		POP_MIN = 500;
		POP_MAX = 15000;
		
		this.population = Random.getRandomInRange(POP_MIN, POP_MAX);
				
		this.DAILY_WEALTH_INCREASE_BASE = 5;
		this.DAILY_POP_INCREASE_BASE = 5;
		
		this.getFaction().cities.add(this);
				
		merchants = new Array<Merchant>();
		merchantExists = new boolean[Kingdom.cityCount];
		
		raiders = new Array<RaidingParty>();
		raiderExists = new boolean[Kingdom.cityCount];
		
		nobles = new Array<Noble>();

        this.farmerCount = getPop() / (POP_MAX / MAX_FARMERS); // arbitrary
        this.hunterCount = getPop() / (POP_MAX / MAX_HUNTERS); // arbitrary

//		closestFriendlyCities = new Array<City>();
//		closestEnemyLocations = new Array<City>();
//		villages = new Array<Village>();
		
//		this.merchantCost = 10;// Merchant.MAX_WEALTH; //PartyType.MERCHANT.maxWealth;
//		this.patrolCost = 10; // PartyType.PATROL.maxWealth;
//		this.raiderCost = 10; //PartyType.RAIDING_PARTY.maxWealth;

        if (cultureType.name.equals("Plains")) {
            setTextureRegion("tipi4");
        } else if (cultureType.name.equals("Forest")) {
            setTextureRegion("longhouse4");
        } else if (cultureType.name.equals("Tundra")) {
            if (center != null && (center.biome == Biomes.SNOW))
                setTextureRegion("inuitcitywhite");
            else setTextureRegion("inuitcity");
            farmerCount = 0;
        } else if (cultureType.name.equals("Desert")){
            setTextureRegion("temple2");
        } else {
            setTextureRegion("City");
        }

        farmers = new Array<Farmer>();

        setScale(SCALE);
		initializeBox();
	}

	@Override
    public void dailyPopIncrease() {
	    Size prevSize = getSize();
	    super.dailyPopIncrease();
	    if (getSize() != prevSize) {
	        handleSizeChange(prevSize, getSize());
        }
    }

    private void handleSizeChange(Size prevSize, Size newSize) {
	    // TODO upgrade city
    }

	public Size getSize() {
        int index = getPop() / (POP_MAX / Size.values().length);
        return Size.values()[index];
	}

	public String getSizeString() {
	    Size size = getSize();
	    if (size == Size.TOWN) return "Town";
	    if (size == Size.CITY) return "City";
	    if (size == Size.LARGE_CITY) return "Large City";
	    throw new AssertionError();
    }
	
	@Override
	protected void setCenter(Center center) {
		super.setCenter(center);
		setWealth(calcInitialWealth());
	}
	
	@Override
	protected void setCorner(Corner corner) {
		super.setCorner(corner);
		setWealth(calcInitialWealth());
	}
	
	public int calcInitialWealth() {
		// TODO base this off of nearby land, like surrounding land.
		return (int) ((CITY_WEALTH_FACTOR + this.getAvgAdjacentWealth()) * population);
	}
	
	public int calcPopWealth() {
		return (int) (this.population * CITY_WEALTH_FACTOR);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}
	
	@Override
	public void autoManage() {
		// Organize patrols
		int patrolCount = Math.min((int) (getWealth()/(500)), MAX_PATROLS);
		if (getPatrols().size < patrolCount) {
			createPatrol();
		}

        if (farmers.size < farmerCount) {
            createFarmer();
        }
		
		// this is an overly complicated way of doing it
		
		// instead: 
		// generate one merchant every x turns depending on city wealth.
		// 		merchant will decide where to travel on its own. 
		// 		can use a random decision with weighting to pick friendly city or village:
		
		// Organize merchants
		for (City city : closestFriendlyCities) {
			if (!merchantExists[closestFriendlyCities.indexOf(city, true)]) {
//				createMerchant(city);
			}
		}
		// do raiders later
//		// Organize raiding parties
//		int raiderCount = (int) (getParty().wealth/(raiderCost*2));
//		if (getRaiders().size < raiderCount) {
//			if (getCloseEnemyVillage() != null) {
//				this.loseWealth(raiderCost);
////				createRaider();
//			}
//		}
	}
	
	public void createPatrol() {
		if (this.patrols.size >= Location.MAX_PATROLS) return;

		Patrol patrol = new Patrol(getKingdom(), this);
		patrol.proximityToBase = .5;
		patrol.patrolAround(this);
		getKingdom().addArmy(patrol);
		getPatrols().add(patrol);
		setContainerForArmy(patrol);
	}
	
//	public void createMerchant(City goal) {
//		if (this != goal) {
//			Merchant merchant = new Merchant(getKingdom(), this, goal);
//			this.changeWealth((int) (-merchant.getWealth()*MERCHANT_COST_FACTOR));
//			getKingdom().addArmy(merchant);
//			merchants.add(merchant);
//			merchantExists[closestFriendlyCities.indexOf(goal, true)] = true;
//			setContainerForArmy(merchant);
//		}
//	}
	
	public void removeMerchant(Merchant merchant) {
		// could make if not -1, but this will stress test
//		if (merchant.getGoal().getFaction() == merchant.getFaction())
		if (closestFriendlyCities.indexOf(merchant.getGoal(), true) >= 0)
			merchantExists[closestFriendlyCities.indexOf(merchant.getGoal(), true)] = false;
		merchants.removeValue(merchant, true);
	}

	public void addNoble(Noble noble) {
		assert(!this.nobles.contains(noble, true));
		this.nobles.add(noble);
		noble.setHome(this);
	}
	
	public float getAvgAdjacentWealth() {
		float sum = 0;
		int count = 0;
		if (this.getCenter() != null) {
			count++;
			sum += this.getCenter().wealth;
			for (Center c : getCenter().neighbors) {
				if (c.wealth > 0) {
					count++;
					sum += c.wealth;
				}
			}
		}
		else if (this.getCorner() != null) {
			for (Center c : getCorner().touches) {
				if (c.wealth > 0) {
					count++;
					sum += c.wealth;
				}
			}
		}
		float avg = (sum / count);
		System.out.println("adjacent wealth: " + avg);
		return avg;
	}
	
//	public void createRaider() {
//		Village targetVillage = getCloseEnemyVillage();
//		RaidingParty raider = new RaidingParty(getKingdom(), "Raider", getFaction(), getCenterX(), getCenterY());
//		raider.raidAround(targetCity);
//		raider.setDefaultTarget(this);
//		getKingdom().addArmy(raider);
//		raiders.add(raider);
//		raiderExists[closestEnemyLocations.indexOf(targetCity, true)] = true;
//		setContainerForArmy(raider);
//	}
	
	public void removeRaider(RaidingParty raider) {
//		raiderExists[closestEnemyLocations.indexOf(raider.getRaidAround(), true)] = false;
//		raiders.removeValue(raider, true);
	}
	
	@Override
	public void updateToHire() {
//		if (playerIn) System.out.println("updating to hire");
		this.toHire = this.nextHire;
		// some random stuff should happen here, small chance of getting a really good crop of soldiers
		// high chance of sucky ones!
//		double random = Math.random();
		this.nextHire = PartyType.generatePT(PartyType.Type.CITY_HIRE, this).generate();
//		System.out.println("updating to hire for " + this.getName());
//		System.out.println(toHire.getAvgSpd());
//		if (random > .9) 		this.nextHire = PartyType.CITY_HIRE_1.generate();
//		else if (random > .5)	this.nextHire = PartyType.CITY_HIRE_2.generate(); // second best
//		else 					this.nextHire = PartyType.CITY_HIRE_3.generate(); // worst
	}
	
//	public void updateClosestLocations() {
//		closestFriendlyCities.clear();
//		closestEnemyLocations.clear();
//		// updates when a city changes hands
//		for (City that : getKingdom().getCities()) {
//			if (that != this && Kingdom.distBetween(this, that) < closeCityDistance) {
//				if (!Faction.isAtWar(getFaction(), that.getFaction())) {
//					closestFriendlyCities.add(that);
//				}
//				else closestEnemyLocations.add(that);
//			}
//		}
//	}
	
//	public City getCloseEnemyLocation() {
//		return closestEnemyLocations.random();
//	}
//	public Array<City> getClosestHostileLocations() {
//		return closestEnemyLocations;
//	}
//	public Array<City> getClosestFriendlyCities() {
//		return closestFriendlyCities;
//	}
	// moved to locations
//	public void changeFaction(Faction f) {
//		BottomPanel.log(f.name + " has taken " + this.getName());
//		this.getFaction().cities.removeValue(this,true);
//		this.setFaction(f);
//		this.getFaction().cities.add(this);
////		for (Village v : villages) v.setFaction(f);
//		Faction.updateFactionCityInfo();
//	}
		
//	public void createVillages() {
//		int maxVillages = 3; // randomize?
//		for (int i = 0; i < maxVillages; i++) {
//			System.out.println("village spots: " + villageSpots.size);
//
////			while(!createVillage());
//			if (villageSpots.size > 0) createVillage();
//			else break;
//		}
//	}
	
//	public void createVillage() {
//		float x;
//		float y;
//
//		PointH loc = villageSpots.random();
//		x = (float) loc.x;
//		y = (float) loc.y;
//		
//		getKingdom().getMap().availableLocationSites.removeValue(loc, false);
//		
//		addVillage(new Village(getKingdom(), getName() + " Village", -1, getFaction(), x, Map.HEIGHT - y, 0));
//	}
	
	// no longer needed
//	public boolean villageTooClose(float x, float y) {
//		for (Village village : villages) {
//			if (Kingdom.distBetween(village, new Point(x, y)) <= villageSeparation) {
//				return true;
//			}
//		}
//		
//		for (City city : getKingdom().getCities()) {
//			for (Village village : city.getVillages()) {
//				if (Kingdom.distBetween(village, new Point(x, y)) <= villageSeparation) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}

//	public void addVillage(Village village) {
//		villages.add(village);
//		this.getKingdom().addActor(village);
//		village.setParent(this);
//	}
//	public Array<Village> getVillages() {
//		return villages;
//	}
//	
	public Array<RaidingParty> getRaiders() {
		return raiders;
	}

}
