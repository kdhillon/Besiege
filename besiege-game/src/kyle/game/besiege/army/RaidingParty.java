/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.PartyType.Type;

public class RaidingParty extends Army {
	private final String textureRegion = "Raider";
	
	private City raidAround; // city, village or point

	public RaidingParty() {}
	
	public RaidingParty(Kingdom kingdom, String name, Faction faction,
			float posX, float posY) {
		super(kingdom, name, faction, posX, posY, Type.RAIDING_PARTY, null);
	}
	
	@Override
	public void uniqueAct() {
		if (raidAround != null) {
			// raid around
			if (!hasTarget()) {
				Village toRaid = getKingdom().villages.random();
				newTarget(toRaid);
			}
		}
	}
	
	public Village getNearbyHostileVillage() {
		return null; // TODO
	}
	
	public void raidAround(City raidAround) {
		this.raidAround = raidAround;
	}
	public City getRaidAround() {
		return raidAround;
	}
	@Override
	public String getUniqueAction() {
		return "Raiding around " + raidAround.getName();
	}
	
	@Override
	public void destroy() {
		getKingdom().removeArmy(this);
		this.remove();
		if (getDefaultTarget() != null) {
			((City) getDefaultTarget()).removeRaider(this);
		}
	}
}
