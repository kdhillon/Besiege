package kyle.game.besiege.party;

import com.badlogic.gdx.utils.Array;

public enum RangedWeapon {
	
	// Tier 4 Ranged
	SHORTBOW (4, "Shortbow", "Archer", 1, 20, 8, 3),
	
	// Tier 6 Ranged
	CROSSBOW (6, "Crossbow", "Crossbowman", 4, 20, 10, 9), // slower, less range, more accurate, more power
	RECURVE (6, "Recurve Bow", "Bowman",    2, 25, 6, 4),  // faster, medium range, less accurate, medium power
	LONGBOW (6, "Longbow", "Longbowman",    2, 40, 8, 7), // slower, more range, medium accurate, medium power
	
	// Tier 8 Ranged
	ADV_CROSSBOW (8, "Crossbow", "Crossbow Master", 5, 20, 8, 10), // slower, less range, more accurate, more power
	ADV_RECURVE  (8, "Recurve Bow", "Bowmaster", 3, 30, 4, 6),  // faster, medium range, less accurate, medium power
	ADV_LONGBOW  (8, "Longbow", "Longbow Master", 3, 50, 6, 9);  // slower, more range, medium accurate, medium power
	
	public final int tier;
	public final String name;
	public final String troopName;
	public final int atkMod;
	//public final int defMod;
	//public final int spdMod;
	public final boolean oneHand;
	public final boolean blunt;
	public final int range;
	public final int accuracy;
	public final int rate; // seconds it takes to shoot

	public static Array<RangedWeapon> all;
	public static Array<RangedWeapon> bandit;
	public static Array<RangedWeapon> city;

	//public final int cost; // cost to purchase

	// Range is squares on the map, accuracy is between 1 and 10, rate will hit at max range
	private RangedWeapon(int tier, String name, String troopName, int atkMod, int range, int rate, int accuracy) {
		this.tier = tier;
		this.name = name;
		this.troopName = troopName;
		this.atkMod = atkMod;
		//this.defMod = defMod;
		//this.spdMod = spdMod;
		this.oneHand = false;
		this.blunt = false;
		this.range = range;
		this.rate = rate;
		this.accuracy = accuracy;
	}

	public static Array<RangedWeapon> upgrade(RangedWeapon weapon) {
		Array<RangedWeapon> upgrades = new Array<RangedWeapon>();
		switch (weapon) {
			case SHORTBOW :
				upgrades.add(CROSSBOW);
				upgrades.add(RECURVE);
				upgrades.add(LONGBOW);
				break;
			case CROSSBOW :
				upgrades.add(ADV_CROSSBOW);
				break;
			case RECURVE :
				upgrades.add(ADV_RECURVE);
				break;
			case LONGBOW :
				upgrades.add(ADV_LONGBOW);
				break;
		}
		return upgrades;
	}
}