/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MiniMap extends Actor {
//	private final float BORDER = .04f;
	private final float BORDER = .1f;

	private SidePanel panel;
	
	public MiniMap(SidePanel panel) {
		this.panel = panel;
		
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
//		this.setWidth(panel.getWidth()*(1-BORDER));
		this.setHeight(panel.getWidth()*(1-BORDER));
		this.setWidth(getHeight()*2/3);
		this.setPosition(panel.getWidth()*(BORDER/2) + getWidth()/4, (panel.getHeight()-this.getHeight())-panel.getWidth()*(BORDER/2));
		
		//this.setOrigin(panel.getOriginX() - getX(), panel.getOriginY() - getY()); // Buggy
		//this.setRotation(panel.getRotation());
//		batch.draw(Assets.map.findRegion("smallMap"), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
		if (panel.getActiveCrest() != null)
			batch.draw(panel.getActiveCrest(), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
	}
}
