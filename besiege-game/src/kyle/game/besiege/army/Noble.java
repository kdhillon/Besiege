/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.PartyType.Type;

import com.badlogic.gdx.math.MathUtils;

public class Noble extends Army {
	private static final String[] RANKS = {"Baron", "Earl", "Count", "Duke", "Prince", "Archduke", "King"};
	private static final int[] REKNOWN_RANK = {0,    50,      100,     150,    200, 	  250,		 300, 301};
	
	// change for testing
	private static final int BASE_PC = 350; // base party count

//	private static final int BASE_PC = 35; // base party count
	private static final float REKNOWN_PC_FACTOR = .5f;
	private final float WAIT = 30;
	//	private static final int MAX_LEVEL = 25;
	public Location home;
	public String rankName;
	public int rank;
	private int reknown;
	private int nextRank;
	public Location specialTarget;
	private boolean toggleWait;

	//	private int level;

	public Noble() {
		// for loading
	}
	
	public Noble(Kingdom kingdom, Location home) {
		// TODO change this bakc
		super(kingdom, "", home.getFaction(), home.getCenterX(), home.getCenterY(), Type.NOBLE);
		this.home = home;
		this.setDefaultTarget((City) home);
		// set up initial party, rank, etc
		rank = 0; // baron for now
		reknown = 0;
		rankName = RANKS[rank];
		nextRank = REKNOWN_RANK[rank + 1];
		updateName();

		String region = "knightFlail";
		double random = Math.random();
		if (random >= .33) region = "knightLance";
		if (random > .67) region = "knightSword";
		this.setTextureRegion(region);

		
		// earl, baron, or count
		this.giveReknown(MathUtils.random(50));
		this.giveReknown(MathUtils.random(50));
		this.giveReknown(MathUtils.random(50));

		//		System.out.println("creating noble");
		kingdom.addArmy(this);
		this.type = ArmyType.NOBLE;
		
		this.calcMaxPartySize();
	}

	@Override
	public void uniqueAct() {

		// nobles do: 
		// travel between their own cities (by default)
		// or are sent to besiege other cities (by faction)
		if (this.hasSpecialTarget() && !this.isGarrisonedIn(specialTarget)) {
			if (getKingdom().currentPanel == this) System.out.println(getName() + " has special target");

			//			System.out.println(getName() + " managing special target " + this.specialTarget.getName());
			// go to city to besiege/raid

			if (this.isGarrisoned()) this.eject();
			manageSpecialTarget();
		}
		else if (getFaction().cities.size > 1) {
			if (getKingdom().currentPanel == this) System.out.println(getName() + " wandering between cities");

			wanderBetweenCities();
		}
		else {
			//System.out.println(getName() + " only one city");
			// wait in city
		}
	}

	@Override
	public void garrisonIn(Location location) {
		super.garrisonIn(location);
		this.waitFor(randomWait());
	}

	private float randomWait() {
		return MathUtils.random(0, WAIT);
	}

	// some problem where noble detects path to new target (friendly city), but still travels to enemy city (perhaps old special target)
	public void manageSpecialTarget() {
		if (this.path.isEmpty() || this.getTarget() != specialTarget) {
			if (specialTarget != null && this.getTarget() != specialTarget) {
				setTarget(specialTarget);
				//System.out.println(getName() + " setting special target " + specialTarget.getName());
			//	path.travel();
			}
			else {
				//System.out.println(getName() + " special target is null");
				//path.travel();
			}
		}
		else {
//			System.out.println(getName() + " path is not empty");
			//path.travel();
			// don't travel here. that's taken care of elsewhere
			// never travel in Unique act!
		}
	}

	@Override 
	public void besiege(Location location) {
		super.besiege(location);
//		System.out.println(this.getName() + " is besieging " + location.getName());
	}

	public void wanderBetweenCities() {
		// I think this is the problem
		if (this.path.isEmpty()) {
			goToNewTarget();
			toggleWait = true;
			//			System.out.println("doesn't have target and is waiting? " + this.isWaiting() + " and is garrisoned? " + isGarrisoned());
			//				System.out.println("starting to wait");

			//System.out.println(this.getName() + "getting new target");
		}
		// make sure not going to enemy city - weird glitch
		else {
//			System.out.println(getName() + " has path");
			if (this.path.nextGoal != null && this.path.nextGoal.getType() == Destination.DestType.LOCATION && this.path.nextGoal.getFaction().atWar(this.getFaction()))
				goToNewTarget();
		}
	}

	public void goToNewTarget() {
		Location newTarget = this.getFaction().getRandomCity();
		if (this.getGarrisonedIn() != newTarget) {
			if (newTarget != null)
				setTarget(newTarget);
			else {
//				System.out.println(getName() + " wanderBetweenCities target");
			}
		}
//		else System.out.println("new target is garrisoned in");
	}

	@Override
	public String getUniqueAction() {
		return "Noble unique action";
	}

	public void giveReknown(int reknown) {
		this.reknown += reknown;
		if (rank <= RANKS.length - 1) {
			if (this.reknown >= nextRank) {
				increaseRank();
			}
		}
		calcMaxPartySize();
	}
	
	public void calcMaxPartySize() {
		getParty().maxSize = (int) (reknown * REKNOWN_PC_FACTOR + BASE_PC);
	}
	
	private void increaseRank() {
		this.rank++;
		rankName = RANKS[rank];
		nextRank = REKNOWN_RANK[rank+1];
		updateName();
	}
	public void updateName() {
		this.setName(rankName + " of " + home.getName());
	}
	// for when their old estate no longer belongs to their kingdom.
	public void giveNewHome() {

	}
	@Override
	public void nextTarget() {
		super.nextTarget();
		//		System.out.println(this.getTarget().getName() + " is new target of " + this.getName());
	}
	public void setSpecialTarget(Location specialTarget) {
		this.specialTarget = specialTarget;
	}
	public boolean hasSpecialTarget() {
		return specialTarget != null;
	}
	@Override 
	public void leaveSiege() {
		super.leaveSiege();
//		this.specialTarget = null;
	}
	
	public static String getTitleForFame(int fame) {
		for (int i = 1; i < REKNOWN_RANK.length; i++) {
			if (REKNOWN_RANK[i] > fame) return RANKS[i-1];
		}
		return "God";
	}
}
