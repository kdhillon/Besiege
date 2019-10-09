package kyle.game.besiege.party;

/* contains information about a specific weapon type */
public class RangedWeaponType {
	public enum Type {
		BOW, SLING, ATLATL, BLOWGUN, THROWN, THROWN_AXE, THROWN_FIRE, FIREARM, CROSSBOW
	};
	
	public String name;
	
	public int range;
	public int accuracy;
	public int rate;
	public int quiver;
		 
	public Type type;
	public String texture;

	public String getStatsSummary() {
		return name + "\n " + range + " Range\n " + accuracy + " Accuracy\n " + rate + " " +
				"Fire Rate";
	}

	public void setType(String typeString) {
		if (typeString.equals("bow"))
			type = Type.BOW;
		if (typeString.equals("sling"))
			type = Type.SLING;

		if (typeString.equals("atlatl"))
			type = Type.ATLATL;
		if (typeString.equals("crossbow"))
			type = Type.CROSSBOW;
		if (typeString.equals("thrown"))
			type = Type.THROWN;
		if (typeString.equals("thrown_axe"))
			type = Type.THROWN_AXE;
        if (typeString.equals("thrown_fire"))
            type = Type.THROWN_FIRE;
		if (typeString.equals("firearm"))
			type = Type.FIREARM;
		if (typeString.equals("blowgun"))
			type = Type.BLOWGUN;

		if (type == null) throw new AssertionError("Type not found: " + typeString);
	}

	public boolean shouldDrawAmmo() {
		switch (type) {
			case THROWN:
			case THROWN_AXE:
			case THROWN_FIRE:
			case ATLATL:
				return true;
			default:
				return false;
		}
	}

	public boolean drawAmmoReversed() {
		switch (type) {
			case THROWN_AXE:
				return true;
			default:
				return false;
		}
	}

	public String getDefaultTexture() {
		switch (type) {
			case BOW:
				return "strongbow";
			case SLING:
				return "sling";
			case THROWN:
				return "spear";
			case THROWN_AXE:
				return "tomahawk";
			case BLOWGUN:
				return "blowgun";
			case ATLATL:
				return "atlatl";
			case FIREARM:
				return "firearm";
			case CROSSBOW:
				return "crossbow";
			default:
				throw new AssertionError(type + " not found");
		}
	}
}
