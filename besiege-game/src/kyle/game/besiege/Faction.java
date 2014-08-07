/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import kyle.game.besiege.army.Noble;
import kyle.game.besiege.location.Castle;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Location.LocationType;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.voronoi.Center;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

public class Faction {
	public static final int INCREASE_INTERVAL = 10; // every these seconds, relations will increase btw these factions
	public static final int CLOSE_CITY_FACTOR = 10; // this times num of close cities of one faction will decrease the relation with that faction
	public static Kingdom kingdom;
	private final int CHECK_FREQ = 5; // how frequently this manages stuff
	private final float ORDER_FACTOR = .9f; // what percent of nobles will be ordered to besiege a city
	public int index; // for keeping track of relations
	public String name; 
	public TextureRegion crest;
	public Color color;
	public Array<City> cities;
	public Array<Castle> castles;
	public Array<City> closeEnemyCities;
	public Array<Castle> closeEnemyCastles;
	public Array<Village> closeEnemyVillages;
	//	public Array<Location> closeEnemyLocations;
	public Array<Location> closeFriendlyLocations;
	public Array<Noble> nobles;
	public Array<Noble> unoccupiedNobles; // nobles that aren't ordered to besiege any cities
	public Array<Location> locationsToAttack; //  and sieges these nobles are currently maintaining
	public Array<Center> centers; // centers under influence of this faction
	public Array<Polygon> territory; // polygon of all centers

	private double timeSinceIncrease;  // can make more efficient by moving this to Kingdom
	private boolean hasChecked;

	public int faction_center_x; // only used when initializing map to keep factions close together
	public int faction_center_y; 
	
	private final int NOBLE_COUNT = 5; //TODO

	public static final int MAX_RELATION = 100;
	public static final int MIN_RELATION = -100;
	public static final int INIT_WAR_RELATION = -40; //when war is declared, this is the relation you will have
//	private final static int WAR_THRESHOLD = -10; //cross this and you're at war
	private final static int WAR_THRESHOLD = 10; //cross this and you're at war

	public static Array<Faction> factions;
	public static final Faction BANDITS_FACTION = new Faction("Bandits", "crestBandits", Color.BLACK);
	public static final Faction PLAYER_FACTION = new Faction("Rogue", "crestBlank", Color.WHITE);
	
	public static final Color BROWN = new Color(184/256.0f, 119/256.0f, 25/256.0f, 1);
	public static final Color OLIVE = new Color(107/256.0f, 138/256.0f, 48/256.0f, 1);
	public static final Color RED = new Color(148/256.0f, 34/256.0f, 22/256.0f, 1);
	public static final Color MAGENTA = new Color(122/256.0f, 41/256.0f, 83/256.0f, 1);
	public static final Color BLUE = new Color(53/256.0f, 128/256.0f, 144/256.0f, 1);
	public static final Color TAN = new Color(176/256.0f, 169/256.0f, 57/256.0f, 1);
	public static final Color PURPLE = new Color(73/256.0f, 54/256.0f, 158/256.0f, 1);
	public static final Color TEAL = new Color(57/256.0f, 170/256.0f, 115/256.0f, 1);
	public static final Color GREEN = new Color(41/256.0f, 72/256.0f, 33/256.0f, 1);

	private  static int factionCount;

	private static Array<Array<Integer>> factionRelations; // should break into multiple arrays
	//	private static Array<Array<Integer>> factionMilitaryAction; // is this worth it?
	//	private static Array<Array<Integer>> factionNearbyCities; // not needed, calced in real time?
	//	private static Array<Array<Integer>> factionTrade;

	public Faction(String name, String textureRegion, Color color) {
		this.name = name;
		crest = Assets.atlas.findRegion(textureRegion);
		this.color = color;

		nobles = new Array<Noble>();
		unoccupiedNobles = new Array<Noble>();
		cities = new Array<City>();
		castles = new Array<Castle>();
		centers = new Array<Center>();
		territory = new Array<Polygon>();
		//		closeEnemyLocations = new Array<Location>();
		closeEnemyCities = new Array<City>();
		closeEnemyCastles = new Array<Castle>();
		closeEnemyVillages = new Array<Village>();
		closeFriendlyLocations = new Array<Location>();
		locationsToAttack = new Array<Location>();
		timeSinceIncrease = 0;
	}

	public static void initializeFactions(Kingdom kingdom) {
		factions = new Array<Faction>();

		factionRelations = new Array<Array<Integer>>();
		//		factionMilitaryAction = new Array<Array<Integer>>();

		// add player faction (index 0) 
		createFaction(PLAYER_FACTION);

		// add bandits faction (index 1)
		createFaction(BANDITS_FACTION);	

		createFaction("Geinever", "crestWhiteLion", Color.DARK_GRAY);
		createFaction("Weyvel", "crestGreenTree", OLIVE);
		createFaction("Rolade", "crestOrangeCross", BROWN);
		createFaction("Myrnfar", "crestYellowStar", TAN);
		createFaction("Corson", "crestRedCross", RED);
		createFaction("Selven", "crestGreenStripe", GREEN);
		createFaction("Halmera", "crestBlueRose", BLUE);

		createFaction("Fernel", "crestRedAxe", Color.LIGHT_GRAY);
	//	createFaction("Draekal", "crestBlank", Color.BLACK);

		for (Faction f : factions) {
			f.kingdom = kingdom;
		}

		factions.get(2).declareWar(factions.get(3));

		//		factionRelations = new int[factionCount][factionCount];
		for (int i = 0; i < factionCount; i++) {
			for (int j = 0; j < factionCount; j++) {
				//				factionRelations[i][j] = -30;
				factionRelations.get(i).set(j, -30);
				factionRelations.get(j).set(i, -30);
			}
		}
		for (int i = 0; i < factionCount; i++) {
			//			factionRelations[i][i] = 100;
			factionRelations.get(i).set(i, 100);
		}
	}

	public static void factionAct(float delta) {
		factions.shrink();
		for (int i = 0; i < factions.size; i++)
			factions.get(i).act(delta);
	}
	public static void createFaction(String name, String textureRegion, Color color) {
		Faction faction = new Faction(name, textureRegion, color);
		factions.add(faction);
		faction.index = factions.indexOf(faction, true);
		for (int i = 0; i < factions.size; i++) {
			//			factionRelations[faction.index][i] = 0; // resets faction relations
			//			factionRelations[i][faction.index] = 0;
			if (factionRelations.size <= faction.index)
				factionRelations.add(new Array<Integer>());

			if (factionRelations.get(i).size <= faction.index)
				factionRelations.get(i).add(0);
			else factionRelations.get(i).set(faction.index, 0);

			if (factionRelations.get(faction.index).size <= i)
				factionRelations.get(faction.index).add(0);
			else factionRelations.get(faction.index).set(i, 0);
		}
		if (faction.index >= 1) {
			faction.declareWar(BANDITS_FACTION);
		}
	}
	public static void createFaction(Faction faction) {
		factions.add(faction);
		faction.index = factions.indexOf(faction, true);
		for (int i = 0; i < factions.size; i++) {
			//			factionRelations[faction.index][i] = 0; // resets faction relations
			//			factionRelations[i][faction.index] = 0;
			if (factionRelations.size <= faction.index)
				factionRelations.add(new Array<Integer>());

			if (factionRelations.get(i).size <= faction.index)
				factionRelations.get(i).add(0);
			else factionRelations.get(i).set(faction.index, 0);

			if (factionRelations.get(faction.index).size <= i)
				factionRelations.get(faction.index).add(0);
			else factionRelations.get(faction.index).set(i, 0);
		}
		if (faction.index >= 1) {
			faction.declareWar(BANDITS_FACTION);
		}
	}

	public void removeFaction(Faction faction) {
		factions.removeValue(faction, true);
		for (int i = 0; i < factions.size; i++) {
			//			factionRelations[faction.index][i] = -999; // 'deletes' faction relations
			//			factionRelations[i][faction.index] = -999;
			factionRelations.get(i).set(faction.index, null);
			factionRelations.get(faction.index).set(i, null);
		}
	}
	
	/** Initialize each faction's list of hostile cities, faction control map 
	 *  of centers, and village control. 
	 */
	public static void initializeFactionCityInfo() {
		System.out.println("initializing faction city info");
		for (Faction f : factions) { 
			f.initializeCloseLocations();
			f.centers.clear();
		}
		// update each faction's centers
		updateFactionCenters();
	}
	/** Update each faction's list of hostile cities, faction control map 
	 *  of centers, and village control. 
	 */
	public static void updateFactionCityInfo() {
		System.out.println("updating faction city info");
		for (Faction f : factions) { 
			f.updateCloseLocations();
			f.centers.clear();
		}
		updateFactionCenters();
	}
	
	/** updates faction centers and village factions
	 */
	private static void updateFactionCenters() {
		// update each faction's centers
		for (Center c : kingdom.getMap().connected) calcInfluence(c);
		kingdom.getMap().calcBorderEdges();
		for (Faction f : factions) {
			f.territory.clear();
			Array<Array<Center>> aaCenters = Map.calcConnectedCenters(f.centers);				
			for (Array<Center> centers : aaCenters) {
				//				System.out.println("working");
				
				f.territory.add(Map.centersToPolygon(centers));
			}
		}
		for (Village v : kingdom.villages) {
			v.changeFaction(getInfluenceAt(v.center));
		}
	}

	/**
	 * figures out which faction has the most influence on this center,
	 * and adds it to that faction's "centers" array
	 * @param center
	 */
	private static void calcInfluence(Center center) {
		Point centerPoint = new Point(center.loc.x, Map.HEIGHT - center.loc.y);
		Faction bestFaction = null;
		double bestScore = 0;

		// go through factions and calc influence, saving it if it's the highest
		for (Faction faction : factions) {
			double score = 0; // score is inverse of city distance
			for (City city : faction.cities) {
				double dist = Kingdom.distBetween(city, centerPoint);
				score += 1/dist;
			}
			for (Castle castle : faction.castles) {
				double dist = Kingdom.distBetween(castle, centerPoint);
				score += 1/dist;
			}
			if (score > bestScore) {
				bestScore = score;
				bestFaction = faction;
			}
		}

		if (bestFaction != null) {
			bestFaction.centers.add(center);
			center.faction = bestFaction;
		}
	}

	public static Faction getInfluenceAt(Center center) {
		for (Faction f : factions) {
			if (f.centers.contains(center, true)) return f;
		}

		System.out.println("no one controls that center");
		return null;

	}

	public void act(float delta) {
		timeSinceIncrease += delta;

		//		if (this == PLAYER_FACTION)
		//			System.out.println(this.name + " " + BANDITS_FACTION.name + getRelations(this, BANDITS_FACTION));

		if (timeSinceIncrease >= INCREASE_INTERVAL) {
			//			System.out.println(timeSinceIncrease);
			for (Faction f : factions)
				changeRelation(this, f, 0); // factor to increase relations by
			timeSinceIncrease = 0;
		}

		if (this != PLAYER_FACTION) autoManage(delta);
	}

	public void autoManage(float delta) {
		// send armies to capture/raid enemy cities/castles/villages
		// negotiate diplomacy, declare war/peace
		// that's it for now :D
		if (Kingdom.getTotalHour() % CHECK_FREQ == 0 && !hasChecked) {
			manageNobles();
			hasChecked = true;
		}
		else if (Kingdom.getTotalHour() % CHECK_FREQ != 0) hasChecked = false;
	}
	
	// reallocate nobles from this city if it's taken by an enemy
	public void allocateNoblesFrom(City city) {
		for (Noble noble : city.nobles) {
			if (this.cities.size == 0) noble.destroy(); // kill noble for now
			else this.getRandomCity().addNoble(noble);
		}
	}
	
	// create new nobles for this city
	public void allocateNoblesFor(City city) {
		assert(this.cities.contains(city, true));
		this.createNobleAt(city);
		// new noble created everytime a city is captured... but also nobles will die when they lose big battles
	}
	
	public void manageNobles() {
		// if a city doesn't have a noble, create a baron or earl
		// when a noble is upgraded to the next level (later, if a city is upgraded) add a fresh noob to replace them.
		
//		while (nobles.size < NOBLE_COUNT && cities.size >= 1) {
//			createNobleAt(cities.random());
//		}
		manageSieges();

		// figure out whether or not to organize a siege or something!
	}
	public void manageSieges() {
		if (locationsToAttack.size < 1 && unoccupiedNobles.size > 1 && closeEnemyCities.size > 1) {
			Location randomLocation = closeEnemyCities.random();
			if (randomLocation.underSiege()) return;
			orderSiegeOf(randomLocation);
		}
		//		if (nobles.size > 1 && closeEnemyCities.size > 1) {
		//			Noble random = nobles.random();
		//			if (!random.hasSpecialTarget()) {
		//				Location randomLoc = closeEnemyCities.random();
		//				random.setSpecialTarget(randomLoc);
		//				System.out.println("giving " + random.getName() + " special target " + randomLoc.getName());
		//			}
		//		}
	}
	public void orderSiegeOf(Location location) {
		locationsToAttack.add(location);
		int noblesToOrder = Math.max((int) (unoccupiedNobles.size * ORDER_FACTOR), 1);
		System.out.println(this.name + " is ordering a siege of " + location.getName() + " involving " + noblesToOrder + " nobles");
		BottomPanel.log(this.name + " is ordering a siege of " + location.getName() + " involving " + noblesToOrder + " nobles", "magenta");
		while (noblesToOrder > 0) {
			Noble randomNoble = unoccupiedNobles.random();
			setTask(randomNoble, location);
			noblesToOrder--;
		}
	}
	public void setTask(Noble noble, Location location) {
		noble.setSpecialTarget(location);
		this.unoccupiedNobles.removeValue(noble, true);
	}
	public void endTask(Noble noble) {
		noble.setSpecialTarget(null);
		this.unoccupiedNobles.add(noble);
	}
	public void createNobleAt(Location location) {
		Noble noble = new Noble(location.getKingdom(), location);
		// randomize size
		this.addNoble(noble);
		((City) location).nobles.add(noble);
		noble.goToNewTarget();
		location.setContainerForArmy(noble);
	}
	public void addNoble(Noble noble) {
		this.nobles.add(noble);
		this.unoccupiedNobles.add(noble);
	}
	public void removeNoble(Noble noble) {
		this.nobles.removeValue(noble, true);
		if (unoccupiedNobles.contains(noble, true)) unoccupiedNobles.removeValue(noble, true);
	}
	
	/** First updates each city's lists of close friendly and 
	 *  hostile cities, then updates this faction's lists based on 
	 *  that list */
	public void initializeCloseLocations() {
//		System.out.println(this.name + " initializing cities: ");
		this.closeEnemyCities.clear();
		this.closeEnemyCastles.clear();
		// find hostile locations near cities
		for (City c: cities) {
//			System.out.println("  close to " + c.getName() + ":");
			c.findCloseLocations();
//			System.out.println("  f  " + c.closestEnemyCities.size);
			for (City hostileCity : c.closestEnemyCities) {
				if (!closeEnemyCities.contains((City) hostileCity, true)) {
//					System.out.println("    " + hostileCity.getName());
					closeEnemyCities.add((City) hostileCity);
				}
			}
			for (Castle hostileCastle : c.closestEnemyCastles) {
				if (!closeEnemyCastles.contains((Castle) hostileCastle, true)) {
//					System.out.println("    " + hostileCastle.getName());
					closeEnemyCastles.add((Castle) hostileCastle);
				}
			}
		}
		// find hostile locations near castles
		for (Castle c: castles) {
//			System.out.println("  close to " + c.getName() + ":");
			c.findCloseLocations();
//			System.out.println("  f  " + c.closestEnemyCities.size);
			for (City hostile : c.closestEnemyCities) {
				if (!closeEnemyCities.contains((City) hostile, true)) {
//					System.out.println("    " + hostile.getName());
					closeEnemyCities.add((City) hostile);
				}
			}
			for (Castle hostile : c.closestEnemyCastles) {
				if (!closeEnemyCastles.contains((Castle) hostile, true)) {
//					System.out.println("    " + hostile.getName());
					closeEnemyCastles.add((Castle) hostile);
				}
			}
		}
	}	
	
	/** First updates each city's lists of close friendly and 
	 *  hostile cities, then updates this faction's lists based on 
	 *  that list
	 *  TODO review this process and make sure it works */
	public void updateCloseLocations() {
		System.out.println(this.name + " updating cities: ");
		this.closeEnemyCities.clear();
		this.closeEnemyCastles.clear();
		// find hostile locations near cities
		for (City c: cities) {
			System.out.println("  close to " + c.getName() + ":");
			c.updateCloseLocations();
			System.out.println("  f  " + c.closestEnemyCities.size);
			for (City hostileCity : c.closestEnemyCities) {
				if (!closeEnemyCities.contains((City) hostileCity, true)) {
					System.out.println("    " + hostileCity.getName());
					closeEnemyCities.add((City) hostileCity);
				}
			}
			for (Castle hostileCastle : c.closestEnemyCastles) {
				if (!closeEnemyCastles.contains((Castle) hostileCastle, true)) {
					System.out.println("    " + hostileCastle.getName());
					closeEnemyCastles.add((Castle) hostileCastle);
				}
			}
		}
		// find hostile locations near castles
		for (Castle c: castles) {
			System.out.println("  close to " + c.getName() + ":");
			c.updateCloseLocations();
			System.out.println("  f  " + c.closestEnemyCities.size);
			for (City hostile : c.closestEnemyCities) {
				if (!closeEnemyCities.contains((City) hostile, true)) {
					System.out.println("    " + hostile.getName());
					closeEnemyCities.add((City) hostile);
				}
			}
			for (Castle hostile : c.closestEnemyCastles) {
				if (!closeEnemyCastles.contains((Castle) hostile, true)) {
					System.out.println("    " + hostile.getName());
					closeEnemyCastles.add((Castle) hostile);
				}
			}
		}
	}	

//	/** First updates each city's lists of close friendly and 
//	 *  hostile cities, then updates this faction's lists based on 
//	 *  that list
//	 *  TODO review this process and make sure it works */
//	public void updateCloseHostileCities() {
//		Array<City> tempCloseEnemyCities = new Array<City>();
//		Array<City> tempCloseFriendlyCities = new Array<City>();
//		//System.out.println(this.name + ":");
//
//
//		for (City c: cities) {
//			c.updateCloseLocations();
//			for (City hostile : c.getClosestHostileLocations()) {
//				tempCloseEnemyCities.add(hostile);
//				//this makes factions dislike factions with nearby cities
//				// compares new array with old array (can be better)
//				if (!closeEnemyLocations.contains(hostile, true)) {
//					if (hostile.getFaction() == this) System.out.println("hostile: " + hostile.getName());
//					changeRelation(this, hostile.getFaction(), -1*CLOSE_CITY_FACTOR);
//				}	
//			}
//			for (City friendly : c.getClosestFriendlyCities()) {
//				if (friendly.getFaction() != this) {
//					tempCloseFriendlyCities.add(friendly);
//					//this makes factions dislike factions with nearby cities
//					if (!closeFriendlyLocations.contains(friendly, true) && friendly.getFaction().index != this.index) {
//						//						System.out.println("new friendly city: " + friendly.getName());
//						changeRelation(this, friendly.getFaction(), -1*CLOSE_CITY_FACTOR);
//					}	
//				}
//			}
//		}
//		closeEnemyLocations = new Array<City>(tempCloseEnemyCities);
//		closeFriendlyLocations = new Array<City>(tempCloseFriendlyCities);
//	}

//	/** Calculates negative effect of close cities on the relations
//	 *  between two factions
//	 * 
//	 * @param that
//	 * @return effect on relations
//	 */
//	public int getCloseCityEffect(Faction that) {
//		if (this.index == that.index) return 0;
//		int totalEffect = 0;
//		if (isAtWar(this, that)) {
//			for (City c : closeEnemyLocations)
//				if (c.getFaction() == that)	totalEffect += -1*CLOSE_CITY_FACTOR;
//		}
//		else {
//			for (City c : closeFriendlyLocations)
//				if (c.getFaction() == that) totalEffect += -1*CLOSE_CITY_FACTOR;
//		}
//		return totalEffect;
//	}

	public int getRelationsWith(Faction that) {
		return getRelations(this, that);
	}

	public void declareWar(Faction that) {
		if (!Faction.isAtWar(this,  that))
			declareWar(this, that);
	}

	public void goRogue() { // just for testing, declares war on all factions other than this one
		for (int i = 0; i < factions.size; i++)
			if (i != index) declareWar(this, factions.get(i));
	}

	public int getTotalWealth() {
		int total = 0;
		for (City c : cities)
			total += c.getParty().wealth;
		return total;
	}


	public static int getRelations(Faction faction1, Faction faction2) {
		return factionRelations.get(faction1.index).get(faction2.index);
	}
	
	/** return whether or not these two factions are at war. 
	 * note that the same faction can't be at war with itself.
	 * @param faction1
	 * @param faction2
	 * @return
	 */
	public static boolean isAtWar(Faction faction1, Faction faction2) {
		if (faction1 == faction2) return false;
		return (getRelations(faction1, faction2) < WAR_THRESHOLD);
	}
	public static void setAtWar(Faction faction1, Faction faction2) {
		//		factionRelations[faction1][faction2] = WAR_THRESHOLD-1;
		//		factionRelations[faction2][faction1] = WAR_THRESHOLD-1;
		factionRelations.get(faction1.index).set(faction2.index, INIT_WAR_RELATION);
		factionRelations.get(faction2.index).set(faction1.index, INIT_WAR_RELATION);
	}
	public static void setNeutral(Faction faction1, Faction faction2) {
		factionRelations.get(faction1.index).set(faction2.index, 0);
		factionRelations.get(faction2.index).set(faction1.index, 0);

		//		factionRelations[faction1][faction2] = 0;
		//		factionRelations[faction2][faction1] = 0;
	}

	public static void makePeace(Faction faction1, Faction faction2) {
		//		BottomPanel.log(faction1.name + " and " + faction2.name + " have signed a peace agreement!", "magenta");
		setNeutral(faction1, faction2);
	}
	public static void declareWar(Faction faction1, Faction faction2) {
		//		BottomPanel.log(faction1.name + " and " + faction2.name + " have declared war!", "magenta");
		setAtWar(faction1, faction2);
	}

	public static void changeRelation(Faction faction1, Faction faction2, int delta) {
		int initialRelation = factionRelations.get(faction1.index).get(faction2.index);
		int newRelation;
		if (initialRelation + delta >= MAX_RELATION) newRelation = MAX_RELATION;
		else if (initialRelation + delta <= MIN_RELATION) newRelation = MIN_RELATION;
		else newRelation = initialRelation + delta;
		if (initialRelation >= WAR_THRESHOLD && newRelation < WAR_THRESHOLD) ;
		//			BottomPanel.log(faction1.name + " and " + faction2.name + " have declared war!", "magenta");
		else if (initialRelation < WAR_THRESHOLD && newRelation >= WAR_THRESHOLD) 
			makePeace(faction1, faction2);
		factionRelations.get(faction1.index).set(faction2.index, newRelation);
	}

	//	public static void calcAllRelations() {
	//		for (Faction f : factions) { 
	//			for (int i = 0; i < factions.size; i++) {
	//				int base = 0;
	////				base += factionMilitaryAction.get(f.index).get(i); // Military actions
	//				base += f.getCloseCityEffect(factions.get(i));	   // Borders
	//				base += factions.get(i).getCloseCityEffect(f);	   // (Borders is 2-way)
	//				factionRelations.get(i).set(f.index, base); 	   // can make more efficient
	//				factionRelations.get(f.index).set(i, base);
	//			}
	//		}
	//	}

	public static Faction get(int index) {
		return factions.get(index);
	}

	public City getRandomCity() {
		if (cities.size > 0) {
			return cities.random();
		}
		else return null;
	}
}
