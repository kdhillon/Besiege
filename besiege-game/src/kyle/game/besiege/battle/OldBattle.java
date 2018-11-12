/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.battle;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.Army.ArmyType;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.PanelBattle;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;
// The way a map battle should work:

import static kyle.game.besiege.battle.BattleStage.DEPRECATED_THRESHOLD;
import static kyle.game.besiege.battle.BattleStage.RETREAT_THRESHOLD;

// It starts, and then waits a fixed length of time (length can be proportional to party size)
public class OldBattle implements Battle { // new battle system involving Party
	private static final float SPEED = 20; //lower is faster

	private static final int MIN_RETREAT_TIME = 3;
	private static final int BASE_RETREAT_TIME = 5;
	private static final double RETREAT_WEALTH_FACTOR = .7; // this is how much of the retreating parties wealth will be lost

	// doesn't require a kingdom necessarily
	private Kingdom kingdom;
	private BattleActor battleActor; // parent
    private VictoryManager victoryManager;

	private StrictArray<Army> aArmies;
	private StrictArray<Army> dArmies;
	private StrictArray<Army> aArmiesRet;
	private StrictArray<Army> dArmiesRet;

	private StrictArray<Party> aParties;
	private StrictArray<Party> dParties;
	private StrictArray<Party> aPartiesRet;
	private StrictArray<Party> dPartiesRet;

	private double aAdvantage;// calculated constants controlled by external factors
	private double dAdvantage;// like player's command skill, etc.

	private boolean playerInA;
	private boolean playerInD;

	private boolean firstTimeInit = false;

	private int aAtk;
	private int dAtk;

	private double balanceA; // Overall balance of the battle, useful for creating a balance bar
	private double balanceD;

	private boolean isOver;
	private boolean didAtkWin;

	// garrison battles are slow for some reason

	// For Kryo
	public OldBattle() {	}

	public OldBattle(BattleActor battleActor, Party initAttackerParty, Party initDefenderParty) {
		if (battleActor != null)
			this.kingdom = battleActor.getKingdom();
		this.battleActor = battleActor;
		Army initAttacker = initAttackerParty.army;
		Army initDefender = initDefenderParty.army;


		aParties = new StrictArray<Party>();
		dParties = new StrictArray<Party>();
		aPartiesRet = new StrictArray<Party>();
		dPartiesRet = new StrictArray<Party>();

		aParties.add(initAttackerParty);
		dParties.add(initDefenderParty);

		calcStats();
        victoryManager = new VictoryManager(kingdom, this, battleActor.getSiege(), balanceD);
        victoryManager.addInitTroopCount(initAttackerParty.getHealthySize() + initDefenderParty.getHealthySize());

		if (initAttackerParty.player) playerInA = true;
		else if (initDefenderParty.player) playerInD = true;

		if (initAttacker != null && initDefender != null) {
			aArmies = new StrictArray<Army>();
			dArmies = new StrictArray<Army>();
			aArmiesRet = new StrictArray<Army>();
			dArmiesRet = new StrictArray<Army>();

			aArmies.add(initAttacker);
			dArmies.add(initDefender);

			initAttacker.setStopped(true);
			initAttacker.setVisible(false);
			initDefender.setStopped(true);
			initDefender.setVisible(false);
		}

		aAdvantage = 1; // for now. make influenced by player's attribute as well as morale.
		dAdvantage = 1;
	}

	private void calcStats() {
        aAtk = 0;
		for (Party p : aParties) {
			p.calcStats();
			aAtk += p.getAtk();
		}
		dAtk = 0;
		for (Party p : dParties) {
			p.calcStats();
			dAtk += p.getAtk();
		}

		balanceA = aAtk*aAdvantage; // method for computing balance
		balanceD = dAtk*dAdvantage;
		double total = balanceA + balanceD;
		balanceA = balanceA / total; // balanceA + balanceD = 1
		balanceD = balanceD / total;
	}

	@Override
	public double getBalanceDefenders() {
		return balanceD;
	}

	@Override
	public float getAttackingAtk() {
		return aAtk;
	}
	@Override
	public float getDefendingAtk() {
		return dAtk;
	}

	private void remove(Army army) {
		if (aArmies.contains(army, true)) {
			aParties.removeValue(army.party, true);
			aArmies.removeValue(army, true);
		}
		else if (dArmies.contains(army, true)) {
			dArmies.removeValue(army, true);
			dParties.removeValue(army.party, true);
		}
		else if (aArmiesRet.contains(army, true)) {
			aArmiesRet.removeValue(army, true);
			aParties.removeValue(army.party, true);
		}
		else if (dArmiesRet.contains(army, true)) {
			dParties.removeValue(army.party, true);
			dArmiesRet.removeValue(army, true);
		}
		else BottomPanel.log("error when removing " + army.getName() + " from battle", "red");

		army.endBattle();
		army.setStopped(false);
		army.setVisible(false);
//		if (kingdom.getArmies().contains(army, true) && army.isGarrisonedSafely()) this.setVisible(true);

		if (army == kingdom.getPlayer()) {
			playerInA = false;
			playerInD = false;
			kingdom.getMapScreen().getSidePanel().setDefault(true);
			this.simulate(.001f);// arbitrary time
		}
	}

	private void destroy(Army army) {
		if (aArmies.contains(army, true) || aArmiesRet.contains(army, true)) {
			if (playerInA) log(army.getName() + " was destroyed!", "red");
			else log(army.getName() + " was destroyed!", "green");
			for (Soldier s : army.getParty().getWounded())
				army.getParty().givePrisonerFromThis(s, dArmies.random().getParty());
			for (Soldier s : army.getParty().getPrisoners())
				army.getParty().returnPrisoner(s, dArmies.random().getParty());
			remove(army);
		}
		else if (dArmies.contains(army, true) || dArmiesRet.contains(army,true)) {
			if (playerInD) log(army.getName() + " was destroyed!", "red");
			else log(army.getName() + " was destroyed!", "green");
			for (Soldier s : army.getParty().getWounded())
				army.getParty().givePrisonerFromThis(s, aArmies.random().getParty());
			for (Soldier s : army.getParty().getPrisoners())
				army.getParty().returnPrisoner(s, aArmies.random().getParty());
			remove(army);
		}
		else BottomPanel.log("error when removing " + army.getName() + " from battle", "red");
//		victoryManager.handleArmyRetreat(army);

		army.destroy();
	}

	private void retreat(Army army) {
		army.retreatCounter = MIN_RETREAT_TIME + BASE_RETREAT_TIME / army.getParty().getAvgSpd(); // start countdown
		if (aArmies.contains(army, true)) {
			aArmies.removeValue(army, true);
			aParties.removeValue(army.party, true);
			if (!aArmiesRet.contains(army, true)) {
				aArmiesRet.add(army);
				aPartiesRet.add(army.party);
			}
			log(army.getName() + " is retreating!", "yellow");
		}
		else if (dArmies.contains(army, true)) {
			dArmies.removeValue(army, true);
			dParties.removeValue(army.party, true);
			if (!aArmiesRet.contains(army, true)) {
				dArmiesRet.add(army);
				dPartiesRet.add(army.party);
			}
			log(army.getName() + " is retreating!", "yellow");
		}
//        victoryManager.handleArmyRetreat(army);
	}

	// returns false if there's been a victory so the next phase can be skipped
	private void meleePhase() {
		calcStats();

		if (aArmies.size >= 1) {
			 if (dArmies.size >= 1)
				 attackStep();
			 else if (dArmiesRet.size <= 0) {
//				 System.out.println(getName() + " point 1");
				 victory(aArmies, dArmies);
				 return;
			 }
		}
		else if (aArmiesRet.size <= 0) {
//			System.out.println(getName() + " point 2");
			victory(dArmies, aArmies);
			return;
		}

		if (!isOver) { // so battle doesn't "end" twice buggy!
			calcStats();

			if (aArmies.size >= 1) {
				if (dArmies.size >= 1)
					defenseStep();
				else if (dArmiesRet.size <= 0) {
//					System.out.println(getName() + " point 3");
					victory(aArmies, dArmies);
					return;
				}
			}
			else if (aArmiesRet.size <= 0) {
//				 System.out.println(getName() + " point 4");
				victory(dArmies, aArmies);
				return;
			}
		}
		// clean up
		if (aArmies.size >= 1) {
			if (dArmies.size == 0 && dArmiesRet.size == 0)
				victory(aArmies, dArmies);
		}
		else { // just do it regardless of dArmies size in case both retreat or die somehow
			if (aArmies.size == 0 && aArmiesRet.size == 0)
				victory(dArmies, aArmies);
		}
	}

	private void finalizeRetreat(Army army) {
		for (Soldier s : army.party.getHealthy()) {
			s.registerBattleRetreat();
		}
		for (Soldier s : army.party.getWounded()) {
			s.registerBattleRetreat();
		}
		remove(army);
	}

	private void retreatPhase(float delta) {
		calcStats();
		if (aArmiesRet.size >= 1) {
			if (dArmies.size >= 1) {
				attackRetreatStep();
			}
		}
		if (dArmiesRet.size >= 1) {
			if (aArmies.size >= 1) {
				defenseRetreatStep();
			}
		}
		// update counter, check if done retreating
		for (Army army : aArmiesRet) {
			army.retreatCounter -= delta;
			if (army.retreatCounter <= 0) {
				log(army.getName() + " has retreated!", "yellow");
//				System.out.println(army.getName() + " retreat point 1 with counter " + army.retreatCounter);
				if (army.getParty().getHealthySize() <= DEPRECATED_THRESHOLD)
					this.destroy(army);
				else {
					finalizeRetreat(army);
				}
			}
			if (army.getParty().getHealthySize() <= DEPRECATED_THRESHOLD)
				this.destroy(army);
		}
		for (Army army : dArmiesRet) {
			army.retreatCounter -= delta;
			if (army.retreatCounter <= 0) {
				log(army.getName() + " has retreated!", "yellow");
//				System.out.println(army.getName() + " retreat point 2 with counter " + army.retreatCounter);
				if (army.getParty().getHealthySize()  <= DEPRECATED_THRESHOLD)
					this.destroy(army);
				else
					remove(army);
			}
			if (army.getParty().getHealthySize()  <= DEPRECATED_THRESHOLD)
				this.destroy(army);
		}
	}

	// when anyone in aArmies is retreating
	private void attackRetreatStep() {
		for (Army defense : aArmiesRet) {
			double defenseRoll = Math.random() * SPEED * defense.getParty().getAvgDef();
			//System.out.println("defense roll of attacker : " + defenseRoll);
			if (dAtk*dAdvantage > defenseRoll)
				killOne(defense, true);
		}
	}
	// when anyone in dArmies is retreating
	private void defenseRetreatStep() {
		for (Army defense : dArmiesRet) {
			double defenseRoll = Math.random() * SPEED * defense.getParty().getAvgDef();
			//System.out.println("defense roll of defender : " + defenseRoll);
			if (aAtk*aAdvantage > defenseRoll)
				killOne(defense, false);
		}
	}

	private void attackStep() {
		for (Army defense : dArmies) {
			double defenseRoll = Math.random() * SPEED * defense.getParty().getAvgDef();
//			System.out.println("defense roll of defender : " + defenseRoll);
			if (aAtk*aAdvantage > defenseRoll)
				killOne(defense, false);
			if (!defense.getParty().player && balanceD < RETREAT_THRESHOLD) {
				if (defense.type != ArmyType.MILITIA)
					retreat(defense);
			}
		}
	}

	private void defenseStep() {
		for (Army defense : aArmies) {
			double defenseRoll = Math.random() * SPEED * defense.getParty().getAvgDef();
			//System.out.println("defense roll of attacker : " + defenseRoll);
			if (dAtk*dAdvantage > defenseRoll)
				killOne(defense, true);
			if (!defense.getParty().player && balanceA < RETREAT_THRESHOLD) {
				if (defense.type != ArmyType.MILITIA)
					retreat(defense);
			}
		}
	}

	private void killOne(Army army, boolean wasInAttackers) { // enemyCasualties/wounds one random troop in this army, weighted by the troop's defense
		// Now choose a random soldier weighted by def
		Soldier random = army.party.getRandomWeightedInverseDefense();
//		if (random == null) throw new java.lang.AssertionError();
		if (random == null) {
			this.destroy(army);
			return;
		}

		casualty(random, wasInAttackers);

		if (army.getParty().getHealthySize() <= DEPRECATED_THRESHOLD) {
			log(army.getName() + " lost all troops and was removed from battle", "red");
			this.destroy(army);
		}
	}

	// main thing called by battlestage?
	public void casualty(Soldier soldier, boolean wasInAttackers) {
		Soldier killer = getRandomForKill(!wasInAttackers);
		boolean killed = soldier.casualty(wasInAttackers, killer, playerInA, playerInD);

	    victoryManager.handleCasualty(soldier, wasInAttackers, killed);
	}

	private Soldier getRandomForKill(boolean fromAttackers) {
		StrictArray<Army> armies;
		if (fromAttackers) armies = aArmies;
		else armies = dArmies;

		armies.shrink();

		// get random playerPartyPanel based on healthy size
		int totalSize = 0;
		for (int i = 0; i < armies.size; i++) {
			totalSize += armies.get(i).party.getHealthySize();
		}

		int randomIndex = -1;
		int randomValue = (int) (Math.random() * totalSize);

		for (int i = 0; i < armies.size; ++i)
		{
			randomValue -= armies.get(i).party.getHealthySize();
			if (randomValue <= 0)
			{
				randomIndex = i;
				break;
			}
		}

		// if for some reason can't register this kill, that's ok.
		if (randomIndex == -1) return null;

		return armies.get(randomIndex).party.getRandomWeightedAttack();
	}

	private void logDefeat(Army army) {
		log(army.getName() + " was defeated!", "green");
	}

	private void victory(StrictArray<Army> victor, StrictArray<Army> loser) {
		if (kingdom.getMapScreen().getSidePanel().getActivePanel().getClass() == PanelBattle.class &&
				((PanelBattle) (kingdom.getMapScreen().getSidePanel().getActivePanel())).battle == this)
			kingdom.getMapScreen().getSidePanel().setActiveArmy(kingdom.getPlayer());

		if (victor == aArmies) didAtkWin = true;
		else if (victor == dArmies) didAtkWin = false;

		victor.shrink();

		// manage victorious armies and calculate contributions
		for (int i = 0; i < victor.size; i++) {
			Army army = victor.get(i);
			army.endBattle();
			army.setStopped(false);
			army.forceWait(army.getForceWait());
			if (army.getParty().player) {
				kingdom.getMapScreen().getSidePanel().setDefault(true);
			}

			//	log(army.getName() + " has won a battle", "cyan");
			if (!army.isGarrisoned()) army.setVisible(true);
			army.nextTarget(); //

			if (army.getParty().player) {
//				army.setStopped(true);
				army.setTarget(null);
			}
		}


		if (victoryManager != null) {
		    victoryManager.handleVictory(aParties, dParties, aPartiesRet, dPartiesRet, didAtkWin);
        }
		destroy();
	}

	private void destroy() {
		if (playerInA || playerInD)
			kingdom.getMapScreen().getSidePanel().setActiveArmy(kingdom.getPlayer());

		if (aArmies != null) {
			aArmies.clear();
			dArmies.clear();
			aArmiesRet.clear();
			dArmiesRet.clear();
			aParties.clear();
			dParties.clear();
			aPartiesRet.clear();
			dPartiesRet.clear();
		}

		aArmies = null;
		dArmies = null;
		aArmiesRet = null;
		dArmiesRet = null;
		aParties = null;
		dParties = null;
		aPartiesRet = null;
		dPartiesRet = null;
		this.isOver = true;

		// Kill battle actor as well.
		if (this.battleActor != null) {
			this.battleActor.destroy();
		}
	}

	private void log(String text, String color) {
		if (playerInA || playerInD) // only logs info if Player is in this battle
			BottomPanel.log(text, color);
	}

//	// used in battle stage
//	@Override
//	public void updateBalance() {
//		int firstAtk = 0;
//		int firstSize = 0;
//
//		for (Party p : aParties) {
//			firstAtk += p.getAtk();
//			firstSize += p.getHealthySize();
//		}
//		int secondAtk = 0;
//		int secondSize = 0;
//		for (Party p : dParties) {
//			secondAtk += p.getAtk();
//			secondSize += p.getHealthySize();
//		}
//		double balanceFirst = firstAtk + firstSize; // method for computing balance
//		double balanceSecond = secondAtk + secondSize;
//		double total = balanceFirst + balanceSecond;
//		balanceA = balanceFirst / total; // balanceA + balanceD = 1
//		balanceD = 1-balanceA;
//
//		if (!firstTimeInit) {
//			initBalanceA = balanceA;
//			initBalanceD = balanceD;
////			System.out.println(initBalanceA + " " + initBalanceD);
//			firstTimeInit = true;
//		}
//	}

	@Override
	public boolean addToAttackers(Army army) {
		if (!shouldJoinAttackers(army)) {
			return false;
		}
		army.setVisible(false);
		if (army == kingdom.getPlayer()) {
			playerInA = true;
		}
		log(army.getName() + " was added to attackers!", "pink");
		aArmies.add(army);
		aParties.add(army.party);
		army.setVisible(false);
        victoryManager.addInitTroopCount(army.getParty().getHealthySize());
		return true;
	}

	@Override
	public boolean addToDefenders(Army army) {
		if (!shouldJoinDefenders(army)) {
			return false;
		}
		army.setVisible(false);
		if (army == kingdom.getPlayer()) {
			playerInD = true;
			kingdom.getMapScreen().getSidePanel().setActiveBattle(this);
		}
		//expA
		dArmies.add(army);
		dParties.add(army.party);
		log(army.getName() + " was added to defenders!", "pink");
		army.setVisible(false);
        victoryManager.addInitTroopCount(army.getParty().getHealthySize());
		return true;
	}

	@Override
	public StrictArray<Party> getAttackingParties() {
		return aParties;
	}

	@Override
	public StrictArray<Party> getDefendingParties() {
		return dParties;
	}

	@Override
	public StrictArray<Party> getAttackingPartiesRetreated() {
		return aPartiesRet;
	}

	@Override
	public StrictArray<Party> getDefendingPartiesRetreated() {
		return dPartiesRet;
	}

	@Override
	public boolean shouldJoinAttackers(Army army) {
		if (aArmies == null || dArmies == null) return false;
		if (aArmies.size >= 1 && dArmies.size >= 1) {
		    if (army.isAtWar(dArmies.first())) {
				if (!army.isAtWar(aArmies.first()))
					return true; // attackers
			}
		}
		return false; // shouldn't join
	}

	@Override
	public boolean shouldJoinDefenders(Army army) {
		if (aArmies == null || dArmies == null) return false;
		if (aArmies.size >= 1 && dArmies.size >= 1) {
			if (army.isAtWar(aArmies.first())) {
				if (!army.isAtWar(dArmies.first()))
					return false; // defenders
			}
		}
		return false; // shouldn't join
	}

	@Override
	public Faction getAttackingFactionOrNull() {
	    if (aParties == null || aParties.first() == null) return null;
		return aParties.first().getFaction();
	}

	@Override
	public Faction getDefendingFactionOrNull() {
		return dParties.first().getFaction();
	}

	@Override
	public void simulate(float delta) {
		if (this.aArmies == null) {
			return;
		}

		// Do entire battle efficiently

		calcStats();
		meleePhase();
		if (!isOver)
			retreatPhase(delta);
	}

	@Override
	public void forceRetreat(Army army) {
		// TODO Auto-generated method stub
	}

	@Override
	public void forceRetreatAllAttackers() {
		// not sure if this works
		victory(dArmies, aArmies);
	}

	@Override
	public void setDefensiveAdvantage(double advantage) {
		dAdvantage = advantage;
	}

	@Override
	public boolean playerAttacking() {
		return playerInA;
	}

	@Override
	public boolean playerDefending() {
		return playerInD;
	}

	@Override
	public boolean isOver() {
		return isOver;
	}
	@Override
	public boolean didAttackersWin() {
		return didAtkWin;
	}

	@Override
	public VictoryManager getVictoryManager() {
		return victoryManager;
	}
}
