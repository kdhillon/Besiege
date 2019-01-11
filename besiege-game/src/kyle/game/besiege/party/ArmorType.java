package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.Color;

/* contains information about a specific weapon type */
public class ArmorType {
	public enum Type {
		NAKED, 		// No armor
		CLOTHES, 	// Can be produced for free (won't be won/lost in battles)
		BASIC, 		// Covers full top/bottom except hands
		FULL, 		// Covers full top/bottom including hands
		CHEST,		// Covers top minus sleeves, plus bottom
		LOINCLOTH,	// Glorified Speedo
		ARMWRAP,    // Wrap only on hands/loincloth
		HOODED,		// Full + hood
	}
	public String name;
	
	public Color color;
	public int defMod;
	public int spdMod;
	public Type type;
	public boolean clothes;

	// TODO replace with a getter for the appropriate textureregion
	public boolean isNaked() {
		return type == Type.NAKED;
	}

	public boolean isClothes() {
		return type == Type.CLOTHES;
	}

	public boolean isChest() {
	    return type == Type.CHEST;
    }

    public Color getColorTopDown() {
		if (isNaked()) return Color.CLEAR;
		if (type == Type.LOINCLOTH) return Color.CLEAR;
		return color;
	}

	public Color getColorPreview() {
		if (isNaked()) return Color.CLEAR;
		return color;
	}

    public String getWalkAnimation() {
	    if (type == Type.CHEST) return "walk-chest-armor";
//		if (type == Type.HOODED) return "walk-armor-hood";
		return "walk-armor";
    }

    public String getFiringAnimation() {
        if (type == Type.CHEST) return "firing-chest-armor";
//		if (type == Type.HOODED) return "firing-armor-hood";
		return "firing-armor";
    }

    public String getFirearmAnimation() {
        if (type == Type.CHEST) return "firearm-chest-armor";
//		if (type == Type.HOODED) return "firearm-armor-hood";
		return "firearm-armor";
    }

    public String getThrownAnimation() {
	    if (type == Type.CHEST) return "thrown-chest-armor";
//		if (type == Type.HOODED) return "thrown-armor-hood";
		return "thrown-armor";
    }

    public String getDyingAnimation() {
	    if (type == Type.CHEST) return "die1-chest-armor";
		if (type == Type.HOODED) return "die1-armor-hood";
		return "die1-armor";
    }

    public String getAtlatlArmor() {
	    if (type == Type.CHEST) return "atlatl-chest-armor";
//		if (type == Type.HOODED) return "atlatl-armor-hood";
	    return "atlatl-armor";
    }

    public String getPreviewTexture() {
		if (type == Type.FULL) return "preview-armor-2";
		if (type == Type.CHEST) return "preview-chest-armor-2";
		if (type == Type.ARMWRAP) return "preview-armwrap-armor";
		if (type == Type.LOINCLOTH) return "preview-loincloth-armor";
		if (type == Type.HOODED) return "preview-hood-armor";
		// basic, clothes
		return "preview-basic-armor";
    }

	public static ArmorType.Type toArmorType(String name) {
		if (name.equals("naked")) return ArmorType.Type.NAKED;
		if (name.equals("chest")) return ArmorType.Type.CHEST;
		if (name.equals("basic")) return ArmorType.Type.BASIC;
		if (name.equals("clothes")) return ArmorType.Type.CLOTHES;
		if (name.equals("loincloth")) return Type.LOINCLOTH;
		if (name.equals("armwrap")) return Type.ARMWRAP;
		if (name.equals("hooded")) return Type.HOODED;
		if (name.equals("full")) return Type.FULL;

		System.out.println("Armor type not found: " + name);
		return null;
	}
}