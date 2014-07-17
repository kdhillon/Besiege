/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Point;
import kyle.game.besiege.army.Army.ArmyType;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.party.PartyType;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Farmer extends Army {
	private final String textureRegion = "Farmer";
	private final float WAIT = 2.5f;
	private final float wanderDistance = 60;
	private final int farmStart = 7;
	private final int farmEnd = 20; 
	private boolean waitToggle;
	
	private int crops;
	
	private Village village;

	public Farmer(Kingdom kingdom, String name, Faction faction,
			float posX, float posY) {
		super(kingdom, name, faction, posX, posY, PartyType.FARMER);
		setTextureRegion(textureRegion);
		crops = 0;
		this.shouldEject = false;
		this.type = ArmyType.FARMER;
		this.passive = true;
	}
	
	@Override
	public void uniqueAct() {
//		if (shouldEject) System.out.println("should eject");
		farm();
	}
	
//	@Override
//	public void drawUniqueInfo(SpriteBatch batch, float parentAlpha) {
//		Kingdom.arial.draw(batch, "Farming around " + village.getName(), getX(), getY() - getOffset()*getKingdom().getZoom());
//	}
	
	@Override
	public String getUniqueAction() {
		return "Farming around " + village.getName();
	}

	@Override
	public void garrisonAct(float delta) {
		if (farmTime() && !isRunning()) {
//			System.out.println("ejecting farmer");
			eject();
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
		if (farmTime()) {
			if (this.isGarrisoned() && this.shouldEject) eject();
			if (this.path.isEmpty()) {
				if (waitToggle) {
					this.waitFor(WAIT);
//					System.out.println("waiting for " + WAIT);
					waitToggle = false;
					crops++;
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
			if (this.getTarget() != village) setTarget(village);
		}
	}
	
	private void setNewFarmDest() {
		Point newTarget;
		int count = 0;
		do {
			float dx = (float) ((Math.random()*2-1)*wanderDistance); //number btw -1 and 1
			float dy = (float) ((Math.random()*2-1)*wanderDistance);
			newTarget = new Point(village.getCenterX() + dx, village.getCenterY() + dy);
			count++;
		}
		while (getKingdom().getMap().isInWater(newTarget) && count < 10); // do this until sets a valid target 
		if (count == 10) System.out.println("count is 10");
		else setTarget(newTarget);
	}
	
	public void setVillage(Village village) {
		this.village = village;
	}
	
	// farmers can garrison in villages
	@Override
	public Location detectNearbyFriendlyCity() {
		for (City city : getKingdom().getCities()) {
			if (!isAtWar(city)) {
				if (this.distToCenter(city) < getLineOfSight() && this.distToCenter(city) < getRunFrom().distToCenter(city)) {
					return city;
				}
			}
		} 
		for (Village village : getKingdom().villages) {
			if (this.distToCenter(village) < getLineOfSight() && this.distToCenter(village) < getRunFrom().distToCenter(village)) {
				return village;
			}
		}
		return null;
	}
	
	public Village getVillage() {
		return village;
	}
	
	@Override
	public void destroy() {
		getKingdom().removeArmy(this);
		this.remove();
		getVillage().removeFarmer(this);
	}
}
