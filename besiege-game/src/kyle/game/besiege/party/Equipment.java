/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import com.badlogic.gdx.utils.Array;

public enum Equipment {
	WOOD_SHIELD ("Wood Shield", Type.SHIELD, 0, 1, -1, 12),
	IRON_SHIELD ("Iron Shield", Type.SHIELD, 0, 2, -2, 20),
	IRON_BREASTPLATE ("Breastplate", Type.CHEST, 0, 3, -2, 999999),
	IRON_HELM ("Iron Helm", Type.HEAD, 0, 1, 0, 10),
	WORK_HORSE ("Workhorse", Type.HORSE, 0, 0, 6, 20),
	WAR_HORSE ("Warhorse", Type.HORSE, 0, 1, 7, 30);
	
	public final String name;
	public final Type type;
	public final int hp;
	public final int atkMod;
	public final int defMod;
	public final int spdMod;
	public enum Type {
		SHIELD, CHEST, HEAD, LEGS, ARMS, HORSE
	}
	
	private Equipment(String name, Type type, int atkMod, int defMod, int spdMod, int hp) {
		this.hp = hp;
		this.name = name;
		this.type = type;
		this.atkMod = atkMod;
		this.defMod = defMod;
		this.spdMod = spdMod;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	
	// returns an array containing the basic equipment for a particular soldier
	public static Array<Equipment> getBaseEquipment(Weapon weapon) {
		Array<Equipment> equip = new Array<Equipment>();
		
		// add shields
//		if (weapon.oneHand && weapon != Weapon.SHORTBOW) equip.add(IRON_SHIELD);
		
		// add horse
		switch (weapon) {
		case CAVALRY_SPEAR :
			equip.add(WORK_HORSE);
			break;
		case CAVALRY_AXE :
			equip.add(WORK_HORSE);
			break;
		case CAVALRY_PICK :
			equip.add(WORK_HORSE);
			break;
		case LANCE :
			equip.add(WAR_HORSE);
			break;
		case ARMING_SWORD :
			equip.add(WAR_HORSE);
			break;
		case FLAIL :
			equip.add(WAR_HORSE);
			break;
		}
		return equip;
	}
}
