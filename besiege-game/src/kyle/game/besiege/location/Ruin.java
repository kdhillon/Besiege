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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Ruin extends Location {
	private final static float SCALE = 10;
	
	public Ruin(){}
	
	public Ruin(Kingdom kingdom, String name, int index, float posX, float posY) {
		super(kingdom, name, index, posX, posY);
		this.type = LocationType.RUIN;
	
		this.population = 0;
		this.DAILY_WEALTH_INCREASE_BASE = 0;
//			
		setTextureRegion("Castle");
		setScale(SCALE);
		initializeBox();
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
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
