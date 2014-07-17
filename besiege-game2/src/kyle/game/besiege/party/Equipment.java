/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import com.badlogic.gdx.utils.Array;

public enum Equipment {
	SHIELD ("Shield", Type.OFFHAND, 0, 1, -1),
	IRON_BREASTPLATE ("Breastplate", Type.CHEST, 0, 3, -2),
	IRON_HELM ("Iron Helm", Type.HEAD, 0, 1, 0),
	WAR_HORSE ("Warhorse", Type.HORSE, 0, 3, 10);
	
	public final String name;
	public final Type type;
	public final int atkMod;
	public final int defMod;
	public final int spdMod;
	public enum Type {
		OFFHAND, CHEST, HEAD, LEGS, ARMS, HORSE
	}
	
	private Equipment(String name, Type type, int atkMod, int defMod, int spdMod) {
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
		
		switch (weapon) {
		case CAVALRY_SPEAR :
			equip.add(WAR_HORSE);
			break;
		case CAVALRY_AXE :
			equip.add(WAR_HORSE);
			break;
		case CAVALRY_PICK :
			equip.add(WAR_HORSE);
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
