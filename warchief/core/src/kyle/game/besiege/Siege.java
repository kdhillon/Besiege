/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import com.badlogic.gdx.scenes.scene2d.Actor;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.battle.BattleActor;
import kyle.game.besiege.battle.BattleSim;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;

/**
 * Represents a prolonged siege of a location.
 * Need to differentiate between a siege and a raid.
 *
 * Maybe get rid of concept of a siege entirely? There was only one example of
 * a siege in the native history I found.
 * A siege is surrounding a city in preparation to attack it.
 * Reasons for a siege in M&B is to starve out the enemy. Enemies inside can't leave.
 * You can wait for a friendly army to come support you while you hold the fort.
 *
 * To make things simple, let's have sieges behave the same for villages and for cities.
 * You can besiege a village. We can just have a shorter duration of the siege.
 *
 */
public class Siege extends Actor {
	private final float MAINTAIN = 10.0f; // siegeOrRaid will exist this many seconds without any army present
	private final float MAINTAIN_VILLAGE = 10.0f; // siegeOrRaid will exist this many seconds without any army present
	private final float CHECK_FREQ = 5; // every this seconds will check if should attack;
	public final static float MIN_BALANCE_TO_ATTACK = 0.2f;
	public final static float MAX_BALANCE_TO_BREAK = 0.1f;
	public Location location;
	public StrictArray<Army> armies;
	public float duration;
	private boolean empty; // no armies present;
	private float countdown; // if no armies
	public BattleActor battleActor;
	public Faction besieging; // faction besieging this city
	
	private boolean hasChecked;
	
	public Siege(){}
	
	public Siege(Location location, Faction besieging) {
//		System.out.println("creating new siegeOrRaid at " + location.getName());
		this.location = location;
		armies = new StrictArray<Army>();
		this.besieging = besieging; 
		this.duration = 0;
	}
	
	public void act(float delta) {
		if (armies.size == 0 && !empty) {
			empty = true;

			if (location.isVillage()) {
				countdown = MAINTAIN_VILLAGE;
			} else {
				countdown = MAINTAIN;
			}
//			BottomPanel.log("Breaking siege at: " + location.getName());
		}
		else if (armies.size >= 1 && empty)
			empty = false;
		else if (empty) {
			countdown -= delta;
			System.out.println("Siege is being broken at " + location.getName());
			if (countdown <= 0) {
				this.destroy();
				return;
			}
		}
		
		// remove armies that are at peace
		for (int i = 0; i < armies.size; i++) {
			if (armies.get(i) != null && !armies.get(i).isAtWar(location)) {
				armies.get(i).leaveSiege();
			}
		}
		
		if (armies.size == 0) {
			this.destroy();
			return;
		}

//		// If no longer at war, end siegeOrRaid
//		if (besieging.atPeace(location.getFaction())) {
//			this.destroy();
//			return;
//		}
		
		if (location.playerBesieging && location.getKingdom().getPlayer().hasTarget()) {
			remove(location.getKingdom().getPlayer());
//			System.out.println(location.playerBesieging + " " + !location.getKingdom().getPlayer().isStopped());
		}
		this.duration += delta;

//		System.out.println("Siege acting: " + location.getName() + " " + location.getKingdom().getTotalHour());
		if (location.getKingdom().getTotalHour() % CHECK_FREQ == 0) {
//			System.out.println("Checking if should attack/destroy: " + location.getName());
			if (!hasChecked) {
//				System.out.println("Attack/destroy: " + location.getName());
				attackOrDestroy();
				hasChecked = true;
			}
		}
		else if (hasChecked) hasChecked = false;
	}

	// Should the givne army join this siege?
	public boolean shouldJoin(Army army) {
		// Make sure at peace with the attacking faction
		if (!army.passive && army.getFaction().atPeace(this.besieging))
			return true;
		return false;
	}
	private void attackOrDestroy() {
//		if (Math.random() < 0.8f) return;
		if (inBattle()) {
			BottomPanel.log("Siege at " + this.location.getName() + " is in battle");
			return;
		}

		if (location.garrison.getHealthySize() <= 0) {
			destroy();
			return;
		}

		//judges whether or not to attack the city
		// calculate probability of victory, add a randomness factor, then attack
		double balance = BattleSim.calcBalance(armies, 1f, location.getGarrisonedAndGarrison(), location.getDefenseFactor());
		if (balance >= MIN_BALANCE_TO_ATTACK || location.isVillage()) attack();
		else if (balance <= MAX_BALANCE_TO_BREAK || shouldDestroy()) destroy(); // end siegeOrRaid if no chance
		else {
			// previously maintained siegeOrRaid, now force attack:
			attack();
		}
	}

	private boolean inBattle() {
		return this.battleActor != null;
	}

	private boolean shouldDestroy() {
		if (this.armies.first().party.getHealthySize() == 0) return true;
		return false;
	}
	
	public void attack() {
		this.battleActor = armies.first().getBattleActor();
//		else System.out.println("trying to attack with no armies!");
//		if (battleActor == null) {
//			System.out.println("null battleactor");
//			return;
//		}
		if (this.battleActor != null && this.battleActor.getSiege() != this) {
			System.out.print("Warning: " + armies.first().getName() + " is already in a siege/battle that's not this");
			return;
//			return;
		}

		System.out.println("attack at " + location.getName() + " which has " + location.getWealth() + " wealth and " + location.garrison.getTotalSize() + " defenders");
		// make sure siegeOrRaid is set
		location.siege = this;
//		System.out.println("SETTING SIEGE");
//		for (Army a : armies) {
//			// make sure siegeOrRaid is set
//			a.setSiege(this);
//		}

        location.siegeAttack(armies);
//		if (armies.size >= 1)
		this.battleActor = armies.first().getBattleActor();
    }
	
	// handle a wealth transfer from city to victors
	public void siegeSuccess() {
		System.out.println("siegeOrRaid success at " + location.getName() + " which has "  + location.getWealth());

		// TODO handle this for a bandit -- just loot the city.
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
		battleActor = null;
	}
	
	// siegeOrRaid attack failed
	public void siegeFailure() {
		battleActor = null;
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
		else BottomPanel.log("can't remove army from siegeOrRaid: doesn't exist", "red");
		
		army.setSiege(null);
	}
	
	public void destroy() {
		System.out.println("destroying siegeOrRaid of " + location.getName());
		for (Army a : armies) {
			a.leaveSiege();
		}
		if (battleActor != null) {
			battleActor.getBattle().forceRetreatAllAttackers();
			battleActor = null;
		}
		location.endSiege();
		besieging.cancelSiegeOf(location);
		this.remove();
	}

}
