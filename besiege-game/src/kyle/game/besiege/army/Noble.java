/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import com.badlogic.gdx.math.MathUtils;

import kyle.game.besiege.Kingdom;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.PartyType.Type;

// Noble should become "warchief"
public class Noble extends Army {
    // TODO change this to be different for each culture
    // Also, doesn't really make sense anymore, since there isn't a feudal structure for inheritance
    // Let's brainstorm what a tribe's war parties are going to look like:
    //
    // TODO add a lot of soldier types for commoners.

    // Mesoamerican:
    //      Central ruler, emperor. Probably has a big army at his command at all times.
    //      Nobles (warchiefs) will have their own armies and do their own raids.
    //          Titles of warchiefs might be irrelevant to their experience. Once you become a war chief you can get a cool title.
    //          Names can be in english and derived from their culture.
    //          Aztec Examples: "Cutter of Men". "High General". "Master of Atlatl", "
    //      Smaller raiding parties will attack enemy villages and enemy parties, and return home when they've raided
    //      1-2 enemies.

//	private static final int[] REKNOWN_RANK = {0,    50,      100,     150,    200, 	  250,		 300, 301};
//	private static final String[] RANKS = {"Baron", "Earl", "Count", "Duke", "Prince", "Archduke", "King"};

	int rank;
	int nextRank;
	public Location home;

	// Title is maintained in General
//	private String title; 	// "earl of x", "bandit warlord"
	
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
		super(kingdom, "", home.getFaction(), home.getSpawnPoint().getX(), home.getSpawnPoint().getY(), Type.NOBLE, home);
		this.setDefaultTarget((City) home);
		// set up initial playerPartyPanel, rank, etc
		rank = 0;

		kingdom.addArmy(this);
		this.type = ArmyType.NOBLE;
		
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

		
//		if (title == null || title.equals("")) throw new java.lang.AssertionError(this.getName() + " reknown : " + this.getFame() + " " + title);

		this.updateName();
	}
	
	// generate the initial soldier for this 
//	public General generateFreshGeneral(Location home) {
//		UnitType type = this.playerPartyPanel.
//		
//		General general = new General(type, this.playerPartyPanel, home);
//		
//		// earl, baron, or count
//		general.giveReknown(MathUtils.random(50));
//		general.giveReknown(MathUtils.random(50));
//		general.giveReknown(MathUtils.random(50));
//		return general;
//	}

	@Override
	public void garrisonAct(float delta) {
		super.garrisonAct(delta);

//		System.out.println(this.getName() + " is garrison acting: " + isGarrisoned() + " safe: " + safeToEject() + " hasTarget " + hasTarget());
		if (isGarrisoned() && safeToEject() && !hasTarget()) {
//			System.out.println(this.getName() + " is unique acting");
			uniqueAct();
		}
		// May be garrisoned because it's between cities?
		// Also will have to do this with farmers?


//		if (this.getFaction().cities.size > 1) {
//		Army army = closestHostileArmy();
//		// only eject for special reasons
//		if (army != null && shouldAttack(army.party) && !isRunning()) {
//			setTarget(army);
//
//			if (isGarrisoned())
//				eject();
//		} else {
////						System.out.println("Unique acting: " + this.getName());
//			clearCurrentTarget();
//			// Just unique act for christ's sake. get out and do something!
//			uniqueAct();
//		}

//				}
	}

	// Finds a target
	@Override
	public void uniqueAct() {
		if (hasTarget()) {
//			System.out.println(getName() + " already has target");
			return;
		}


		// TESTING
//		if (getKingdom().map.isInWater(this)) {
//			System.out.println(this.getName() + " is in water!");
//		}

		// nobles do: 
		// travel between their own cities (by default)
		// or are sent to besiege other cities (by faction)
		if (shouldRepair()) {
//			System.out.println(getName() + " only one city");
			this.returnHome();
			if (!hasTarget()) throw new AssertionError();
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

			manageSpecialTarget();
			if (!hasTarget()) throw new AssertionError();
		}
		else if (getFaction().cities.size > 1) {
			if (getKingdom().currentPanel == this) System.out.println(getName() + " wandering between cities");

			wanderBetweenCities();

			if (!hasTarget()) throw new AssertionError();
		}
		else {
//			System.out.println(getName() + " only one city");
			// wait in city
			if (!isGarrisoned()) {
				returnHome();
				if (!hasTarget()) throw new AssertionError();
			}
		}
		if (!isGarrisoned() && !hasTarget()) throw new AssertionError();
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
				if (shouldRaidOrBesiege(specialTargetToBesiege))
					setTarget(specialTargetToBesiege);
				else {
					returnHome();
				}
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
	
//	public String getTitle() {
//		if (this.home != null) {
//			return this.title + " of " + this.home.getName();
//		}
//		return this.title;
//	}

	// Returns a more official sounding name (Commander X, etc).
	public String getOfficialName() {
		return this.getGeneral().getOfficialName();
	}

	public String getTitle() {
		return getGeneral().getRank();
	}

	public void updateHome(Location home) {
		this.home = home;
	}

//	public void updateRank() {
////		System.out.println("fame of this noble is : " + this.getGeneral().getFame());
//        if (this.getGeneral() == null) {
//            		System.out.println("noble has no general? : " + this.getName());
//            		throw new AssertionError();
//        }
//		while (this.getGeneral().getFame() >= nextRank) {
//			if (rank <= RANKS.length - 1) {
//				increaseRank();
////				System.out.println("increasing rank to: " + this.rank);
//			}
//		}
//		//		calcMaxPartySize();
//	}
//
//	private void increaseRank() {
//		this.rank++;
//		updateTitle(RANKS[rank]);
//		nextRank = REKNOWN_RANK[rank+1];
//	}
//
//	public static String getTitleForFame(int fame) {
//		for (int i = 1; i < REKNOWN_RANK.length; i++) {
//			if (REKNOWN_RANK[i] > fame) return RANKS[i-1];
//		}
//		return "God";
//	}
	
	// TODO move this stuff to General class?
//	public void updateTitle(String title) {
//		this.title = getTitleForFame(this.getGeneral().getFame());
//	}

	@Override 
	public void besiege(Location location) {
		super.besiege(location);
//		System.out.println(this.getName() + " is besieging " + location.getName());
	}
	
	public void returnHome() {
//		System.out.println(this.getName() + " returning home");
		if (getHome() == null) {
			updateHome(this.getFaction().getRandomLocation());
		}
		if (getHome() == null) this.destroy();
		this.specialTargetToBesiege = null;
		this.getFaction().unoccupiedNobles.removeValue(this, true);
		setTarget(getHome());
	}

	public void wanderBetweenCities() {
		clearAllTargets();

		// I think this is the problem
		if (this.path.isEmpty()) {
			goToNewTarget();
			toggleWait = true;
			//			System.out.println("doesn't have target and is waiting? " + this.isWaiting() + " and is garrisoned? " + isGarrisonedSafely());
			//				System.out.println("starting to wait");

//			System.out.println(this.getName() + "getting new target");
			if (!hasTarget()) throw new AssertionError();
		}
		// make sure not going to enemy city - weird glitch
		else {
			throw new AssertionError();
//			System.out.println(getName() + " has path");
//			if (this.path.nextGoal != null && this.path.nextGoal.getType() == Destination.DestType.LOCATION && this.path.nextGoal.getFaction().atWar(this.getFaction()))
//				goToNewTarget();
		}
	}

	public void goToNewTarget() {
		Location newTarget;
		do {
			newTarget = this.getFaction().getRandomLocation();
		}
		while (this.getGarrisonedIn() == newTarget);

		if (newTarget != null) {
			if (setTarget(newTarget)) {
				if (!hasTarget()) throw new AssertionError();
			}
		} else {
			System.out.println(getName() + " wanderBetweenCities target is " +
					"null");
			if (!hasTarget()) throw new AssertionError();
		}
	}
//		else System.out.println("new target is garrisoned in");


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
		this.setName(getOfficialName());
//		this.setName(title + " of " + home.getName());
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
