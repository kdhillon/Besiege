package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.Color;

/* contains information about a specific weapon type */
public class ArmorType {
	public enum Type {
		NAKED, 		// No armor
		CLOTHES, 	// Can be produced for free (won't be won/lost in battles)
		BASIC, 		// Covers full top/bottom
		CHEST,		// Covers top minus sleeves, plus bottom
		LOINCLOTH,		// Glorified Speedo
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

    public String getWalkAnimation() {
	    if (type == Type.CHEST) return "walk-chest-armor";
	    return "walk-armor";
    }

    public String getFiringAnimation() {
        if (type == Type.CHEST) return "firing-chest-armor";
        return "firing-armor";
    }

    public String getFirearmAnimation() {
        if (type == Type.CHEST) return "firearm-chest-armor";
        return "firearm-armor";
    }

    public String getThrownAnimation() {
	    if (type == Type.CHEST) return "thrown-chest-armor";
	    return "thrown-armor";
    }

    public String getDyingAnimation() {
	    if (type == Type.CHEST) return "die1-chest-armor";
	    return "die1-armor";
    }

    public String getAtlatlArmor() {
	    if (type == Type.CHEST) return "atlatl-chest-armor";
	    return "atlatl-armor";
    }

    public String getPreviewTexture() {
        if (type == Type.CHEST) return "preview-chest-armor";
        return "preview-armor";
    }

	public static ArmorType.Type toArmorType(String name) {
		if (name.equals("naked")) return ArmorType.Type.NAKED;
		if (name.equals("chest")) return ArmorType.Type.CHEST;
		if (name.equals("basic")) return ArmorType.Type.BASIC;
		if (name.equals("clothes")) return ArmorType.Type.CLOTHES;
		if (name.equals("loincloth")) return Type.LOINCLOTH;

		System.out.println("Armor type not found: " + name);
		return null;
	}
}