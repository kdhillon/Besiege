/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import com.badlogic.gdx.math.MathUtils;

import kyle.game.besiege.Destination;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.PartyType.Type;

public class Noble extends Army {
	private static final int[] REKNOWN_RANK = {0,    50,      100,     150,    200, 	  250,		 300, 301};
	private static final String[] RANKS = {"Baron", "Earl", "Count", "Duke", "Prince", "Archduke", "King"};

	int rank;
	int nextRank;
	public Location home;
	public String title; 	// "earl of x", "bandit warlord"
	
	
	private final float WAIT = 30;
	//	private static final int MAX_LEVEL = 25;
//	public Location home;
//	public String rankName;
//	public int rank;
//	private int reknown;
//	private int nextRank;
	public Location specialTargetToBesiege;
	private boolean toggleWait;

	//	private int level;

	public Noble() {
		// for loading
	}
	
	public Noble(Kingdom kingdom, Location home) {
		// TODO change this bakc
		super(kingdom, "", home.getFaction(), home.getSpawnPoint().getX(), home.getSpawnPoint().getY(), Type.NOBLE);
		this.setDefaultTarget((City) home);
		// set up initial party, rank, etc
		rank = 0; // baron for now
//		reknown = 0;
		updateTitle(RANKS[rank]);
		nextRank = REKNOWN_RANK[rank + 1];
//		System.out.println("next rank: " + nextRank);
		
		String region = "knightFlail";
		double random = Math.random();
		if (random >= .33) region = "knightLance";
		if (random > .67) region = "knightSword";
		this.setTextureRegion(region);

		//		System.out.println("creating noble");
		kingdom.addArmy(this);
		this.type = ArmyType.NOBLE;
		
		updateRank();
		updateHome(home);

		// TODO make this based on fame general already has.
		// this is key
//		updateRank(MathUtils.random(100));
//		updateRank(MathUtils.random(100));
//		updateRank(MathUtils.random(100));
//		giveReknown(MathUtils.random(100));
	
//		giveReknown(MathUtils.random(50));
//		giveReknown(MathUtils.random(50));
//		giveReknown(MathUtils.random(50));
//		giveReknown(MathUtils.random(50));
		
		
		if (title == null || title.equals("")) throw new java.lang.AssertionError(this.getName() + " reknown : " + this.getFame() + " " + title);
		
		this.updateName();
	}
	
	// generate the initial soldier for this 
//	public General generateFreshGeneral(Location home) {
//		UnitType type = this.party.
//		
//		General general = new General(type, this.party, home);
//		
//		// earl, baron, or count
//		general.giveReknown(MathUtils.random(50));
//		general.giveReknown(MathUtils.random(50));
//		general.giveReknown(MathUtils.random(50));
//		return general;
//	}

	@Override
	public void uniqueAct() {
		
		// TESTING
//		if (getKingdom().map.isInWater(this)) {
//			System.out.println(this.getName() + " is in water!");
//		}
		
		// nobles do: 
		// travel between their own cities (by default)
		// or are sent to besiege other cities (by faction)
		if (shouldRepair()) {
			this.returnHome();
		}
		else if (this.hasSpecialTarget() && !this.isGarrisonedIn(specialTargetToBesiege)) {
			if (getKingdom().currentPanel == this) System.out.println(getName() + " has special target");
			
			// If for some reason at peace with special target, stop going towards it.
			// This is slow, find a better way to do this.
			if (!this.specialTargetToBesiege.getFaction().atWar(this.getFaction())) {
//				System.out.println("WHY IS THIS HAPPENING (nOBLE)");
				this.getFaction().endTask(this);
			}

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
		if (this.path.isEmpty() || this.getTarget() != specialTargetToBesiege) {
			if (specialTargetToBesiege != null && this.getTarget() != specialTargetToBesiege) {
				setTarget(specialTargetToBesiege);
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
	
	public String getTitle() {
		if (this.home != null) {
			return this.title + " of " + this.home.getName();			
		}
		return this.title;
	}

	public void updateHome(Location home) {
		this.home = home;
	}
	
	public void updateRank() {
//		System.out.println("fame of this noble is : " + this.getGeneral().getFame());
		while (this.getGeneral().getFame() >= nextRank) {
			if (rank <= RANKS.length - 1) {
				increaseRank();
//				System.out.println("increasing rank to: " + this.rank);
			}
		}
		//		calcMaxPartySize();
	}

	private void increaseRank() {
		this.rank++;
		updateTitle(RANKS[rank]);
		nextRank = REKNOWN_RANK[rank+1];
	}

	public static String getTitleForFame(int fame) {
		for (int i = 1; i < REKNOWN_RANK.length; i++) {
			if (REKNOWN_RANK[i] > fame) return RANKS[i-1];
		}
		return "God";
	}
	
	// TODO move this stuff to General class?
	public void updateTitle(String title) {
		this.title = getTitleForFame(this.getGeneral().getFame());
	}

	@Override 
	public void besiege(Location location) {
		super.besiege(location);
//		System.out.println(this.getName() + " is besieging " + location.getName());
	}
	
	public void returnHome() {
//		System.out.println(this.getName() + " returning home");
		if (getHome() == null) {
			updateHome(this.getFaction().getRandomCity());
		}
		if (getHome() == null) this.destroy();
		this.specialTargetToBesiege = null;
		this.getFaction().unoccupiedNobles.removeValue(this, true);
		setTarget(getHome());
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
	
	public int getFame() {
		return this.party.getGeneral().getFame();
	}
	
//	public void calcMaxPartySize() {
//		getParty().getMaxSize();
//	}
	
	public void updateName() {
		this.setName(title + " of " + home.getName());
	}
	
	@Override
	public void nextTarget() {
		super.nextTarget();
		//		System.out.println(this.getTarget().getName() + " is new target of " + this.getName());
	}
	public void setSpecialTarget(Location specialTarget) {
		assert(specialTarget == null || this.getFaction().atWar(specialTarget.getFaction()));
		this.specialTargetToBesiege = specialTarget;
	}
	public boolean hasSpecialTarget() {
		return specialTargetToBesiege != null;
	}
	@Override 
	public void leaveSiege() {
		super.leaveSiege();
//		this.specialTarget = null;
	}
	
	public Location getHome() {
		return this.home;
	}
	public void setHome(Location home) {
		this.updateHome(home);
	}
}
