/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.army.Army.ArmyType;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.PartyType;


public class Merchant extends Army {
//	public static int MIN_WEALTH = 10;
//	public static int MAX_WEALTH = 30;
	public static int MERCHANT_WAIT = 60;
	public static double MERCHANT_WEALTH_FACTOR = .2f; // this is how much more wealth merchants have than regular armies
//	private final double waitTime = 10;
	private final String textureRegion = "Merchant";
	public City goal;

	public Merchant() {}
	
	public Merchant(Kingdom kingdom,
			City defaultTarget, City goal) {
		super(kingdom, "Merchant of " + defaultTarget.getName(), defaultTarget.getFaction(), defaultTarget.getCenterX(), defaultTarget.getCenterY(), PartyType.MERCHANT);
		this.setTextureRegion(textureRegion);
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
			if (isGarrisonedIn(goal)) {
				deposit();
			}
			setWaiting(false);
			setWaitUntil(0);
			setForceWait(false);
		}
	}
	
	public void calcMerchantWealth() {
		this.getParty().wealth = (int) (Merchant.MERCHANT_WEALTH_FACTOR * this.distToCenter(goal) * Army.WEALTH_FACTOR);
	}
	
	public void deposit() {
		goal.getParty().wealth += getParty().wealth;
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
