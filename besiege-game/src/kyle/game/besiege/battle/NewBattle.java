//package kyle.game.besiege.battle;
//
//import kyle.game.besiege.Faction;
//import kyle.game.besiege.StrictArray;
//import kyle.game.besiege.army.Army;
//import kyle.game.besiege.party.Party;
//import kyle.game.besiege.party.Soldier;
//
//public class NewBattle implements Battle {
//
//	// Adds the given party to the list of attackers. Disables the army until they leave the battle or are destroyed.
//	// Returns true if party was added successfully, false otherwise.
//	public boolean addToAttackers(Army army) {
//		return true;
//	}
//	
//	public boolean addToDefenders(Army army) {
//		return true;
//	}
//	
//	public StrictArray<Army> getAttackingArmies() {
//		
//	}
//	public StrictArray<Army> getDefendingArmies();
//	public StrictArray<Party> getAttackingParties();
//	public StrictArray<Party> getDefendingParties();
//	public StrictArray<Party> getAttackingRetreatingParties();
//	public StrictArray<Party> getDefendingRetreatingParties();
//
//	public boolean shouldJoinAttackers(Army army);
//    public boolean shouldJoinDefenders(Army army);	
//    
//	// Returns a double in [0, 1] representing the current battle balance for the defenders. 
//	// E.g. 0.9 means defenders are doing very well.
//	public double getBalanceDefenders();
//	public void updateBalance();
//	
//	// Sets an advantage for the defenders, in [0, 1]
//	public void setDefensiveAdvantage(double advantage);
//	
//	// Note that this should not change throughout the duration of the battle. 
//	public Faction getAttackingFaction();
//	// Note that this should not change
//	public Faction getDefendingFaction();
//	
//	public float getAttackingAtk();
//	public float getDefendingAtk();
//
//	// Run one round of battle simulation
//	public void simulate(float delta);
//	
//	// If the given army is in this battle, force it to retreat.
//	public void forceRetreat(Army army);
//	public void forceRetreatAllAttackers();
//	
//	// Force a casualty of the given soldier. Used mostly by battlestage.
//	public void casualty(Soldier soldier, boolean atkDead);
//	
//	public boolean playerAttacking();
//	public boolean playerDefending();
//	
//	public boolean isOver();
//	public boolean didAttackersWin();
//}
