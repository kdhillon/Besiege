package kyle.game.besiege.party;

/* contains information about a specific unit type */
public class UnitType {
	
	// make this an object later 
	public String name;
	public CultureType cultureType;
	public int tier;
	public WeaponType melee;
	public RangedWeaponType ranged;
	public AmmoType ammoType;
	public ArmorType armor;
//	public boolean hideBonus;
//	public boolean hideBonus;
//	public boolean mounted;
	
	public String[]		upgradeStrings;
	public UnitType[]	upgrades;
	
	// shield is a boolean for now
	public boolean shield;	
}
