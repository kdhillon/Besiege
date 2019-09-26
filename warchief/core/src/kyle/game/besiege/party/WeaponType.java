package kyle.game.besiege.party;

/* contains information about a specific weapon type */
public class WeaponType {
	public enum Type {
		UNARMED("Unarmed"),
		ONE_HANDED("One-Handed"),
		CLUB("Blunt"),
		POLEARM("Polearm");

		public String string;
		Type(String string) {
			this.string = string;
		}
	}
	public String name;
	
	public int atkMod;
	public int spdMod;
	public Type type;
	public String texture;

	public String getDefaultTexture() {
		throw new AssertionError();
	}

	public String getStatsSummary() {
		return name +"\n " + type.string + "\n " + UnitType.formatStat(atkMod) + " Attack\n " + UnitType.formatStat(spdMod) + " Speed\n";
	}

	public boolean isPolearm() {
		return type == Type.POLEARM;
	}
}
