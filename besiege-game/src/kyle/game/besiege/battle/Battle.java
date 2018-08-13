package kyle.game.besiege.battle;

import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

// Represents a battle between two or more parties.
// Battles are in "real-time" and should support adding parties to one of the two sides (attackers and defenders).
// If a battle involves the player, a PlayerBattle is used to represent the battle. This special type of battle interface
// behaves as if the battle takes place instantaneously.
// If a player interrupts an ongoing battle, they will convert it into a PlayerBattle. If a player leaves a PlayerBattle,
// it is converted back into a normal battle. 
// Once a non-player army enters a battle, it is essentially *passing control* of itself to the battle. Depending on the 
// battle implementation, it may be destroyed or retreat without control over that.
public interface Battle {	
	// TODO remove all armies, just use parties.
	
	// Adds the given playerPartyPanel to the list of attackers. Disables the army until they leave the battle or are destroyed.
	// Returns true if playerPartyPanel was added successfully, false otherwise.
	public boolean addToAttackers(Army army);
//	
//	// Adds the given playerPartyPanel to the list of defenders. Disables the army until they leave the battle or are destroyed.
//	// Returns true if playerPartyPanel was added successfully, false otherwise.
	public boolean addToDefenders(Army army);
	
	public StrictArray<Party> getAttackingParties();
	public StrictArray<Party> getDefendingParties();
	public StrictArray<Party> getAttackingRetreatingParties();
	public StrictArray<Party> getDefendingRetreatingParties();

	public boolean shouldJoinAttackers(Army army);
    public boolean shouldJoinDefenders(Army army);	
    
	// Returns a double in [0, 1] representing the current battle balance for the defenders. 
	// E.g. 0.9 means defenders are doing very well.
	public double getBalanceDefenders();

	// Sets an advantage for the defenders, in [0, 1]
	public void setDefensiveAdvantage(double advantage);
	
	// Note that this should not change throughout the duration of the battle. 
	public Faction getAttackingFactionOrNull();
	// Note that this should not change
	public Faction getDefendingFactionOrNull();
	
	public float getAttackingAtk();
	public float getDefendingAtk();

	// Run one round of battle simulation
	public void simulate(float delta);
	
	// If the given army is in this battle, force it to retreat.
	public void forceRetreat(Army army);
	public void forceRetreatAllAttackers();
	
	// Force a casualty of the given soldier. Used mostly by battlestage.
	public void casualty(Soldier soldier, boolean atkDead);

	public boolean playerAttacking();
	public boolean playerDefending();
	
	public boolean isOver();
	public boolean didAttackersWin();
}
