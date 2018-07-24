package kyle.game.besiege.party;

import java.util.Iterator;

import kyle.game.besiege.StrictArray;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.Soldier.SoldierType;

// contains a general and her bodyguard, as well as any subparties under her control
public class Subparty {
	public static int HARD_MAX = 20;
	
	private Party party;
	Subparty parent; // this is the boss, null usually.
	
	private StrictArray<Subparty> children;
	
	public General general; // who commands the playerPartyPanel!
	
	public StrictArray<Soldier> healthy;
	public StrictArray<Soldier> wounded;
	public StrictArray<Soldier> upgradable;
	
	public float atkTotal;
	public float defTotal;
	public float spdTotal;
	
	public float randomGreen;
	public float randomBlue;
	
	// for kryo
	public Subparty() {} ;
	
	public Subparty(Party party) {
		this.party = party;
		healthy = new StrictArray<Soldier>();
		wounded = new StrictArray<Soldier>();
		upgradable = new StrictArray<Soldier>();
		children = new StrictArray<Subparty>();
		
		randomGreen = (float) Math.random() / 2;
		randomBlue = (float) Math.random() / 2;
	}
	
	// could have this be automatically done! 
	// player clicks "promote new general" - randomly generates new general from an existing soldier?
	// player has option to "demote" them - but lowers morale, and chance of mutiny if popular!
	public void promoteToGeneral(Soldier s) {
        General g = (new General(s));
		addSoldier(g);
        setGeneral(g);
	}
	
	public void setGeneral(General g) {
		if (this.general != null) {
			demoteGeneral(general);
		}
		this.general = g;
		g.subparty = this;
		for (Soldier s : healthy) {
			s.updateGeneral(g);
		}
		for (Soldier s : wounded) {
			s.updateGeneral(g);
		}
	}
	
	public void demoteGeneral(General general) {
		System.out.println("demoting general");
		this.removeSoldier(general);
		
		this.general = null;
		general.subparty = null;
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
//		System.out.println("casualty: " + soldier.unitType.name);
		// wound chance = base_chance*heal factor + (level of unit / max level)/2
		double thisWoundChance = party.woundChance + (soldier.level / Soldier.MAX_LEVEL) / 2;
		thisWoundChance = 0; //TODO remove
		if (Math.random() < thisWoundChance) {
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
//		System.out.println("Killing "  + soldier.unitType.name);
		removeSoldier(soldier); //can be used to kill both healthy and wounded soldiers.
		wounded.sort();
		healthy.sort();
		
		// TODO this isn't getting called enough or something
		// subparties aren't being destroyed and still aren't being displayed in panel
		// check if the playerPartyPanel is dead
		if (wounded.size == 0 & healthy.size == 0 && general == null) {
			this.destroy();
			return;
		}
//		else {
//			System.out.println("wounded: " + wounded.size + " healthy: " + healthy.size);
//		}
	}
	
	public void handleBattleEnded() {
		if (general == null) promoteNextGeneral();
		if (general == null) throw new java.lang.AssertionError();
	}
	
	// kill this subparty
	public void destroy() {	
		System.out.println("destroying subparty");
		this.party.destroySub(this);
	}
	
	public void promoteNextGeneral() {
		int maxTier = 0;
		for (Soldier s : healthy) {
			if (s.getTier() > maxTier) maxTier = s.getTier();
		}
		for (Soldier s : wounded) {
			if (s.getTier() > maxTier) maxTier = s.getTier();
		}
		
		for (Soldier s : healthy) {
			if (s.getTier() == maxTier) {
				promoteToGeneral(s);
				return;
			}
		}
		for (Soldier s : wounded) {
			if (s.getTier() == maxTier) {
				promoteToGeneral(s);
				return;
			}
		}
		throw new java.lang.AssertionError("Couldn't promote general");
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
		if (general != null && this.getTotalSize() >= general.getMaxSubPartySize()) {
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
			if (general != null) soldier.updateGeneral(this.general);
			calcStats();
		}
	}
	
	public StrictArray<StrictArray<Soldier>> getConsolHealthy() {
		return Party.getConsol(healthy);
	}
	public StrictArray<StrictArray<Soldier>> getConsolWounded() {
		return Party.getConsol(wounded);
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
	public int getHealthyLevelSum() {
		int levelSum = 0;
		for (Soldier s : getHealthyInfantry()) {
			levelSum += s.level;
		}
		for (Soldier s : getHealthyArchers()) {
			levelSum += s.level;
		}
		for (Soldier s : getHealthyCavalry()) {
			levelSum += s.level;
		}
		return levelSum;
	}
	
	public void removeSoldier(Soldier soldier) {
//		System.out.println("removing " + soldier.getName());
		party.updated = true;
		if (healthy.contains(soldier, true)) {
			healthy.removeValue(soldier, true);
			calcStats();
		}
		else if (wounded.contains(soldier, true)) {
			wounded.removeValue(soldier, true);
			calcStats();
		}
		if (soldier == general) {
//			System.out.println("setting general to be null");
			this.general = null;
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
			atkTotal += s.getAtk();
			defTotal += s.getDef();
			spdTotal += s.getSpd();
		}
	}
	
	public int getSpotsRemaining() {
	    if (this.general != null)
		    return this.general.getMaxSubPartySize() - this.getTotalSize();
	    else return HARD_MAX - this.getTotalSize();
	}
	
	public boolean isFull() {
		return getSpotsRemaining() == 0;
	}
	
	public void addSub(Subparty s) {
		this.children.add(s);
		s.parent = this;
	}

	// Returns rank of this subparty
    // 0 is root.
    // 1 is first level
    // etc.
	public int getRank() {
		Subparty p = parent;
		int count = 0;
		
		while (p != null) {
			p = p.parent;
			count++;
		}
		
		return count;
	}
	
	public float getBonusGeneralAtk() {
		if (general == null) return 0;
		return general.getBonusGeneralAtk();
	}
	
	public float getBonusGeneralDef() {
		if (general == null) return 0;
		return general.getBonusGeneralDef();	}
	
	public float getBonusRangedAtk() {
		if (general == null) return 0;
		return general.getBonusRangedAtk();	}
	
	public float getBonusGeneralRange() {
		if (general == null) return 0;
		return general.getBonusGeneralRange();	}
	
	public float getHPBonus() {		
		if (general == null) return 0;
		return general.getHPBonus();	}
	
	public float getMoraleBonus() {
		if (general == null) return 0;
		return general.getMoraleBonus();	}
}
