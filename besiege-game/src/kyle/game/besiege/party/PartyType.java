/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.voronoi.Biomes;
import kyle.game.besiege.voronoi.Center;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;


public class PartyType { // todo add ability for max party size
	public static Array<PartyType> types;

	public enum Type {FARMERS, PATROL, MERCHANT, CITY_GARRISON, CASTLE_GARRISON, VILLAGE_GARRISON, 
						SCOUT, MILITIA, NOBLE, RAIDING_PARTY, ELITE, CITY_HIRE, CASTLE_HIRE, VILLAGE_HIRE, TEST};
	
	//	private final Weapon[] troopTypes;
	public String name;
	
	private UnitClass unitClass; // if this is null, use distributions
	private float[] biomeWeights;
	
	
	private int[] tiers;
//	private NewUnitType[] unitTypes;

	private int minCount;
	private int maxCount;
	
//	private int[] minCount;
//	private int[] maxCount;

	public int minWealth; // wealth
	public int maxWealth; // this is also cost
	//private final int maxTroopLevel;
	//private final int minTotal;


	public Party generate() {
		Party party = new Party();

//		if (unitTypes != null) {
	
		int toGenerate = MathUtils.random(minCount, maxCount);
		
		while (toGenerate > 0) {
			// generate random unit from available tiers in available class TODO
			UnitType type = randomSoldierType();
			party.addSoldier(new Soldier(type, party));
			toGenerate--;
		}

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
		// weighted based on soldier ffrequencies (in max):		
		
		// use rejection sampling because why not
		
		UnitClass classToUse;
		if (unitClass != null) 
			classToUse = unitClass;
		else {
			// choose probablistically
			double random = Math.random();
			double current = 0;
			int currentIndex = -1;
			while (current < random) {
				currentIndex++;
				current += biomeWeights[currentIndex];
//				System.out.println(biomeWeights[currentIndex]);
			}
			Biomes biome = Biomes.values()[currentIndex];
//			System.out.println(biome.name());
			classToUse = UnitLoader.biomeClasses.get(biome);
			if (classToUse == null) {
//				System.out.println(biome.toString());
			}
		}
		
		Object[] types = (classToUse.units.values().toArray());
		
		int randomIndex;
		UnitType unitType;
		
		boolean isInTier;
		
		boolean valid = false;
		
//		// make sure class actually has a unit in the right tier
		for (UnitType unit : classToUse.units.values()) {
			if (unit.tier == 5) valid = true;
		}
		if (!valid) {
			System.out.println("you need to make all 5 unit types for " + classToUse.name);
			throw new java.lang.NullPointerException();
		}
		
		do {
			randomIndex = MathUtils.random(0, types.length-1);
			unitType = (UnitType) types[randomIndex];
			
			isInTier = false;
			for (int i = 0; i < tiers.length; i++) {
				if (unitType.tier == tiers[i]) isInTier = true;
			}
		}
		while (!isInTier);
		
		return unitType;
	}
	
	// for use by cities and armies created in cities
	public static PartyType generatePT(Type type, Location location) {
		PartyType pt = generatePartyType(type);
		pt.unitClass = null;
		if (location.biomeDistribution == null) {
			System.out.println("NO BIOME FOR: " + location.getTypeStr() + " " + location.getName());
		}
		pt.biomeWeights = location.biomeDistribution;
		return pt;
	}
	
	public static PartyType generatePT(Type type, Center center) {
//		System.out.println("getting default pt for: " + center.biome.toString());
		
		PartyType pt = generatePartyType(type);
		pt.unitClass = null;
		pt.biomeWeights = center.getBiomeDistribution();
		return pt;
	}
	
	public static PartyType generatePartyType(Type type) {	
		UnitClass none = null;
		return getPartyType(type, none);	
	}
	
	// generates a party type for this particular type
	public static PartyType getPartyType(Type type, UnitClass unitClass) {
		PartyType pt = new PartyType();
		pt.unitClass = unitClass;
		
		switch (type) {
		case FARMERS:
			pt.name = "Farmers";
			pt.maxCount = 12;
			pt.minCount = 4;
			pt.tiers = new int[]{1};
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
			pt.maxCount = 150;
			pt.minCount = 100;
			pt.tiers = new int[]{2, 3, 4};
			break;
		case CASTLE_GARRISON:
			pt.name = "Garrison";
			pt.maxCount = 150;
			pt.minCount = 100;
			pt.tiers = new int[]{2, 3, 4, 5};
		case VILLAGE_GARRISON:
			pt.name = "Village Garrison";
			pt.maxCount = 5;
			pt.minCount = 2;
			pt.tiers = new int[]{1};
			break;
		case SCOUT:
			pt.name = "Scout";
			pt.maxCount = 25;
			pt.minCount = 10;
			pt.tiers = new int[]{2, 3};
			break;
		case MILITIA:
			pt.name = "Militia";
			pt.maxCount = 50;
			pt.minCount = 30;
			pt.tiers = new int[]{1, 2};
			break;
		case NOBLE:
			pt.name = "Noble";
			pt.maxCount = 100;
			pt.minCount = 50;
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
			pt.tiers = new int[]{1, 2, 3, 4, 5};
			break;
		case VILLAGE_HIRE:
			pt.name = "For Hire";
			pt.maxCount = 5;
			pt.minCount = 1;
			pt.tiers = new int[]{1, 2};
			break;
		case CASTLE_HIRE:
			pt.name = "For Hire";
			pt.maxCount = 3;
			pt.minCount = 1;
			pt.tiers = new int[]{2, 3, 4};
			break;
		case CITY_HIRE:
			pt.name = "For Hire";
			pt.maxCount = 8;
			pt.minCount = 1;
			pt.tiers = new int[]{1, 2, 3};
			break;
		case TEST:
			pt.name = "Test";
			pt.maxCount = 100;
			pt.minCount = 100;
			pt.tiers = new int[]{1, 2, 3, 4, 5};
			break;
		}
//		pt.name = "Default";
//		pt.tiers = new int[]{1, 2, 3};
	
		
		return pt;
	}
}
