package kyle.game.besiege.party;

import com.badlogic.gdx.utils.Array;

public enum OldRangedWeapon {
	
	// Tier 4 Ranged
	SHORTBOW (4, "Shortbow", "Archer", 3, 30, 8, 5),
	
	// Tier 6 Ranged
	CROSSBOW (6, "Crossbow", "Crossbowman", 3, 30, 10, 8), // slower, medium range, more accurate
	RECURVE (6, "Recurve Bow", "Bowman",    3, 35, 6, 4),  // faster, medium range, less accurate
	LONGBOW (6, "Longbow", "Longbowman",    3, 45, 8, 5), // slower, more range, medium accurate
	
	// Tier 8 Ranged
	ADV_CROSSBOW (8, "Crossbow", "Crossbow Master", 3, 35, 8, 9), // slower, less range, more accurate
	ADV_RECURVE  (8, "Recurve Bow", "Bowmaster", 	3, 40, 4, 6),  // faster, medium range, less accurate
	ADV_LONGBOW  (8, "Longbow", "Longbow Master",	3, 50, 6, 7);  // slower, more range, medium accurate
	
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

	public static Array<OldRangedWeapon> all;
	public static Array<OldRangedWeapon> bandit;
	public static Array<OldRangedWeapon> city;

	//public final int cost; // cost to purchase

	// Range is squares on the map, accuracy is between 1 and 10, rate will hit at max range
	private OldRangedWeapon(int tier, String name, String troopName, int damage, int range, int rate, int accuracy) {
		this.tier = tier;
		this.name = name;
		this.troopName = troopName;
		this.atkMod = damage;
		//this.defMod = defMod;
		//this.spdMod = spdMod;
		this.oneHand = false;
		this.blunt = false;
		this.range = range;
		this.rate = rate;
		this.accuracy = accuracy;
	}

	public static Array<OldRangedWeapon> upgrade(OldRangedWeapon weapon) {
		Array<OldRangedWeapon> upgrades = new Array<OldRangedWeapon>();
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