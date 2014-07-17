/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class Siege extends Actor {
	private final float MAINTAIN = 10.0f; // siege will exist this long without any army present
	private final float CHECK_FREQ = 5; // every this seconds will check if should attack;
	public Location location;
	public Array<Army> armies;
	public float duration;
	public boolean inBattle;
	private boolean empty; // no armies present;
	private float countdown; // if no armies
	private Battle battle;
	public Faction besieging; // faction besieging this city
	
	private boolean hasChecked;
	
	public Siege(Location location, Faction besieging) {
		System.out.println("creating new siege at " + location.getName());
		this.location = location;
		armies = new Array<Army>();
		this.besieging = besieging; 
		this.duration = 0;
	}
	
	public void act(float delta) {
		if (armies.size == 0 && !empty) {
			empty = true;
			countdown = MAINTAIN;
		}
		else if (armies.size >= 1 && empty)
			empty = false;
		else if (empty) {
			countdown -= delta;
			if (countdown <= 0) this.destroy();
		}
		
		if (location.playerBesieging && location.getKingdom().getPlayer().hasTarget()) {
			remove(location.getKingdom().getPlayer());
//			System.out.println(location.playerBesieging + " " + !location.getKingdom().getPlayer().isStopped());
		}
		this.duration += delta;
		
		if (Kingdom.getTotalHour() % CHECK_FREQ == 0) {
			if (!hasChecked) {
				shouldAttack();
				hasChecked = true;
			}
		}
		else if (hasChecked) hasChecked = false;
	}
	public void shouldAttack() {
		//judges whether or not to attack the city
		// calculate probability of victory, add a randomness factor, then attack
		double balance = Battle.calcBalance(armies, 1f, location.getGarrisonedAndGarrison(), location.getDefenseFactor());
		if (balance >= .4 && !inBattle) attack();
		else if (balance <= .1 && !inBattle) destroy(); // end siege if no chance
	}
	
	public void attack() {
		System.out.println("attack at " + location.getName());
		location.siegeAttack(armies, location);
		// make sure siege is set
		location.siege = this;
		System.out.println("SETTING SIEGE");
//		for (Army a : armies) {
//			// make sure siege is set
//			a.setSiege(this);
//		}f
		inBattle = true;
//		if (armies.size >= 1) 
		this.battle = armies.first().getBattle();
//		else System.out.println("trying to attack with no armies!");
		this.battle.siegeOf = location;
		if (this.battle.siegeOf.siege != this) System.out.println("THIS IS REALLY FUCKED");
	}
	public void endAttack() {
		System.out.println("ending attack at " + location.getName());
		inBattle = false;
	}
	
	public void add(Army army) {
		if (armies.contains(army, true)) {
			System.out.println("adding twice");
			return;
		}
		if (army.getParty().player) location.playerBesieging = true; 
		army.setSiege(this);
		armies.add(army);
	}
	
	public void remove(Army army) {
		if (army.getParty().player) {
			location.playerBesieging = false;
			System.out.println("removed player");
			if (location.playerWaiting) location.stopWait();
		}
		if (armies.contains(army, true)) {
			armies.removeValue(army, true);
		}
		else BottomPanel.log("can't remove army from siege: doesn't exist", "red");
		
		army.setSiege(null);
	}
	
	public void destroy() {
		System.out.println("destroying siege");
		for (Army a : armies) {
			a.leaveSiege();
		}
		location.endSiege();
		this.remove();
	}

}
