/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Kingdom;
import kyle.game.besiege.location.City;
import kyle.game.besiege.party.PartyType.Type;


public class Merchant extends Army {
//	public static int MIN_WEALTH = 10;
//	public static int MAX_WEALTH = 30;
	public static int MERCHANT_WAIT = 60;
	public static double MERCHANT_WEALTH_FACTOR = .2f; // this is how much more wealth merchants have than regular armies
//	private final double waitTime = 10;
	public City goal;
	
	// if garrisoned not in goal, delete after this.
	// Sometimes merchants get stuck in random cities and can't leave, just kill them after a few hours 
	public float secondsToLive = MERCHANT_WAIT + 65;

	public Merchant() {}
	
	// think of a better merchant system.
	// merchants shouldn't belong to a playerPartyPanel?
	// should there be a "unaligned" faction for units that are independent?
	//		merchants
	//		bandits
	//		nomads
	//		monks/zealots
	// do this later, for now, merchants should belong to factions
	
	// instead, make them more autonomous:
	//		Merchants "think" rationally and will travel routes that provide the most wealth
	//			this is ambiguous without "resources" but could just be rich city to poor city
	//			or they're anchored to one city/village and must decide where else to trade (richest nearby city)
	
	// wealth flows from villages to cities:
	// 
	
	public Merchant(Kingdom kingdom,
			City defaultTarget, City goal) {
		super(kingdom, "Merchant of " + defaultTarget.getName(), defaultTarget.getFaction(), defaultTarget.getCenterX(), defaultTarget.getCenterY(), Type.MERCHANT, defaultTarget);
		this.type = ArmyType.MERCHANT;
		this.passive = true;
		
		this.goal = goal;
		this.calcMerchantWealth();
		this.setDefaultTarget(defaultTarget);
	}
	
	@Override
	public void postAdd() {
		super.postAdd();
		this.setGoal(goal);
	}

//	@Override
//	public void detectNearby() {
//		//naive approach (N^2)
//		Army army = closestHostileArmy();
//		if (army != null) {
//			if (shouldRunFrom(army)) runFrom(army);
//			//else if (shouldAttack(army)) setTarget(army);
//			// Doesn't attack
//		}
//	}
//
	
//	@Override
//	public void garrisonIn(Location city) {
//		if (city == goal) {
//			waitFor(waitTime);
//			runFrom(null);
//			city.garrison(this); 
//			setGarrisonedIn(city);
//		}
//		else {
//			// test to see if should garrison goes here
//			waitFor(3); //arbitrary
//			runFrom(null);
//			city.garrison(this); 
//			setGarrisonedIn(city);
//		}
//	}
	
	@Override
	public void garrisonAct(float delta) {
		if (this.isWaiting()) this.wait(delta);
	}
	
	
	@Override
	public void wait(float delta) {
		if (getKingdom().clock() >= getWaitUntil()) {
			super.wait(delta);
			if (isGarrisonedIn(goal)) {
				deposit();
			}
			
//			else {
//				this
//			}
//			System.out.println(this.getName() + " is done waiting");
//			setWaiting(false);
////			setWaitUntil(0);
//			setForceWait(false);
		}
		this.secondsToLive -= delta;
		if (secondsToLive < 0) {
			this.destroy();
		}
	}
	
	public void calcMerchantWealth() {
		this.getParty().wealth = (int) (Merchant.MERCHANT_WEALTH_FACTOR * this.distToCenter(goal) * Army.WEALTH_FACTOR);
//		System.out.println("merchant wealth: " + this.getParty().wealth);
	}
	
	public void deposit() {
		goal.addWealth(getParty().wealth);
		this.destroy();
	}
	
	@Override
	public void destroy() {
		//System.out.println("removing merchant " + this.getName() + " from " + this.getGarrisonedIn().getName());
		if (isGarrisoned())
			goal.eject(this);
		super.destroy();
		if (getKingdom() != null)
			getKingdom().removeArmy(this);
		this.remove();
		if (getDefaultTarget() != null) {
			((City) getDefaultTarget()).removeMerchant(this);
		}
	}
	@Override
	public void uniqueAct() {
//		System.out.println(getName() + " setting next target");
		if (this.path.isEmpty())
			setTarget(goal);
	}
	public void setGoal(City goal) {
		this.goal = goal;
		newTarget(goal);
	}
	public void returnHome() {
		this.goal = (City) this.getDefaultTarget();
		this.setTarget(this.getDefaultTarget());
	}
	public City getGoal() {
		return goal;
	}
}
