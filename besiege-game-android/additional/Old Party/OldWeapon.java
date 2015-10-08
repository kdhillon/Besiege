/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import kyle.game.besiege.panels.BottomPanel;

import com.badlogic.gdx.utils.Array;

import static kyle.game.besiege.party.OldArmor.*;

public enum OldWeapon {
	
	// also have a default armor
	
	// Tier 0 (Best total = 1)
	PITCHFORK (0, "Pitchfork", "Farmer", 1, 0, -1, false, false, CLOTHES),
	
	// Tier 2?
	MILITARY_FORK (2, "Military Fork", "Militia", 1, 1, -1, false, false, CLOTHES),
	
	// Tier 4 (Best total = 2)
	SPEAR (4, "Spear", "Spearman", 		2, 1, -1, false, false, LEATHER), 
	HATCHET (4, "Hatchet", "Axeman", 	2, 0, 0, true, false, LEATHER),
	CLUB (4, "Cudgel", "Clubman", 		2, 0, 0, true, true, LEATHER),

	// Tier 4 Ranged
	SHORTBOW (4, "Dagger", "Archer", 	-1, -2, 0, true, false, CLOTHES),

	// Tier 6 Mounted (Best total = 2)
	CAVALRY_SPEAR (6, "Cavalry Spear", "Horseman", 	3, 0, -1, false, false, LEATHER),
	CAVALRY_AXE (6, "Cavalry Axe", "Horseman", 		2, 0, 0, true, false, LEATHER),
	CAVALRY_PICK (6, "Cavalry Pick", "Horseman", 	3, -1, 0, true, false, LEATHER),

	// Tier 6 (Best total = 3)
	PIKE (6, "Pike", "Pikeman",  				2, 3, -2, false, false, LIGHTPLATE),
	HALBERD (6, "Halberd", "Poleman", 			3, 2, -2, false, false, LIGHTPLATE),
	LONGSWORD (6, "Longsword", "Swordsman", 	2, 2, -1, false, false, CHAINMAIL),
	BATTLE_AXE (6, "Battle Axe", "Axeman", 		3, 1, -1, false, false, CHAINMAIL),
	SHORTSWORD (6, "Shortsword", "Swordsman", 	2, 1, 0, true, false, STUDDED),
	WAR_HAMMER (6, "War Hammer", "Hammerman", 	3, 0, 0, true, true, STUDDED),
	MACE (6, "Mace", "Maceman", 				2, 1, 0, true, true, STUDDED),
	
	// Tier 6 Ranged
	CROSSBOW (6, "Dagger", "Crossbowman", 	0, -1, 0, true, false, LEATHER),
	RECURVE (6, "Dagger", "Bowman", 		0, -1, 0, true, false, LEATHER),  
	LONGBOW (6, "Dagger", "Longbowman", 	0, -1, 0, true, false, LEATHER), 

	// Tier 8 Mounted (Tier '7') 
	LANCE (8, "Lance", "Lancer", 				4, 1, -1, false, false, CHAINMAIL),
	ARMING_SWORD (8, "Arming Sword", "Slicer", 	3, 1, 0, true, false, LIGHTPLATE),
	FLAIL (8, "Flail", "Flailer", 				4, 0, 0, true, false, LIGHTPLATE),
	
	// Tier 8 (Best total = 4)
	GUISARME (8, "Guisarme", "Pikemaster", 			3, 3, -2, false, false, HEAVYPLATE),
	VOULGE (8, "Voulge", "Blademaster", 			4, 2, -2, false, false, HEAVYPLATE),
	GREATSWORD (8, "Claymore", "Swordmaster", 		3, 2, -1, false, false, HEAVYPLATE),
	GLAIVE (8, "Glaive", "Axemaster", 				4, 1, -1, false, false, HEAVYPLATE),
	FALCHION (8, "Falchion", "Sabermaster", 		3, 1, 0, true, false, HEAVYPLATE),
	MAUL (8, "Maul", "Hammermaster", 				4, 1, -1, false, true, HEAVYPLATE),
	MORNINGSTAR (8, "Morningstar", "Macemaster", 	3, 1, 0, true, true, HEAVYPLATE), 
	
	// Tier 8 Ranged
	ADV_CROSSBOW (8, "Shortsword", "Crossbow Master", 	-1, -2, 0, true, false, CHAINMAIL), 
	ADV_RECURVE  (8, "Shortsword", "Bowmaster",    		-1, -2, 0, true, false, STUDDED),  
	ADV_LONGBOW  (8, "Shortsword", "Longbow Master",  	-1, -2, 0, true, false, LEATHER); 

									 	// 0  1, 2,  3,  4,  5,  6,  7,  8,  9 
	public static final int[] TIER_COST = {0, 0, 5, 10, 15, 25, 40, 60, 80, 120};
	//public static final int[] UPG_COST =  {0, 0,  5,  0, 10,  0, 15,  0, 25, 0};
	
	public final int tier;
	public final String name;
	public final String troopName;
	public final int atkMod;
	public final int defMod;
	public final int spdMod;
	public final boolean oneHand;
	public final boolean blunt;
	
	public final OldArmor oldArmor;
		
	public static Array<OldWeapon> all;
	public static Array<OldWeapon> bandit;
	public static Array<OldWeapon> city;

	//public final int cost; // cost to purchase
	
	private OldWeapon(int tier, String name, String troopName, int atkMod, int defMod, int spdMod, boolean oneHand, boolean blunt, OldArmor oldArmor) {
		this.tier = tier;
		this.name = name;
		this.troopName = troopName;
		this.atkMod = atkMod;
		this.defMod = defMod;
		this.spdMod = spdMod;
		this.oneHand = oneHand;
		this.blunt = blunt;
		this.oldArmor = oldArmor;
	}

	public static RangedWeapon getRanged(OldWeapon oldWeapon) {
		if (oldWeapon == OldWeapon.SHORTBOW) return RangedWeapon.SHORTBOW;
		if (oldWeapon == OldWeapon.LONGBOW) return RangedWeapon.LONGBOW;
		if (oldWeapon == OldWeapon.CROSSBOW)return RangedWeapon.CROSSBOW;
		if (oldWeapon == OldWeapon.RECURVE) return RangedWeapon.RECURVE;
		if (oldWeapon == OldWeapon.ADV_LONGBOW) return RangedWeapon.ADV_LONGBOW;
		if (oldWeapon == OldWeapon.ADV_CROSSBOW) return RangedWeapon.ADV_CROSSBOW;
		if (oldWeapon == OldWeapon.ADV_RECURVE) return RangedWeapon.ADV_RECURVE;
		else return null;
	}
	
	public static void load() {
		OldWeapon[] banditArray = {PITCHFORK, SPEAR, MACE, CLUB, HATCHET};
		bandit = new Array<OldWeapon>(banditArray);
	}
	
//	public static int getUpgradeCost(int tier) {
//		if (tier == TIER_COST.length) return 999999;
//		return (TIER_COST[tier]-TIER_COST[tier-1]);
//	}
	
	public static Array<OldWeapon> upgrade(OldWeapon oldWeapon) {
		Array<OldWeapon> upgrades = new Array<OldWeapon>();
		switch (oldWeapon) {
			case PITCHFORK :
				upgrades.add(MILITARY_FORK);
				break;
				
			case MILITARY_FORK : 
				upgrades.add(SPEAR); 
				upgrades.add(HATCHET); 
				upgrades.add(CLUB);
				break;
				
			case SPEAR :
				upgrades.add(PIKE);
				upgrades.add(HALBERD);
				upgrades.add(LONGSWORD);
				break;
			case HATCHET :
				upgrades.add(LONGSWORD);
				upgrades.add(BATTLE_AXE);
				upgrades.add(SHORTSWORD);
				break;
			case CLUB :
				upgrades.add(SHORTSWORD);
				upgrades.add(WAR_HAMMER);
				upgrades.add(MACE);
				break;
				
			case PIKE :
				upgrades.add(GUISARME);
				break;
			case HALBERD :
				upgrades.add(GLAIVE);
				break;
			case LONGSWORD :
				upgrades.add(GREATSWORD);
				break;
			case BATTLE_AXE :
				upgrades.add(VOULGE);
				break;
			case SHORTSWORD :
				upgrades.add(FALCHION);
				break;
			case WAR_HAMMER :
				upgrades.add(MAUL);
				break;
			case MACE :
				upgrades.add(MORNINGSTAR);
				break;
				
			case CAVALRY_SPEAR :
				upgrades.add(LANCE);
				break;
			case CAVALRY_AXE :
				upgrades.add(ARMING_SWORD);
				break;
			case CAVALRY_PICK :
				upgrades.add(FLAIL);
				break;	
				
			// ranged
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
			
			default :
				BottomPanel.log("Upgrade for \"" + oldWeapon.name + "\" not found!");
		}
		return upgrades;
	}
	
	public int getCost() {
		return TIER_COST[this.tier];
	}
}
