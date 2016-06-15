/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import com.badlogic.gdx.scenes.scene2d.Actor;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;

public class Siege extends Actor {
	private final float MAINTAIN = 10.0f; // siege will exist this long without any army present
	private final float CHECK_FREQ = 5; // every this seconds will check if should attack;
	public final static float MIN_BALANCE_TO_ATTACK = 0.2f;
	public final static float MAX_BALANCE_TO_BREAK = 0.1f;
	public Location location;
	public StrictArray<Army> armies;
	public float duration;
	public boolean inBattle;
	private boolean empty; // no armies present;
	private float countdown; // if no armies
	private Battle battle;
	public Faction besieging; // faction besieging this city
	
	private boolean hasChecked;
	
	public Siege(){}
	
	public Siege(Location location, Faction besieging) {
//		System.out.println("creating new siege at " + location.getName());
		this.location = location;
		armies = new StrictArray<Army>();
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
			if (countdown <= 0) {
				this.destroy();
				return;
			}
		}
		
		// remove armies that are at peace
		for (int i = 0; i < armies.size; i++) {
			if (armies.get(i) != null && armies.get(i).getFaction().atPeace(location.getFaction())) {
				armies.get(i).leaveSiege();
			}
		}
		
		if (armies.size == 0) {
			this.destroy();
			return;
		}

//		// If no longer at war, end siege
//		if (besieging.atPeace(location.getFaction())) {
//			this.destroy();
//			return;
//		}
		
		if (location.playerBesieging && location.getKingdom().getPlayer().hasTarget()) {
			remove(location.getKingdom().getPlayer());
//			System.out.println(location.playerBesieging + " " + !location.getKingdom().getPlayer().isStopped());
		}
		this.duration += delta;
		
		if (location.getKingdom().getTotalHour() % CHECK_FREQ == 0 && armies.size > 0) {
			if (!hasChecked) {
				attackOrDestroy();
				hasChecked = true;
			}
		}
		else if (hasChecked) hasChecked = false;
	}
	// TODO change back to have some chance of maintaining
	public void attackOrDestroy() {
		//judges whether or not to attack the city
		// calculate probability of victory, add a randomness factor, then attack
		double balance = Battle.calcBalance(armies, 1f, location.getGarrisonedAndGarrison(), location.getDefenseFactor());
		if (balance >= MIN_BALANCE_TO_ATTACK && !inBattle) attack();
		else if (balance <= MAX_BALANCE_TO_BREAK && !inBattle) destroy(); // end siege if no chance
		else {
			// previously maintained siege, now force attack:
			attack();
		}
	}
	
	public void attack() {
//		System.out.println("attack at " + location.getName());
		location.siegeAttack(armies);
		// make sure siege is set
		location.siege = this;
//		System.out.println("SETTING SIEGE");
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
//		System.out.println("ending attack at " + location.getName());
		inBattle = false;
	}
	
	public void siegeSuccess() {
		location.changeFaction(besieging);
		if (location.playerBesieging) {
			location.getKingdom().getPlayer().garrisonIn(location);
			// this doesn't work
			location.getKingdom().getMapScreen().getSidePanel().setActiveLocation(location);
			location.playerBesieging = false;
		}
		if (location.playerWaiting) {
			location.getKingdom().getPlayer().garrisonIn(null);
		}
		location.siege = null;
		battle = null;
	}
	
	// siege attack failed
	public void siegeFailure() {
		battle = null;
		this.destroy();
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
//			location.playerBesieging = false;
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
//		System.out.println("destroying siege of " + location.getName());
		for (Army a : armies) {
			a.leaveSiege();
		}
		if (battle != null) {
			battle.victory(battle.dArmies, battle.aArmies);
			battle = null;
		}
		location.endSiege();
		besieging.cancelSiegeOf(location);
		this.remove();
	}

}
