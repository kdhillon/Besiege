/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import kyle.game.besiege.Inventory;
import kyle.game.besiege.panels.BottomPanel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class Soldier implements Comparable { // should create a heal-factor, so garrisonning will heal faster

	public static enum SoldierType {
		INFANTRY, ARCHER, CAVALRY
	}

	// getTier()  0, 1, 2, 3,  4,  5,  6,  7,  8, 9, max}
	private final static int[] LEVEL_TIER = {1, 3, 5, 7, 10, 12, 15, 18, 22, 25, 31};
	public final static boolean[] ATK_TIER = {true, false, false, true, false, true, false, false, true, false, false};
	public final static boolean[] DEF_TIER = {true, false, true, false, true, false, false, true, false, false, false};
	public final static boolean[] SPD_TIER = {true, false, false, false, false, false, true, false, false, true, false};
	private static final float LEVEL_FACTOR = 1.1f; // exp needed for next level each time;
	private static final int INITIAL_NEXT = 20;
	private static final float HEAL_TIME = 120; // seconds
	public static final int MAX_LEVEL = 30;
	private static final double COST_FACTOR = 2; // 
	private static final double UPGRADE_FACTOR = 2;

	private static final String VETERAN = ""; // "Veteran" or maybe invisible

	public Party party;

	public Soldier killedBy; // once killed

	public int level;
	public int exp;
	public int next;   // exp needed for level up
	//	public int getTier();  // goes up to getTier() 7 (aka 3.5) so used in arrays above 
	private int nextUpgrade; // next level at which soldier can be upgraded

	// personal attributes
	public Color skinColor;
	public boolean female;

	//	public int atk; // total
	//	public int def;
	//	public int spd;

	public int baseAtk; // increases with level
	public int baseDef;
	public int baseSpd;
	
	public UnitType unitType;

	private boolean wounded;
//	private boolean retreated;
	
	private float timeWounded;
	public boolean canUpgrade;

	public float healTime;

//	public Weapon weapon;
//	public RangedWeapon rangedWeapon;
//	
//	public Armor armor;
//	public Color armorColor;
	
	public Array<Equipment> equipment; // specify this in the unit type later

	public Soldier() {}

	public Soldier(UnitType unitType, Party party) {
		this.unitType = unitType;
		
		this.equipment = Equipment.getBaseEquipment(unitType);

		//		if (rangedWeapon != null) System.out.println(rangedWeapon.name);

		this.party = party;

		//		if (Math.random() > 0.5)
		//			getTier() = weapon.getTier();
		//		else getTier() = weapon.getTier() + 1;

		int tier = getTier();;
		
		this.level = LEVEL_TIER[tier] + (int) (Math.random()*(LEVEL_TIER[tier + 1]-LEVEL_TIER[tier]));

		this.nextUpgrade = LEVEL_TIER[getTier()+1];

		this.exp = 0;
		this.next = (int) (Math.pow(LEVEL_FACTOR, level)*INITIAL_NEXT); 

		baseAtk = 0;
		baseDef = 0;
		baseSpd = 2;
		for (int i = 0; i <= getTier(); i++) {
			if (ATK_TIER[i]) baseAtk++;
			if (DEF_TIER[i]) baseDef++;
			if (SPD_TIER[i]) baseSpd++;
		}

		wounded = false;
		this.healTime = (float) (HEAL_TIME + Math.random() * HEAL_TIME - HEAL_TIME/2); //different for all units
		timeWounded = 0;

		//		this.equipment = new Array<Equipment>();
		//		while (equipment.size > 0)
		//			this.equipment.add(equipment.pop());
		createSkinColor();
		if (Math.random() < 0.5) {
			this.female = true;
		}
		calcStats();
	}

	public String getName() {
		String name;
		if (getTier() % 2 == 0)
			name = unitType.name;
		else name = VETERAN + unitType.name;
		return name;
	}

	public int getTier() {
//		int tier = 0;
//		for (int i = 0; i < LEVEL_TIER.length; i++) {
//			if (this.level < LEVEL_TIER[i]) {
//				tier = i;
//				break;
//			}
//		}
		
		return unitType.tier * 2 - 1;
	}

	public void createSkinColor() {
		// generate out of 256
		// darkest is 45, 34, 30
		// lightest is 225, 172, 150
		// 1.3, 1.14
		// generate base between 45 and 256
		int skinColorRed = (int) (Math.random()*105);
		skinColorRed += 50;
		int base_g = (int) (skinColorRed/1.4f);
		int base_b = (int) (base_g/1.13f);
		this.skinColor = new Color(skinColorRed/256.0f, base_g/256.0f, base_b/256.0f, 1);

	}
	
	public Color getColor() {
		return skinColor;
	}
	

	//	public Soldier(Array<Weapon> weapons, Party party) {
	//		this.weapon = weapons.random();
	//		this.rangedWeapon = Weapon.getRanged(weapon);
	//		this.equipment = Equipment.getBaseEquipment(weapon);
	//
	//		this.party = party;
	//		
	//		if (Math.random() > 0.5)
	//			getTier() = weapon.getTier();
	//		else getTier() = weapon.getTier() + 1;
	//		this.nextUpgrade = LEVEL_TIER[getTier()+1];
	//		
	//		if (getTier() % 2 == 0)
	//			this.name = weapon.troopName;
	//		else this.name = VETERAN + weapon.troopName;
	//		
	//		this.level = LEVEL_TIER[getTier()] + (int) (Math.random()*(LEVEL_TIER[getTier() + 1]-LEVEL_TIER[getTier()]));
	//		this.exp = 0;
	//		this.next = (int) (Math.pow(LEVEL_FACTOR, level)*INITIAL_NEXT); 
	//		
	//		baseAtk = 0;
	//		baseDef = 0;
	//		baseSpd = 2;
	//		for (int i = 0; i <= getTier(); i++) {
	//			if (ATK_TIER[i]) baseAtk++;
	//			if (DEF_TIER[i]) baseDef++;
	//			if (SPD_TIER[i]) baseSpd++;
	//		}
	//		
	//		wounded = false;
	//		timeWounded = 0;
	//		
	////		this.equipment = new Array<Equipment>();
	////		while (equipment.size > 0)
	////			this.equipment.add(equipment.pop());
	//		
	//		calcBonus();
	//		calcStats();
	//	}

	//	public Soldier(Soldier template) {
	//		this.level = 1;
	//		this.exp = 0;
	//		this.next = 100;
	//		this.nextUpgrade = 5;
	//		
	//		this.name = template.name;
	//		this.baseAtk = template.baseAtk;
	//		this.baseDef = template.baseDef;
	//		this.baseSpd = template.baseSpd;
	//		
	//		wounded = false;
	//		timeWounded = 0;
	//		
	//		this.weapon = template.weapon;
	//		this.equipment = new Array<Equipment>();
	//		while (template.equipment.size > 0)
	//			this.equipment.add(template.equipment.pop());
	//		
	//		calcBonus();
	//		calcStats();
	//	}

	public int getBonusAtk() {
		int bonusAtk = 0;
		bonusAtk += unitType.melee.atkMod;
		for (Equipment e : equipment) {
			bonusAtk += e.atkMod;
		}
		return bonusAtk;
	}

	public int getBonusDef() {
		int bonusDef = 0;
		bonusDef += unitType.melee.defMod;
		bonusDef += unitType.armor.defMod;
		
		for (Equipment e : equipment) {
			bonusDef += e.defMod;
		}
		return bonusDef;
	}

	public int getBonusSpd() {
		int bonusSpd = 0;
		bonusSpd += unitType.melee.spdMod;
		bonusSpd += unitType.armor.spdMod;

//		for (Equipment e : equipment) {
//			bonusSpd += e.spdMod;
//		}
		return bonusSpd;
	}

	public void calcStats() {
		//		atk = baseAtk + bonusAtk;
		//		def = baseDef + bonusDef;
		//		spd = baseSpd + bonusSpd;
		if (party != null) party.calcStats();
	}

	public void addExp(int additional) {
		if (this.level != MAX_LEVEL) {
			this.exp += additional;
			while (this.exp > next) {
				additional -= next;
				levelUp();
				this.exp += additional;
				if (this.exp < 0) this.exp = 0;
			}

//			System.out.println(this.exp + " / " + next);

		}
	}

	public void levelUp() {
		if (this.canUpgrade) return;
		level++;
		exp = 0;
		next = (int) (LEVEL_FACTOR*next);

//		System.out.println("levelling up: " + level + " and " + nextUpgrade);

		if (level >= nextUpgrade) {
			if (this.getTier() % 2 != 0) {
				if (party.player)
					BottomPanel.log(this.getName() + " ready for upgrade!", "green");
				if (this.unitType.upgrades != null && this.unitType.upgrades.length > 0) canUpgrade = true;
				else canUpgrade = false;
			}
			else upgrade(null);
		}

		calcStats();
	}
	
	// Upgrades with items from inventory
	public boolean upgrade(UnitType unitType, Inventory inventory) {
		if (party.player && party.army != null) {
			inventory.equip(unitType);
			
			if (unitType != null) {// only if not upgrading to veteran
				this.unitType = unitType;
			}

			if (LEVEL_TIER.length <= getTier() + 1) return false;

			this.nextUpgrade = LEVEL_TIER[getTier() + 1];

			if (ATK_TIER[getTier()]) baseAtk++;
			if (DEF_TIER[getTier()]) baseDef++;
			if (SPD_TIER[getTier()]) baseSpd++;

			this.canUpgrade = false;

			calcStats();
		}	
		return true;
	}

	// Old upgrade method, maybe delete this.
	public boolean upgrade(UnitType unitType) { // returns true if upgraded, false otherwise	
		int cost;
		if (unitType != null) cost = this.getUpgradeCost(); //Weapon.UPG_COST[upgrade.getTier()];
		else cost = 0;

		if (party.player && party.army != null) cost *= party.army.getCharacter().getAttributeFactor("Bargaining");
		if (!(party.wealth - cost >= party.minWealth) && cost != 0) {
			if (party.player)
				BottomPanel.log("Cannot afford " + cost + " cost to upgrade " + this.getName());
			return false;
		}
		else {
			party.wealth -= cost;

			if (unitType != null) {// only if not upgrading to veteran
				this.unitType = unitType;
			}

			if (LEVEL_TIER.length <= getTier() + 1) return false;

			this.nextUpgrade = LEVEL_TIER[getTier() + 1];

			if (ATK_TIER[getTier()]) baseAtk++;
			if (DEF_TIER[getTier()]) baseDef++;
			if (SPD_TIER[getTier()]) baseSpd++;

			this.canUpgrade = false;

			calcStats();

			return true;
		}
	}
	
	public UnitType[] getUpgrades() {
		return unitType.upgrades;
	}
	
	// change later
	public int getCost() {
		return 10;
	}

	public int getExpForKill() {
		return level;
	}

	public boolean isHealed() {
		if (party.army.getKingdom().clock - timeWounded >= healTime)
			return true;
		return false;
	}
	public void wound() {
		wounded = true;
		timeWounded = party.army.getKingdom().clock; // seconds elapsed when wounded
	}
	public void heal() {
		wounded = false;
	}

	public void addStats(int add) {
		baseAtk += add;
		baseDef += add;
		baseSpd += add;
	}
	public boolean isWounded() {
		return wounded;
	}
	public int getAtk() {
		return baseAtk + getBonusAtk();	
	}
	public int getDef() {
		return baseDef + getBonusDef();	
	}
	public int getSpd() {
		return baseSpd + getBonusSpd();	
	}

	public Equipment getHorse() {
		for (Equipment e : equipment) {
			if (e.type == Equipment.Type.HORSE) 
				return e;
		}
		return null;
	}

	public Equipment getShield() {
//		for (Equipment e : equipment) {
//			if (e.type == Equipment.Type.SHIELD) 
//				return e;
//		}
		if (unitType.shield) {
			return Equipment.WOOD_SHIELD;
		}
		return null;
	}

	// square function
	public int getBuyCost() {
		return (int) (Math.pow(this.level, 1.2) * COST_FACTOR);
	}

	public int getUpgradeCost() {
		return (int) (this.level * UPGRADE_FACTOR);
	}
	
	public int getHireCost() {
		return (int) (this.level * UPGRADE_FACTOR);
	}

	//	public String equipmentList() {
	//		String list;
	//		if (equipment.size == 0)
	//			list = "None";
	//		else {
	//			list = "";
	//			for (Equipment e : equipment) {
	//				list += e.name + "\n";
	//			}
	//		}
	//		return list;
	//	}

	@Override
	public int compareTo(Object thatObject) {
		Soldier that = (Soldier) thatObject;
		//		return this.getName().compareTo(that.getName());		
		if (this.level != that.level)
			return that.level - this.level;
		else return this.getName().compareTo(that.getName());
	}

	public SoldierType getType() {
		if (this.getHorse() != null) return SoldierType.CAVALRY;
		if (this.unitType.ranged != null) return SoldierType.ARCHER;
		else return SoldierType.INFANTRY;
	}
	
	public WeaponType getWeapon() {
		return this.unitType.melee;
	}
	
	public RangedWeaponType getRanged() {
		return this.unitType.ranged;
	}
	
	public ArmorType getArmor() {
		return this.unitType.armor;
	}
	
	public UnitClass getUnitClass() {
		return this.unitType.unitClass;
	}
}
