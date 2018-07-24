/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kyle.game.besiege.voronoi;

/**
 * Biomes.java Function Date Jul 31, 2013
 *
 * @author Connor
 */
public enum Biomes {

    OCEAN, LAKE, BEACH, SNOW, TUNDRA, MOUNTAINS, SCORCHED, TAIGA, SHRUBLAND, PLATEAU,
    SWAMP, TEMPERATE_DECIDUOUS_FOREST, GRASSLAND, SUBTROPICAL_DESERT,
    ICE, MARSH, TROPICAL_RAIN_FOREST, TROPICAL_SEASONAL_FOREST,
    LAKESHORE;
    
    @Override
    public String toString() {
    	switch(this) {
    	case OCEAN: return "Ocean";
    	case LAKE: return "Lake";
    	case BEACH: return "Beach";
    	case SNOW: return "Snow";
    	case TUNDRA: return "Tundra";
    	case MOUNTAINS: return "Mountains";
    	case SCORCHED: return "Scorched";
    	case TAIGA: return "Taiga";
    	case SHRUBLAND: return "Wasteland";
    	case PLATEAU: return "Plateau";
    	case SWAMP: return "Swamp";
    	case TEMPERATE_DECIDUOUS_FOREST: return "Deciduous Forest";
    	case GRASSLAND: return "Plains";
    	case SUBTROPICAL_DESERT: return "Desert";
    	case ICE:  return "Ice";
    	case MARSH: return "Marsh";
    	case TROPICAL_RAIN_FOREST: return "Rainforest";
    	case TROPICAL_SEASONAL_FOREST: return "Seasonal Forest";
    	case LAKESHORE: return "Lakeshore";
    	default: return "BIOME NOT RECOGNIZED";
    	}
    }
    
    public float getWealthFactor() {
    	switch(this) {
    	case GRASSLAND: return 1.0f;
    	case LAKESHORE: return 1.0f;
    	case TEMPERATE_DECIDUOUS_FOREST: return 1.0f;
    	case TROPICAL_SEASONAL_FOREST: return 1.0f;
    	case SHRUBLAND: return 1f;
    	case BEACH: return 0.9f;
    	case TROPICAL_RAIN_FOREST: return 0.9f;
    	case SWAMP: return 0.8f;
    	case PLATEAU: return 0.7f;
    	case TAIGA: return 0.7f;
    	case TUNDRA: return 0.6f;    	
    	case SUBTROPICAL_DESERT: return 0.6f;
    	case SNOW: return 0.5f;
    	case MOUNTAINS: return 0.4f;
    	case SCORCHED: return 0.2f;
    	case ICE:  return 0;
    	case MARSH: return 0;
    	case OCEAN: return 0;
    	case LAKE: return 0;
    	default: return 0;
    	}
    }
}
