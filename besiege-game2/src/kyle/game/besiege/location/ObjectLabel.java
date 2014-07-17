/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.location;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class ObjectLabel extends Label {
	public Object object;
	
	public ObjectLabel(String text, LabelStyle ls, Object object) {
		super(text, ls);
		this.object = object;
	}
}
