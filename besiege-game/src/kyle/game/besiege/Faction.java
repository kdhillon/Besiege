/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import kyle.game.besiege.army.Noble;
import kyle.game.besiege.battle.BattleSim;
import kyle.game.besiege.battle.OldBattle;
import kyle.game.besiege.location.Castle;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.ObjectLabel;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.party.UnitLoader;
import kyle.game.besiege.voronoi.Center;

/* time to make factions really awesome: 
 * nations can be at war, at peace, or allies with other nations
 * relations are re-calculated every day based on:
 * 		
 * 		nearby cities/troops (negative)
 * 		long-term peace (positive)
 * 		alliance (positive)
 * 		past wars (negative)
 * 		current wars (negative)
 */

public class Faction {
    public static final String INDEPENDENT_NAME = "Independent";
	//	public static final int INCREASE_INTERVAL = 10; // every these seconds, relations will increase btw these factions
	public static final int CLOSE_CITY_FACTOR = 2; // this times num of close cities of one faction will decrease the relation with that faction
	private static final int FACTION_UPDATE_FREQ = 5; // every x hours, factions update nobles and diplomacy
	//	private static final int DIPLOMACY_CHECK_FREQ = 21;
	private static final float ORDER_FACTOR = .9f; // what percent of nobles will be ordered to besiege a city
	private static final int WAR_BONUS = 20;
	private static final int ALLIANCE_BONUS = 20;
	private static final int DAILY_PEACE_EFFECT = 1;
	private static final int DAILY_WAR_EFFECT = 1;

	private static final double PEACE_PROBABILITY = 0.0001; // how likely it is that on any given update, factions will 
	private static final double WAR_PROBABILITY = 0.0001; // how likely it is that on any given update, factions will 

	private static final int STATIC_WAR_EFFECT = -20;
	private static final int STATIC_PEACE_EFFECT = 10;
	private static final int STATIC_ALLY_EFFECT = 10;

	private static final int PEACE_THRESHOLD = -10;
	//	private static final int INIT_WAR_EFFECT = 10;

	//	public static final int MAX_RELATION = 100;
	//	public static final int MIN_RELATION = -100;
	//	public static final int INIT_WAR_RELATION = -40; //when war is declared, this is the relation you will have
	//	private final static int WAR_THRESHOLD = -10; //cross this and you're at war
	//	public static final int WAR_THRESHOLD = 10; //cross this and you're at war

	public static Faction BANDITS_FACTION;
	public static Faction ROGUE_FACTION;
	public static boolean initialized = false;

	public static final Color BROWN = new Color(184/256.0f, 119/256.0f, 25/256.0f, 1);
	public static final Color OLIVE = new Color(107/256.0f, 138/256.0f, 48/256.0f, 1);
	public static final Color RED = new Color(148/256.0f, 34/256.0f, 22/256.0f, 1);
	public static final Color MAGENTA = new Color(122/256.0f, 41/256.0f, 83/256.0f, 1);
	public static final Color BLUE = new Color(53/256.0f, 128/256.0f, 144/256.0f, 1);
	public static final Color TAN = new Color(176/256.0f, 169/256.0f, 57/256.0f, 1);
	public static final Color PURPLE = new Color(73/256.0f, 54/256.0f, 158/256.0f, 1);
	public static final Color TEAL = new Color(57/256.0f, 170/256.0f, 115/256.0f, 1);
	public static final Color GREEN = new Color(41/256.0f, 72/256.0f, 33/256.0f, 1);

	public Kingdom kingdom;

	public int index; // for keeping track of relations
	public String name; 
	public String textureName;
//	transient public TextureRegion crest; // will have to load this separately
	public Crest crest;
	public CultureType cultureType;
	
	public Color color = Color.WHITE;
	public StrictArray<City> cities;
	public StrictArray<Castle> castles;
	public StrictArray<Village> villages;
	
	public StrictArray<City> closeEnemyCities;
	public StrictArray<Castle> closeEnemyCastles;
	public StrictArray<Village> closeEnemyVillages;
	//	public StrictArray<Location> closeEnemyLocations;
	public StrictArray<City> closeFriendlyCities;
	public StrictArray<Castle> closeFriendlyCastles;
	//	public StrictArray<Village> closeFriendlyVillages;
	public StrictArray<Noble> nobles;
	public StrictArray<Noble> unoccupiedNobles; // nobles that aren't ordered to besiege any cities
	public StrictArray<Location> locationsToAttack; //  and sieges these nobles are currently maintaining
	public StrictArray<Center> centers; // centers under influence of this faction
	public StrictArray<Polygon> territory; // polygon of all centers
	public StrictArray<Faction> atPeace;
	public StrictArray<Faction> atWar;
//	public StrictArray<Integer> atWarInt;
	public StrictArray<Faction> allies;

	// this keeps track of historical relations -- long peace increases this, long war decreases this
	public StrictArray<Integer> warEffects; // negative if war, positive if peace, etc
	//	public StrictArray<Faction> allied;

	// this was the cause of a lot of Kryo errors
	// NOTE: KRYO ERRORS READ BOTTOM TO TOP
	public transient ObjectLabel label;
	public transient ObjectLabel label2;
	public transient Image miniCrest;

	//	private double timeSinceIncrease;  // can make more efficient by moving this to Kingdom
	private boolean hasChecked;

	public int faction_center_x; // only used when initializing map to keep factions close together
	public int faction_center_y; 

	private final int NOBLE_COUNT = 5; //TODO

	//	private static StrictArray<StrictArray<Integer>> factionMilitaryAction; // is this worth it?
	//	private static StrictArray<StrictArray<Integer>> factionNearbyCities; // not needed, calced in real time?
	//	private static StrictArray<StrictArray<Integer>> factionTrade;

	public static RandomCrestGenerator rc = new RandomCrestGenerator();
	
	// for Kryo
	public Faction() {
		// need to load crest after this is created.
	}
	public Faction(Kingdom kingdom) {
	    this(kingdom, null, null, null);
    }

	/**
	 * Creates a faction. if name is null, will generate a random name. If crest is null, will generate a random crest.
	 * @param kingdom
	 * @param name
	 * @param crest
	 */
	public Faction(Kingdom kingdom, String name, Crest crestIn, Color color) {
		this.kingdom = kingdom;

		// don't set name until we have a few cities and can base the culture off of them.
        // Name should only be present if it's a bandit or special faction
        this.name = name;
        this.color = color;
		
		nobles = new StrictArray<Noble>();
		unoccupiedNobles = new StrictArray<Noble>();
		cities = new StrictArray<City>();
		castles = new StrictArray<Castle>();
		villages = new StrictArray<Village>();

		centers = new StrictArray<Center>();
		territory = new StrictArray<Polygon>();
		//		closeEnemyLocations = new StrictArray<Location>();
		closeEnemyCities = new StrictArray<City>();
		closeEnemyCastles = new StrictArray<Castle>();
		closeEnemyVillages = new StrictArray<Village>();
		closeFriendlyCities = new StrictArray<City>();
		closeFriendlyCastles = new StrictArray<Castle>();

		locationsToAttack = new StrictArray<Location>();
		faction_center_x = 0;
		faction_center_y = 0;

		atWar = new StrictArray<Faction>();
//		atWarInt = new StrictArray<Integer>();
		atPeace = new StrictArray<Faction>();
		allies = new StrictArray<Faction>();
	}

	// should only be called once per faction
	public void initializeRelations() {
		//		System.out.println(this.name + " initializing relations");
		warEffects = new StrictArray<Integer>();

		for (Faction f : kingdom.factions) {
			//			System.out.println("adding war effect " + f.name);
			warEffects.insert(f.index, 0);
		}

		for (Faction f : kingdom.factions) {
			//			System.out.println("declaring peace " + f.name);
			//			if (!this.atPeace(f))
			if (f == this) continue;
			System.out.println("initializing peace");
			this.declarePeace(f);
			if (Math.random() < 0.5)
				this.declareWar(f);
		}
	}

	public void removeOther(Faction faction) {
		kingdom.factions.removeValue(faction, true);
		atWar.removeValue(faction, true);
//		atWarInt.removeValue(faction.index, true);
		atPeace.removeValue(faction, true);

		//		allied.removeValue(faction, true);
		//		for (int i = 0; i < kingdom.factions.size; i++) {
		//			//			factionRelations[faction.index][i] = -999; // 'deletes' faction relations
		//			//			factionRelations[i][faction.index] = -999;
		////			kingdom.factions.get(i).
		////			kingdom.factionRelations.get(i).set(faction.index, null);
		////			kingdom.factionRelations.get(faction.index).set(i, null);
		//		}
	}

	public void removeFaction() {
		for (int i = 0; i < kingdom.factions.size; i++) {
			kingdom.factions.get(i).removeOther(this);
		}
	}

	public void act(float delta) {
//		if (this.atWar == null || this.atPeace == null) refreshAtWar();

		//		timeSinceIncrease += delta;

		//		if (this == PLAYER_FACTION)
		//			System.out.println(this.name + " " + BANDITS_FACTION.name + getRelations(this, BANDITS_FACTION));

		//		if (timeSinceIncrease >= INCREASE_INTERVAL) {
		//			//			System.out.println(timeSinceIncrease);
		//			for (Faction f : kingdom.factions)
		//				kingdom.changeRelation(this, f, 0); // factor to increase relations by
		//			timeSinceIncrease = 0;
		//		}

		if (this != ROGUE_FACTION) autoManage(delta);
	}

	public void autoManage(float delta) {
		// send armies to capture/raid enemy cities/castles/villages
		// negotiate diplomacy, declare war/peace
		// that's it for now :D
		// Check once per day
		if (kingdom.getTotalHour() % FACTION_UPDATE_FREQ == 0 && !hasChecked) {
			if (this != Faction.BANDITS_FACTION && this != Faction.ROGUE_FACTION)
				manageDiplomacy();
			manageNobles();
			hasChecked = true;
		}
		else if (kingdom.getTotalHour() % FACTION_UPDATE_FREQ != 0) hasChecked = false;
	}

	public boolean hasNewCenter() {
		return (this.faction_center_x != 0 || this.faction_center_y != 0);
	}

	// reallocate nobles from this city if it's taken by an enemy
	public void allocateNoblesFrom(City city) {
		for (Noble noble : city.nobles) {
			//			if (this.cities.size == 0) noble.destroy(); // kill noble for now
			//			else {
			//				City newCity = this.getRandomCity();
			//				if (!newCity.nobles.contains(noble, true)) newCity.addNoble(noble);
			//			}
			// Just destroy all nobles for now, in future, can make them nobles of other cities
			noble.destroy();
		}
	}

	// create new nobles for this city
	public void allocateNoblesFor(City city) {
		assert(this.cities.contains(city, true));
		// this isn't actually leaking nobles...
		//		this.createNobleAt(city);
		// new noble created everytime a city is captured... but also nobles will die when they lose big battles
	}

	public void manageNobles() {
		// if a city doesn't have a noble, create a baron or earl
		// when a noble is upgraded to the next level (later, if a city is upgraded) add a fresh noob to replace them.

		//		while (nobles.size < NOBLE_COUNT && cities.size >= 1) {
		//			createNobleAt(cities.random());
		//		}
		manageSieges();

		// figure out whether or not to organize a siegeOrRaid or something!
	}

	public void manageDiplomacy() {
//		if (this.atWar == null || this.atPeace == null) refreshAtWar();
		for (Faction that : this.atWar) {
			if (Math.random() < PEACE_PROBABILITY && 
					that != Faction.BANDITS_FACTION &&
					this != Faction.BANDITS_FACTION &&
					that != Faction.ROGUE_FACTION &&
					this != Faction.ROGUE_FACTION) {
				this.declarePeace(that);
				return;
			}
		}
		for (Faction that : this.atPeace) {
			if (Math.random() < WAR_PROBABILITY  && 
					that != Faction.BANDITS_FACTION &&
					this != Faction.BANDITS_FACTION &&
					that != Faction.ROGUE_FACTION &&
					this != Faction.ROGUE_FACTION) {
				this.declareWar(that);
				return;
			}
		}
	}

	public void manageSieges() {
		// update locations to attack
		for (Location l : locationsToAttack) {
			if (l.getFaction().atPeace(this)) {
				cancelSiegeOf(l);
				updateCloseLocations();
				//				throw new java.lang.AssertionError();
				//				cancelSiegeOf(l);
			}
		}

		// You should be able to either execute, capture, or release Nobles.
		// Nobles should have names, get promoted, etc. 
		// Factions should promote new nobles as necessary (# nobles should reflect size of cities. )
		// Also, nobles should have a huge variety of strengths. Everything from 10 soldiers to 200+.
		// Do a smarter check of what cities to attack:
		// 		 if the city has a smaller garrison (simulate a hypothetical battle), then order a siegeOrRaid of it
		if (locationsToAttack.size < 1 && unoccupiedNobles.size > 1 && (closeEnemyCities.size > 0 || closeEnemyCastles.size > 0)) {
			Location randomLocation;
			if (Math.random() < .5 && closeEnemyCities.size > 0) randomLocation = closeEnemyCities.random();
			else if (closeEnemyCastles.size > 0) randomLocation = closeEnemyCastles.random();
			else return;

			if (randomLocation == null || randomLocation.getFaction() == this || !this.shouldSiege(randomLocation)) return;

			if (randomLocation.underSiege()) return;
			// check that no other factions are besieging it
			for (Faction f : kingdom.factions) {
				if (f.locationsToAttack.contains(randomLocation, true)) return;
			}
			orderSiegeOf(randomLocation);
		}
		else {
			//			System.out.println();
			//			System.out.println(this.name);
			//			System.out.println("locations to attack: " + locationsToAttack.size);
			//			System.out.println("locations to attack:");
			//			for (Location l : locationsToAttack) {
			//				System.out.println(l.getName());
			//			}
			//			System.out.println("unoccupied nobles: " + unoccupiedNobles.size);
			//			System.out.println("total nobles: " + this.nobles.size);
			//			System.out.println("noble special tasks:");
			//			for (Noble n : nobles) {
			//				if (n.specialTarget != null)
			//					System.out.println(n.getName() + ": " + n.specialTarget.getName());
			//			}
			//			System.out.println("Close enemy locations: " + (closeEnemyCities.size + closeEnemyCastles.size));
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
	// Returns true if this faction should order a siegeOrRaid of location.
	public boolean shouldSiege(Location location) {
		if (this.atPeace(location.getFaction())) return false;
		double balance = BattleSim.calcBalanceNobles(unoccupiedNobles, 1f, location.getGarrisonedAndGarrison(), location.getDefenseFactor());
		return balance >= Siege.MIN_BALANCE_TO_ATTACK;
	}
	public void orderSiegeOf(Location location) {
		locationsToAttack.add(location);

		// always order all nobles
		//		int noblesToOrder = Math.max((int) (unoccupiedNobles.size * ORDER_FACTOR), 2);
		//		while (noblesToOrder > 0) {
		//			Noble randomNoble = unoccupiedNobles.random();
		//			setTask(randomNoble, location);
		//			noblesToOrder--;
		//		}

		//		System.out.println(this.name + " is ordering a siegeOrRaid of " + location.getName() + " involving " + unoccupiedNobles.size + " nobles");
//		BottomPanel.log(this.name + " is ordering a siegeOrRaid of " + location.getName() + " involving " + unoccupiedNobles.size + " nobles", "magenta");

		for (Noble n : unoccupiedNobles) {
			setTask(n, location);
		}

	}
	public void cancelSiegeOf(Location location) {
		locationsToAttack.removeValue(location, true);
//		BottomPanel.log(this.name + " is cancelling siegeOrRaid of " + location.getName(), "magenta");
		for (Noble noble : this.nobles) {
			if (noble.specialTargetToBesiege == location) {
				endTask(noble);
			}
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

	public void restoreCrest() {
		crest.loadFromInts(rc);
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

			for (City friendlyCity : c.closestFriendlyCities) {
				if (!closeFriendlyCities.contains((City) friendlyCity, true)) {
					//					System.out.println("    " + hostileCity.getName());
					closeFriendlyCities.add((City) friendlyCity);
				}
			}
			for (Castle friendlyCastle : c.closestFriendlyCastles) {
				if (!closeFriendlyCastles.contains((Castle) friendlyCastle, true)) {
					//					System.out.println("    " + hostileCastle.getName());
					closeFriendlyCastles.add((Castle) friendlyCastle);
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
			for (City friendlyCity : c.closestFriendlyCities) {
				if (!closeFriendlyCities.contains((City) friendlyCity, true)) {
					//					System.out.println("    " + hostileCity.getName());
					closeFriendlyCities.add((City) friendlyCity);
				}
			}
			for (Castle friendlyCastle : c.closestFriendlyCastles) {
				if (!closeFriendlyCastles.contains((Castle) friendlyCastle, true)) {
					//					System.out.println("    " + hostileCastle.getName());
					closeFriendlyCastles.add((Castle) friendlyCastle);
				}
			}
		}

		setCultureType();
	}

	private void setCultureType() {
	    if (cultureType != null ) return;
	    if (name != null) return;

	    System.out.println("cities: " + cities.size + " castles: " + castles.size + " " + this.name);

        Object[] cultureTypes = UnitLoader.cultureTypes.values().toArray();
	    int[] cultureCounts = new int[UnitLoader.cultureTypes.values().size()];
	    for (City city : cities) {
            for (int i = 0; i < cultureTypes.length; i++) {
                System.out.println(((CultureType) cultureTypes[i]).name + " is a culture");
                if (cultureTypes[i] == city.cultureType) {
                    cultureCounts[i]++;
                }
            }
        }
        for (Castle castle : castles) {
            for (int i = 0; i < cultureTypes.length; i++) {
                if (cultureTypes[i] == castle.cultureType) {
                    cultureCounts[i]++;
                }
            }
        }
        for (Village village : villages) {
            for (int i = 0; i < cultureTypes.length; i++) {
                if (cultureTypes[i] == village.cultureType) {
                    cultureCounts[i]++;
                }
            }
        }
        int maxCount = 0;
	    int maxIndex = -1;
        for (int i = 0; i < cultureCounts.length; i++) {
	        if (cultureCounts[i] > maxCount) {
	            maxIndex = i;
	            maxCount = cultureCounts[i];
            }
        }
        if (maxIndex == -1) {
            System.out.println("Problem. Faction Cities: " + cities.size + " Castles: " + castles.size);
            throw new AssertionError();
        }
        this.cultureType = (CultureType) cultureTypes[maxIndex];

        // Set name if it hasn't been manually set
        if (name == null) {
            this.name = cultureType.nameGenerator.generateFactionName();
            this.name = this.name + " " + getRandomFactionTitle();
            System.out.println("ksd: " + this.name);
        }

        generateCrest();
    }

    private String getRandomFactionTitle() {
	    double rand = Math.random();
	    int count = 8;
	    double div = 1/ (double) count;
	    if (rand < div) {
	        return "Tribe";
        } else if (rand < div * 2) {
            return "Empire";
        } else if (rand < div * 3) {
            return "Nation";
        } else if (rand < div * 4) {
            return "Confederacy";
        } else return "";
    }

    private void generateCrest() {
//        if (crestIn == null) {
        this.crest = rc.create(cultureType);
//        }
//        else {
//            this.crest = crestIn;
//        }
        this.color = Color.WHITE;
        if (this.color.equals(Color.WHITE)) {
            if (crest.cOverlay != -1) {
                this.color = rc.getColor(crest.cOverlay);
            }
            else if (crest.cDetail != -1) {
                this.color = rc.getColor(crest.cDetail);
            }
        }
        crest.loadFromInts(rc);
    }

	/** First updates each city's lists of close friendly and 
	 *  hostile cities, then updates this faction's lists based on 
	 *  that list
	 *  TODO review this process and make sure it works */
	public void updateCloseLocations() {
		//		System.out.println(this.name + " updating cities: ");
		this.closeEnemyCities.clear();
		this.closeEnemyCastles.clear();
		// find hostile locations near cities
		for (City c: cities) {
			//			System.out.println("  close to " + c.getName() + ":");
			c.updateCloseLocations();
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

			// friendly
			for (City friendlyCity : c.closestFriendlyCities) {
				if (!closeFriendlyCities.contains((City) friendlyCity, true)) {
					//					System.out.println("    " + friendlyCity.getName());
					closeEnemyCities.add((City) friendlyCity);
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
			c.updateCloseLocations();
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
		updateAllRelations();
	}	

	//	/** First updates each city's lists of close friendly and 
	//	 *  hostile cities, then updates this faction's lists based on 
	//	 *  that list
	//	 *  TODO review this process and make sure it works */
	//	public void updateCloseHostileCities() {
	//		StrictArray<City> tempCloseEnemyCities = new StrictArray<City>();
	//		StrictArray<City> tempCloseFriendlyCities = new StrictArray<City>();
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
	//		closeEnemyLocations = new StrictArray<City>(tempCloseEnemyCities);
	//		closeFriendlyLocations = new StrictArray<City>(tempCloseFriendlyCities);
	//	}

//	public void refreshAtWar() {
//		this.atWar = new StrictArray<Faction>();
//		this.atPeace = new StrictArray<Faction>();
//		for (int i : this.atWarInt) {
//			this.atWar.add(kingdom.factions.get(i));
//		}
//		for (Faction f : kingdom.factions) {
//			if (!this.atWar.contains(f, true)) atPeace.add(f);;
//		}
//	}

	//	/** Calculates negative effect of close cities on the relations
	//	 *  between two factions
	//	 * 
	//	 * @param that
	//	 * @return effect on relations
	//	 */
	public int getCloseCityEffect(Faction that) {
		if (this.index == that.index) return 0;
		int totalEffect = 0;
		if (atWar(that)) {
			for (City c : closeEnemyCities)
				if (c.getFaction() == that)	totalEffect += -1*CLOSE_CITY_FACTOR;
			for (Castle c : closeEnemyCastles)
				if (c.getFaction() == that)	totalEffect += -1*CLOSE_CITY_FACTOR;
		}
		else {
			for (City c : closeFriendlyCities)
				if (c.getFaction() == that) totalEffect += -1*CLOSE_CITY_FACTOR;
			for (Castle c : closeFriendlyCastles)
				if (c.getFaction() == that) totalEffect += -1*CLOSE_CITY_FACTOR;

			//			for (Castle c : closeEnemyCastles)
			//				if (c.getFaction() == that)	totalEffect += -1*CLOSE_CITY_FACTOR;
		}
		return totalEffect;
	}

	//	public int getRelationsWith(Faction that) {
	//		return kingdom.getRelations(this, that);
	//	}

	// this isn't stored, but calculated on demand
	public int calcRelations(Faction that) {
		/* 		nearby cities/troops (negative)
		 * 
		 * 		// "war effects":
		 * 		long-term peace (positive)
		 * 		past wars (negative)
		 * 
		 * 		current wars (negative) 
		 * 		alliance (positive)
		 */
		if (that == null) return 0;
		//		if (this.warEffects == null) System.out.println("war effects are null for " + this.name);
		int warEffect = 0;
		if (this.atWar(that)) warEffect = STATIC_WAR_EFFECT;
		if (this.atPeace(that)) warEffect = STATIC_PEACE_EFFECT;
		if (this.alliedWith(that)) warEffect += STATIC_ALLY_EFFECT;
		int relation = warEffect + this.getCloseCityEffect(that);
		//		int relation = this.warEffects.get(that.index) + getAllianceBonus(that) - getWarBonus(that) + this.getCloseCityEffect(that);

		if (this.atWar(that) && relation >= PEACE_THRESHOLD) {
			this.declarePeace(that);
		}

		return relation;
	}

	public void updateAllRelations() {
		for (int i = 0; i < kingdom.factions.size; i++) {
			if (kingdom.factions.get(i) != null) {
				this.calcRelations(kingdom.factions.get(i));
			}
			else {
				System.out.println("Faction " + i + " doesn't exist");
			}
		}
	}

	public int getWarBonus(Faction that) {
		//		if (this.atWar(that)) return WAR_BONUS;
		return 0;
	}

	public int getAllianceBonus(Faction that) {
		//		if (this.alliedWith(that)) return ALLIANCE_BONUS;
		return 0;
	}

	// change daily
	//	public void updateWarEffects() {
	//		for (Faction f : kingdom.factions) {
	//			int currentEffect = this.warEffects.get(f.index);
	//			if (f.atWar(this))
	//				this.warEffects.insert(f.index, currentEffect - DAILY_WAR_EFFECT);
	//			else if (f.atPeace(this))
	//				this.warEffects.insert(f.index, currentEffect + DAILY_PEACE_EFFECT);
	//		}
	//		updateAllRelations();
	//	}

	public boolean atWar(Faction that) {
//		if (this.atWar == null || this.atPeace == null) { 
//			refreshAtWar(); 
//		}
//		if (that.atWar == null || that.atPeace == null) { 
//			that.refreshAtWar(); 
//		}
		if (this.atWar.contains(that, true) != that.atWar.contains(this,  true)) {
			System.out.println(this.name + " and " + that.name + " don't have the same war status");
			throw new java.lang.RuntimeException();
		}
		return this.atWar.contains(that, true);
	}

	public boolean alliedWith(Faction that) {
//		if (this.atWar == null || this.atPeace == null) { 
//			refreshAtWar(); 
//		}
//		if (that.atWar == null || that.atPeace == null) { 
//			that.refreshAtWar(); 
//		}
		return this.allies.contains(that, true);
	}

	public boolean atPeace(Faction that) {
//		if (this.atWar == null || this.atPeace == null) { 
//			refreshAtWar(); 
//		}
//		if (that.atWar == null || that.atPeace == null) { 
//			that.refreshAtWar(); 
//		}
		if (this.atPeace.contains(that, true) != that.atPeace.contains(this,  true)) {
			System.out.println(this.name + " and " + that.name + " don't have the same peace status");
			throw new java.lang.RuntimeException();
		}
		return this.atPeace.contains(that, true);
	}

	public void declareWar(Faction that) {
		//		if (!kingdom.isAtWar(this,  that))
		//			kingdom.declareWar(this, that);
		if (this.atWar.contains(that, true)) {
			System.out.println(this.name + " ALREADY AT WAR with " + that.name);
			throw new java.lang.RuntimeException();
		}
		if (!this.atPeace.contains(that, true)) {
			System.out.println(this.name + " is not at peace with " + that.name);
			throw new java.lang.RuntimeException();
		}

		if (!this.atWar.contains(that, true)) {
			this.atWar.add(that);
//			this.atWarInt.add(that.index);
		}
		if (!that.atWar.contains(this, true)){
			that.atWar.add(this);
//			that.atWarInt.add(this.index);
		}

		//		if (!this.atPeace.removeValue(that, true)) throw new java.lang.RuntimeException();
		//		if (!that.atPeace.removeValue(this, true)) throw new java.lang.RuntimeException();

		while (this.atPeace.contains(that, true)) {
			if (!this.atPeace.removeValue(that, true)) throw new java.lang.RuntimeException();
		}

		while (that.atPeace.contains(this, true)) {
			if (!that.atPeace.removeValue(this, true)) throw new java.lang.RuntimeException();
		}

		if (that.atPeace.contains(this, true)) throw new java.lang.RuntimeException();
		if (this.atPeace.contains(that, true)) throw new java.lang.RuntimeException();

		if (Faction.initialized)
			BottomPanel.log(this.name + " and " + that.name + " have declared war!");

		this.updateCloseLocations();
	}

	public void declarePeace(Faction that) {
		if (this.atPeace.contains(that, true)) {
			System.out.println(this.name + " ALREADY AT PEACE with " + that.name);
		}
		//		if (!this.atWar.contains(that, true)) {
		//			System.out.println(this.name + " is not at war with " + that.name);
		//		}

		this.atWar.removeValue(that, true);
//		this.atWarInt.removeValue(that.index, true);
		that.atWar.removeValue(this, true);
//		that.atWarInt.removeValue(this.index, true);
		this.atPeace.add(that);
		that.atPeace.add(this);

		if (Faction.initialized)
			BottomPanel.log(this.name + " and " + that.name + " have signed a peace agreement!");

		for (Location l : locationsToAttack) {
			if (l.getFaction() == that) {
				this.cancelSiegeOf(l);
			}
		}
		updateCloseLocations();
	}

	public void goRogue() { // just for testing, declares war on all factions other than this one
		for (int i = 0; i < kingdom.factions.size; i++) {
			if (i != index) {
				if (this.atPeace(kingdom.factions.get(i))) {

					System.out.println(this.name + " declaring war against: " + kingdom.factions.get(i).name);
					kingdom.factions.get(i).declareWar(this);
				}
			}
		}
	}
	
	public boolean isBandit() {
		return this == Faction.BANDITS_FACTION;
	}

	public int getTotalWealth() {
		int total = 0;
		for (City c : cities)
			total += c.getWealth();
		for (Village v : villages)
			total += v.getWealth();
//		System.out.println("total wealth is : " + total);
		return total;
	}

	public City getRandomCity() {
		if (cities.size > 0) {
			return cities.random();
		}
		else return null;
	}
}
