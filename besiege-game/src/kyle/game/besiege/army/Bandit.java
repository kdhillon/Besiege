/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Point;
import kyle.game.besiege.party.PartyType;

public class Bandit extends Army {
	private final int MID_SIZE = 15;
	private String texture;

	public Bandit() {}
	
	public Bandit(Kingdom kingdom, String name,
			float posX, float posY) {
		super(kingdom, name, Faction.BANDITS_FACTION, posX, posY, PartyType.Type.BANDIT);
		
		if (getParty().getTotalSize() >= MID_SIZE)
			texture = "Raider";
		else texture = "Bandit";
		
		this.setDefaultTarget(new Point(posX, posY));
		this.setTextureRegion(texture);
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
			// create new random target
			float dx = (float) ((Math.random()*2-1)*getLineOfSight()); //number btw -1 and 1
			float dy = (float) ((Math.random()*2-1)*getLineOfSight());
			Point newTarget = new Point(getCenterX() + dx, getCenterY() + dy);
			setTarget(newTarget);
		}
	}
	
	@Override
	public void destroy() {
		getKingdom().banditCount--;
		super.destroy();
	}
}
