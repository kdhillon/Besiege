/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.*;
import kyle.game.besiege.MultiValue.TypeInfo;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.PanelUnit;

public class Soldier implements Comparable<Soldier> { // should create a heal-factor, so garrisonning will heal faster
	public static boolean WEAPON_NEEDED = false;
		
	//testing
	static int namesGenerated = 0;
	
	public static enum SoldierType {
		INFANTRY, ARCHER, CAVALRY, GENERAL, SHAMAN
	}

	// getTier()  0, 1, 2, 3,  4,  5,  6,  7,  8, 9, max}
	private final static short[] LEVEL_TIER = {1, 3, 5, 7, 10, 12, 15, 18, 22, 25, 31};
	public final static boolean[] ATK_TIER = {true, false, false, true, false, true, false, false, true, false, false};
	public final static boolean[] DEF_TIER = {true, false, true, false, true, false, false, true, false, false, false};
	public final static boolean[] SPD_TIER = {true, true, true, false, false, false, true, false, false, true, false};
	private static final float LEVEL_FACTOR = 1.1f; // exp needed for next level each time;
	private static final int INITIAL_NEXT = 20;
	private static final float HEAL_TIME = 120; // seconds
	public static final short MAX_LEVEL = 30;
	private static final double COST_FACTOR = 2; // 
	private static final double UPGRADE_FACTOR = 2;
	private static final int BASE_HP = 15;

//	private static final String VETERAN = "Vet."; // "Veteran" or maybe invisible

	public Party party; // containing playerPartyPanel
	public Subparty subparty;
	
	// personal attributes
	public Color skinColor;
	public boolean female;
	public short age; // fixed for now, later should change based on year
	protected String name;
	public boolean isImportant;

	// like pokemon, should randomize stats (not all be fixed)
	// can use this to generate epithet (high speed = "the quick", low speed = "the slow")
	
	// battle statistics
	public short kills = 0; // this could start randomized. instead, start at 0.
	public short battlesWon = 0; // even if wounded
	public short battlesSurvived = 0;
	public short battlesFled = 0;
	public short timesCaptured = 0;
	
	public Array<General> ipKilled;
	
	public Soldier killedBy; // once killed

	public short level;
	public short exp;
	public short next;   // exp needed for level up
	private short nextUpgrade; // next level at which soldier can be upgraded
	public boolean canUpgrade; // TODO remove this and check on fly
	
	public MultiValue hp;
	
	public MultiValue atk;
	public MultiValue def;
	public MultiValue spd;
	
	public UnitType unitType; // determines weapon and armor for now.

	// TODO remove this, just have "timeWounded"
	private boolean wounded;
	private float timeWounded;
	public float healTime; // time it takes to heal (not needed to store)

    // This doesn't need to be saved.
    private transient StrictArray<Equipment> equipment;

//	public Armor armor;
//	public Color armorColor;
	
//	public Array<Equipment> equipment; // specify this in the unit type later

	public Soldier() {}

	// copy constructor (for making generals)
	public Soldier(Soldier that) {
		this.party = that.party;
		this.subparty = that.subparty;
		this.skinColor = that.skinColor;
		this.female = that.female;
		this.age = that.age; // fixed for now, later should change based on year
		this.name = that.name;

		this.kills = that.kills; // this could start randomized. instead, start at 0.
		this.battlesWon = that.battlesWon; // even if wounded
		this.battlesSurvived = that.battlesSurvived;
		this.battlesFled = that.battlesFled;
		this.timesCaptured = that.timesCaptured;
		
		this.ipKilled = that.ipKilled;
		
		this.killedBy = that.killedBy; // once killed

		this.level = that.level;
		this.exp = that.exp;
		this.next = that.next;   // exp needed for level up
		this.nextUpgrade = that.nextUpgrade; // next level at which soldier can be upgraded
		this.canUpgrade = that.canUpgrade; // TODO remove this and check on fly	
		
		this.atk = that.atk;
		this.def = that.def;
		this.spd = that.spd;
		
		this.hp = that.hp;
		
		// TODO remove this and calculate on fly, based on level.
//		this.baseAtk = that.baseAtk; // increases with level
//		this.baseDef = that.baseDef;
//		this.baseSpd = that.baseSpd;
		
		this.unitType = that.unitType; // determines weapon and armor for now.

		this.wounded = that.wounded;		
		this.timeWounded= that.timeWounded;

		this.healTime = that.healTime;
	}

	public Soldier(UnitType unitType, Party party) {
//		this.equipment = Equipment.getBaseEquipment(unitType);

		//		if (rangedWeapon != null) System.out.println(rangedWeapon.name);

		this.party = party;
		this.unitType = unitType;

		//		if (Math.random() > 0.5)
		//			getTier() = weapon.getTier();
		//		else getTier() = weapon.getTier() + 1;
		assignBaseStats();
		init();
	}

	void assignBaseStats() {
		this.atk = new MultiValue("Attack");
		atk.addSubValue(TypeInfo.S_BASE_ATK);
		atk.addSubValue(TypeInfo.S_WEAPON);
		atk.addSubValue(TypeInfo.S_GENERAL);

		this.def = new MultiValue("Defense");
		def.addSubValue(TypeInfo.S_BASE_DEF);
//		def.addSubValue(TypeInfo.S_WEAPON);
		def.addSubValue(TypeInfo.S_ARMOR);
		def.addSubValue(TypeInfo.S_GENERAL);
		def.addSubValue(TypeInfo.S_SHIELD);

		this.spd = new MultiValue("Speed");
		spd.addSubValue(TypeInfo.S_BASE_SPD);
		spd.addSubValue(TypeInfo.S_WEAPON);
		spd.addSubValue(TypeInfo.S_ARMOR);
		spd.addSubValue(TypeInfo.S_GENERAL);
		spd.addSubValue(TypeInfo.S_SHIELD);

		this.hp = new MultiValue("HP");
		hp.addSubValue(TypeInfo.S_BASE_HP);
		hp.addSubValue(TypeInfo.S_HP_DEF);
		hp.addSubValue(TypeInfo.S_GENERAL);

		updateUnitType(unitType);

		int tier = getTier();;

		int baseAtk = 0;
		int baseDef = 0;
		int baseSpd = 2;
		for (int i = 0; i <= getTier(); i++) {
			if (ATK_TIER[i]) baseAtk++;
			if (DEF_TIER[i]) baseDef++;
			if (SPD_TIER[i]) baseSpd++;
		}

//		System.out.println("base speed: " + baseSpd);
		atk.updateValue(TypeInfo.S_BASE_ATK, baseAtk);
		def.updateValue(TypeInfo.S_BASE_DEF, baseDef);
		spd.updateValue(TypeInfo.S_BASE_SPD, baseSpd);

		hp.updateValue(TypeInfo.S_BASE_HP, BASE_HP);
		hp.updateValue(TypeInfo.S_HP_DEF, def);

		if (tier <= 0) this.level = 0;
		else this.level = (short) (LEVEL_TIER[tier] + (short) (Math.random()*(LEVEL_TIER[tier + 1]-LEVEL_TIER[tier])));
	}

	public void init() {
		this.nextUpgrade = LEVEL_TIER[getTier()+1];

		this.exp = 0;
		this.next = (short) (Math.pow(LEVEL_FACTOR, level)*INITIAL_NEXT);

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

		ipKilled = new Array<General>();

		age = (short) (Math.random() * 20 + 18); // between 17 and 48
	}

	public void generateName() {
		String firstName, lastName;
		if (this.female) {
			firstName = getCulture().nameGenerator.generateFirstNameFemale();
			lastName =  getCulture().nameGenerator.generateLastName();
		}
		else {
			firstName = getCulture().nameGenerator.generateFirstNameMale();
			lastName = getCulture().nameGenerator.generateLastName();
		}
		name = firstName + " " + lastName;
	}
	
	public String getLastName() {
		if (name == null) this.name = getName();
		String[] firstLast = this.name.split(" ");
		return firstLast[firstLast.length - 1];
	}
	
	public String getName() {
		if (this.name == null) {
			generateName();
			namesGenerated++;
//			System.out.println("Names generated: " + namesGenerated);
		}
		return name;
	}

	public String getTypeName() {
		String name;
//		if (getTier() % 2 != 0)
			name = unitType.name;
//		else name = VETERAN + unitType.name;
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
		// generate base between 100 and 150
		int min = 120;
		int max = 150;
		if (getCulture().name.equals("Tundra")) {
			min = 150;
			max = 200;
		}
		if (getCulture().name.equals("Desert")) {
			min = 100;
			max = 130;
		}
		int skinColorRed = Random.getRandomInRange(min, max);
		int base_g = (int) (skinColorRed/1.4f);
		int base_b = (int) (base_g/1.13f);
		this.skinColor = new Color(skinColorRed/256.0f, base_g/256.0f, base_b/256.0f, 1);
	}
	
	public Color getColor() {
		return skinColor;
	}
	

	//	public Soldier(Array<Weapon> weapons, Party playerPartyPanel) {
	//		this.weapon = weapons.random();
	//		this.rangedWeapon = Weapon.getRanged(weapon);
	//		this.equipment = Equipment.getBaseEquipment(weapon);
	//
	//		this.playerPartyPanel = playerPartyPanel;
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
	
	public void updateUnitType(UnitType unitType) {
		this.unitType = unitType;
		if (unitType.melee == null) {
			System.out.println(unitType.name + " doesn't have a melee weapon!");
		}
		this.updateWeapon(unitType.melee);
		this.updateArmor(unitType.armor);
//		if (unitType.shield)
		this.updateShield(this.getShield());
	}

	public void updateWeapon(WeaponType weapon) {
//		System.out.println(weapon.name);
//		System.out.println(atk.getValue());
		this.atk.updateValue(TypeInfo.S_WEAPON, weapon.atkMod);
//		this.def.updateValue(TypeInfo.S_WEAPON, weapon.defMod);
		this.spd.updateValue(TypeInfo.S_WEAPON, weapon.spdMod);
	}
	
	public void updateArmor(ArmorType armor) {
		this.def.updateValue(TypeInfo.S_ARMOR, armor.defMod);
		this.spd.updateValue(TypeInfo.S_ARMOR, armor.spdMod);
	}
	
	public void updateShield(ShieldType shield) {
		if (shield == null) return;
		this.spd.updateValue(TypeInfo.S_SHIELD, shield.spdMod);
	}

	public void updateGeneral(General general) {
		if (this.atk != null)
			this.atk.updateValue(TypeInfo.S_GENERAL, general.getBonusGeneralAtk());
		this.def.updateValue(TypeInfo.S_GENERAL, general.getBonusGeneralDef());	
	}

//	public float getBonusSpd() {
//		float bonusSpd = 0;
//		bonusSpd += unitType.melee.spdMod;
//		bonusSpd += unitType.armor.spdMod;
//
////		for (Equipment e : equipment) {
////			bonusSpd += e.spdMod;
////		}
//		return bonusSpd;
//	}

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
		next = (short) (LEVEL_FACTOR*next);

//		System.out.println("levelling up: " + level + " and " + nextUpgrade);

		if (level >= nextUpgrade) {
			if (this.getTier() % 2 != 0) {
				if (party.player)
					BottomPanel.log(this.getTypeName() + " ready for upgrade!", "green");
				if (this.unitType.upgrades != null && this.unitType.upgrades.length > 0) canUpgrade = true;
				else canUpgrade = false;
			}
			else upgrade(null);
		}

		calcStats();
	}
	
	// Upgrades with items from inventory
	public boolean upgrade(UnitType unitType, Inventory inventory) {
		if (WEAPON_NEEDED && party.player && party.army != null) {
			inventory.equip(unitType);
		}
		return upgrade(unitType);	
//			if (unitType != null) {// only if not upgrading to veteran
//				this.unitType = unitType;
//			}
//
//			if (LEVEL_TIER.length <= getTier() + 1) return false;
//
//			this.nextUpgrade = LEVEL_TIER[getTier() + 1];
//
//			if (ATK_TIER[getTier()]) atk.updateValue("Base", atk.getValue("Base").getValue() + 1);
//			if (DEF_TIER[getTier()]) def.updateValue("Base", def.getValue("Base").getValue() + 1);
//			if (SPD_TIER[getTier()]) spd.updateValue("Base", spd.getValue("Base").getValue() + 1);
//
//			this.canUpgrade = false;
//
//			calcStats();
//		}	
//		return true;
	}

	// Old upgrade method, maybe delete this.
	public boolean upgrade(UnitType unitType) { // returns true if upgraded, false otherwise	
		int cost;
		if (unitType != null) cost = this.getUpgradeCost(); //Weapon.UPG_COST[upgrade.getTier()];
		else cost = 0;

//		if (playerPartyPanel.player && playerPartyPanel.army != null) cost *= playerPartyPanel.army.getCharacter().getAttributeFactor("Bargaining");
		if ((party.wealth - cost < party.minWealth) && cost != 0) {
			if (party.player)
				BottomPanel.log("Cannot afford " + cost + " cost to upgrade " + this.getTypeName() + " player only has: " + party.wealth);
			return false;
		}
		else {
			party.wealth -= cost;

			if (unitType != null) {// only if not upgrading to veteran
				this.unitType = unitType;
			}

			if (LEVEL_TIER.length <= getTier() + 1) return false;

			this.nextUpgrade = LEVEL_TIER[getTier() + 1];

			if (ATK_TIER[getTier()]) atk.incrementValue(TypeInfo.S_BASE_ATK);
			if (DEF_TIER[getTier()]) def.incrementValue(TypeInfo.S_BASE_DEF);
			if (SPD_TIER[getTier()]) spd.incrementValue(TypeInfo.S_BASE_SPD);

			this.canUpgrade = false;

			calcStats();

			return true;
		}
	}
	
	public UnitType[] getUpgrades() {
		return unitType.upgrades;
	}

	// Returns true if this soldier is currently in a "hire" party.
	public boolean availableForHire() {
        return party.pt.hire;
    }

	// Returns true if the unit was killed, false if they were injured.
	public boolean casualty(boolean wasInAttackers, Soldier killer, boolean playerInA, boolean playerInD) {
		boolean killed = subparty.casualty(this);
		
		// randomize who gets the kill
		if (killedBy == null) {
			killedBy = killer;
		}
		
		if (killedBy == this) killedBy = null;
		
		if (killedBy != null) {
//			System.out.println("casualty for " + killedBy.getTypeName() + " of " + killedBy.party.getName() +" killing " + getTypeName() + " of " + party.getName());
			killedBy.registerKill(this);
		}
		
		if (playerInD || playerInA) {
			String status = getTypeName();
			if (killedBy != null) {
				if (killed) status += " was killed by " + killedBy.getTypeName() + "!";
				else status += " was wounded by " + killedBy.getTypeName() + "!";
			}
			else {
				if (killed) status += " was killed!";
				else status += " was wounded!";
			}

			String color = "white";
			// determines color of logged text (yellw if wounded, orange if killed, blue if enemy killed)
			if (playerInD == wasInAttackers) {
				if (killed) color = "red";
				else color = "orange";
			}
//			else if (aArmies.contains(army, true)) {
//				if (playerInD)
//					color = "cyan";
//				else color = "purple";
//			}
			else if (playerInA == wasInAttackers)
				color = "cyan";
			//	else color = "purple";
			
			BottomPanel.log(status, color);
		}
		return killed;
	}
	
	// change later
	public int getCost() {
		return 10;
	}

	public void registerKill(Soldier that) {
		this.addExp(that.getExpForKill());
		this.kills++;
		if (that.isImportant) this.ipKilled.add((General) that);
	}
	
	public void registerBattleVictory() {
		this.battlesSurvived++;
		this.battlesWon++;
	}
	
	public void registerBattleRetreat() {
		this.battlesSurvived++;
		this.battlesFled++;
	}
	
	public void registerBattleLoss() {
		this.battlesSurvived++;
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
		// For now, don't let shamans be wounded.
		if (this.isShaman()) return;

		wounded = true;
		if (party.army != null)
			timeWounded = party.army.getKingdom().clock; // seconds elapsed when wounded
	}
	public void heal() {
		wounded = false;
	}

//	public void addStats(int add) {
//		baseAtk += add;
//		baseDef += add;
//		baseSpd += add;
//	}
	public boolean isWounded() {
		return wounded;
	}
	
	public float getHp() {
		return hp.getValue();
	}
	
	public float getAtk() {
		return atk.getValue();
	}
	public float getDef() {
		return def.getValue();	
	}
	public float getSpd() {
		return spd.getValue();	
	}

	public Equipment getHorse() {
//		for (Equipment e : equipment) {
//			if (e.type == Equipment.Type.HORSE) 
//				return e;
//		}
		return null;
	}

	public ShieldType getShield() {
        return unitType.shieldType;
	}

	public StrictArray<Equipment> getEquipment() {
        if (equipment == null) {
            equipment = new StrictArray<>();
            if (getHead() != null)
                equipment.add(getHead());
            if (getHorse() != null)
                equipment.add(getHorse());
        }
        return equipment;
    }

	// Get helmet or headgear
    // See http://www.native-languages.org/headdresses.htm
	// This is overriden by Genearl and Shaman
	public Equipment getHead() {
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
	public int compareTo(Soldier thatObject) {
		Soldier that = thatObject;
		//		return this.getName().compareTo(that.getName());		
		if (this.level != that.level)
			return that.level - this.level;
		else return this.getTypeName().compareTo(that.getTypeName());
	}

	public SoldierType getType() {
		if (this.isGeneral()) return SoldierType.GENERAL;
		if (this.isShaman()) return SoldierType.SHAMAN;
		if (this.getHorse() != null) return SoldierType.CAVALRY;
		if (this.unitType.ranged != null && (this.unitType.ranged.type ==
				RangedWeaponType.Type.BOW || this.unitType.ranged.type ==
				RangedWeaponType.Type.ATLATL ||this.unitType.ranged.type ==
				RangedWeaponType.Type.SLING || this.unitType.ranged.type ==
				RangedWeaponType.Type.BLOWGUN || this.unitType.ranged.type ==
				RangedWeaponType.Type.FIREARM || this.unitType.ranged.type ==
				RangedWeaponType.Type.CROSSBOW))
			return SoldierType.ARCHER;
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
	
	public CultureType getCulture() {
		return this.unitType.cultureType;
	}
	
	public boolean isGeneral() {
		return false;
	}

	public boolean isShaman() {
		return false;
	}
}
