package kyle.game.besiege.party;

import java.util.HashMap;
import java.util.Scanner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import kyle.game.besiege.voronoi.Biomes;

public class UnitLoader {
	public static final String PATH = "data/units/";
	public static String rootName;

	public static HashMap<String, Color> colors;
	public static HashMap<String, UnitClass> classTypes;
//	public static HashMap<String, NewUnitType> unitTypes;
	public static HashMap<String, WeaponType> weaponTypes;
	public static HashMap<String, RangedWeaponType> rangedWeaponTypes;
	public static HashMap<String, ArmorType> armorTypes;
	public static HashMap<String, Biomes> biomes;
	public static HashMap<Biomes, UnitClass> biomeClasses;

	public static void load(String root) {
		rootName = root;

		classTypes = new HashMap<String, UnitClass>();
		
		
		loadColors();

		loadWeapons();
		loadRangedWeapons();

		loadArmors();

		loadUnits();
		
		assignUpgrades();
		
		initializeBiomes();
		
//		printAllUnits();
//		
//		printAllBiomes();
//		
//		for (NewUnitType unit : unitTypes.values()) {
//			printUnit(unit);
//		}
		//		for (NewWeaponType weapon : weaponTypes.values()) {
		//			printWeapon(weapon);
		//		}
	}
	
	public static void initializeBiomes() {
		// load biomes into a hashmap first (may be unnecessary)
		biomes = new HashMap<String, Biomes>();
		for (Biomes b : Biomes.values()) {
			biomes.put(b.toString(), b);
		}
		
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_biomes.txt").reader()).useDelimiter("\\t\\t*|\r\n[\r\n]*");
		in.nextLine(); // skip first line

		biomeClasses = new HashMap<Biomes, UnitClass>();
		
		// read one line of input from the 
		while(in.hasNextLine()) {			
			if (!in.hasNext()) return;
			
			String strBiome 		= in.next();
			String strClass			= in.next();
			
			Biomes biome 			= biomes.get(strBiome);
			UnitClass classType 	= classTypes.get(strClass);
			
			if (biome == null) {
				throw new java.lang.NullPointerException("BIOME NOT FOUND: " + strBiome);
			}
			if (classType == null) 
				throw new java.lang.NullPointerException("Class not found: " + strClass);
			
//			System.out.println(strBiome);
			
			biomeClasses.put(biome, classType);
		}
		in.close();
		
	}

	public static void loadColors() {
		colors = new HashMap<String, Color>();
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_colors.txt").reader()).useDelimiter("\\t\\t*|\r\n[\r\n]*");
		in.nextLine(); // skip first line

		// read one line of input from the 
		while(in.hasNextLine()) {			
			Color color = new Color();
			if (!in.hasNext()) return;
			String name		= in.next();
			int r 	= in.nextInt();
			int g 	= in.nextInt();
			int b	= in.nextInt();
			color.set(r/256f, g/256f, b/256f, 1);
			colors.put(name, color);
		}
		in.close();
	}

	public static void loadArmors() {
		armorTypes = new HashMap<String, ArmorType>();
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_armors.txt").reader()).useDelimiter("\\t\\t*|\r\n[\r\n]*");
		in.nextLine(); // skip first line

		// read one line of input from the 
		while(in.hasNextLine()) {			
			ArmorType armor = new ArmorType();
			if (!in.hasNext()) return;
			armor.name		= in.next();
			String colorToGet = in.next();
			armor.color 	= colors.get(colorToGet);
			if (armor.color == null) throw new java.lang.NullPointerException("Can't find color: " + colorToGet);
			armor.defMod 	= in.nextInt();
			armor.spdMod	= in.nextInt();
			armor.clothes 	= in.nextBoolean();
			armorTypes.put(armor.name, armor);
			//	printArmor(armor);
		}
		in.close();
	}

	public static void loadWeapons() {
		weaponTypes = new HashMap<String, WeaponType>();
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_weapons.txt").reader()).useDelimiter("\\t\\t*|\r\n[\r\n]*");
		in.nextLine(); // skip first line

		// read one line of input from the 
		while(in.hasNextLine()) {			
			WeaponType weapon = new WeaponType();
			if (!in.hasNext()) return;
			weapon.name		= in.next();
			weapon.atkMod 	= in.nextInt();
			weapon.defMod 	= in.nextInt();
			weapon.spdMod	= in.nextInt();
			weapon.oneHand	= in.nextBoolean();
			weapon.blunt 	= in.nextBoolean();
			weapon.polearm 	= in.nextBoolean();
			weaponTypes.put(weapon.name, weapon);
			//			printWeapon(weapon);
		}
		in.close();
	}

	public static void loadRangedWeapons() {
		rangedWeaponTypes = new HashMap<String, RangedWeaponType>();
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_ranged_weapons.txt").reader()).useDelimiter("\\t\\t*|\r\n[\r\n]*");
		in.nextLine(); // skip first line

		// read one line of input from the 
		while(in.hasNextLine()) {			
			RangedWeaponType weapon = new RangedWeaponType();
			if (!in.hasNext()) return;
			weapon.name		= in.next();
			weapon.atkMod 	= in.nextInt();
			weapon.range 	= in.nextInt();
			weapon.accuracy	= in.nextInt();
			weapon.rate 	= in.nextInt();
			weapon.setType(in.next());
			rangedWeaponTypes.put(weapon.name, weapon);
//			printRangedWeapon(weapon);
		}
		in.close();
	}

	public static void loadUnits() {
//		unitTypes = new HashMap<String, NewUnitType>();
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_units.txt").reader()).useDelimiter("\\t\\t*|\\t*\r\n\\t*[\r\n]*\\t*");
		in.nextLine(); // skip first line

		String currentClass = "NO CLASS";
		UnitClass classType = null;

		// read one line of input from the 
		while(in.hasNextLine()) {
			if (!in.hasNext()) return;

			String first = in.next();
			if (first.equals("//")) {
				in.nextLine();
				continue;
			}
			if (first.equals("Class:")) {
				currentClass = in.next();
				classType = new UnitClass();
				classType.name = currentClass;
				classType.units = new HashMap<String, UnitType>();
				classType.color = colors.get(in.next());
				if (classType.color == null) {
					throw new java.lang.NullPointerException();
				}
				
				classTypes.put(currentClass, classType);
				first = in.next();
			}

			UnitType unit = new UnitType();
//			System.out.println(first);
			unit.tier 		= Integer.parseInt(first);
			unit.name		= in.next();
//			System.out.println(unit.name);
			unit.unitClass 	= classType;
			classType.units.put(unit.name, unit);
			String weaponString = in.next();
			String[] weapons = weaponString.split("/");

			// supports only two weapons (1 ranged 1 not) for now.
			for (int i = 0; i < weapons.length; i++) {
				if (weaponTypes.get(weapons[i]) != null) {
					unit.melee = weaponTypes.get(weapons[i]);
					if (unit.melee == null) {
						throw new java.lang.NullPointerException(weapons[i] + " can't be found");
					}
				}
				else if (rangedWeaponTypes.get(weapons[i]) != null) {
					unit.ranged = rangedWeaponTypes.get(weapons[i]);
					if (unit.ranged == null) {
						throw new java.lang.NullPointerException(weapons[i] + " can't be found");
					}
				}					
				else if (weapons[i].equals("Shield")) {
					unit.shield = true;
				}
				else {
					System.out.println("CAN'T FIND WEAPON: " + weapons[i]);
					throw new java.lang.NullPointerException();
				}
			}

			String armorString = in.next();
			unit.armor = armorTypes.get(armorString);
			if (unit.armor == null) {
				throw new java.lang.NullPointerException(armorString + " can't be found");
			}

			String upgradeString = in.next();
			if (!upgradeString.equals("none")) {
				unit.upgradeStrings = upgradeString.split("/");
				// else it is null;
			}
//			else {
//				unit.
//			}
//			unitTypes.put(unit.name, unit);
//			printUnit(unit);
		}

		in.close();
	}
	
	public static void printAllUnits() {
		for (UnitClass classType : classTypes.values()) {
			for (UnitType unit : classType.units.values()) {
				printUnit(unit);
			}
		}
	}

	public static void assignUpgrades() {
		for (UnitClass classType : classTypes.values()) {
			for (UnitType unit : classType.units.values()) {
				if (unit.upgradeStrings == null) {
					unit.upgrades = new UnitType[0]; // maybe make it with size 0
				}
				else {
					unit.upgrades = new UnitType[unit.upgradeStrings.length];
					for (int i = 0; i < unit.upgradeStrings.length; i++) {
						unit.upgrades[i] = classType.units.get(unit.upgradeStrings[i]);
						if (unit.upgrades[i] == null) {
							System.out.println("CAN'T FIND UPGRADE: " + unit.upgradeStrings[i]);
							throw new java.lang.NullPointerException();
						}
//						System.out.println("just added " + unit.upgradeStrings[i]);
					}
					// null it out for memory
					unit.upgradeStrings = null;
				}
			}
		}
	}

	public static void printRangedWeapon(RangedWeaponType weapon) {
		System.out.println(weapon.name);
		System.out.println(weapon.atkMod);
		System.out.println(weapon.range);
		System.out.println(weapon.accuracy);
		System.out.println(weapon.rate);
		System.out.println(weapon.type);
		System.out.println();
	}

	public static void printArmor(ArmorType armor) {
		System.out.println(armor.name);
		System.out.println("(" + armor.color.r + ", " + armor.color.g + ", "+ armor.color.b + ")");
		System.out.println(armor.defMod);
		System.out.println(armor.spdMod);
		System.out.println();
	}

	public static void printWeapon(WeaponType weapon) {
		System.out.println(weapon.name);
		System.out.println(weapon.atkMod);
		System.out.println(weapon.defMod);
		System.out.println(weapon.spdMod);
		System.out.println(weapon.oneHand);
		System.out.println(weapon.blunt);
		System.out.println();
	}

	public static void printUnit(UnitType unit) {
		System.out.println(unit.unitClass.name);
		System.out.println(unit.name);
		System.out.println(unit.tier);
		System.out.println(unit.melee.name);
		if (unit.ranged != null)
			System.out.println(unit.ranged.name);
		System.out.println(unit.armor.name);
		
		if (unit.upgrades != null) {
			for (UnitType upgrade : unit.upgrades) {
				System.out.println(upgrade.unitClass.name + " " + upgrade.name);
			}
		}
		System.out.println();
	}
	
	public static void printAllBiomes() {
		for (Biomes b : Biomes.values()) {
			System.out.println(b.toString() + ": " + biomeClasses.get(b));
		}
	}
}
