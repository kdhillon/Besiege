package kyle.game.besiege.party;

/* contains information about a specific unit type */
public class UnitType {

	public enum UnitClass {
		HEAVY_INF,	// 	heavy infantry (infantry with shield, spearman)
		LIGHT_INF,	//  light infantry (clubman, axeman)
		RANGED_INF,	//  ranged infantry (any infantry with a bow or atlatl)
		RANGED,		//  ranged (slinger, atlatlist, archer)
		STEALTH,	// stealth focused units
	}

	// make this an object later 
	public String name;
	public CultureType cultureType;
	public int tier;
	public WeaponType melee;
	public RangedWeaponType ranged; // Optional
	public AmmoType ammoType;       // Only needed if ranged weapon present.
	public ShieldType shieldType;   // Optional
	public ArmorType armor;
	public UnitClass unitClass;

//	public boolean hideBonus;
//	public boolean hideBonus;
//	public boolean mounted;
	
	public String[]		upgradeStrings;
	public UnitType[]	upgrades;

	public String weaponSummary;

	public String getWeaponSummary() {
		if (weaponSummary == null) {
			if (ranged != null)
				weaponSummary = removeParens(melee.name) + ", " +  removeParens(ranged.name);
			else
				weaponSummary = removeParens(melee.name);
		}
		return weaponSummary;
	}

	private String removeParens(String string) {
		String[] split = string.split("\\(");
		return split[0];
	}
}
