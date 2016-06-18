/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

import kyle.game.besiege.Faction;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.panels.BottomPanel;

public class Party {
	private final double BASE_CHANCE = .3;
	private final double MIN_WEALTH_FACTOR = 1.4; // times troopcount

	public boolean updated; // does the panel need to be updated.
	public int wealth;
	public int minWealth; // keeps the party out of debt, of course!
	public int maxSize; // current max size of the party

	public boolean player;
	public Army army;

	public Subparty root;

	private StrictArray<Soldier> prisoners;
	public StrictArray<Subparty> sub;

	private int atkTotal;
	private int defTotal;
	private int spdTotal;

	public double woundChance;

	public Party() {
		player = false;

		root = new Subparty(this);
		sub = new StrictArray<Subparty>();
		sub.add(root);

		prisoners = new StrictArray<Soldier>();

		atkTotal = 0;
		defTotal = 0;
		spdTotal = 0;
		calcStats();		

		maxSize = 10000;
		wealth = 0;

		woundChance = BASE_CHANCE;
	}

	public void act(float delta) {
		if (player && army != null) woundChance = BASE_CHANCE * army.getCharacter().getAttributeFactor("Reviving");
		if (!this.army.isInBattle()) {
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

	public void addSoldier(Soldier soldier) {
		if (this.getTotalSize() >= maxSize) {
			System.out.println("trying to add more than max size");
			return;
		}
		else {
			// put this guy in a subparty that's not full, or create a new par
			Subparty p = getNonEmptySub();
			if (p == null) {
				p = createNewSub();
			}
			p.addSoldier(soldier);
			
			// for now, just promote new soldier to general
			if (p.general == null)
				p.promoteToGeneral(soldier);
		}
	}
	
	public Subparty getNonEmptySub() {
		for (Subparty s : sub) {
			if (s.isFull()) continue;
			return s;
		}
		return null;
	}
	
	// promote an existing soldier to be general of a new subparty
	public Subparty createNewSub() {
		Subparty newSub = new Subparty(this);
		root.addSub(newSub);
		sub.add(newSub);
//		Soldier soldierToPromote = getSoldierToPromote();
//		if (soldierToPromote == null) return null;
//		newSub.setGeneral(soldierToPromote);
		return newSub;
	}
	
//	public Soldier getSoldierToPromote() {
		
//	}
	
	public void removeSoldier(Soldier soldier) {
		for (Subparty p : sub) {
			p.removeSoldier(soldier);
		}
	}

	public boolean casualty(Soldier soldier) {
		for (Subparty p : sub) {
			if (p.healthy.contains(soldier, true))
				return p.casualty(soldier);
		}
		return false;
	}

	public void addPrisoner(Soldier soldier) {
		updated = true;
		soldier.timesCaptured++;
		prisoners.add(soldier);
		prisoners.sort();
	}


	public StrictArray<Soldier> getUpgradable() {
		StrictArray<Soldier> total = new StrictArray<Soldier>();
		//		StrictArray<Subparty> subparties = getAllSub();
		for (Subparty p : sub)
			total.addAll(p.getUpgradable());
		return total;
	}

	public void calcStats() {
		atkTotal = 0;
		defTotal = 0;
		spdTotal = 0;
		//		StrictArray<Subparty> subparties = getAllSub();
		for (Subparty s : sub) {
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

	public void givePrisoner(Soldier prisoner, Party recipient) {
		boolean removed = false;
		for (Subparty s : sub) {
			if (s.wounded.contains(prisoner, true)) {
				s.wounded.removeValue(prisoner, true);
				removed = true;
			}
			else if (s.healthy.contains(prisoner, true)) {
				s.healthy.removeValue(prisoner, true);
				removed = true;
			}
		}
		if (!removed) BottomPanel.log("trying to add invalid prisoner", "red");
		recipient.addPrisoner(prisoner);
	}

	public void returnPrisoner(Soldier prisoner, Party recipient) {
		if (this.prisoners.contains(prisoner, true))
			this.prisoners.removeValue(prisoner, true);
		else BottomPanel.log("trying to remove invalid prisoner", "red");
		recipient.addSoldier(prisoner);
	}

	public int getHealthySize() {
		int total = 0; 
		for (Subparty s : sub) {
			total += s.getHealthySize();
		}
		return total;
	}
	public int getWoundedSize() {
		int total = 0; 
		for (Subparty s : sub) {
			total += s.getWoundedSize();
		}
		return total;	
	}

	public int getTotalSize() {
		return getHealthySize() + getWoundedSize();
	}

	public StrictArray<Soldier> getHealthy() {
		StrictArray<Soldier> healthy = new StrictArray<Soldier>();
		for (Subparty s : sub) {
			healthy.addAll(s.healthy);
		}		
		return healthy;
	}

	public StrictArray<Soldier> getWounded() {
		StrictArray<Soldier> wounded = new StrictArray<Soldier>();
		for (Subparty s : sub) {
			wounded.addAll(s.wounded);
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
	// TODO maybe inefficient? can make more by sorting array by name
	public static StrictArray<StrictArray<Soldier>> getConsol(StrictArray<Soldier> arrSoldier) {
		// first thing: sort arrSoldier by name
		arrSoldier.sort();


		StrictArray<String> names = new StrictArray<String>();
		StrictArray<StrictArray<Soldier>> consol = new StrictArray<StrictArray<Soldier>>();
		for (Soldier s : arrSoldier) {
			if (!names.contains(s.getTypeName() + s.getUnitClass(), false)) {
				names.add(s.getTypeName() + s.getUnitClass());
				StrictArray<Soldier> type = new StrictArray<Soldier>();
				type.add(s);
				consol.add(type);
			}
			else {
				consol.get(names.indexOf(s.getTypeName() + s.getUnitClass(), false)).add(s);
			}
		}
		return consol;	
	}

	public Faction getFaction() {
		if (army != null) return army.getFaction();
		else return null;
	}

	public String getName() {
		if (army != null) return army.getName();
		return "";
	}

	public void createFreshGeneral(UnitType type) {
		this.root.setGeneral(new General(type, this));
	}

	public void setGeneral(General general) {
		root.setGeneral(general);
	}

//	public Location getHome() {
//		return getGeneral().home;
//	}
	
	public General getGeneral() {
		return root.general;
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
				this.addSoldier(newSoldier);
				this.wealth -= cost;
				missing--;
			}
			else {
				canAfford = false;
			}
		}
		System.out.println("party repaired");
	}

	public void registerBattleVictory() {
		for (Subparty p : sub) {
			for (Soldier s : p.healthy) {
				s.registerBattleVictory();
			}
			for (Soldier s : p.wounded) {
				s.registerBattleVictory();
			}
		}
	}

	public void registerBattleLoss() {
		for (Subparty p : sub) {
			for (Soldier s : p.healthy) {
				s.registerBattleLoss();
			}
			for (Soldier s : p.wounded) {
				s.registerBattleLoss();
			}
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
