/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import kyle.game.besiege.Assets;

public enum Equipment {
    HEADDRESS_MESO_1 ("Desert Headdress", "mesoHeaddress3", Type.HEAD, 0, 1, 0, 20),
    HEADDRESS_MESO_2 ("Desert Headdress", "mesoHeaddress2", Type.HEAD, 0, 1, 0, 20),
    HEADDRESS_MESO_3 ("Desert Headdress", "mesoHeaddress", Type.HEAD, 0, 1, 0, 20),
    HEADDRESS_PLAINS_1 ("Plains Headdress", "plainsHeaddress3", Type.HEAD, 0, 1, 0, 20),
    HEADDRESS_PLAINS_2 ("Plain Headdress", "plainsHeaddress2", Type.HEAD, 0, 1, 0, 20),
    HEADDRESS_PLAINS_3 ("Plain Headdress", "plainsHeaddress", Type.HEAD, 0, 1, 0, 20),
    HEADDRESS_FOREST_1 ("Forest Headdress", "forestHeaddress3", Type.HEAD, 0, 1, 0, 20),
    HEADDRESS_FOREST_2 ("Forest Headdress", "forestHeaddress2", Type.HEAD, 0, 1, 0, 20),
    HEADDRESS_FOREST_3 ("Forest Headdress", "forestHeaddress", Type.HEAD, 0, 1, 0, 20),

	MASK_WOLF("Wolf Mask", "wolf-mask", Type.HEAD, 0, 0, 0, 20),
	MASK_SKULL("Skull Mask", "blue-mask", Type.HEAD, 0, 0, 0, 20),
	MASK_TROPICAL("Tropical Mask", "shaman-mask-skull", Type.HEAD, 0, 0, 0, 20),
	MASK_BEAR("Bear Mask", "bear-mask", Type.HEAD, 0, 0, 0, 20),
	MASK_HAWK("Hawk Mask", "hawk-mask", Type.HEAD, 0, 0, 0, 20),
	MASK_BUFFALO("Buffalo Mask", "bison-mask", Type.HEAD, 0, 0, 0, 20),

    WOLFSKIN_CAP("Wolfskin Cap", "wolfskinCap", Type.HEAD, 0, 1, 0, 20),
    RED_FOX_TURBAN("Fox Turban", "foxTurban", Type.HEAD, 0, 1, 0, 20);

//	IRON_SHIELD ("Iron Shield", Type.SHIELD, 0, 2, -2, 40),
//	IRON_BREASTPLATE ("Breastplate", Type.CHEST, 0, 3, -2, 999999),
//	IRON_HELM ("Iron Helm", Type.HEAD, 0, 1, 0, 10),
//	WORK_HORSE ("Workhorse", Type.HORSE, 0, 0, 4, 20),
//	WAR_HORSE ("Warhorse", Type.HORSE, 0, 1, 6, 30);l
	
	public final String name;
	public final String textureName;
	public final Type type;
	public final int hp;
	public final int atkMod;
	public final int defMod;
	public final int spdMod;
	public enum Type {
		CHEST, HEAD, LEGS, ARMS, HORSE
	}
	
	private Equipment(String name, String textureName, Type type, int atkMod, int defMod, int spdMod, int hp) {
		this.hp = hp;
		this.name = name;
		this.textureName= textureName;
		this.type = type;
		this.atkMod = atkMod;
		this.defMod = defMod;
		this.spdMod = spdMod;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public TextureRegion getRegion() {
	    return Assets.equipment.findRegion(textureName);
    }
	
	
//	// returns an array containing the basic equipment for a particular soldier
//	public static Array<Equipment> getBaseEquipment(OldWeapon oldWeapon) {
//		Array<Equipment> equip = new Array<Equipment>();
//		
//		// add shields
////		if (weapon.oneHand && weapon != Weapon.SHORTBOW) equip.add(IRON_SHIELD);
//		
//		// add horse
//		switch (oldWeapon) {
//		case CAVALRY_SPEAR :
//			equip.add(WORK_HORSE);
//			break;
//		case CAVALRY_AXE :
//			equip.add(WORK_HORSE);
//			break;
//		case CAVALRY_PICK :
//			equip.add(WORK_HORSE);
//			break;
//		case LANCE :
//			equip.add(WAR_HORSE);
//			break;
//		case ARMING_SWORD :
//			equip.add(WAR_HORSE);
//			break;
//		case FLAIL :
//			equip.add(WAR_HORSE);
//			break;
//		}
//		return equip;
//	}
	
	public static Array<Equipment> getBaseEquipment(UnitType type) {
		Array<Equipment> equip = new Array<Equipment>();
		
		// add shields
//		if (weapon.oneHand && weapon != Weapon.SHORTBOW) equip.add(IRON_SHIELD);
		
		// add horse
//		switch (weapon) {
//		case CAVALRY_SPEAR :
//			equip.add(WORK_HORSE);
//			break;
//		case CAVALRY_AXE :
//			equip.add(WORK_HORSE);
//			break;
//		case CAVALRY_PICK :
//			equip.add(WORK_HORSE);
//			break;
//		case LANCE :
//			equip.add(WAR_HORSE);
//			break;
//		case ARMING_SWORD :
//			equip.add(WAR_HORSE);
//			break;
//		case FLAIL :
//			equip.add(WAR_HORSE);
//			break;
//		}
		return equip;
	}
}
