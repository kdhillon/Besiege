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

    OCEAN, LAKE, BEACH, SNOW, TUNDRA, BARE, SCORCHED, TAIGA, SHURBLAND, TEMPERATE_DESERT,
    TEMPERATE_RAIN_FOREST, TEMPERATE_DECIDUOUS_FOREST, GRASSLAND, SUBTROPICAL_DESERT,
    SHRUBLAND, ICE, MARSH, TROPICAL_RAIN_FOREST, TROPICAL_SEASONAL_FOREST, COAST,
    LAKESHORE;
    
    @Override
    public String toString() {
    	switch(this) {
    	case OCEAN: return "Ocean";
    	case LAKE: return "Lake";
    	case BEACH: return "Beach";
    	case SNOW: return "Snow";
    	case TUNDRA: return "Tundra";
    	case BARE: return "Bare";
    	case SCORCHED: return "Scorched";
    	case TAIGA: return "Taiga";
    	case SHURBLAND: return "Shrubland";
    	case TEMPERATE_DESERT: return "Temperate Desert";
    	case TEMPERATE_RAIN_FOREST: return "Coniferous Forest";
    	case TEMPERATE_DECIDUOUS_FOREST: return "Deciduous Forest";
    	case GRASSLAND: return "Grassland";
    	case SUBTROPICAL_DESERT: return "Desert";
    	case SHRUBLAND: return "Shurbland";
    	case ICE:  return "Ice";
    	case MARSH: return "Marsh";
    	case TROPICAL_RAIN_FOREST: return "Tropical Rainforest";
    	case TROPICAL_SEASONAL_FOREST: return "Seasonal Forest";
    	case COAST: return "Coast";
    	case LAKESHORE: return "Lakeshore";
    	default: return "BIOME NOT RECOGNIZED";
    	}
    }
}
