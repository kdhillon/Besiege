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
//	private String nameFormatted;

	public int atkMod;
	public int spdMod;
	public Type type;
	public String texture;

//	public String getName() {
//
//	}

//	public String setName(String name) {
//		this.name = name;
////		this.setNameFormatted(name);
//	}

//	public String getNameFormatted() {
//		return nameFormatted;
//	}
//
//	private void setNameFormatted(String name) {
//		String[] split = name.split("\\(");
//		if (split.length == 1) {
//			 this.nameFormatted = name;
//			 return;
//		}
//		this.nameFormatted = split[0] + " (" + split[1];
//	}

	public String getDefaultTexture() {
		throw new AssertionError();
	}

	public String getStatsSummary() {
		return name +"\n Type: " + type.string + "\n " + UnitType.formatStat(atkMod) + " Attack\n " + UnitType.formatStat(spdMod) + " Speed";
	}

	public boolean isPolearm() {
		return type == Type.POLEARM;
	}
}
