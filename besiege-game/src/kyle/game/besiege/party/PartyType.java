/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.location.Location;
import kyle.game.besiege.voronoi.Biomes;
import kyle.game.besiege.voronoi.Center;


public class PartyType { // todo add ability for max playerPartyPanel size
	public static Array<PartyType> types;
	
	public enum Type {FARMERS, PATROL, MERCHANT, CITY_GARRISON, CASTLE_GARRISON, VILLAGE_GARRISON, BANDIT,
						SCOUT, NOBLE, RAIDING_PARTY, ELITE, CITY_HIRE, CASTLE_HIRE, VILLAGE_HIRE, TEST, TEST_1, TEST_2, TEST_ALL};
	
	//	private final Weapon[] troopTypes;
	public String name;
	
	private CultureType cultureType; // if this is null, use distribution
	private UnitType unitType; // force one unit type
//	private float[] biomeWeights;

	private int[] tiers;
//	private NewUnitType[] unitTypes;

    private String[] unitNames; // If present, only allow units with these words in their names

	private int minCount;
	private int maxCount;
	
//	private int[] minCount;
//	private int[] maxCount;

	public int minWealth; // wealth
	public int maxWealth; // this is also cost

    public boolean hire; // Is this a "hire" party, in other words, no general.

	//private final int maxTroopLevel;
	//private final int minTotal;

	
	// also add generals!
	public Party generate() {
		return generate(false);
	}
	
	public Party generate(boolean player) {
		Party party = new Party();
		party.pt = this;
				
		party.player = player;

//		if (unitTypes != null) {

		// generate general first, use their fame to determine max playerPartyPanel size.
        if (!this.hire) {
            if (unitType != null)
                party.createFreshGeneral(unitType, this);
            else
                party.createFreshGeneral(this.randomBestSoldierType(), this);
        }
		
		// generate a general first, with random fame. then generate playerPartyPanel size based on that.
		int toGenerate = party.getMaxSize() - 1;
//		int toGenerate = MathUtils.random(minCount, playerPartyPanel.getMaxSize());
//        System.out.println("to generate" + toGenerate);

        while (toGenerate > 0) {
			// generate random unit from available tiers in available class TODO
//            System.out.println("to generate" + toGenerate);
			UnitType type = randomSoldierType();
			party.addSoldier(new Soldier(type, party), false);
			toGenerate--;
		}
//        System.out.println("Done generating!");
//		UnitType unitType, Party playerPartyPanel, String title, Location home

		int randomWealth = MathUtils.random(minWealth, maxWealth);
		party.wealth = randomWealth;
				
		return party;
	}

	public int getMinSize() {
		return minCount;
	}
	public int getMaxSize() {
		return maxCount;
	}
	public int getRandomSize() {
		return MathUtils.random(minCount, maxCount);
	}

	public UnitType randomSoldierType() {
		if (this.unitType != null) return unitType;

		// weighted based on soldier ffrequencies (in max):
		
		// use rejection sampling because why not
		
		CultureType classToUse;
		if (cultureType != null)
			classToUse = cultureType;
		else {
//			// choose probablistically
//			double random = Math.random();
//			double current = 0;
//			int currentIndex = -1;
//			Biomes biome;
//
//			if (biomeWeights != null) {
//				while (current < random) {
//					currentIndex++;
//					current += biomeWeights[currentIndex];
//					//				System.out.println(biomeWeights[currentIndex]);
//				}
//				biome = Biomes.values()[currentIndex];
//			}
//			else biome = Biomes.GRASSLAND;
//
//			//			System.out.println(biome.name());
////			classToUse = UnitLoader.biomeCultures.get(biome);
//			if (classToUse == null) {
				System.out.println(name + " has null culture type!");
				throw new AssertionError();
//			}
		}
				
		int randomIndex;
		UnitType unitType;
				
		// Pick a random tier first, not weighted.
		int tierIndex = (int) (Math.random() * tiers.length);
		int tierToUse = tiers[tierIndex];

		Object[] types = (classToUse.units.values().toArray());

		boolean valid = false;
//		// make sure class actually has a unit in the right tier
		for (UnitType unit : classToUse.units.values()) {
			if (unit.tier == tierToUse) {
			    // If forcing from a list of units (ie farmers only) select that.
			    if (unitNames != null) {
			        for (String s : unitNames) {
			            if (unit.name.contains(s)) valid = true;
                    }
                } else {
                    valid = true;
                }
            }
		}
		if (!valid) {
			System.out.println("Missing tier " + tierToUse + " for " + classToUse.name);
			throw new java.lang.NullPointerException();
		}
		
		// Random sample from all units at this tier level.
        valid = false;
		do {
			randomIndex = MathUtils.random(0, types.length-1);
			unitType = (UnitType) types[randomIndex];

            if (unitType.tier == tierToUse) {
                // If forcing from a list of units (ie farmers only) select that.
                if (unitNames != null) {
                    for (String s : unitNames) {
                        if (unitType.name.contains(s)) valid = true;
                    }
                } else {
                    valid = true;
                }
            }
		}
		while (!valid);

//		System.out.println("returning: " + unitType.name);
		return unitType;
	}
	
	public UnitType randomBestSoldierType() {
		int maxTier = 0;
		for (int i = 0; i < tiers.length; i++) {
			if (tiers[i] > maxTier) maxTier = tiers[i];
		}
		
		UnitType best = randomSoldierType();
		while (best.tier < maxTier)
			best = randomSoldierType();
		return best;
	}
	
	// for use by cities and armies created in cities
	public static PartyType generatePT(Type type, Location location) {
		PartyType pt = generatePartyType(type);
		pt.cultureType = null;

		// TEMPORARY! Try out using "primary class"
        pt.cultureType = location.cultureType;
        System.out.println("PRIMARY CLASS: " + location.cultureType.name);

		if (location.biomeDistribution == null) {
			System.out.println("NO BIOME FOR: " + location.getTypeStr() + " " + location.getName());
		}
//		pt.biomeWeights = location.biomeDistribution;
		return pt;
	}
	
	public static PartyType generatePT(Type type, Center center) {
//		System.out.println("getting default pt for: " + center.biome.toString());
		
		PartyType pt = generatePartyType(type);
		pt.cultureType = center.cultureType;
		if (center.cultureType == null) {
		    throw new AssertionError();
        }
//		pt.biomeWeights = center.getBiomeDistribution();
		return pt;
	}
	
	public static PartyType generatePartyType(Type type) {
		CultureType none = null;
		return getPartyType(type, none);	
	}

	// generates a playerPartyPanel type for this particular type
	public static PartyType getPartyType(Type type, CultureType cultureType) {
		// We allow no party type, for random armies that aren't from a location?
		PartyType pt = new PartyType();
		pt.cultureType = cultureType;
		
		switch (type) {
		case FARMERS:
			pt.name = "Farmers";
			pt.maxCount = 12;
			pt.minCount = 4;
			pt.tiers = new int[]{1};
			pt.unitNames = new String[]{"Farmer", "Villager"};
			break;
		case PATROL:
			pt.name = "Patrol";
			pt.maxCount = 20;
			pt.minCount = 10;
			pt.tiers = new int[]{1, 2, 3};
			break;
		case MERCHANT:
			pt.name = "Merchant";
			pt.maxCount = 30;
			pt.minCount = 15;
			pt.tiers = new int[]{2, 3};
			break;
		case CITY_GARRISON:
			pt.name = "Garrison";
			pt.maxCount = 100;
			pt.minCount = 50;
			pt.tiers = new int[]{2, 3, 4};
			break;
		case CASTLE_GARRISON:
			pt.name = "Garrison";
			pt.maxCount = 100;
			pt.minCount = 1;
			pt.tiers = new int[]{2, 3, 4};
			break;
		case VILLAGE_GARRISON:
			pt.name = "Village Garrison";
			pt.maxCount = 20;
			pt.minCount = 1;
			pt.tiers = new int[]{1, 2};
			break;
		case SCOUT:
			pt.name = "Scout";
			pt.maxCount = 25;
			pt.minCount = 10;
			pt.tiers = new int[]{2, 3};
			break;
		case BANDIT:
			pt.name = "Bandit";
			pt.maxCount = 60;
			pt.minCount = 5;
			pt.tiers = new int[]{2, 3};
			break;
//		case MILITIA:
//			pt.name = "Militia";
//			pt.maxCount = 50;
//			pt.minCount = 30;
//			pt.tiers = new int[]{1, 2};
//			break;
		case NOBLE:
			pt.name = "Noble";
			pt.maxCount = 200;
			pt.minCount = 20;
			pt.tiers = new int[]{2, 3, 4};
			break;
		case RAIDING_PARTY:
			pt.name = "Raiding Party";
			pt.maxCount = 30;
			pt.minCount = 20;
			pt.tiers = new int[]{2, 3};
			break;
		case ELITE:
			pt.name = "Elite";
			pt.maxCount = 40;
			pt.minCount = 40;
			pt.tiers = new int[]{3, 4, 5};
			break;
		case VILLAGE_HIRE:
			pt.name = "For Hire";
			pt.maxCount = 5;
			pt.minCount = 1;
			pt.tiers = new int[]{1, 2};
			pt.hire = true;
			break;
		case CASTLE_HIRE:
			pt.name = "For Hire";
			pt.maxCount = 3;
			pt.minCount = 1;
			pt.tiers = new int[]{2, 3, 4};
            pt.hire = true;
            break;
		case CITY_HIRE:
			pt.name = "For Hire";
			pt.maxCount = 8;
			pt.minCount = 1;
			pt.tiers = new int[]{1, 2, 3};
            pt.hire = true;
            break;
		case TEST:
			pt.name = "Test";
			pt.maxCount = 100;
			pt.minCount = 50;
			pt.tiers = new int[]{2, 3};
			break;
			// NOTE that the test parties may have forced culture types.
		case TEST_1:
			pt.name = "Test1";
			pt.maxCount = 10;
			pt.minCount = 10;
			pt.cultureType =  UnitLoader.cultureTypes.get("Desert");
			// Don't forget number at the end of this!
			pt.unitType = pt.cultureType.units.get("Eagle Warrior4");
			if (pt.unitType == null) {
			    throw new AssertionError();
            }
			break;
		case TEST_2:
			pt.name = "Test2";
			pt.maxCount = 60;
			pt.minCount = 60;
			pt.tiers = new int[]{3};
            pt.cultureType =  UnitLoader.cultureTypes.get("Forest");
            pt.unitType = pt.cultureType.units.get("Eagle Bowman4");
            if (pt.unitType == null) {
                throw new AssertionError();
            }
			break;
		case TEST_ALL:
			pt.name = "Testall";
			pt.maxCount = 30;
			pt.minCount = 30;
			pt.tiers = new int[]{1, 2, 3, 4};

			break;
//		case ENLIGHTENMENT:
//			pt.name = "Enlightenment";
//			pt.maxCount = 100;
//			pt.minCount = 100;
//			pt.tiers = new int[]{2, 3};
//			break;
		}
//		pt.name = "Default";
//		pt.tiers = new int[]{1, 2, 3};
	
		
		return pt;
	}
}
