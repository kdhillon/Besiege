/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Point;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.party.PartyType.Type;

public class Farmer extends Army {
	private final String textureRegion = "Farmer";
	private final float FARMER_WAIT = 2.5f;
	private final float wanderDistance = 60;
	private final int farmStart = 7;
	private final int farmEnd = 20; 
	private boolean waitToggle;

	private Location location;

	public Farmer() {}
	
	public Farmer(Kingdom kingdom, String name, Faction faction,
			float posX, float posY) {
		super(kingdom, name, faction, posX, posY, Type.FARMERS, null);
//		this.shouldEject = false;
		this.type = ArmyType.FARMER;
		this.passive = true;
	}
	
	@Override
	public void uniqueAct() {
//		if (shouldEject) System.out.println("should eject");
		farm();
	}

	public void resetWaitToggle() {
		this.waitToggle = false;
	}
	
//	@Override
//	public void drawUniqueInfo(SpriteBatch batch, float parentAlpha) {
//		Kingdom.arial.draw(batch, "Farming around " + village.getName(), getX(), getY() - getOffset()*getKingdom().getZoom());
//	}
	
	@Override
	public String getUniqueAction() {
		return "Farming";
	}

	@Override
	public void garrisonAct(float delta) {
//		shouldStopRunning();
		detectNearby();
		if (farmTime() && shouldStopRunning()) {
			//System.out.println("ejecting " + this.getName());
			// double check to see if fixes problem
//			detectNearby();
//			if (shouldStopRunning())
				eject();
				setNewFarmDest();
		}
//		else System.out.println(runFrom.getName());
	}
	
	/** return true if time to farm, false otherwise
	 * 
	 * @return
	 */
	private boolean farmTime() {
		return getKingdom().getTime() >= farmStart && getKingdom().getTime() <= farmEnd;
	}
	
	public void farm() {
		if (isRunning()) throw new AssertionError();
		if (farmTime()) {
			if (this.isGarrisoned()) eject();
			if (!this.hasTarget()) {
				if (waitToggle) {
					this.waitFor(FARMER_WAIT);
//					System.out.println("waiting for " + FARMER_WAIT);
					waitToggle = false;
				}
				else {
					setNewFarmDest();
					waitToggle = true;
				}
			}
			else {
				path.travel();
			}
		}
		else {
			if (this.getTarget() != location) setTarget(location);
		}
	}
	
	private void setNewFarmDest() {
		Point newTarget;
		int count = 0;
		do {
			float dx = (float) ((Math.random()*2-1)*wanderDistance); //number btw -1 and 1
			float dy = (float) ((Math.random()*2-1)*wanderDistance);
			newTarget = new Point(location.getCenterX() + dx, location.getCenterY() + dy);
			count++;
		}
		while (getKingdom().getMap().isInWater(newTarget) && count < 10); // do this until sets a valid target 
		if (count == 10) {
			if (location == null) throw new AssertionError();
			setTarget(location);
			System.out.println("farmer just searched for 10 targets and couldn't find valid");
		}
		else {
			setTarget(newTarget);
		}
		if (!hasTarget()) throw new AssertionError();
	}
	
	public void setLocation(Location location) {
		this.location = location;
//		if (getKingdom().night)
		// Start garrisoned for efficiency.
		this.garrisonIn(location);

	}
	
	// farmers can garrison in villages
//	@Override
//	public Location detectNearbyFriendlyLocationForRunning() {
//		for (City city : getKingdom().getCities()) {
//			if (!isAtWar(city)) {
//				double dist = this.distToCenter(city);
//				if (dist < getLineOfSight() && dist < getRunFrom().distToCenter(city)) {
//					return city;
//				}
//			}
//		}
//		for (Village village : getKingdom().villages) {
//			double dist = this.distToCenter(village);
//			if (dist < getLineOfSight() && dist < getRunFrom().distToCenter(village)) {
//				return village;
//			}
//		}
//		return null;
//	}
	
	public Location getLocation() {
		return location;
	}

	@Override
	public boolean canHideInVillage() {
		return true;
	}

	@Override
	public void destroy() {
		getKingdom().removeArmy(this);
		this.remove();
		getLocation().removeFarmer(this);
	}
}
