/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import kyle.game.besiege.Faction;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;

public class Party {
	private static final String GARRISON_STRING = "Garrison";
	private final double BASE_CHANCE = .3;
	private final double MIN_WEALTH_FACTOR = 1.4; // times troopcount

	public boolean updated; // does the panel need to be updated.
	public int wealth;
	public int minWealth; // keeps the playerPartyPanel out of debt, of course!
//	public int maxSize; // current max size of the playerPartyPanel

	public boolean player;
	public Army army;
	private Location location; // This will only be set for Garrisons.

	private String name;

	public PartyType pt;
	
	public Subparty root;

	private StrictArray<Soldier> prisoners;
	public StrictArray<Subparty> subparties;

	private int atkTotal;
	private int defTotal;
	private int spdTotal;

	public double woundChance;

	public Party() {
		player = false;

		root = new Subparty(this);

		subparties = new StrictArray<Subparty>();
		subparties.add(root);

		prisoners = new StrictArray<Soldier>();

		atkTotal = 0;
		defTotal = 0;
		spdTotal = 0;
		calcStats();		

		wealth = 0;

		woundChance = BASE_CHANCE;
	}

	public void act(float delta) {
		if (player && army != null) woundChance = BASE_CHANCE * army.getCharacter().getAttributeFactor("Reviving");
		if (this.army != null && !this.army.isInBattle()) {
			root.checkHeal();
		}
		calcStats();
	}

	public void checkUpgrades() {
		for (Soldier s : getUpgradable()) {
			if (s.unitType.upgrades.length == 0) return;
			int selection = (int) (Math.random() * s.unitType.upgrades.length);
			s.upgrade(s.unitType.upgrades[selection]);
			//			s.upgrade(Weapon.upgrade(s.getWeapon()).random());
		}
	}

	// 1) make all subparties have equal size. Pre-calculate the max number of subparties this party can have based on its current (or max) size.
	// 2) put certain units into certain subparties. Either put best units in one subparty (root), separate by class (archers, infantry, etc), or evenly distribute (random)
	public boolean addSoldier(Soldier soldier, boolean force, boolean toSmallest) {
		if (isFull() && !force) {
			System.out.println("trying to add more than max size");
			return false;
		}
		else {
//			System.out.println("current subs: " + subparties.size);
			// TODO when a garrison is destroyed, all subparties are removed.
			Subparty p;
			if (toSmallest)
				p = getSmallestSub();
			else
				p = getNonEmptySub();

			if (p == null) {
				if (toSmallest) throw new AssertionError();
				return createNewSubWithGeneral(soldier);
			} else {
                // Some soldiers are being added, but not getting counted in total size... suspicious. generals?
                return p.addSoldier(soldier);
            }
		}
	}

	public boolean addSoldier(Soldier soldier, boolean force) {
		return addSoldier(soldier, force, false);
	}

	void initializeForToHire(StrictArray<Soldier> soldiers) {
		for (Soldier s : soldiers) {
			addSoldier(s, false);
		}
	}

	// Initialize this subparty with the given soldier list.
	// Basic algo:
	// 		decide how many subparties we need (say, 3)
	// 		Pick the best 3 soldiers and create subparties with them as generals.
	// 		Using one of several party-organization systems, allocate the remaining soldiers.
	void initializeWith(StrictArray<Soldier> soldiers) {
		// Figure out the smallest number of subparties we can generate.
		// ie, if there  22 units and general can command 19 under him (20 total), we need to have 2 subparties

		int subpartyCount = soldiers.size / getGeneral().getMaxSubPartySize() + 1;

		// Note we skip the first subparty, as it was already created when the general was added.
		for (int i = 1; i < subpartyCount; i++) {
			Soldier best = getBestSoldier(soldiers);
			createNewSubWithGeneral(best);
			soldiers.removeValue(best, true);
		}

		for (Soldier s : soldiers) {
			addSoldier(s, false, true);
		}
	}

	public Soldier getBestSoldier(StrictArray<Soldier> soldiers) {
		Soldier best = null;
		int maxLevel = 0;
		for (Soldier soldier : soldiers) {
			if (soldier.level > maxLevel && !soldier.isGeneral()) {
				best = soldier;
				maxLevel = soldier.level;
			}
		}
		return best;
	}
	
	public Soldier getBestSoldier() {
		// not efficient, but consistent with above.
		StrictArray<Soldier> soldiers = new StrictArray<>();
		for (Subparty sub : subparties) {
			soldiers.addAllFromStrictArray(sub.healthy);
		}
		return getBestSoldier(soldiers);
	}
	
	public boolean isFull() {
		return this.getTotalSize() >= getMaxSize();
	}
	
	public int getMaxSize() {
	    if (this.hasGeneral())
		    return getGeneral().getMaxSize();
	    else return pt.getMaxSize();
	}
	
	public Subparty getNonEmptySub() {
		for (int i = 0; i < subparties.size; i++) {
			Subparty s = subparties.get(i);
			if (s.isFull()) continue;
			return s;
		}
		return null;
	}

	Subparty getSmallestSub() {
		int smallestSize = 99999;
		Subparty smallest = null;
		for (int i = 0; i < subparties.size; i++) {
			Subparty s = subparties.get(i);
			if (s.getTotalSize() < smallestSize) {
				smallest = s;
				smallestSize = s.getTotalSize();
			}
		}
		return smallest;
	}
	
	// promote an existing soldier from another party to be general of a new subparty
    public void createNewSubWithExistingGeneral() {
	    Soldier s = this.getBestSoldier();
	    s.subparty.removeSoldier(s);
	    createNewSubWithGeneral(s);
    }

    // create new subparty with the given soldier as general (should be a fresh soldier in no other subparty)
	// Return true on success, false otherwise.
    public boolean createNewSubWithGeneral(Soldier soldier) {
        Subparty newSub = new Subparty(this);

        if (root == null || root.getTotalSize() == 0) {
        	subparties.removeValue(root, true);
        	root = newSub;
		} else {
			root.addSub(newSub);
		}
        subparties.add(newSub);

        // promote best soldier to general
        if (newSub.getGeneral() == null && !pt.hire) {
            if (soldier.subparty != null) {
				soldier.subparty.removeSoldier(soldier);
			}
            newSub.promoteToGeneral(soldier);

            System.out.println("adding random shaman");
			newSub.addRandomShaman();
            // Testing

            return true;
        }
        return false;
    }
//	
//	// root is probably dead, move a subparty to be root and kill root.
//	public void rearrangeSubs() {
//		
//	}
	
	public void destroySub(Subparty toDestroy) {
		// first check if any subparties has this as its parent
		// for now, everything is a child of the root subparties.
		// so this should only happen if s is a root.
		System.out.println("Destroying subparty of " + this.getName() + " with rank " + toDestroy.getRank());

		if (toDestroy == root) {
//			throw new AssertionError(); // this is actually ok. just for testing.
		}

		StrictArray<Subparty> children = new StrictArray<Subparty>();
		
		for (Subparty s : subparties) {
			if (s.parent == s) continue;

			// special case, clean up empty children subparties
			if (s.getTotalSize() == 0 && s.getRank() != 0) {
				continue;
				// TODO actually remove this subparty from subparties list, it shouldn't be there.
			}

			if (s.parent == toDestroy) {
				children.add(s);
			}
		}
		
		// if we're removing a root
		if (children.size > 0) {
			Subparty newRoot = children.first();
			promoteToRoot(newRoot);
			children.removeValue(newRoot, true);

			for (Subparty s : children) {
				s.parent = newRoot;
			}
		}

		subparties.removeValue(toDestroy, true);
		if (subparties.size == 0) {
			if (this.army != null) {
				this.army.destroy();
				System.out.println("Destroying party");
			} else {
				// Location will be null for "hire" party... Maybe don't use a party to represent hire party
				if (location != null)
					System.out.println(location.getName() + " garrison completely destroyed");
			}
		}
	}
	
	public void promoteToRoot(Subparty s) {
		s.parent = null;
		
		// verify
		for (Subparty that : subparties) {
			if (root == that) {
				if (that.parent != null) throw new java.lang.AssertionError();
			}
			else {
				if (that.parent != null && that.parent != root) throw new java.lang.AssertionError();
			}
		}
		this.root = s;

		if (s.getTotalSize() == 0) throw new AssertionError();
	}
	
	public void removeSoldier(Soldier soldier) {
		for (Subparty p : subparties) {
			p.removeSoldier(soldier);
		}
	}

	public void addPrisoner(Soldier soldier) {
		updated = true;
		soldier.timesCaptured++;
		prisoners.add(soldier);
		prisoners.sort();
		soldier.subparty = null;
	}


	public StrictArray<Soldier> getUpgradable() {
		StrictArray<Soldier> total = new StrictArray<Soldier>();
		//		StrictArray<Subparty> subparties = getAllSub();
		for (Subparty p : subparties)
			total.addAllFromStrictArray(p.getUpgradable());
		return total;
	}

	public void calcStats() {
		atkTotal = 0;
		defTotal = 0;
		spdTotal = 0;
		//		StrictArray<Subparty> subparties = getAllSub();
		for (Subparty s : subparties) {
			atkTotal += s.atkTotal;
			defTotal += s.defTotal;
			spdTotal += s.spdTotal;
		}
		if (!player) minWealth = (int) (MIN_WEALTH_FACTOR*getTotalSize());
		else minWealth = 0;
//		System.out.println("total size: " + getTotalSize() + " min wealth: " + minWealth);
	}

	//	public StrictArray<Subparty> getAllSub() {
	//		StrictArray<Subparty> p =  new StrictArray<Subparty>();
	//		p.add(root);
	//		return p;
	//	}

	public void givePrisonerFromThis(Soldier prisoner, Party recipient) {
		boolean removed = false;
		for (int i = 0; i < subparties.size; i++) {
			Subparty s = subparties.get(i);
			// TODO wounded/healthy still contain general, so we need to handle that.
			if (s.wounded.contains(prisoner, true)) {
				s.wounded.removeValue(prisoner, true);
				removed = true;
				if (prisoner == s.getGeneral()) {
					if (s.getGeneral().isPlayerUnit())
						throw new AssertionError();
					s.demoteGeneral((General) prisoner);
					if (s.getHealthySize() > 0 || s.getWoundedSize() > 0)
						s.promoteNextGeneral();
//					if (s.getGeneral() == null) throw new AssertionError();
					removed = true;
				}
			}
			else if (s.healthy.contains(prisoner, true)) {
				s.healthy.removeValue(prisoner, true);
				removed = true;
				if (prisoner == s.getGeneral()) {
					if (s.getGeneral().isPlayerUnit())
						throw new AssertionError();
					s.demoteGeneral((General) prisoner);
					if (s.getHealthySize() > 0 || s.getWoundedSize() > 0)
						s.promoteNextGeneral();
//					if (s.getGeneral() == null) throw new AssertionError();
					removed = true;
				}
			} else if (prisoner == s.getGeneral()) {
				if (s.getGeneral().isPlayerUnit()) throw new AssertionError();
				s.demoteGeneral((General) prisoner);
				if (s.getHealthySize() > 0 || s.getWoundedSize() > 0)
					s.promoteNextGeneral();
//				if (s.getGeneral() == null) throw new AssertionError();
				removed = true;
			} else if (prisoner == s.shaman) {
				s.shaman = null;
				removed = true;
			}
		}
		if (!removed) {
			throw new AssertionError();
		}
		recipient.addPrisoner(prisoner);
	}

	public void returnPrisoner(Soldier prisoner, Party recipient) {
		if (this.prisoners.contains(prisoner, true))
			this.prisoners.removeValue(prisoner, true);
		else BottomPanel.log("trying to remove invalid prisoner", "red");
		recipient.addSoldier(prisoner, false);
	}

	public int getHealthySize() {
		int total = 0; 
		for (Subparty s : subparties) {
			total += s.getHealthySize();
		}
		return total;
	}
	public int getWoundedSize() {
		int total = 0; 
		for (Subparty s : subparties) {
			total += s.getWoundedSize();
		}
		return total;	
	}

	public int getTotalSize() {
		return getHealthySize() + getWoundedSize();
	}

	public int getTotalLevel() {
        int total = 0;
	    for (Subparty s : subparties) {
            total += s.getHealthyLevelSum();
        }
        return total;
    }

	public StrictArray<Soldier> getHealthy() {
		StrictArray<Soldier> healthy = new StrictArray<Soldier>();
		for (Subparty s : subparties) {
			healthy.addAllFromStrictArray(s.healthy);
		}		
		return healthy;
	}

	public StrictArray<Soldier> getWounded() {
		StrictArray<Soldier> wounded = new StrictArray<Soldier>();
		for (Subparty s : subparties) {
			wounded.addAllFromStrictArray(s.wounded);
		}		
		return wounded;
	}

	public StrictArray<Soldier> getHealthyCopy() {
		return getHealthy();
	}
	public StrictArray<Soldier> getPrisoners() {
		return prisoners;
	}
	public void clearPrisoners() {
		prisoners.clear();
	}
//	public StrictArray<StrictArray<Soldier>> getConsolHealthy() {
//		return getConsol(getHealthy());
//	}
//	public StrictArray<StrictArray<Soldier>> getConsolWounded() {
//		return getConsol(getWounded());
//	}
	public StrictArray<StrictArray<Soldier>> getConsolPrisoners() {
		return getConsol(prisoners);
	}

	// NOTE: THIS DOESN'T INCLUDE THE GENERAL (for a simpler display)
	public  static StrictArray<StrictArray<Soldier>> getConsol(StrictArray<Soldier> arrSoldier) {
		return getConsol(arrSoldier, false);
	}

	// TODO maybe inefficient? can make more by sorting array by name
	public static StrictArray<StrictArray<Soldier>> getConsol(StrictArray<Soldier> arrSoldier, boolean includeGeneral) {

		// first thing: sort arrSoldier by name
		arrSoldier.sort();

		StrictArray<String> names = new StrictArray<String>();
		StrictArray<StrictArray<Soldier>> consol = new StrictArray<StrictArray<Soldier>>();
		for (Soldier s : arrSoldier) {
		    if (!includeGeneral && s.isGeneral()) continue;
			if (!names.contains(s.getTypeName() + s.getCulture(), false)) {
				names.add(s.getTypeName() + s.getCulture());
				StrictArray<Soldier> type = new StrictArray<Soldier>();
				type.add(s);
				consol.add(type);
			}
			else {
				consol.get(names.indexOf(s.getTypeName() + s.getCulture(), false)).add(s);
			}
		}
		return consol;	
	}

	public Faction getFaction() {
		if (army != null) return army.getFaction();
		else return null;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setName(String name) {
	    // ONLY SET FOR GARRISONS
        this.name = name;
    }

	public String getName() {
		if (army != null) return army.getName();
		if (location != null) return location.getName() + " " + GARRISON_STRING;
		if (name != null) return name;
//		throw new java.lang.AssertionError();
		return "No name set!";
	}

	public General createFreshGeneral(UnitType type, PartyType pt) {
	    General general = new General(type, this, pt);

        // promote best soldier to general
        if (root.getGeneral() == null && !pt.hire) {
            if (general.subparty != null)
                general.subparty.removeSoldier(general);
            root.promoteToGeneral(general);
        }

		return general;
	}

	public void setGeneral(General general) {
	    if (pt.hire) {
	        throw new AssertionError();
        }
        if (general == null) throw new AssertionError();
		root.setGeneral(general);
	}

//	public Location getHome() {
//		return getGeneral().home;
//	}

	public boolean hasGeneral() {
		return root.getGeneral() != null;
	}

	// TODO promote other subparty to root when general subparty has 0.
	public General getGeneral() {
	    if (root.getGeneral() == null) {
	        System.out.println( "a party of type " + this.army.type + " has no general, subparty size: " + root.getTotalSize() + " other subparties " + (subparties.size - 1));
	        throw new AssertionError();
        }
		return root.getGeneral();
	}

	@Override
	public String toString() {
		return null;
	}
	public int getAtk() {
		return atkTotal;
	}
	public int getDef() {
		return defTotal;
	}
	public int getSpd() {
		return spdTotal;
	}
	public float getAvgDef() {
		return defTotal/1f/getHealthySize();
	}
	public float getAvgSpd() {
		return spdTotal/1f/getHealthySize();
	}
	public void distributeExp(int total) {
		if (this.player) System.out.println("distributing " + total + " exp to you");
		if (this.player) BottomPanel.log("Party receives " + total + " experience!", "green");

		int exp = (int) (total/1.0/getHealthySize());
		getHealthy().shrink();
		for (int i = 0; i < getHealthy().size; i++)
			getHealthy().get(i).addExp(exp);
		//		wounded.sort();
		//		healthy.sort();
	}

	// TODO adjust to be fair, based on fame.
	// repairs an army as much as its wealth will allow it.
	public void repair(PartyType pt) { // returns a repair cost
		int newSize = pt.getRandomSize();
		int missing = Math.max(newSize - this.getTotalSize(), 0); // no negative ints
		//		int totalCost = 0;
		boolean canAfford = true;
		while (missing > 0 && canAfford) {
			Soldier newSoldier = new Soldier(pt.randomSoldierType(), this);
			int cost = newSoldier.getCost();
			if (this.wealth > cost) {
				this.addSoldier(newSoldier, false);
				this.wealth -= cost;
				missing--;
			}
			else {
				canAfford = false;
			}
		}
//		System.out.println("playerPartyPanel repaired");
	}

	public void registerBattleVictory() {
		for (int i = 0; i < subparties.size; i++) {
			Subparty p = subparties.get(i);
			for (Soldier s : p.healthy) {
				s.registerBattleVictory();
			}
			for (Soldier s : p.wounded) {
				s.registerBattleVictory();
			}
			p.handleBattleEnded();
		}
	}

	public void registerBattleLoss() {
		for (Subparty p : subparties) {
			for (Soldier s : p.healthy) {
				s.registerBattleLoss();
			}
			for (Soldier s : p.wounded) {
				s.registerBattleLoss();
			}
			p.handleBattleEnded();
		}	
	}

	/**
	 * @return random soldier from healthy weighted by attack
	 */
	public Soldier getRandomWeightedAttack() {
		// Compute the total weight of all soldier's defenses together
		double totalWeight = 0.0d;
		for (Soldier s : getHealthy())
		{
			totalWeight += s.getAtk();
		}

		int randomIndex = -1;
		double randomDouble = Math.random() * totalWeight;
		getHealthy().shrink();
		for (int i = 0; i < getHealthySize(); ++i)
		{
			randomDouble -= getHealthy().get(i).getAtk();
			if (randomDouble <= 0.0d)
			{
				randomIndex = i;
				break;
			}
		}
		
		if (randomIndex < 0) return null;
		
		Soldier random = getHealthy().get(randomIndex);
		return random;
	}

	/**
	 *  @return random soldier from healthy weighted by inverse defense
	 */
	public Soldier getRandomWeightedInverseDefense() {
	    if (getHealthySize() == 0) throw new AssertionError();

		// Compute the total weight of all soldier's defenses together
		double totalWeight = 0.0d;
		for (Soldier s : getHealthy())
		{
			totalWeight += 1/s.getDef();
		}

		int randomIndex = -1;
		double randomDouble = Math.random() * totalWeight;
		getHealthy().shrink();
		for (int i = 0; i < getHealthySize(); ++i)
		{
			randomDouble -= 1/getHealthy().get(i).getDef();
			if (randomDouble <= 0.0d)
			{
				randomIndex = i;
				break;
			}
		}
		
		if (randomIndex < 0) return null;
		Soldier random = getHealthy().get(randomIndex);
		return random;
	}
}
