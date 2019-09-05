package kyle.game.besiege.battle;

import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Squad;

public class BattleParty {
	public boolean besieging;
	public boolean player;

	public boolean retreating;

	public StrictArray<BattleSquad> squads;
	StrictArray<Party> parties;
	StrictArray<Unit> units; // reflects squads' units

	public BattleStage stage;
	
	public int team;

	public BattleParty(BattleStage stage, int team) {
		this.stage = stage;
		this.parties = new StrictArray<Party>();
		this.squads = new StrictArray<BattleSquad>();
		this.units = new StrictArray<Unit>();
		this.team = team;
	}
	
	public void addParty(Party party) {
		if (party == null) return;
		this.parties.add(party);
//		System.out.println("adding " + playerPartyPanel.army.getName());
		for (Squad s : party.squads) {
			System.out.println("adding squad of " + party.getName());
			this.squads.add(new BattleSquad(this, s, team));
		}
	}

	public void setUpdated() {
		for (Party p : parties) {
			p.updated = true;
		}
	}

	public Party first() {
		for (Party p : this.parties) {
		    if (p != null)
		        return p;
        }
		return this.parties.first();
	}
	
	public int getLevelSum() {
		int total = 0;
		for (BattleSquad p : squads) {
			if (!p.retreating) {
				total += p.getHealthyLevelSum();
			}
		}
		return total;
	}
	
	public int getAtk() {
		int total = 0;
		for (BattleSquad p : squads) {
			if (!p.retreating) {
				total += p.getHealthySize();
			}
		}
		return total;
	}
	
	public int getHealthySize() {
		int total = 0;
		for (BattleSquad p : squads) {
			if (!p.retreating) {
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
		for (BattleSquad s : squads) {
			s.stance = stance;
		}
	}
	
	public void setGlobalFormation(Formation f) {
		for (BattleSquad s : squads) {
			s.formation = f;
		}
	}

	private boolean isDefenders() {
	    return stage.getDefending() == this;
    }

    public boolean canRetreat() {
	    if (isDefenders() && stage.siegeOrRaid) {
	        return false;
        }
        return true;
    }

	public void tryToRetreatAll() {
	    if (!canRetreat()) {
	    	System.out.println("player can't retreat!!");
	    	return;
		}

		for (BattleSquad s : squads) {
			s.retreat();
		}
	}
	
	public void updateHiddenAll() {
		for (BattleSquad s : squads) {
			s.updateHiddenAll();
		}	
	}
	
	public boolean noUnits() {
		for (BattleSquad b : squads) {
			if (!b.noUnits()) return false;
		}
		System.out.println("no units!!!");
		return true;
	}
	
	public void removeUnit(Unit u, boolean dying) {
		u.bsp.removeUnit(u, dying);
	}
	
//	public StrictArray<Soldier> getHealthyInfantry() {ppp
//		StrictArray<Soldier> healthyArchers = new StrictArray<Soldier>();
//		for (Party p : parties) {
//			healthyArchers.addAll(p.root.getHealthyArchers());
//		}
//		return healthyArchers;
//	}
//	
//	public StrictArray<Soldier> getHealthyInfantry() {
//		int total = 0;
//		for (BattleSquad p : squads) {
//			if (!p.retreated) {
//				total += p.getHealthyInfantry();
//			}
//		}
//		return total;
//	}
}
