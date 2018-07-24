/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class SoldierLabel extends Label {
	public Soldier soldier;
	
	public SoldierLabel(CharSequence text, LabelStyle style, Soldier soldier) {
		super(text, style);
		this.soldier = soldier;
	}
}
