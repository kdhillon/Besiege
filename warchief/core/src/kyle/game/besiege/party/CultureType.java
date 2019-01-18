package kyle.game.besiege.party;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import kyle.game.besiege.NameGenerator;

/* contains information about a specific unit type */
public class CultureType {
	public String name;
	public HashMap<String, UnitType> units;

	public Color colorLite;
	public Color colorDark;
	/* first read in strings for each */

	public NameGenerator nameGenerator;

	// TODO add things like specific textures for their general, base biomes, etc.
}

