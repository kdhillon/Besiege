/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.location;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Map;
import kyle.game.besiege.Point;
import kyle.game.besiege.army.Merchant;
import kyle.game.besiege.army.Noble;
import kyle.game.besiege.army.Patrol;
import kyle.game.besiege.army.RaidingParty;
import kyle.game.besiege.geom.PointH;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class City extends Location {
	private final static float SCALE = 7;
	private final static int MAX_PATROLS = 3;
	private static int CITY_UPPER_VALUE = Assets.cityArray.size; // highest number of cities possible
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

	public City(Kingdom kingdom, String name, int index, Faction faction, float posX,
			float posY, int wealth) {
		super(kingdom, name, index, faction, posX, posY, PartyType.CITY_GARR_1.generate());
		this.type = LocationType.CITY;
				
		getParty().wealth = wealth;
		
		POP_MIN = 2000;
		POP_MAX = 15000;
		
		this.population = Math.random()*(POP_MAX - POP_MIN) + POP_MIN;
		
		this.DAILY_WEALTH_INCREASE_BASE = 5;
		this.DAILY_POP_INCREASE_BASE = 5;
		
		this.getFaction().cities.add(this);
				
		merchants = new Array<Merchant>();
		merchantExists = new boolean[CITY_UPPER_VALUE];
		
		raiders = new Array<RaidingParty>();
		raiderExists = new boolean[CITY_UPPER_VALUE];
		
		nobles = new Array<Noble>();

//		closestFriendlyCities = new Array<City>();
//		closestEnemyLocations = new Array<City>();
//		villages = new Array<Village>();
		
//		this.merchantCost = 10;// Merchant.MAX_WEALTH; //PartyType.MERCHANT.maxWealth;
//		this.patrolCost = 10; // PartyType.PATROL.maxWealth;
//		this.raiderCost = 10; //PartyType.RAIDING_PARTY.maxWealth;
		
		setTextureRegion("City");
		setScale(SCALE);
		initializeBox();
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}
	
	@Override
	public void autoManage() {
		// Organize patrols
		int patrolCount = Math.min((int) (getParty().wealth/(500)), MAX_PATROLS);
		if (getPatrols().size < patrolCount) {
			createPatrol();
		}
		// Organize merchants
		for (City city : closestFriendlyCities) {
			if (!merchantExists[closestFriendlyCities.indexOf(city, true)]) {
				createMerchant(city);
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
		Patrol patrol = new Patrol(getKingdom(), this);
		patrol.proximityToBase = .5;
		patrol.patrolAround(this);
		getKingdom().addArmy(patrol);
		getPatrols().add(patrol);
		setContainerForArmy(patrol);
	}
	
	public void createMerchant(City goal) {
		if (this != goal) {
			Merchant merchant = new Merchant(getKingdom(), this, goal);
			this.changeWealth((int) (-merchant.getWealth()*MERCHANT_COST_FACTOR));
			getKingdom().addArmy(merchant);
			merchants.add(merchant);
			merchantExists[closestFriendlyCities.indexOf(goal, true)] = true;
			setContainerForArmy(merchant);
		}
	}
	
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
		noble.home = this;
		noble.updateName();
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
		double random = Math.random();
		if (random > .9) 		this.nextHire = PartyType.CITY_HIRE_1.generate();
		else if (random > .5)	this.nextHire = PartyType.CITY_HIRE_2.generate(); // second best
		else 					this.nextHire = PartyType.CITY_HIRE_3.generate(); // worst
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
