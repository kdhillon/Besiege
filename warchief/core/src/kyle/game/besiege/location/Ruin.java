/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 * 
 * Castle.java
 ******************************************************************************/

package kyle.game.besiege.location;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;

import com.badlogic.gdx.graphics.g2d.Batch;
import kyle.game.besiege.voronoi.Biomes;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

public class Ruin extends Location {
	private final static float SCALE = 10;
	
	public Ruin(){}
	
	public Ruin(Kingdom kingdom, String name, int index, float posX, float posY, Center center, Corner corner) {
		super(kingdom, name, index, posX, posY, center, corner);
		this.type = LocationType.RUIN;
	
		this.population = 0;
		this.DAILY_WEALTH_INCREASE_BASE = 0;
//			
        if (cultureType.name.equals("Plains")) {
            setTextureRegion("tipiruin");
        } else if (cultureType.name.equals("Forest")) {
            setTextureRegion("longhouseruin");
        }
        else if (cultureType.name.equals("Tundra")) {
            if (center != null && (center.biome == Biomes.SNOW))
                setTextureRegion("inuitcityruinwhite");
            else setTextureRegion("inuitcityruin");
        }
        else if (cultureType.name.equals("Desert")){
            setTextureRegion("templeruin");
        } else if (cultureType.name.equals("Jungle")){
			setTextureRegion("templeruin");
		}
        else {
            setTextureRegion("Castle");
        }

        setScale(SCALE);
		initializeBox();
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
//		Assets.pixel18.setColor(Kingdom.factionColors.get(getFaction()));
//		String toDraw = getName() + " (" + getParty().wealth + ")";
//		Assets.pixel18.draw(batch, toDraw, getX() - (int) (3*toDraw.length()), getY()-15);
//		Assets.pixel18.setColor(Color.WHITE);
	}
	
	@Override 
	public void changeFaction(Faction faction) {
		return;
	}
	
	@Override
	public Faction getFaction() {
		return null;
	}
}
