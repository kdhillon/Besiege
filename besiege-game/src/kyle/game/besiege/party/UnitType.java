package kyle.game.besiege.party;

import com.badlogic.gdx.utils.Array;

/* contains information about a specific unit type */
public class UnitType {
	
	// make this an object later 
	public String name;
	public UnitClass unitClass;
	public int tier;
	public WeaponType melee;
	public RangedWeaponType ranged;
	public ArmorType armor;
	
	public String[]		upgradeStrings;
	public UnitType[]	upgrades;
	
	// shield is a boolean for now
	public boolean shield;	
}
