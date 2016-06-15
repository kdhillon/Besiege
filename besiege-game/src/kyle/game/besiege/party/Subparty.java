package kyle.game.besiege.party;

import java.util.Iterator;

import kyle.game.besiege.StrictArray;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.Soldier.SoldierType;

// contains a general and her bodyguard, as well as any subparties under her control
public class Subparty {
	public int MAX_SIZE = 1000;
	
	private Party party;
	private Subparty parent; // this is the boss, null usually.
	
	public General general; // who commands the party!
	
	public StrictArray<Soldier> healthy;
	public StrictArray<Soldier> wounded;
	public StrictArray<Soldier> upgradable;
	
	public float atkTotal;
	public float defTotal;
	public float spdTotal;
	
	public Subparty(Party party) {
		this.party = party;
		healthy = new StrictArray<Soldier>();
		wounded = new StrictArray<Soldier>();
		upgradable = new StrictArray<Soldier>();
	}
	
	// could have this be automatically done! 
	// player clicks "promote new general" - randomly generates new general from an existing soldier?
	// player has option to "demote" them - but lowers morale, and chance of mutiny if popular!
	public void promoteToGeneral(Soldier s) {
		// can promote any soldier
		if (!this.healthy.contains(s, true) && !this.wounded.contains(s, true)) {
			System.out.println("trying to promote a soldier not in this subparty!!!");
			return;
		}
		
		if (general != null) {
			System.out.println("you haven't demoted this general yet");
		}
		
		if (this.healthy.contains(s, true)) this.healthy.removeValue(s, true);
		if (this.wounded.contains(s, true)) this.wounded.removeValue(s, true);

		general = new General(s);
	}
	
	public void checkHeal() { // to be called every frame 
		Iterator<Soldier> iter = wounded.iterator();
		while (iter.hasNext()) {
			Soldier soldier = iter.next();
			if (soldier.isHealed()) {
				heal(soldier);
				party.updated = true;
			}
		}
	}
	
	public void healNoMessage(Soldier soldier) {
		soldier.heal();
		wounded.removeValue(soldier, true);
		this.addSoldier(soldier);
		healthy.sort();
		party.updated = true;
	}
	
	public boolean casualty(Soldier soldier) { // returns true if killed, false if wounded
		// wound chance = base_chance*heal factor + (level of unit / max level)/2
		double thisWoundChance = party.woundChance + (soldier.level / Soldier.MAX_LEVEL) / 2;
		if (Math.random() < party.woundChance) {
			wound(soldier);
			return false;
		}
		else kill(soldier);
		party.updated = true;
		return true;
	}

	public StrictArray<Soldier> getUpgradable() {
		upgradable.clear();
		for (Soldier s : healthy) {
			if (s.canUpgrade)
				upgradable.add(s);
		}
		for (Soldier s : wounded) {
			if (s.canUpgrade)
				upgradable.add(s);
		}
		return upgradable;
	}
	
	public void kill(Soldier soldier) {
		removeSoldier(soldier); //can be used to kill both healthy and wounded soldiers.
		wounded.sort();
		healthy.sort();
	}
	public void wound(Soldier soldier) {
//		if (army != null)
		soldier.wound();
		healthy.removeValue(soldier, true);
		this.addSoldier(soldier);
		//	if (player) BottomPanel.log(soldier.name + " wounded", "orange");
		calcStats();
	}
	public void heal(Soldier soldier) {
		healNoMessage(soldier);
		if (party.player) BottomPanel.log(soldier.getTypeName() + " healed", "blue");
	}
	
	public void addSoldier(Soldier soldier) {
		if (this.getTotalSize() >= MAX_SIZE) {
			System.out.println("trying to add more than max size to subparty");
			return;
		}
		else {
			party.updated = true;
			if (soldier.isWounded()) {
				if (!wounded.contains(soldier, true))
					wounded.add(soldier);
				wounded.sort();
			}
			else {
				if (!healthy.contains(soldier, true))
					healthy.add(soldier);
				healthy.sort();
			}
			soldier.subparty = this;
			soldier.party = this.party;
			calcStats();
		}
	}
	
	public int getHealthySize() {
		return healthy.size;
	}
	public int getWoundedSize() {
		return wounded.size;
	}
	public int getTotalSize() {
		return getHealthySize() + getWoundedSize();
	}
	
	public void removeSoldier(Soldier soldier) {
		party.updated = true;
		if (healthy.contains(soldier, true)) {
			healthy.removeValue(soldier, true);
			calcStats();
		}
		else if (wounded.contains(soldier, true)) {
			wounded.removeValue(soldier, true);
			calcStats();
		}
	}

	// used for placing troops
	public StrictArray<Soldier> getHealthyInfantry() {
		StrictArray<Soldier> roReturn = new StrictArray<Soldier>();
		for (Soldier s : healthy) {
			if (s.getType() == SoldierType.INFANTRY) roReturn.add(s);
		}
		return roReturn;
	}
	public StrictArray<Soldier> getHealthyArchers() {
		StrictArray<Soldier> toReturn = new StrictArray<Soldier>();
		for (Soldier s : healthy) {
			if (s.getType() == SoldierType.ARCHER) toReturn.add(s);
		}
		return toReturn;
	}
	public StrictArray<Soldier> getHealthyCavalry() {
		StrictArray<Soldier> toReturn = new StrictArray<Soldier>();
		for (Soldier s : healthy) {
			if (s.getType() == SoldierType.CAVALRY) toReturn.add(s);
		}
		return toReturn;
	}
	
	public General getGeneral() {
		return general;
	}
	
	public void calcStats() {
		atkTotal = 0;
		defTotal = 0;
		spdTotal = 0;
		for (Soldier s : healthy) {
			atkTotal += s.baseAtk + s.getBonusAtk();
			defTotal += s.baseDef + s.getBonusDef();
			spdTotal += s.baseSpd + s.getBonusSpd();
		}
	}
}
