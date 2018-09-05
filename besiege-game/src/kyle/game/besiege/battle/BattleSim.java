package kyle.game.besiege.battle;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.PanelBattle;
import kyle.game.besiege.party.*;

public class BattleSim implements Battle {
    // doesn't require a kingdom necessarily
    private Kingdom kingdom;
    private BattleActor battleActor; // parent
    private VictoryManager victoryManager;

    // Ok for these to be null (if one party is a garrison, for instance).
    private StrictArray<Army> aArmies;
    private StrictArray<Army> dArmies;

    // Armies that have retreated.
    private StrictArray<Army> aArmiesRet;
    private StrictArray<Army> dArmiesRet;

    private StrictArray<Party> aParties;
    private StrictArray<Party> dParties;

    // Parties that have retreated.
    private StrictArray<Party> aPartiesRet;
    private StrictArray<Party> dPartiesRet;

    // Balance defenders (e.g. 0.9 means defenders about to win)
    private double currentBalanceD;

    // is the player in either party?
    private boolean playerInA;
    private boolean playerInD;

    private boolean isOver;
    private boolean didAtkWin;

    // For kryo
    public BattleSim() { }

    public BattleSim(BattleActor battleActor, Party initAttackerParty, Party initDefenderParty) {
        if (battleActor != null)
            this.kingdom = battleActor.getKingdom();
        if (initAttackerParty == initDefenderParty) throw new AssertionError();
        if (initAttackerParty.getHealthySize() == 0) throw new AssertionError(initAttackerParty.getName() + " size is 0");
        if (initDefenderParty.getHealthySize() == 0) throw new AssertionError(initDefenderParty.getName() + " size is 0");

        this.battleActor = battleActor;
        Army initAttacker = initAttackerParty.army;
        Army initDefender = initDefenderParty.army;

        aParties = new StrictArray<>();
        dParties = new StrictArray<Party>();
        aPartiesRet = new StrictArray<Party>();
        dPartiesRet = new StrictArray<Party>();

        aParties.add(initAttackerParty);
        dParties.add(initDefenderParty);

        calcBalance();

        victoryManager = new VictoryManager(kingdom, this, battleActor.getSiegeLocation(), currentBalanceD);
        victoryManager.addInitTroopCount(initAttackerParty.getHealthySize() + initDefenderParty.getHealthySize());

        if (initAttackerParty.player) playerInA = true;
        else if (initDefenderParty.player) playerInD = true;

        if (initAttacker != null) {
            aArmies = new StrictArray<Army>();
            aArmiesRet = new StrictArray<Army>();

            initAttacker.setStopped(true);
            initAttacker.setVisible(false);

            if (initAttackerParty.getHealthySize() > 0)
                aArmies.add(initAttacker);
            else throw new AssertionError();
        }

        if (initDefender != null) {
            dArmies = new StrictArray<Army>();
            dArmiesRet = new StrictArray<Army>();

            initDefender.setStopped(true);
            initDefender.setVisible(false);

            if (initDefender.party.getHealthySize() > 0)
                dArmies.add(initDefender);
        }

        double balanceAttackers = getLevelSum(aParties);
        double balanceDefenders = getLevelSum(dParties);

        System.out.println("Attacker strength: " + balanceAttackers);
        System.out.println("Defender strength: " + balanceDefenders);

        calcBalance();
        checkIfVictory();
    }

	// Adds the given playerPartyPanel to the list of attackers. Disables the army until they leave the battle or are destroyed.
	// Returns true if playerPartyPanel was added successfully, false otherwise.
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
    public StrictArray<Party> getAttackingRetreatingParties() {
        return aPartiesRet;
    }

    @Override
    public StrictArray<Party> getDefendingRetreatingParties() {
        return dPartiesRet;
    }

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

    // Calculates the balance and sets it.
    private void calcBalance() {
        this.currentBalanceD = getBalanceDefenders();
    }

	// Returns a double in [0, 1] representing the current battle balance for the defenders.
	// E.g. 0.9 means defenders are doing very well.
	public double getBalanceDefenders() {
        double balanceAttackers = getLevelSum(aParties);
        double balanceDefenders = getLevelSum(dParties);

        double total = balanceAttackers + balanceDefenders;
        balanceAttackers = balanceAttackers / total; // balanceA + balanceD = 1
        balanceDefenders = 1-balanceAttackers;

       return balanceDefenders;
    }

    private int getLevelSum(StrictArray<Party> parties) {
        if (parties == null) return 0;
	    int total = 0;
	    for (Party p : parties) {
	       total += p.getTotalLevel();
        }
        return total;
    }

	// Sets an advantage for the defenders, in [0, 1]
	public void setDefensiveAdvantage(double advantage) {
        // TODO this can be used in a siegeOrRaid.
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

    // This is booboo
	public float getAttackingAtk() {
        return getLevelSum(aParties);
    }
	public float getDefendingAtk() {
        return getLevelSum(dParties);
    }

	// Run one round of battle simulation
	public void simulate(float delta) {
        if (isOver) throw new AssertionError();
        // New battle algo
        // wait for x seconds (battle hasn't actually started yet)
        //
        // flip a weighted coin, kill/wound random unit from losing party (including generals)
        // calculate morale of all subparties. if subparty morale < 0, all units in subparty retreat.
        // repeat until no soldiers in party

        // Easy way to decide who to kill:
        //      probability that attacker is killed is based on how well defenders are doing:
        //      if def balance is 0.9, 0.9 chance to kill attacker

        // Kill a soldier in all of the parties for one side.
        boolean killAttacker = Math.random() < currentBalanceD;

        if (killAttacker) {
            // Kill one soldier in every attacking army
            for (Party p : aParties)
                killOne(p, true);
        } else {
            // Kill one soldier in every attacking army
            for (Party p : dParties)
                killOne(p, false);
        }
//        double balanceAttackers = getLevelSum(aParties);
//        double balanceDefenders = getLevelSum(dParties);

//        System.out.println("Attacker strength: " + balanceAttackers);
//        System.out.println("Defender strength: " + balanceDefenders);

        calcBalance();
        checkIfVictory();
    }

    private void checkIfVictory() {
        if (aParties.size == 0) {
            victory(dParties);
        } else if (dParties.size == 0) {
            victory(aParties);
        }
    }

    private void victory(StrictArray<Party> victor) {
        if (kingdom.getMapScreen().getSidePanel().getActivePanel().getClass() == PanelBattle.class &&
                ((PanelBattle) (kingdom.getMapScreen().getSidePanel().getActivePanel())).battle == this)
            kingdom.getMapScreen().getSidePanel().setActiveArmy(kingdom.getPlayer());

        // May be null, if fighting a garrison
        StrictArray<Army> victorArmies = null;
        if (victor == aParties) {
            didAtkWin = true;
            victorArmies = aArmies;
        }
        else if (victor == dParties) {
            didAtkWin = false;
            victorArmies = dArmies;
        }

        victor.shrink();

        if (victorArmies != null) {
            // manage victorious armies and calculate contributions
            for (int i = 0; i < victorArmies.size; i++) {
                Army army = victorArmies.get(i);
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
        }


        if (victoryManager != null) {
            victoryManager.handleVictory(aParties, dParties, didAtkWin);
        }
        destroy();
    }


    // kills/wounds one random troop in this army, weighted by the troop's defense
    private void killOne(Party party, boolean wasInAttackers) {
        Soldier random = party.getRandomWeightedInverseDefense();
		if (random == null) throw new java.lang.AssertionError();

        casualty(random, wasInAttackers);

        if (party.getHealthySize() == 0) {
            System.out.println(party.getName() + " lost all troops and was removed from battle");
            log(party.getName() + " lost all troops and was removed from battle", "red");
            removeParty(party, wasInAttackers);
        }
    }

    // TODO move prisoner management to the end of the battle
    private void removeParty(Party party, boolean wasInAttackers) {
        if (wasInAttackers) {
            if (playerInA) log(party.getName() + " was defeated!", "red");
            else log(party.getName() + " was defeated!", "green");
            for (Soldier s : party.getWounded())
                party.givePrisoner(s, dParties.random());
            for (Soldier s : party.getPrisoners())
                party.returnPrisoner(s, dParties.random());
        }
        else if (dParties.contains(party, true) || dPartiesRet.contains(party,true)) {
            if (playerInD) log(party + " was defeated!", "red");
            else log(party + " was defeated!", "green");
            for (Soldier s : party.getWounded())
                party.givePrisoner(s, aParties.random());
            for (Soldier s : party.getPrisoners())
                party.returnPrisoner(s, aParties.random());
        } else throw new AssertionError();
//        increaseSpoilsForKill(army);

        if (aParties.contains(party, true)) {
            aParties.removeValue(party, true);
            if (party.army != null)
                aArmies.removeValue(party.army, true);
            aPartiesRet.add(party);
        } else if (dParties.contains(party, true)) {
            dParties.removeValue(party, true);
            if (party.army != null)
                dArmies.removeValue(party.army, true);
            dPartiesRet.add(party);
        } else if (aPartiesRet.contains(party, true)) {
           throw new AssertionError();
        } else if (dPartiesRet.contains(party, true)) {
            throw new AssertionError();
        } else throw new AssertionError();

        if (party.army != null) {
            party.army.endBattle();
            party.army.setStopped(false);
            party.army.setVisible(false);

            if (party.army == kingdom.getPlayer()) {
                playerInA = false;
                playerInD = false;
                kingdom.getMapScreen().getSidePanel().setDefault(true);
                this.simulate(.001f);// arbitrary time
            }

            party.army.destroy();
        }
    }

	// If the given army is in this battle, force it to retreat.
	public void forceRetreat(Army army) {
	    // TODO fill or remove
    }
	public void forceRetreatAllAttackers() {
	    // TODO fill or remove?
    }

    // TODO move repeated methods like this to a base class for Battle, that can be shared by both BattleStage and BattleSim
    //
    //  We want something shared between this and battle, because we should have a similar panel system.
    // Put all stats code in the panel class (calculating balance, total atk, etc). Then just have simple methods to
    // access all soldiers in the battle in these classes.

	// Force a casualty of the given soldier.
	public void casualty(Soldier soldier, boolean wasInAttackers) {
        Soldier killer = getRandomKiller(!wasInAttackers);
        boolean killed = soldier.casualty(wasInAttackers, killer, playerInA, playerInD);

        victoryManager.handleCasualty(soldier, wasInAttackers, killed);
    }

    private Soldier getRandomKiller(boolean fromAttackers) {
	    // for now, return null
        return null;
//        StrictArray<Army> armies;
//        if (fromAttackers) armies = aArmies;
//        else armies = dArmies;
//        armies.shrink();
//        // get random playerPartyPanel based on healthy size
//        int totalSize = 0;
//        for (int i = 0; i < armies.size; i++) {
//            totalSize += armies.get(i).party.getHealthySize();
//        }
//
//        int randomIndex = -1;
//        int randomValue = (int) (Math.random() * totalSize);
//
//        for (int i = 0; i < armies.size; ++i)
//        {
//            randomValue -= armies.get(i).party.getHealthySize();
//            if (randomValue <= 0)
//            {
//                randomIndex = i;
//                break;
//            }
//        }
//        // if for some reason can't register this kill, that's ok.
//        if (randomIndex == -1) return null;
//        return armies.get(randomIndex).party.getRandomWeightedAttack();
    }

    public void destroy() {
        if (playerInA || playerInD)
            kingdom.getMapScreen().getSidePanel().setActiveArmy(kingdom.getPlayer());

        if (aArmies != null) {
            aArmies.clear();
            aArmiesRet.clear();
        }

        if (dArmies != null) {
            dArmies.clear();
            dArmiesRet.clear();
        }

        aParties.clear();
        dParties.clear();
        aPartiesRet.clear();
        dPartiesRet.clear();

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

	public boolean playerAttacking() {
	    return playerInA;
    }
	public boolean playerDefending() {
	    return playerInD;
    }

	public boolean isOver() {
	    return isOver;
    }

	public boolean didAttackersWin() {
	    return didAtkWin;
    }

    private void log(String text, String color) {
        if (playerInA || playerInD) // only logs info if Player is in this battle
            BottomPanel.log(text, color);
    }
}
