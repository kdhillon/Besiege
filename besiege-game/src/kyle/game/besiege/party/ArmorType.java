package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.Color;

/* contains information about a specific weapon type */
public class ArmorType {
	public enum Type {
		NAKED, 		// No armor
		CLOTHES, 	// Can be produced for free (won't be won/lost in battles)
		BASIC,
		CHEST}
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
}
