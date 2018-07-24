/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 * 
 * Castle.java
 ******************************************************************************/

package kyle.game.besiege.location;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Patrol;
import kyle.game.besiege.army.RaidingParty;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

public class Castle extends Location {
	private final static int SCOUT_TRAVEL_FACTOR = 5;
	private final static float SCALE = 10;
	private final static float closeCityDistance = 500; // cities within this distance are considered "close" for trading, raiding, etc
	private final static double MERCHANT_GAIN = .008; // calculates merchant's wealth which goes to other cities.
	private  int patrolCost;
	private int raiderCost;
	
//	private StrictArray<Castle> closestFriendlyCities;
//	private StrictArray<Castle> closestEnemyCities;
	
//	private StrictArray<Patrol> scouts;
	private StrictArray<RaidingParty> raiders;
	private boolean[] raiderExists;
	
//	private StrictArray<Village> villages;
//	private StrictArray<PointH> villageSpots;

	public Castle(){}
	
	public Castle(Kingdom kingdom, String name, int index, Faction faction, float posX,
                  float posY, Center center, Corner corner) {
		super(kingdom, name, index, faction, posX, posY, PartyType.Type.CASTLE_GARRISON, center, corner);
		this.type = LocationType.CASTLE;
				
		setWealth(calcInitialWealth());
		
		this.DAILY_WEALTH_INCREASE_BASE = 0;
//
        if (this.getFaction() == null) {
            throw new AssertionError();
        }
        if (getFaction().castles == null) {
            System.out.println(getFaction().name);
        }

		this.getFaction().castles.add(this);
		
//		scouts = new StrictArray<Patrol>();
	
		raiders = new StrictArray<RaidingParty>();
		raiderExists = new boolean[Kingdom.castleCount];

//		closestFriendlyCities = new StrictArray<Castle>();
//		closestEnemyCities = new StrictArray<Castle>();
//		villages = new StrictArray<Village>();
		
//		this.merchantCost = PartyType.MERCHANT.maxWealth;
//		this.patrolCost = PartyType.PATROL.maxWealth;
//		this.raiderCost = PartyType.RAIDING_PARTY.maxWealth;
		
		this.patrolCost = 10; // PartyType.PATROL.maxWealth;
		this.raiderCost = 10; //
		
//		this.POP_MIN = 100;
//		this.POP_MAX = 100;
		
		setTextureRegion("Castle");
		setScale(SCALE);
		initializeBox();
	}
	
	public int calcInitialWealth() {
		return 0;
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
//		Assets.pixel18.setColor(Kingdom.factionColors.get(getFaction()));
//		String toDraw = getName() + " (" + getParty().wealth + ")";
//		Assets.pixel18.draw(batch, toDraw, getX() - (int) (3*toDraw.length()), getY()-15);
//		Assets.pixel18.setColor(Color.WHITE);
	}
	
	@Override
	public void autoManage() {
		// Organize patrols
		int patrolCount = (int) (getWealth()/(100));
		if (getPatrols().size < patrolCount) {
			this.loseWealth(patrolCost);
			createScout();
		}
//		// Organize merchants
//		for (Castle city : closestFriendlyCities) {
//			if (!merchantExists[closestFriendlyCities.indexOf(city, true)] && this.getParty().wealth >= merchantCost) {
//				this.loseWealth(merchantCost);
//				int merchantWealth = merchantCost + (int) (Kingdom.distBetween(this, city)*MERCHANT_GAIN) + 1;
//				createMerchant(merchantWealth, city);
//			}
//		}
//		// Organize raiding parties
//		int raiderCount = (int) (getParty().wealth/(raiderCost*2));
//		if (getRaiders().size < raiderCount) {
//			if (closestEnemyCities.random() != null) {
//				this.loseWealth(raiderCost);
//				createRaider();
//			}
//		}
	}
	
	@Override
	public boolean shouldIncreaseGarrison() {
		if (this.garrison.party.getAtk() > 1000) return false;
		return this.garrison.party.getAtk() < (this.getFaction().getTotalWealth() * 0.1 / this.getFaction().castles.size);
//		return this.getWealth() > 100;
	}
	
	public void createScout() {
		if (this.patrols.size >= Location.MAX_PATROLS) return;
		Patrol scout = new Patrol(getKingdom(), this);
		scout.patrolDist = 200;
		scout.proximityToBase = .1;
		scout.setName(getFaction().name + " Scout");
		scout.patrolAround(this);
		getKingdom().addArmy(scout);
		getPatrols().add(scout);
		setContainerForArmy(scout);
	}
	
	public void removePatrol(Patrol patrol) {
		patrols.removeValue(patrol, true);
	}
	public StrictArray<Patrol> getPatrols() {
		return patrols;
	}
//
//	public void createRaider() {
//		Castle targetCity = getCloseEnemyCity();
//		RaidingParty raider = new RaidingParty(getKingdom(), "Raider", getFaction(), getCenterX(), getCenterY());
//		raider.raidAround(targetCity);
//		raider.setDefaultTarget(this);
//		getKingdom().addArmy(raider);
//		raiders.add(raider);
//		raiderExists[closestEnemyCities.indexOf(targetCity, true)] = true;
//		setContainerForArmy(raider);
//	}
//	
//	public void removeRaider(RaidingParty raider) {
//		raiderExists[closestEnemyCities.indexOf(raider.getRaidAround(), true)] = false;
//		raiders.removeValue(raider, true);
//	}
	
	@Override
	public void updateToHire() {
//		if (playerIn) System.out.println("updating to hire");
		this.toHire = this.nextHire;
		// some random stuff should happen here, small chance of getting a really good crop of soldiers
		// high chance of sucky ones!
//		double random = Math.random();
		// TODO
		this.nextHire = PartyType.generatePT(PartyType.Type.CASTLE_HIRE, this).generate();
//		if (random > .9) 		this.nextHire = PartyType.generate(PartyType.Type.CITY_HIRE);
//		else if (random > .7)	this.nextHire = PartyType.CITY_HIRE_2.generate(); // second best
//		else 					this.nextHire = PartyType.CITY_HIRE_3.generate(); // worst
	}
//	
//	public void updateClosestLocations() {
//		closestFriendlyCities.clear();
//		closestEnemyCities.clear();
//		// updates when a city changes hands
//		for (Castle that : getKingdom().castles) {
//			if (that != this && Kingdom.distBetween(this, that) < closeCityDistance) {
//				if (!Faction.isAtWar(getFaction(), that.getFaction())) {
//					closestFriendlyCities.add(that);
//				}
//				else closestEnemyCities.add(that);
//			}
//		}
//	}
	
//	public Castle getCloseEnemyLocation() {
//		return closestEnemyCities.random();
//	}
//	public StrictArray<Castle> getClosestHostileLocations() {
//		return closestEnemyCities;
//	}
//	public StrictArray<Castle> getClosestFriendlyCities() {
//		return closestFriendlyCities;
//	}
	// moved to location
//	public void changeFaction(Faction f) {
//		BottomPanel.log(f.name + " has taken " + this.getName());
//		this.getFaction().castles.removeValue(this,true);
//		this.setFaction(f);
//		this.getFaction().castles.add(this);
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
//	public StrictArray<Village> getVillages() {
//		return villages;
//	}
//	
	public StrictArray<RaidingParty> getRaiders() {
		return raiders;
	}

}
