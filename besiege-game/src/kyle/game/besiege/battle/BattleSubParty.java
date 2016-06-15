package kyle.game.besiege.battle;

import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Subparty;


// make BattleSubParty - for controlling individual parties.
public class BattleSubParty {
	Subparty subparty;
	BattleParty parent;
	BattleStage stage;
	
	public StrictArray<Unit> units;
	
	public Stance stance;
	public Formation formation;
	public float minSpeed;
	public boolean retreating;
	public boolean retreated;
	
	public BattleSubParty(BattleParty parent, Subparty subparty) {
		this.subparty = subparty;
		this.parent = parent;
		this.units = new StrictArray<Unit>();
		this.stage = parent.stage;
	}
	
	public void addUnit(Unit unit) {
		this.units.add(unit);
		this.parent.units.add(unit);
		stage.addActor(unit);
		this.calcMinSpeed();
	}
	
	public void removeUnit(Unit remove) {
		units.removeValue(remove, true);
		parent.units.removeValue(remove, true);
		if (remove.inMap())
			if (stage.units[remove.pos_y][remove.pos_x] == remove) stage.units[remove.pos_y][remove.pos_x] = null;
		stage.removeActor(remove);
		calcMinSpeed();
	}
	public boolean noUnits() {
		return this.units.size == 0;
	}

	public int getHealthySize() {
		return subparty.getHealthySize();
	}

	public StrictArray<Soldier> getHealthyInfantry() {
		return subparty.getHealthyInfantry();
	}
//
	public StrictArray<Soldier> getHealthyArchers() {
//		StrictArray<Soldier> healthyArchers = new StrictArray<Soldier>();
//		for (Party p : parties) {
//			healthyArchers.addAll(p.root.getHealthyArchers());
//		}
//		return healthyArchers;
		return subparty.getHealthyArchers();
	}
	
	public StrictArray<Soldier> getHealthyCavalry() {
//		StrictArray<Soldier> healthyCavalry = new StrictArray<Soldier>();
//		for (Party p : parties) {
//			healthyCavalry.addAll(p.root.getHealthyCavalry());
//		}
//		return healthyCavalry;
		return subparty.getHealthyCavalry();
	}
	
	public void calcMinSpeed() {
		float min = Float.MAX_VALUE;
		for (Unit unit : units) {
			if (unit.retreating) continue;
			if (unit.spd < min) min = unit.spd;
		}
		this.minSpeed = min;
	}
	
	public void updateHiddenAll() {
		for (Unit unit : units) unit.updateHidden();
//		System.out.println("updating hidden");
	}
	
	public boolean isPlayer() {
		return parent.player;
	}
}
