package kyle.game.besiege.party;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import kyle.game.besiege.NameGenerator;
import kyle.game.besiege.voronoi.Biomes;

import java.util.HashMap;
import java.util.Scanner;

public class UnitLoader {
//	public static final String PATH = "/Users/kdhillon/Documents/repo/besiege/besiege-game-android/assets/data/units/";
	public static final String PATH = "data/units/";
	public static String rootName;

	public static HashMap<String, Color> colors;
	public static HashMap<String, CultureType> cultureTypes;
	public static HashMap<String, WeaponType> weaponTypes;
	public static HashMap<String, RangedWeaponType> rangedWeaponTypes;
	public static HashMap<String, AmmoType> ammoTypes;
	public static HashMap<String, ArmorType> armorTypes;
    public static HashMap<String, ShieldType> shieldTypes;
    public static HashMap<String, Biomes> biomes;
	public static HashMap<Biomes, CultureType> biomeCultures;

	public static void load(String root) {
		rootName = root;

		cultureTypes = new HashMap<String, CultureType>();

		loadColors();
		loadAmmo();
		loadWeapons();
		loadRangedWeapons();
		loadArmors();
		loadShields();
		loadUnits();
		assignUpgrades();
		initializeBiomes();
	}
	
	public static void initializeBiomes() {
		// load biomes into a hashmap first (may be unnecessary)
		biomes = new HashMap<String, Biomes>();
		for (Biomes b : Biomes.values()) {
			biomes.put(b.toString(), b);
		}

//		Scanner in = null;
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_biomes.txt").reader());

		if (!in.hasNext()) throw new AssertionError();
		in.nextLine();

		biomeCultures = new HashMap<Biomes, CultureType>();
		
		// read one line of input from the 
		while(in.hasNextLine()) {			
			if (!in.hasNext()) return;

			String first = in.next();
			if (first.equals("//")) {
				in.nextLine();
				continue;
			}

			String strBiome 		=  addSpaces(first);
			String strClass			= in.next();
			
			Biomes biome 			= biomes.get(strBiome);
			CultureType classType 	= cultureTypes.get(strClass);
			
			if (biome == null) {
				throw new java.lang.NullPointerException("BIOME NOT FOUND: " + strBiome);
			}
//			if (classType == null)
//				throw new java.lang.NullPointerException("Class not found: " + strClass);
//			System.out.println(strBiome);

//            if (classType == null)
			 biomeCultures.put(biome, classType);
		}
		in.close();	
	}

	public static void loadColors() {
		colors = new HashMap<String, Color>();
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_colors.txt").reader());
		if (!in.hasNext()) throw new AssertionError();
		in.nextLine(); // skip first line

		// read one line of input from the 
		while(in.hasNextLine()) {
			Color color = new Color();
			if (!in.hasNext()) return;
			String first = in.next();
			if (first.equals("//")) {
				in.nextLine();
				continue;
			}
			String name		= first;
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
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_armors.txt").reader());
		if (!in.hasNext()) throw new AssertionError();
		in.nextLine();

		// read one line of input from the 
		while(in.hasNextLine()) {			
			ArmorType armor = new ArmorType();
			if (!in.hasNext()) return;
			String first = in.next();
			if (first.equals("//")) {
				in.nextLine();
				continue;
			}
			armor.name		= addSpaces(first);
			String colorToGet = in.next();
			armor.color 	= colors.get(colorToGet);
			if (armor.color == null) throw new java.lang.NullPointerException("Can't find color: " + colorToGet);
			armor.defMod 	= in.nextInt();
			armor.spdMod	= in.nextInt();
			armor.type 	= ArmorType.toArmorType(in.next());
			armorTypes.put(armor.name, armor);
//				printArmor(armor);
		}
		in.close();
	}

	public static void loadWeapons() {
		weaponTypes = new HashMap<String, WeaponType>();
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_weapons.txt").reader());

		if (!in.hasNext()) throw new AssertionError();
		in.nextLine();

		// read one line of input from the 
		while(in.hasNextLine()) {
			Scanner line = new Scanner(in.nextLine());

			WeaponType weapon = new WeaponType();
			if (!line.hasNext()) continue;
			String first = line.next();
			if (first.equals("//")) {
				continue;
			}

			weapon.name		= addSpaces(first);
			weapon.atkMod 	= line.nextInt();
			weapon.spdMod	= line.nextInt();
			weapon.type	= toWeaponType(line.next());

			if (line.hasNext()) {
				weapon.texture = line.next();
			} else {
				weapon.texture = weapon.getDefaultTexture();
			}

			weaponTypes.put(weapon.name, weapon);
						printWeapon(weapon);
		}
		in.close();
	}

	private static WeaponType.Type toWeaponType(String name) {
		if (name.equals("unarmed")) return WeaponType.Type.UNARMED;
		if (name.equals("1h")) return WeaponType.Type.ONE_HANDED;
		if (name.equals("polearm")) return WeaponType.Type.POLEARM;
		if (name.equals("club")) return WeaponType.Type.CLUB;

		System.out.println("Weapon type not found: " + name);
		return null;
	}
	
	private static String addSpaces(String input) {
		return input.replace("_",  " " );
	}

	public static void loadRangedWeapons() {
		rangedWeaponTypes = new HashMap<String, RangedWeaponType>();
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_ranged_weapons.txt").reader());

		if (!in.hasNext()) throw new AssertionError();
		in.nextLine();		in.nextLine(); // skip first line

		// read one line of input from the 
		while(in.hasNextLine()) {
		    Scanner line = new Scanner(in.nextLine());
			RangedWeaponType weapon = new RangedWeaponType();
            if (!line.hasNext()) continue;

            String first = line.next();
			if (first.equals("//")) {
				continue;
			}

			weapon.name		= addSpaces(first);
			weapon.range 	= line.nextInt();
			weapon.accuracy	= line.nextInt();
			weapon.rate 	= line.nextInt();
			weapon.quiver 	= line.nextInt();
			weapon.setType(line.next());

			if (line.hasNext()) {
			    weapon.texture = line.next();
            } else {
			    weapon.texture = weapon.getDefaultTexture();
            }

			rangedWeaponTypes.put(weapon.name, weapon);
//			printRangedWeapon(weapon);
		}
		in.close();
	}

	private static void loadAmmo() {
		ammoTypes = new HashMap<String, AmmoType>();
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_ammo.txt").reader());

		if (!in.hasNext()) throw new AssertionError();
		in.nextLine();		in.nextLine(); // skip first line

		// read one line of input from the
		while(in.hasNextLine()) {
			AmmoType ammoType = new AmmoType();
			if (!in.hasNext()) return;
			String first = in.next();
			if (first.equals("//")) {
				in.nextLine();
				continue;
			}

			ammoType.name		= addSpaces(first);
			ammoType.dmg  = in.nextInt();
			ammoType.setType(in.next());
			ammoTypes.put(ammoType.name, ammoType);
//			printRangedWeapon(weapon);
		}
		in.close();
	}

    public static void loadShields() {
        shieldTypes = new HashMap<String, ShieldType>();
        Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_shields.txt").reader());

        if (!in.hasNext()) throw new AssertionError();
        in.nextLine();		in.nextLine(); // skip first line

        // read one line of input from the
        while(in.hasNextLine()) {
            ShieldType shieldType = new ShieldType();
            if (!in.hasNext()) return;
            String first = in.next();
            if (first.equals("//")) {
                in.nextLine();
                continue;
            }

            shieldType.name		= addSpaces(first) + " Shield";
            shieldType.color  = colors.get(in.next());
            shieldType.hp       = in.nextInt();
            shieldType.spdMod   = in.nextInt();
            shieldType.setType(in.next());
            shieldTypes.put(shieldType.name, shieldType);
//			printRangedWeapon(weapon);
        }
        in.close();
    }

	private static void loadUnits() {
//		unitTypes = new HashMap<String, NewUnitType>();
		Scanner in = new Scanner(Gdx.files.internal(PATH + rootName + "_units.txt").reader());

		if (!in.hasNext()) throw new AssertionError();
		in.nextLine();

		String currentClass = "NO CLASS";
		CultureType culture = null;

        NameGenerator gen = null;
//        if (Kingdom.DEBUG_MODE)
//            gen = new NameGenerator("mexica.txt");

		// read one line of input from the 
		while(in.hasNextLine()) {
            String first = in.next();
            if (first.equals("//")) {
                in.nextLine();
                continue;
            }

            if (first.equals("Class:")) {
                currentClass = in.next();
                culture = new CultureType();
                culture.name = currentClass;
                culture.units = new HashMap<String, UnitType>();
                culture.colorLite = colors.get(in.next());
                culture.colorDark = colors.get(in.next());

                if (gen != null) {
                    culture.nameGenerator = gen;
                    in.next();
                } else {
                    culture.nameGenerator = new NameGenerator(in.next());
                }

//				System.out.println("Color: " + culture.name);
                if (culture.colorLite == null || culture.colorDark == null) {
                    throw new java.lang.NullPointerException();
                }

                cultureTypes.put(currentClass, culture);

                continue;
            }
//			System.out.println(first);

            UnitType unit = new UnitType();

            // This is a shaman!
			boolean shaman = false;
            if (first.equals("S")) {
				unit.tier = 0;
				shaman = true;
			} else {
				unit.tier = Integer.parseInt(first);
			}
            // remove numbers from name
            unit.name = addSpaces(in.next());
//			System.out.println(unit.name);
            unit.cultureType = culture;
            culture.units.put(unit.name + unit.tier, unit);

            String weaponString = addSpaces(in.next());
            String[] weapons = weaponString.split("/");

            // supports only two weapons (1 ranged 1 not) for now.
            for (int i = 0; i < weapons.length; i++) {
                if (weaponTypes.get(weapons[i]) != null && unit.melee != weaponTypes.get(weapons[i])) {
                    unit.melee = weaponTypes.get(weapons[i]);
                    if (unit.melee == null) {
                        throw new java.lang.NullPointerException(weapons[i] + " can't be found");
                    }
                } else if (rangedWeaponTypes.get(weapons[i]) != null) {
                    unit.ranged = rangedWeaponTypes.get(weapons[i]);

                    // If thrown, default to same ammo as weapon name

                    i++;
                    if (i < weapons.length)
                        unit.ammoType = ammoTypes.get(weapons[i]);
                    else {
                        if (unit.ranged.type == RangedWeaponType.Type.THROWN ||
                                unit.ranged.type == RangedWeaponType.Type.THROWN_AXE ||
                                unit.ranged.type == RangedWeaponType.Type.THROWN_FIRE) {
                            unit.ammoType = ammoTypes.get(unit.ranged.name);
                        } else if (unit.ranged.type == RangedWeaponType.Type.SLING) {
                            unit.ammoType = ammoTypes.get("Stone");
                        } else if (unit.ranged.type == RangedWeaponType.Type.ATLATL) {
                            unit.ammoType = ammoTypes.get("Dart");
                        } else if (unit.ranged.type == RangedWeaponType.Type.BOW) {
                            unit.ammoType = ammoTypes.get("Arrow");
                        } else {
                            throw new AssertionError("can't find ammo type for: " + unit.ranged.name);
                        }
                    }

                    if (unit.ranged == null) {
                        throw new java.lang.NullPointerException(weapons[i] + " can't be found");
                    }
                } else {
                    System.out.println("CAN'T FIND WEAPON: " + weapons[i]);
                    throw new java.lang.NullPointerException();
                }
            }

            String armorShieldString = addSpaces(in.next());
            String[] armorShield = armorShieldString.split("/");

            String armorString = armorShield[0];
            unit.armor = armorTypes.get(addSpaces(armorString));
            if (unit.armor == null) {
            }

            if (armorShield.length == 2) {
                unit.shieldType = shieldTypes.get(addSpaces(armorShield[1]) + " Shield");
            }

            String upgradeString = addSpaces(in.next());
            if (!upgradeString.equals("none")) {
                unit.upgradeStrings = upgradeString.split("/");
            }

            if (!shaman) {
            	String typeString = in.next();
            	if (typeString.equals("HVY_INF")) unit.unitClass = UnitType.UnitClass.HEAVY_INF;
				else if (typeString.equals("LGT_INF")) unit.unitClass = UnitType.UnitClass.LIGHT_INF;
				else if (typeString.equals("RNG_INF")) unit.unitClass = UnitType.UnitClass.RANGED_INF;
				else if (typeString.equals("RNG")) unit.unitClass = UnitType.UnitClass.RANGED;
				else if (typeString.equals("STEALTH")) unit.unitClass = UnitType.UnitClass.STEALTH;
				else throw new AssertionError("no class found: " + typeString);
			}
        }
		in.close();
	}

	public static void printAllUnits() {
		for (CultureType classType : cultureTypes.values()) {
			for (UnitType unit : classType.units.values()) {
				printUnit(unit);
			}
		}
	}

	public static void assignUpgrades() {
		for (CultureType classType : cultureTypes.values()) {
			for (UnitType unit : classType.units.values()) {
				if (unit.upgradeStrings == null) {
					unit.upgrades = new UnitType[0]; // maybe make it with size 0
				}
				else {
					unit.upgrades = new UnitType[unit.upgradeStrings.length];
					for (int i = 0; i < unit.upgradeStrings.length; i++) {
						// This handles distinguishing between units with same display name but different "tier"

						unit.upgrades[i] = classType.units.get(unit.upgradeStrings[i] + (unit.tier + 1));

						if (unit.upgrades[i] == null) {
							// Try again, 2 levels up.
							unit.upgrades[i] = classType.units.get(unit.upgradeStrings[i] + (unit.tier + 2));
						}

						if (unit.upgrades[i] == null) {
							System.out.println("CAN'T FIND UPGRADE: " + unit.upgradeStrings[i]);
							throw new java.lang.NullPointerException();
						}
						
						
//						String[] split = unit.upgradeStrings[i].split(":");
//						if (split.length == 1) {
//							unit.upgrades[i] = classType.units.get(split[0]);
//							if (unit.upgrades[i] == null) {
//								System.out.println("CAN'T FIND UPGRADE: " + unit.upgradeStrings[i]);
//								throw new java.lang.NullPointerException();
//							}
//						} else if (split.length == 2) {
//							unit.upgrades[i] = cultureTypes.get(split[1]).units.get(split[0]);
//							if (unit.upgrades[i] == null) {
//								System.out.println("CAN'T FIND UPGRADE: " + unit.upgradeStrings[i]);
//								throw new java.lang.NullPointerException();
//							}
//						}
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
		System.out.println(weapon.spdMod);
		System.out.println(weapon.type);
		System.out.println();
	}

	public static void printUnit(UnitType unit) {
		System.out.println(unit.cultureType.name);
		System.out.println(unit.name);
		System.out.println(unit.tier);
		System.out.println(unit.melee.name);
		if (unit.ranged != null)
			System.out.println(unit.ranged.name);
		System.out.println(unit.armor.name);
		
		if (unit.upgrades != null) {
			for (UnitType upgrade : unit.upgrades) {
				System.out.println(upgrade.cultureType.name + " " + upgrade.name);
			}
		}
		System.out.println();
	}
	
	public static void printAllBiomes() {
		for (Biomes b : Biomes.values()) {
			System.out.println(b.toString() + ": " + biomeCultures.get(b));
		}
	}

	// Test UnitLoader
	public static void main(String[] args) {
		UnitLoader.load("chieftain");
		UnitLoader.printAllUnits();
	}
}
