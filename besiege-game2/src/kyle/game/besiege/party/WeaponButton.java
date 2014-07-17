/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class WeaponButton extends Button {
	public Weapon weapon;
	
	public WeaponButton(Actor actor, ButtonStyle style, Weapon weapon) {
		super(actor, style);
		this.weapon = weapon;
	}
}
