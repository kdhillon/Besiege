package kyle.game.besiege.party;

/* contains information about a specific weapon type */
public class WeaponType {
	public enum Type {
		UNARMED,
		ONE_HANDED,
		CLUB,
		POLEARM
	}
	public String name;
	
	public int atkMod;
	public int spdMod;
	public Type type;
	public String texture;

	public String getDefaultTexture() {
		// TODO

		throw new AssertionError();
	}

	public boolean isPolearm() {
		return type == Type.POLEARM;
	}
}
