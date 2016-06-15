package kyle.game.besiege.battle;

import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Subparty;

public class BattleParty {
	public boolean besieging;
	public boolean player;
	
	public boolean retreating;
	
	StrictArray<BattleSubParty> subparties;
	StrictArray<Party> parties;
	StrictArray<Unit> units; // reflects subparties' units

	public BattleStage stage;
	
	

	public BattleParty(BattleStage stage) {
		this.stage = stage;
		this.parties = new StrictArray<Party>();
		this.subparties = new StrictArray<BattleSubParty>();
		this.units = new StrictArray<Unit>();
	}
	
	public void addParty(Party party) {
		if (party == null) return;
		this.parties.add(party);
//		System.out.println("adding " + party.army.getName());
		for (Subparty s : party.sub) {
			this.subparties.add(new BattleSubParty(this, s));
		}
	}
	
	public Party first() {
		for (Party p : this.parties) return p;
		return this.parties.first();
	}
	
	public int getHealthySize() {
		int total = 0;
		for (BattleSubParty p : subparties) {
			if (!p.retreated) {
				total += p.getHealthySize();
			}
		}
		return total;
	}
	
	public StrictArray<Party> getPartiesCopy() {
		StrictArray<Party> partiesNew = new StrictArray<Party>();
		for (Party p : parties) partiesNew.add(p);
		return partiesNew;
	}
	

	public void updatePolygon() {
		if (parties == null) return;
		for (Party p : parties) {
			if (p != null && p.army != null && !p.army.isGarrison)
				p.army.updatePolygon();
		}
	}
	
	public void setStance(Stance stance) {
		for (BattleSubParty s : subparties) {
			s.stance = stance;
		}
	}
	
	public void setGlobalFormation(Formation f) {
		for (BattleSubParty s : subparties) {
			s.formation = f;
		}
	}
	
	public void retreatAll() {
		for (BattleSubParty s : subparties) {
			s.retreating = true;
		}
	}
	
	public void updateHiddenAll() {
		for (BattleSubParty s : subparties) {
			s.updateHiddenAll();
		}	
	}
	
	public boolean noUnits() {
		for (BattleSubParty b : subparties) {
			if (!b.noUnits()) return false;
		}
		return true;
	}
	
	public void removeUnit(Unit u) {
		u.bsp.removeUnit(u);
	}
	
//	public StrictArray<Soldier> getHealthyInfantry() {
//		StrictArray<Soldier> healthyArchers = new StrictArray<Soldier>();
//		for (Party p : parties) {
//			healthyArchers.addAll(p.root.getHealthyArchers());
//		}
//		return healthyArchers;
//	}
//	
//	public StrictArray<Soldier> getHealthyInfantry() {
//		int total = 0;
//		for (BattleSubParty p : subparties) {
//			if (!p.retreated) {
//				total += p.getHealthyInfantry();
//			}
//		}
//		return total;
//	}
}
