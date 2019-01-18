/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.*;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.PartyType;

public class Bandit extends Army {
	private static final int RAID_VILLAGE_SIZE = 10; // TODO make this higher.

	private static final double NEARBY_VILLAGE_MIN_DIST = 100; // TODO is this a reasonable number?

	private Village villageToAttack;

	public Bandit() {}
	
	public Bandit(Kingdom kingdom, String name,
			float posX, float posY, Faction faction) {
		super(kingdom, name, faction, posX, posY, PartyType.Type.BANDIT, null);
		
		this.setDefaultTarget(new Point(posX, posY));
		this.type = ArmyType.BANDIT;
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
	}
	
	@Override
	public void uniqueAct() {
		if (!this.hasTarget()) { //key
			if (shouldAttackNearbyVillage()) {
				villageToAttack = getNearbyVillageToAttack();
				if (villageToAttack != null && villageToAttack.garrison.getHealthySize() > 0) {
					System.out.println("Bandit is gonna go raid village: " + villageToAttack.getName());
					if (this.setTarget(villageToAttack));
						return;
				}
			} else {
				villageToAttack = null;
			}

			Point newTarget;
			do {
//			 create new random target
				float dx = Random.getRandomInRange(-getLineOfSight(), getLineOfSight()); //number btw -1 and 1

				float dy = Random.getRandomInRange(-getLineOfSight(), getLineOfSight()); //number btw -1 and 1
				newTarget = new Point(getCenterX() + dx, getCenterY() + dy);
			} while (getKingdom().getMap().isInWater(newTarget));

			if (!setTarget(newTarget)) {
				throw new AssertionError();
			}
		}
	}

	private boolean shouldAttackNearbyVillage() {
		return true;
	}

	private Village getNearbyVillageToAttack() {
		double minDist = NEARBY_VILLAGE_MIN_DIST;
		Village nearestVillage = null;

		for (Village village : getKingdom().villages) {
			double dist = this.distToCenter(village);
			if (dist < getLineOfSight() && dist < minDist && shouldRaidOrBesiege(village)) {
				nearestVillage = village;
				minDist = dist;
			}
		}

		// Make sure it's not across the sea or a lake.
		if (nearestVillage == null || this.getKingdom().pathDistBetween(this, nearestVillage) > NEARBY_VILLAGE_MIN_DIST)
			return null;
//		System.out.println("distance from bandit to village is " + minDist);
		return nearestVillage;
	}

//	@Override
//	public boolean setTarget(Destination dest) {
////		if (dest != null && dest.getType() == DestType.LOCATION) {
//////			BottomPanel.log("Bandit is attacking " + dest.getName());
//////			throw new AssertionError();
////		}
//		return super.setTarget(dest);
//	}
	
	@Override
	public void destroy() {
		((NomadicFaction) getFaction()).banditCount--;
		super.destroy();
	}
}
