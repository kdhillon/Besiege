/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class UpgradeButton extends Button {
	public UnitType upgrade;
	
	public UpgradeButton(Actor actor, ButtonStyle style, UnitType oldWeapon) {
		super(actor, style);
		this.upgrade = oldWeapon;
	}
}
