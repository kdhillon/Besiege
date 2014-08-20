/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Map;
import kyle.game.besiege.Point;
import kyle.game.besiege.location.Castle;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.Panel;
import kyle.game.besiege.party.PartyType;

public class Patrol extends Army {
 	private final static int PATROL_TRAVEL_FACTOR = 2;

	private final String textureRegion = "KnightHorse";
	public float patrolDist;
	// frequency with which patrol will stay near base (0 to 1)
	public double proximityToBase;
	private Location patrolAround;

	public Patrol() {}
	
	public Patrol(Kingdom kingdom, Location defaultTarget) {
		super(kingdom, defaultTarget.getName() + " Patrol", defaultTarget.getFaction(), defaultTarget.getCenterX(), defaultTarget.getCenterY(), PartyType.PATROL);
		this.setDefaultTarget(defaultTarget);
		this.patrolAround = null;
		setTextureRegion(textureRegion);
		this.type = ArmyType.PATROL;
		patrolDist = this.getLineOfSight()*PATROL_TRAVEL_FACTOR;
		//System.out.println("Patrol dist is " + patrolDist);
	}
	
	@Override
	public void uniqueAct() {
		if (!isRunning())
			patrol();
//		else System.out.println(getName() + " is running");
	}

	@Override
	public String getUniqueAction() {
		return "Patrolling around " + patrolAround.getName() + " (Speed: " + Panel.format(getSpeed()*SPEED_DISPLAY_FACTOR+"", 2)+")";
	}
	
	public void patrol() {
		if (path.isEmpty()) { //key
			// create new patrol target
			//TODO inefficient
//			System.out.println(getName() + " getting new patrol target");

			Point newTarget;
			
			// randomly choose between either point near army or near city, to randomize motion
			float center_x;
			float center_y;
			
			if (Math.random() < proximityToBase) {
				center_x = patrolAround.getCenterX();
				center_y = patrolAround.getCenterY();
			} else {
				center_x = this.getCenterX();
				center_y = this.getCenterY();
			}
			
			do {
				float dx = (float) ((Math.random()*2-1)*patrolDist); //number btw -1 and 1
				float dy = (float) ((Math.random()*2-1)*patrolDist);
				newTarget = new Point(center_x + dx, center_y + dy);
			} while (getKingdom().getMap().isInWater(newTarget)); 
			setTarget(newTarget);// System.out.println(" patrol set bad water targe");;
		}
//		else 
//			System.out.println(getName() + " has target so not patrolling");
	}
	
	public void patrolAround(Location city) {
		patrolAround = city;
	}
	public void stopPatrolling() {
		System.out.println(getName() + " stopping patrollin");
		patrolAround = null;
		findTarget();
	}
	
	@Override
	public void destroy() {
		getKingdom().removeArmy(this);
		this.remove();
		if (getDefaultTarget() != null) {
			Location defaultCity = (Location) getDefaultTarget();
			defaultCity.removePatrol(this);
		}
	}
}
