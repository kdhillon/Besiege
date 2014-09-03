package kyle.game.besiege.battle;

import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

import com.badlogic.gdx.utils.Array;

public class BattleParty {

	public BattleStage stage;
	public boolean besieging;
	public boolean player;
	public Array<Unit> units;
	public Array<Party> parties;
//	public Party party;
	public Stance stance;
	public Formation formation;
	public float minSpeed;
	public boolean retreating;
	
	
//	public void removeUnit(remove, true) {
//		
//	}

	
	public BattleParty(BattleStage stage) {
		this.units = new Array<Unit>();
		this.stage = stage;
		this.parties = new Array<Party>();
	}
	
	
	public void addParty(Party party) {
		if (party == null) return;
		this.parties.add(party);
//		System.out.println("adding " + party.army.getName());
	}

	public void addUnit(Unit unit) {
		this.units.add(unit);
		stage.addActor(unit);
		this.calcMinSpeed();
	}
	
	public void removeUnit(Unit remove) {
		units.removeValue(remove, true);
		if (remove.inMap())
			if (stage.units[remove.pos_y][remove.pos_x] == remove) stage.units[remove.pos_y][remove.pos_x] = null;
		stage.removeActor(remove);
		calcMinSpeed();
	}
	public boolean noUnits() {
		return this.units.size == 0;
	}
	
	public Party first() {
		for (Party p : this.parties) return p;
		return this.parties.first();
	}
	
	public int getHealthySize() {
		int total = 0;
		for (Party p : parties) {
			total += p.getHealthySize();
		}
		return total;
	}

	public Array<Soldier> getHealthyInfantry() {
		Array<Soldier> healthyInfantry = new Array<Soldier>();
		for (Party p : parties) {
			healthyInfantry.addAll(p.getHealthyInfantry());
		}
		return healthyInfantry;
	}

	public Array<Soldier> getHealthyArchers() {
		Array<Soldier> healthyArchers = new Array<Soldier>();
		for (Party p : parties) {
			healthyArchers.addAll(p.getHealthyArchers());
		}
		return healthyArchers;
	}
	
	public Array<Soldier> getHealthyCavalry() {
		Array<Soldier> healthyCavalry = new Array<Soldier>();
		for (Party p : parties) {
			healthyCavalry.addAll(p.getHealthyCavalry());
		}
		return healthyCavalry;
	}
	
	public Array<Party> getPartiesCopy() {
		Array<Party> partiesNew = new Array<Party>();
		for (Party p : parties) partiesNew.add(p);
		return partiesNew;
	}
	
	public void updatePolygon() {
		if (parties == null) return;
		for (Party p : parties) {
			if (p != null && p.army != null)
				p.army.updatePolygon();
		}
	}

	public void calcMinSpeed() {
		float min = Float.MAX_VALUE;
		for (Unit unit : units) {
			if (unit.retreating || unit.bowOut()) continue;
			if (unit.spd < min) min = unit.spd;
		}
		this.minSpeed = min;
	}
}
