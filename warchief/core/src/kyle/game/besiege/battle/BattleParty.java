package kyle.game.besiege.battle;

import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Subparty;

public class BattleParty {
	public boolean besieging;
	public boolean player;

	public boolean retreating;

	public StrictArray<BattleSubParty> subparties;
	StrictArray<Party> parties;
	StrictArray<Unit> units; // reflects subparties' units

	public BattleStage stage;
	
	public int team;

	public BattleParty(BattleStage stage, int team) {
		this.stage = stage;
		this.parties = new StrictArray<Party>();
		this.subparties = new StrictArray<BattleSubParty>();
		this.units = new StrictArray<Unit>();
		this.team = team;
	}
	
	public void addParty(Party party) {
		if (party == null) return;
		this.parties.add(party);
//		System.out.println("adding " + playerPartyPanel.army.getName());
		for (Subparty s : party.subparties) {
			System.out.println("adding subparty of " + party.getName());
			this.subparties.add(new BattleSubParty(this, s, team));
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
		for (BattleSubParty p : subparties) {
			if (!p.retreating) {
				total += p.getHealthyLevelSum();
			}
		}
		return total;
	}
	
	public int getAtk() {
		int total = 0;
		for (BattleSubParty p : subparties) {
			if (!p.retreating) {
				total += p.getHealthySize();
			}
		}
		return total;
	}
	
	public int getHealthySize() {
		int total = 0;
		for (BattleSubParty p : subparties) {
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
		for (BattleSubParty s : subparties) {
			s.stance = stance;
		}
	}
	
	public void setGlobalFormation(Formation f) {
		for (BattleSubParty s : subparties) {
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

		for (BattleSubParty s : subparties) {
			s.retreat();
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
//		for (BattleSubParty p : subparties) {
//			if (!p.retreated) {
//				total += p.getHealthyInfantry();
//			}
//		}
//		return total;
//	}
}
