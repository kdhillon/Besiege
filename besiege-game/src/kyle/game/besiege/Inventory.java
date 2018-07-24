package kyle.game.besiege;

import java.util.HashMap;

import kyle.game.besiege.party.ArmorType;
import kyle.game.besiege.party.RangedWeaponType;
import kyle.game.besiege.party.UnitType;
import kyle.game.besiege.party.WeaponType;

public class Inventory {

	// Contains info about weapons and armor player currently has
	public HashMap<WeaponType, Integer> weapons;
	public HashMap<ArmorType, Integer> armors;
	public HashMap<RangedWeaponType, Integer> rangedWeapons;

	public Inventory() {
		weapons = new HashMap<WeaponType, Integer>();
		rangedWeapons = new HashMap<RangedWeaponType, Integer>();
		armors = new HashMap<ArmorType, Integer>();
		
		// testing:
//		addWeapons(UnitLoader.weaponTypes.values());
//		addWeapons(UnitLoader.weaponTypes.values());
//		addWeapons(UnitLoader.weaponTypes.values());
//	addRanged(UnitLoader.rangedWeaponTypes.values());
//		addArmor(UnitLoader.armorTypes.values());
//		addArmor(UnitLoader.armorTypes.values());
//		addArmor(UnitLoader.armorTypes.values());
//		addArmor(UnitLoader.armorTypes.values());
//		addArmor(UnitLoader.armorTypes.values());
//		addArmor(UnitLoader.armorTypes.values());
//		addArmor(UnitLoader.armorTypes.values());
//		addArmor(UnitLoader.armorTypes.values());
	}
	
	// Add these to inventory
	public void addWeapons(Iterable<WeaponType> newWeapons) {
		for (WeaponType weapon : newWeapons) {
			int existing = 0;
			if (weapons.containsKey(weapon)) existing = this.weapons.get(weapon);
			this.weapons.put(weapon, existing + 1);
		}
	}

	// Add these to inventory
	public void addRanged(Iterable<RangedWeaponType> newWeapons) {
		for (RangedWeaponType weapon : newWeapons) {
			int existing = 0;
			if (rangedWeapons.containsKey(weapon)) existing = this.rangedWeapons.get(weapon);
			this.rangedWeapons.put(weapon, existing + 1);
		}
	}

	// Add these to inventory
	public void addArmor(Iterable<ArmorType> newArmors) {
		for (ArmorType armor : newArmors) {
			if (armor.clothes) continue;
			int existing = 0;
			if (armors.containsKey(armor)) existing = this.armors.get(armor);
			this.armors.put(armor, existing + 1);
		}
	}
	
	// Remove from inventory
	public void removeWeapon(WeaponType toRemove) {
		assert(weapons.containsKey(toRemove));
		assert(weapons.get(toRemove) > 0);
		if (weapons.get(toRemove) == 1) {
			weapons.remove(toRemove);
		}
		else {
			weapons.put(toRemove, weapons.get(toRemove)-1);
		}
	}
	public void removeRanged(RangedWeaponType toRemove) {
		assert(rangedWeapons.containsKey(toRemove));
		assert(rangedWeapons.get(toRemove) > 0);
		if (rangedWeapons.get(toRemove) == 1) {
			rangedWeapons.remove(toRemove);
		}
		else {
		rangedWeapons.put(toRemove, rangedWeapons.get(toRemove)-1);
		}
	}
	public void removeArmor(ArmorType toRemove) {
		assert(armors.containsKey(toRemove));
		assert(armors.get(toRemove) > 0);
		if (armors.get(toRemove) == 1) {
			armors.remove(toRemove);
		}
		else{
			armors.put(toRemove, armors.get(toRemove)-1);
		}
	}
	
	public boolean hasWeapon(UnitType type) {
		if (type.melee != null) {
			if (!weapons.containsKey(type.melee)) return false;
		}
		return true;
	}
	public boolean hasRanged(UnitType type) {
		if (type.ranged != null) {
			if (!rangedWeapons.containsKey(type.ranged)) return false;
		}
		return true;
	}
	public boolean hasArmor(UnitType type) {
		if (type.armor != null && !type.armor.clothes) {
			if (!armors.containsKey(type.armor)) return false;
		}
		return true;
	}
	
	public boolean canEquip(UnitType type) {		
//		System.out.println(hasWeapon(type) + " " + hasRanged(type) + " " + hasArmor(type));
		return hasWeapon(type) && hasRanged(type) && hasArmor(type);
	}
	
	public void equip(UnitType type) {
		if (type.melee == null) throw new java.lang.AssertionError();
		removeWeapon(type.melee);
		if (type.ranged != null)
			removeRanged(type.ranged);
		if (!type.armor.clothes)
			removeArmor(type.armor);
	}
}
