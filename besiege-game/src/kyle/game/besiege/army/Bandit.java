/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.*;
import kyle.game.besiege.party.PartyType;

public class Bandit extends Army {
	public Bandit() {}
	
	public Bandit(Kingdom kingdom, String name,
			float posX, float posY) {
		super(kingdom, name, Faction.BANDITS_FACTION, posX, posY, PartyType.Type.BANDIT, null);
		
		this.setDefaultTarget(new Point(posX, posY));
		this.type = ArmyType.BANDIT;
	}
	
	@Override
	public void act(float delta) {
//		System.out.println("Bandit act");
		super.act(delta);
	}
	
	@Override
	public void uniqueAct() {
		if (!this.hasTarget()) { //key
//			 create new random target
			float dx = Random.getRandomInRange(-getLineOfSight(), getLineOfSight()); //number btw -1 and 1
			float dy = Random.getRandomInRange(-getLineOfSight(), getLineOfSight()); //number btw -1 and 1
			Point newTarget = new Point(getCenterX() + dx, getCenterY() + dy);
			setTarget(newTarget);
		}
	}

	@Override
	public boolean setTarget(Destination dest) {
		if (dest != null && dest.getType() == DestType.LOCATION) {
			throw new AssertionError();
		}
		return super.setTarget(dest);
	}
	
	@Override
	public void destroy() {
		getKingdom().banditCount--;
		super.destroy();
	}
}
