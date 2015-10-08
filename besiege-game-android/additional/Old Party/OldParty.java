package kyle.game.besiege.party;
///*******************************************************************************
// * Besiege
// * by Kyle Dhillon
// * Source Code available under a read-only license. Do not copy, modify, or distribute.
// ******************************************************************************/
//package kyle.game.besiege.party;
//
//import java.util.Iterator;
//
//import kyle.game.besiege.Character;
//import kyle.game.besiege.Faction;
//import kyle.game.besiege.army.Army;
//import kyle.game.besiege.panels.BottomPanel;
//import kyle.game.besiege.party.Soldier.SoldierType;
//
//import com.badlogic.gdx.utils.Array;
//
//public class PartyOld {
//	private final double BASE_CHANCE = .3;
//	private final double MIN_WEALTH_FACTOR = 1.4; // times troopcount
//
//	public boolean updated; // does the panel need to be updated.
//	public int wealth;
//	public int minWealth; // keeps the party out of debt, of course!
//	public int maxSize; // current max size of the party
//
//	public boolean player;
//	public Army army;
//	private Array<Soldier> healthy;
//	private Array<Soldier> wounded;
//	private Array<Soldier> prisoners;
//	private Array<Soldier> upgradable;
//
//	private int atkTotal;
//	private int defTotal;
//	private int spdTotal;
//
//	public double woundChance;
//
//	public PartyOld() {
//		player = false;
//		healthy = new Array<Soldier>();
//		wounded = new Array<Soldier>();
//		prisoners = new Array<Soldier>();
//		upgradable = new Array<Soldier>();
//		atkTotal = 0;
//		defTotal = 0;
//		spdTotal = 0;
//		calcStats();		
//
//		maxSize = 10000;
//		wealth = 0;
//
//		woundChance = BASE_CHANCE;
//	}
//
//	public void act(float delta) {
//		if (player && army != null) woundChance = BASE_CHANCE * army.getCharacter().getAttributeFactor("Reviving");
//		if (!this.army.isInBattle())
//			checkHeal();
//		calcStats();
//	}
//
//	public void checkUpgrades() {
//		for (Soldier s : getUpgradable()) {
//			s.upgrade(s.unitType.upgrades.random());
////			s.upgrade(Weapon.upgrade(s.getWeapon()).random());
//		}
//	}
//
//	public void checkHeal() { // to be called every frame 
//		Iterator<Soldier> iter = wounded.iterator();
//		while (iter.hasNext()) {
//			Soldier soldier = iter.next();
//			if (soldier.isHealed())
//				heal(soldier);
//			updated = true;
//		}
//	}
//
//	public void addSoldier(Soldier soldier) {
//		if (this.getTotalSize() >= maxSize) {
//			System.out.println("trying to add more than max size");
//			return;
//		}
//		else {
//			updated = true;
//			if (soldier.isWounded()) {
//				wounded.add(soldier);
//				wounded.sort();
//			}
//			else {
//				healthy.add(soldier);
//				healthy.sort();
//			}
//			calcStats();
//		}
//	}
//	public void removeSoldier(Soldier soldier) {
//		updated = true;
//		if (healthy.contains(soldier, true)) {
//			healthy.removeValue(soldier, true);
//		}
//		else if (wounded.contains(soldier, true))
//			wounded.removeValue(soldier, true);
//		calcStats();
//	}
//
//	public void addPrisoner(Soldier soldier) {
//		updated = true;
//		prisoners.add(soldier);
//		prisoners.sort();
//	}
//	public boolean casualty(Soldier soldier) { // returns true if killed, false if wounded
//		// wound chance = base_chance*heal factor + (level of unit / max level)/2
//		double thisWoundChance = woundChance + (soldier.level / Soldier.MAX_LEVEL) / 2;
//		if (Math.random() < woundChance) {
//			wound(soldier);
//			return false;
//		}
//		else kill(soldier);
//		updated = true;
//		return true;
//	}
//	public void kill(Soldier soldier) {
//		removeSoldier(soldier); //can be used to kill both healthy and wounded soldiers.
//		wounded.sort();
//		healthy.sort();
//	}
//	public void wound(Soldier soldier) {
//		if (army != null)
//			soldier.wound();
//		healthy.removeValue(soldier, true);
//		this.addSoldier(soldier);
//		//	if (player) BottomPanel.log(soldier.name + " wounded", "orange");
//		calcStats();
//	}
//	public void heal(Soldier soldier) {
//		healNoMessage(soldier);
//		if (player) BottomPanel.log(soldier.getName() + " healed", "blue");
//	}
//	public void healNoMessage(Soldier soldier) {
//		soldier.heal();
//		wounded.removeValue(soldier, true);
//		this.addSoldier(soldier);
//		healthy.sort();
//		updated = true;
//	}
//	
//	public Array<Soldier> getUpgradable() {
//		upgradable.clear();
//		for (Soldier s : healthy) {
//			if (s.canUpgrade)
//				upgradable.add(s);
//		}
//		for (Soldier s : wounded) {
//			if (s.canUpgrade)
//				upgradable.add(s);
//		}
//		return upgradable;
//	}
//	public void calcStats() {
//		atkTotal = 0;
//		defTotal = 0;
//		spdTotal = 0;
//		for (Soldier s : healthy) {
//			atkTotal += s.baseAtk + s.getBonusAtk();
//			defTotal += s.baseDef + s.getBonusDef();
//			spdTotal += s.baseSpd + s.getBonusSpd();
//		}
//		if (!player) minWealth = (int) (MIN_WEALTH_FACTOR*getTotalSize());
//		else minWealth = 0;
//	}
//	public void givePrisoner(Soldier prisoner, PartyOld recipient) {
//		if (this.wounded.contains(prisoner, true))
//			this.wounded.removeValue(prisoner, true);
//		else if (this.healthy.contains(prisoner, true))
//			this.healthy.removeValue(prisoner, true);
//		else BottomPanel.log("trying to add invalid prisoner", "red");
//		recipient.addPrisoner(prisoner);
//	}
//
//	public void returnPrisoner(Soldier prisoner, PartyOld recipient) {
//		if (this.prisoners.contains(prisoner, true))
//			this.prisoners.removeValue(prisoner, true);
//		else BottomPanel.log("trying to remove invalid prisoner", "red");
//		recipient.addSoldier(prisoner);
//	}
//
//	public int getHealthySize() {
//		return healthy.size;
//	}
//	public int getWoundedSize() {
//		return wounded.size;
//	}
//	public int getTotalSize() {
//		return getHealthySize() + getWoundedSize();
//	}
//	
//	// used for placing troops
//	public Array<Soldier> getHealthyInfantry() {
//		Array<Soldier> roReturn = new Array<Soldier>();
//		for (Soldier s : healthy) {
//			if (s.getType() == SoldierType.INFANTRY) roReturn.add(s);
//		}
//		return roReturn;
//	}
//	public Array<Soldier> getHealthyArchers() {
//		Array<Soldier> toReturn = new Array<Soldier>();
//		for (Soldier s : healthy) {
//			if (s.getType() == SoldierType.ARCHER) toReturn.add(s);
//		}
//		return toReturn;
//	}
//	public Array<Soldier> getHealthyCavalry() {
//		Array<Soldier> toReturn = new Array<Soldier>();
//		for (Soldier s : healthy) {
//			if (s.getType() == SoldierType.CAVALRY) toReturn.add(s);
//		}
//		return toReturn;
//	}
//	
//	public Array<Soldier> getHealthy() {
//		return healthy;
//	}
//	public Array<Soldier> getWounded() {
//		return wounded;
//	}
//	public Array<Soldier> getHealthyCopy() {
//		return new Array<Soldier>(healthy);
//	}
//	public Array<Soldier> getPrisoners() {
//		return prisoners;
//	}
//	public void clearPrisoners() {
//		prisoners.clear();
//	}
//	public Array<Array<Soldier>> getConsolHealthy() {
//		return getConsol(healthy);
//	}
//	public Array<Array<Soldier>> getConsolWounded() {
//		return getConsol(wounded);
//	}
//	public Array<Array<Soldier>> getConsolPrisoners() {
//		return getConsol(prisoners);
//	}
//	// TODO maybe inefficient? can make more by sorting array by name
//	private Array<Array<Soldier>> getConsol(Array<Soldier> arrSoldier) {
//		// first thing: sort arrSoldier by name
//		arrSoldier.sort();
//
//
//		Array<String> names = new Array<String>();
//		Array<Array<Soldier>> consol = new Array<Array<Soldier>>();
//		for (Soldier s : arrSoldier) {
//			if (!names.contains(s.getName(), false)) {
//				names.add(s.getName());
//				Array<Soldier> type = new Array<Soldier>();
//				type.add(s);
//				consol.add(type);
//			}
//			else {
//				consol.get(names.indexOf(s.getName(), false)).add(s);
//			}
//		}
//		return consol;	
//	}
//	
//	public Faction getFaction() {
//		if (army != null) return army.getFaction();
//		else return null;
//	}
//	
//	public String getName() {
//		if (army != null) return army.getName();
//		return "";
//	}
//
//	@Override
//	public String toString() {
//		return null;
//	}
//	public int getAtk() {
//		return atkTotal;
//	}
//	public int getDef() {
//		return defTotal;
//	}
//	public int getSpd() {
//		return spdTotal;
//	}
//	public float getAvgDef() {
//		return defTotal/1f/getHealthySize();
//	}
//	public float getAvgSpd() {
//		return spdTotal/1f/getHealthySize();
//	}
//	public void distributeExp(int total) {
//		System.out.println("distributing " + total + " exp to you");
//		if (this.player) BottomPanel.log("Party receives " + total + " experience!", "green");
//		int exp = (int) (total/1.0/getHealthySize());
//		getHealthy().shrink();
//		for (int i = 0; i < getHealthy().size; i++)
//			getHealthy().get(i).addExp(exp);
//		wounded.sort();
//		healthy.sort();
//	}
//
//	// repairs an army as much as its wealth will allow it.
//	public void repair(PartyType pt) { // returns a repair cost
//		int newSize = pt.getRandomSize();
//		int missing = Math.max(newSize - this.getTotalSize(), 0); // no negative ints
////		int totalCost = 0;
//		boolean canAfford = true;
//		while (missing > 0 && canAfford) {
//			Soldier newSoldier = new Soldier(pt.randomSoldierType(), this);
//			int cost = newSoldier.getWeapon().getCost();
//			if (this.wealth > cost) {
//				this.addSoldier(newSoldier);
//				this.wealth -= cost;
//				missing--;
//			}
//			else {
//				canAfford = false;
//			}
//		}
//		System.out.println("party repaired");
//	}
//
//}
