/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.battle;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.Army.ArmyType;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.PanelBattle;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class Battle extends Actor implements Destination { // new battle system involving Party
	private static final float SPEED = 2000; //lower is faster
	private static final int EXP_FACTOR = 47; // how much more exp is given to winning party than total atk of enemies
	private static final int BASE_EXP = 1;
	private static final int MIN_EXP = 500;
	private static final float MIN_BALANCE = .3f;
	private static final int MIN_RETREAT_TIME = 3;
	private static final int BASE_RETREAT_TIME = 5;
	private static final double RETREAT_WEALTH_FACTOR = .7; // this is how much of the retreating parties wealth will be lost
	public static final double RETREAT_THRESHOLD = 0.3; // if balance less than this, army will retreat (btw 0 and 1, but obviously below 0.5)
	public static final int WAIT = 3; // time army must wait after winning a battle to give the retreater a head start? maybe a better way to do this.
	public static final int DESTROY_THRESHOLD = 2; // if less than x soldiers left in retreating army, destroy it.
	
	private final int baseMoraleReward = 25;
	private final String REGION = "battle";
	transient private TextureRegion region;
	transient public TextureRegion halfCrest;
	private String name;
	
	// doesn't require a kingdom necessarily
	private Kingdom kingdom;
	public Array<Army> aArmies;
	public Array<Army> dArmies;
	public Array<Army> aArmiesRet; 
	public Array<Army> dArmiesRet;
	
	public Array<Party> aParties;
	public Array<Party> dParties;
	public Array<Party> aPartiesRet;
	public Array<Party> dPartiesRet;
	
	public Location siegeOf; // only if a siege
	
	public double aAdvantage;// calculated constants controlled by external factors 
	public double dAdvantage;// like player's command skill, etc.
	
	public boolean playerInA;
	public boolean playerInD;
	
	private boolean firstTimeInit = false;
	
	public int aAtk;
	public int dAtk;
	
	public int spoils;
	
	public int expA;
	public int expD;
	
	public double balanceA; // Overall balance of the battle, useful for creating a balance bar
	public double balanceD; 
	public double initBalanceA;
	public double initBalanceD;
	public int initTotalTroops;
	
	private boolean mouseOver;
	
	public boolean isOver;
	public boolean didAtkWin;
	
	// For Kryo
	public Battle() {
		
	}
	
	// for simulation purposes
	public Battle(Kingdom kingdom, Party initAttackerParty, Party initDefenderParty) {
		this.kingdom = kingdom;
		
//		region = Assets.atlas.findRegion(REGION);

		Army initAttacker = initAttackerParty.army;
		Army initDefender = initDefenderParty.army;

		aParties = new Array<Party>();
		dParties = new Array<Party>();
		aPartiesRet = new Array<Party>();
		dPartiesRet = new Array<Party>();
		
		aParties.add(initAttackerParty);
		dParties.add(initDefenderParty);
		
		calcStats();
		initBalanceA = balanceA;
		initBalanceD = balanceD;
		initTotalTroops = initAttackerParty.getHealthySize() + initDefenderParty.getHealthySize();
		
		spoils = 0;
		//expA = initDefender.getParty().getAtk();
		//expD = initAttacker.getParty().getAtk(); // what should this be based on? number of enemies killed!
		expA = BASE_EXP;
		expD = BASE_EXP;
		
		if (initAttackerParty.player) playerInA = true;
		else if (initDefenderParty.player) playerInD = true;
		
		
//		if (playerInA || playerInD) 
//			kingdom.getMapScreen().getSidePanel().setActiveBattle(this);
		
		if (initAttacker != null && initDefender != null) {
			aArmies = new Array<Army>();
			dArmies = new Array<Army>();
			aArmiesRet = new Array<Army>();
			dArmiesRet = new Array<Army>();
			
			aArmies.add(initAttacker);
			dArmies.add(initDefender);
			
			initAttacker.setStopped(true);
			initAttacker.setVisible(false);
			initDefender.setStopped(true);
			initDefender.setVisible(false);
			this.name = initAttacker.getName() + " vs " + initDefender.getName();
			
			mouseOver = false;
			
			region = Assets.atlas.findRegion(REGION);
			this.setPosition((initAttacker.getCenterX() + initDefender.getCenterX())/2, (initAttacker.getCenterY() + initDefender.getCenterY())/2);
			this.setWidth(region.getRegionWidth()*getScaleX());
			this.setHeight(region.getRegionHeight()*getScaleY());
			this.setOrigin(region.getRegionWidth()*getScaleX()/2, region.getRegionWidth()*getScaleY()/2);
			
			if (initAttacker.getFaction().crest == null) {
				System.out.println("No crest found for " + initAttacker.getFaction());
			}
			if (initDefender.getFaction().crest == null) {
				System.out.println("No crest found for " + initDefender.getFaction());
			}
			TextureRegion[][] split = initAttacker.getFaction().crest.split(initDefender.getFaction().crest.getRegionWidth()/2, initAttacker.getFaction().crest.getRegionHeight());
			this.halfCrest = split[0][1];
		}
		
		aAdvantage = 1; // for now. make influenced by player's attribute as well as morale.
		dAdvantage = 1;
		
	}
	
	@Override
	public void act(float delta) {
//		if (kingdom.getMapScreen().losOn) {
//			if (Kingdom.distBetween(this, kingdom.getPlayer()) > kingdom.getPlayer().getLineOfSight())
//				setVisible(false);
//			else setVisible(true);
//		}
		
		calcStats();
		meleePhase();
		if (!isOver)
			retreatPhase(delta);
		super.act(delta);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		
		// draw crests too?
		if (kingdom.getMapScreen().losOn && Kingdom.distBetween(this, kingdom.getPlayer()) > kingdom.getPlayer().getLineOfSight()) return;
		
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), 1, 1, getRotation());
		
//		if (aArmies.first() != null)
//			batch.draw(this.aArmies.first().getFaction().crest, getX()+getWidth()/2, getY() + getHeight(), getOriginX(), getOriginY(),
//				getWidth(), getHeight(), 1, 1, getRotation());
//		if (dArmies.first() != null)
//			batch.draw(this.halfCrest, getX(), getY() + getHeight(), getOriginX(), getOriginY(),
//				getWidth()/2, getHeight(), 1, 1, getRotation());
		super.draw(batch, parentAlpha);
	}
	
	public void calcStats() {
		// calculate advantages
		if (playerInA)
			aAdvantage = kingdom.getPlayer().getCharacter().getAttributeFactor("Attacking");
		else if (playerInD) {
			dAdvantage = kingdom.getPlayer().getCharacter().getAttributeFactor("Defending");
			//BottomPanel.log("Defense Advantage = " + dAdvantage, "green");
		}
		
		aAtk = 0;
		
//		aParties.removeValue(null, true);
//		dParties.removeValue(null, true);
		System.out.println(aParties.size);
		
		for (Party p : aParties) {
			p.calcStats();
			aAtk += p.getAtk();
		}
		dAtk = 0;
		for (Party p : dParties) {
			p.calcStats();
			dAtk += p.getAtk();
		}

		balanceA = aAtk*aAdvantage; // method for computing balance
		balanceD = dAtk*dAdvantage;
		double total = balanceA + balanceD;
		balanceA = balanceA / total; // balanceA + balanceD = 1
		balanceD = balanceD / total; 
	}
	
	public void add(Army army) {
		int join = shouldJoin(army);
		if (join == 1)  {
			army.setVisible(false);
			if (army == kingdom.getPlayer()) {
				playerInD = true;
				kingdom.getMapScreen().getSidePanel().setActiveBattle(this);
			}
			//expA 
			dArmies.add(army);
			dParties.add(army.party);
			log(army.getName() + " was added to defenders!", "pink");
			army.setVisible(false);
			initTotalTroops += army.getParty().getHealthySize();
		}
		else if (join == 2) {
			army.setVisible(false);
			if (army == kingdom.getPlayer()) {
				playerInA = true;
				kingdom.getMapScreen().getSidePanel().setActiveBattle(this);
			}
			log(army.getName() + " was added to attackers!", "pink");
			aArmies.add(army);
			aParties.add(army.party);
			army.setVisible(false);
			initTotalTroops += army.getParty().getHealthySize();
		}
		
		else log(army.getName() + " shouldn't join", "red");
	}
	
	
	/**
	 * returns 1 if should join defenders, 2 if attackers, 0 if shouldn't join
	 * @param army
	 * @return
	 */
	public int shouldJoin(Army army) {
		if (aArmies == null || dArmies == null) return 0;
		if (aArmies.size >= 1 && dArmies.size >= 1) {
			if (army.isAtWar(aArmies.first())) {
				if (!army.isAtWar(dArmies.first()))
					return 1; // defenders
			} 
			else if (army.isAtWar(dArmies.first())) {
				if (!army.isAtWar(aArmies.first()))
					return 2; // attackers
			}
		}
		return 0; // shouldn't join
	}
	
	public void remove(Army army) {	
//		System.out.println("removeing " + army.getName());
		if (aArmies.contains(army, true)) {
			aParties.removeValue(army.party, true);
			aArmies.removeValue(army, true);
		}
		else if (dArmies.contains(army, true)) {
			dArmies.removeValue(army, true);
			dParties.removeValue(army.party, true);
		}
		else if (aArmiesRet.contains(army, true)) {
			aArmiesRet.removeValue(army, true);
			aParties.removeValue(army.party, true);
		}
		else if (dArmiesRet.contains(army, true)) {
			dParties.removeValue(army.party, true);
			dArmiesRet.removeValue(army, true);
		}
		else BottomPanel.log("error when removing " + army.getName() + " from battle", "red");
		
//		System.out.println("removing " + army.getName() + " dArmies = "  + dArmies.size + " aArmies = " + aArmies.size + " dArmiesRet = " + dArmiesRet.size + " aArmiesRet = " + aArmiesRet.size);
		
		army.endBattle();
		army.setStopped(false);

		army.setVisible(false);
		if (kingdom.getArmies().contains(army, true) && army.isGarrisoned()) this.setVisible(true);
		
		if (army == kingdom.getPlayer()) {
			playerInA = false;
			playerInD = false;
			kingdom.getMapScreen().getSidePanel().setStay(false);
			kingdom.getMapScreen().getSidePanel().setDefault();
			this.act(.001f);// arbitrary time
		}
	}
	
	public void destroy(Army army) {
		if (aArmies.contains(army, true) || aArmiesRet.contains(army, true)) {
			if (playerInA) log(army.getName() + " was destroyed!", "red");
			else log(army.getName() + " was destroyed!", "green");
			for (Soldier s : army.getParty().getWounded())
				army.getParty().givePrisoner(s, dArmies.random().getParty());
			for (Soldier s : army.getParty().getPrisoners())
				army.getParty().returnPrisoner(s, dArmies.random().getParty());
			remove(army);
		}
		else if (dArmies.contains(army, true) || dArmiesRet.contains(army,true)) {
			if (playerInD) log(army.getName() + " was destroyed!", "red");
			else log(army.getName() + " was destroyed!", "green");
			for (Soldier s : army.getParty().getWounded())
				army.getParty().givePrisoner(s, aArmies.random().getParty());
			for (Soldier s : army.getParty().getPrisoners())
				army.getParty().returnPrisoner(s, aArmies.random().getParty());
			remove(army);
		}
		else BottomPanel.log("error when removing " + army.getName() + " from battle", "red");
		increaseSpoilsForKill(army);
		
		army.destroy();
	}
	
	public void increaseSpoilsForKill(Army army) {
		spoils += army.getParty().wealth;
	}
	
	public void increaseSpoilsForRetreat(Army army) {
		
		double wealthFactor = army.getParty().getWoundedSize()*1.0/army.getParty().getTotalSize();
		
		int wealthChange = (int) (army.getParty().wealth * wealthFactor);
		army.getParty().wealth -= wealthChange;
		spoils += wealthChange;
	}
	
	public void retreat(Army army) {
		army.retreatCounter = MIN_RETREAT_TIME + BASE_RETREAT_TIME / army.getParty().getAvgSpd(); // start countdown
		if (aArmies.contains(army, true)) {
			aArmies.removeValue(army, true);
			aParties.removeValue(army.party, true);
			aArmiesRet.add(army);
			aPartiesRet.add(army.party);
			log(army.getName() + " is retreating!", "yellow");
		}
		else if (dArmies.contains(army, true)) {
			dArmies.removeValue(army, true);	
			dParties.removeValue(army.party, true);
			dArmiesRet.add(army);
			dPartiesRet.add(army.party);

			log(army.getName() + " is retreating!", "yellow");
		}
		increaseSpoilsForRetreat(army);
	}
	
	// returns false if there's been a victory so the next phase can be skipped
	public void meleePhase() {
		calcStats();
		
		if (aArmies.size >= 1) {
			 if (dArmies.size >= 1)
				 attackStep();
			 else if (dArmiesRet.size <= 0) {
//				 System.out.println(getName() + " point 1");
				 victory(aArmies);
				 return;
			 }
		}
		else if (aArmiesRet.size <= 0) {
//			System.out.println(getName() + " point 2");
			victory(dArmies);	
			return;
		}
		
		if (!isOver) { // so battle doesn't "end" twice buggy!
			calcStats();

			if (aArmies.size >= 1) {
				if (dArmies.size >= 1)
					defenseStep();
				else if (dArmiesRet.size <= 0) {
//					System.out.println(getName() + " point 3");
					victory(aArmies);
					return;
				}
			}
			else if (aArmiesRet.size <= 0) {
//				 System.out.println(getName() + " point 4");
				victory(dArmies);
				return;
			}
		}
		// clean up
		if (aArmies.size >= 1) {
			if (dArmies.size == 0 && dArmiesRet.size == 0)
				victory(aArmies);
		}
		else { // just do it regardless of dArmies size in case both retreat or die somehow
			if (aArmies.size == 0 && aArmiesRet.size == 0)
				victory(dArmies);
		}
	}
	
	public void retreatPhase(float delta) {
		calcStats();
		if (aArmiesRet.size >= 1) {
			if (dArmies.size >= 1) {
				attackRetreatStep();
			}
		}
		if (dArmiesRet.size >= 1) {
			if (aArmies.size >= 1) {
				defenseRetreatStep();
			}
		}
		// update counter, check if done retreating
		for (Army army : aArmiesRet) {
			army.retreatCounter -= delta;
			if (army.retreatCounter <= 0) {
				log(army.getName() + " has retreated!", "yellow");
//				System.out.println(army.getName() + " retreat point 1 with counter " + army.retreatCounter);
				if (army.getParty().getHealthySize() <= DESTROY_THRESHOLD) 
					this.destroy(army);
				else
					remove(army);
			}
			if (army.getParty().getHealthySize() <= DESTROY_THRESHOLD) 
				this.destroy(army);
		}
		for (Army army : dArmiesRet) {
			army.retreatCounter -= delta;
			if (army.retreatCounter <= 0) {
				log(army.getName() + " has retreated!", "yellow");
//				System.out.println(army.getName() + " retreat point 2 with counter " + army.retreatCounter);
				if (army.getParty().getHealthySize()  <= DESTROY_THRESHOLD) 
					this.destroy(army);
				else
					remove(army);
			}
			if (army.getParty().getHealthySize()  <= DESTROY_THRESHOLD) 
				this.destroy(army);
		}
	}
	// when anyone in aArmies is retreating
	public void attackRetreatStep() {
		for (Army defense : aArmiesRet) {
			double defenseRoll = Math.random() * SPEED * defense.getParty().getAvgDef();
			//System.out.println("defense roll of attacker : " + defenseRoll);
			if (dAtk*dAdvantage > defenseRoll)
				killOne(defense, true);
		}
	}
	// when anyone in dArmies is retreating
	public void defenseRetreatStep() {
		for (Army defense : dArmiesRet) {
			double defenseRoll = Math.random() * SPEED * defense.getParty().getAvgDef();
			//System.out.println("defense roll of defender : " + defenseRoll);
			if (aAtk*aAdvantage > defenseRoll)
				killOne(defense, false);
		}
	}
	
	public void attackStep() {
		for (Army defense : dArmies) {
			double defenseRoll = Math.random() * SPEED * defense.getParty().getAvgDef();
			//System.out.println("defense roll of defender : " + defenseRoll);
			if (aAtk*aAdvantage > defenseRoll)
				killOne(defense, false);
			if (!defense.getParty().player && balanceD < RETREAT_THRESHOLD) {
				if (defense.type != ArmyType.MILITIA)
					retreat(defense);
			}
		}
	}
	public void defenseStep() {	
		for (Army defense : aArmies) {
			double defenseRoll = Math.random() * SPEED * defense.getParty().getAvgDef();
			//System.out.println("defense roll of attacker : " + defenseRoll);
			if (dAtk*dAdvantage > defenseRoll)
				killOne(defense, true);
			if (!defense.getParty().player && balanceA < RETREAT_THRESHOLD) {
				if (defense.type != ArmyType.MILITIA)
					retreat(defense);
			}
		}
	}
	
	public void killOne(Army army, boolean atkKill) { // kills/wounds one random troop in this army, weighted by the troop's defense
		// Compute the total weight of all soldier's defenses together
		double totalWeight = 0.0d;
		for (Soldier s : army.getParty().getHealthy())
		{
		    totalWeight += 1/s.getDef();
		}
		// Now choose a random soldier
		int randomIndex = -1;
		double randomDouble = Math.random() * totalWeight;
		army.getParty().getHealthy().shrink();
		for (int i = 0; i < army.getParty().getHealthySize(); ++i)
		{
		    randomDouble -= 1/army.getParty().getHealthy().get(i).getDef();
		    if (randomDouble <= 0.0d)
		    {
		        randomIndex = i;
		        break;
		    }
		}
		Soldier random = army.getParty().getHealthy().get(randomIndex);
		casualty(random, atkKill);
		
		if (army.getParty().getHealthySize() <= DESTROY_THRESHOLD) {
			log(army.getName() + " lost all troops and was removed from battle", "red");
			this.destroy(army);
		}
	}
	
	// main thing called by battlestage?
	public void casualty(Soldier soldier, boolean atkKill) {
		boolean killed = soldier.party.casualty(soldier);
		if (atkKill) expD += soldier.getExpForKill();
		else expA += soldier.getExpForKill();
		
		if (playerInD || playerInA) {
			String status = soldier.getName();
			if (soldier.killedBy != null) {
				if (killed) status += " was killed by " + soldier.killedBy.getName() + "!";
				else status += " was wounded by " + soldier.killedBy.getName() + "!";
			}
			else {
				if (killed) status += " was killed!";
				else status += " was wounded!";
			}

			String color = "white";
			// determines color of logged text (yellw if wounded, orange if killed, blue if enemy killed)
			if (playerInD == atkKill) {
				if (killed) color = "red";
				else color = "orange";
			}
//			else if (aArmies.contains(army, true)) {
//				if (playerInD)
//					color = "cyan";
//				else color = "purple";
//			}
			else if (playerInA == atkKill)
				color = "cyan";
			//	else color = "purple";
			
			log(status, color);
		}
	}
	
	public void logDefeat(Army army) {
		log(army.getName() + " was defeated!", "green");
	}
	
	public void victory(Array<Army> victor) {
//		System.out.println("victory in " + name);
//		System.out.println("battle over");
//		if (isOver) System.out.println(getName() + " ENDING BATTLE TWICE!!!?!");
//		isOver = true;
		
		//if ()
		 
		if (kingdom.getMapScreen().getSidePanel().getActivePanel().getClass() == PanelBattle.class &&
				((PanelBattle) (kingdom.getMapScreen().getSidePanel().getActivePanel())).battle == this) 
			kingdom.getMapScreen().getSidePanel().setActiveArmy(kingdom.getPlayer());
		
		if (victor == aArmies) didAtkWin = true;
		else if (victor == dArmies) didAtkWin = false;
		
		int[] victorContribution = new int[victor.size]; // should depend on how much an army sacrificed in battle
		int totalContribution = 0; // maybe number of troops they killed + their own troops killed.
		
		victor.shrink();

		// change faction of city if siege
		if (siegeOf != null) manageSiege();
		
		// manage victorious armies and calculate contributions
		for (int i = 0; i < victor.size; i++) {
			Army army = victor.get(i);
			army.endBattle();
			army.setStopped(false);
			army.forceWait(WAIT);
			if (army.getParty().player) {
				kingdom.getMapScreen().getSidePanel().setStay(false);
				kingdom.getMapScreen().getSidePanel().setDefault();
			}
		
			//	log(army.getName() + " has won a battle", "cyan");
			if (!army.isGarrisoned()) army.setVisible(true);
			army.nextTarget(); // 
			
			if (army.getParty().player) {
//				army.setStopped(true);
				army.setTarget(null);
			}
			
			victorContribution[i] = army.getParty().getAtk(); // for now just do atk.
			totalContribution += victorContribution[i];
		}
		
		// distribute rewards
		for (int i = 0; i < victor.size; i++) {
			double contribution = victorContribution[i]/1.0d/totalContribution;
			this.distributeRewards(victor.get(i), contribution, didAtkWin);
		}
				
//		// TESTING
//		if (victor == aArmies) {
//			for (Army leftOver : dArmies) {
//				System.out.println("***** " + leftOver.getName() + " is still in battle!?!");
//			}
//		}
//		else if (victor == dArmies) {
//			for (Army leftOver : aArmies) {
//				System.out.println("***** " + leftOver.getName() + " is still in battle!?!");
//			}
//		}
	
		//	log("battle ended");
		
		destroy();
	}
	
	public void destroy() {
		if (playerInA || playerInD)
			kingdom.getMapScreen().getSidePanel().setActiveArmy(kingdom.getPlayer());
		
		aArmies.clear();
		dArmies.clear();
		aArmiesRet.clear();
		dArmiesRet.clear();
		aParties.clear();
		dParties.clear();
		aPartiesRet.clear();
		dPartiesRet.clear();
		
		aArmies = null;
		dArmies = null;
		aArmiesRet = null;
		dArmiesRet = null;
		aParties = null;
		dParties = null;
		aPartiesRet = null;
		dPartiesRet = null;
		this.isOver = true;
		
		this.kingdom.removeBattle(this);
		this.remove();
	}
	
	public void distributeRewards(Army army, double contribution, boolean attackVictory) {
		int reward = (int) (contribution*spoils);
		int expReward;
		int moraleReward;
		if (attackVictory) {
			expReward = (int) (contribution*expA);
			moraleReward = (int) (initBalanceA*baseMoraleReward);
		}
		else {
			expReward = (int) (contribution*expD);
			moraleReward = (int) (initBalanceD*baseMoraleReward);
		}
		expReward *= EXP_FACTOR; // just to beef it up
		expReward += MIN_EXP;
		
		if (army.getParty().player) {
//			System.out.println("initBalanceD = " + this.initBalanceD);
//			System.out.println("initBalanceA = " + this.initBalanceA);

			// also distribute honor and fame
			int fameReward;
			if (attackVictory) {
				fameReward = (int) (this.initBalanceD * this.initTotalTroops)/5;
				if (this.initBalanceD < MIN_BALANCE) fameReward = 0;
			}
			else {
				fameReward = (int) (this.initBalanceA * this.initTotalTroops)/5;
				if (this.initBalanceA < MIN_BALANCE) fameReward = 0;
			}
			kingdom.getMapScreen().getCharacter().addFame(fameReward);
			log(army.getName() + " receives " + moraleReward + " morale, " + fameReward + " fame, " + reward + " gold and " + expReward + " experience!", "green");
		}
		army.getParty().wealth += reward;
		army.getParty().distributeExp(expReward);
		army.setMomentum(army.getMomentum()+moraleReward);
	}
	
	public void manageSiege() {
		if (didAtkWin && siegeOf.getSiege() != null) {
			siegeOf.getSiege().siegeSuccess();
			
//			Faction newOwner;
//			if (siegeOf.getSiege() == null) newOwner = aArmies.first().getFaction();
//			else siegeOf.getSiege().siegeSuccess();
//			siegeOf.changeFaction(newOwner);
		}
		else if (siegeOf.getSiege() != null) siegeOf.getSiege().destroy();
	}
	
	public void rangedPhase() {
	}

	@Override
	public Faction getFaction() {
		return null;
	}

	@Override
	public DestType getType() {
		return Destination.DestType.BATTLE;
	}
	
	public String getName() {
		return name;
	}

//	@Override
//	public double distTo(Destination d) {
//		return Math.sqrt((d.getCenterX() - getCenterX()) * (d.getCenterX() - getCenterX())
//				+ (d.getCenterY() - getCenterY()) * (d.getCenterY() - getCenterY()));
//	}
//
//	@Override
//	public double distToCenter(Destination d) {
//		float thisX = getX() + getOriginX();
//		float thisY = getY() + getOriginY();
//		float dX = d.getX() + d.getOriginX();
//		float dY = d.getY() + d.getOriginY();
//		return Math.sqrt((dX - thisX) * (dX - thisX) + (dY - thisY)
//				* (dY - thisY));
//	}
	
	public float getCenterX() {
		return this.getX() + this.getOriginX();
	}
	
	public float getCenterY() {
		return this.getY() + this.getOriginY();
	}
	
	public void log(String text, String color) {
		if (playerInA || playerInD) // only logs info if Player is in this battle
			BottomPanel.log(text, color);
	}

	@Override
	public void setMouseOver(boolean b) {
		mouseOver = b;
	}
	
	// calculates the hypothetical initial balance (0 - 1.0) in a battle between these armies, for the first set (w/ no advantage)
	public static double calcBalance(Array<Army> first, double firstAdvantage, Array<Army> second, double secondAdvantage) {
		int firstAtk = 0;
		int firstSize = 0;
		for (Army a : first) {
			firstAtk += a.getParty().getAtk();
			firstSize += a.getParty().getTotalSize();
		}
		int secondAtk = 0;
		int secondSize = 0;
		for (Army a : second) {
			secondAtk += a.getParty().getAtk();
			secondSize += a.getParty().getTotalSize();
		}
		double balanceFirst = firstAtk*firstAdvantage + firstSize; // method for computing balance
		double balanceSecond = secondAtk*secondAdvantage + secondSize;
		double total = balanceFirst + balanceSecond;
		return balanceFirst / total; // balanceA + balanceD = 1
	}
	
	// used in battle stage
	public void calcBalancePlayer() {
		int firstAtk = 0;
		int firstSize = 0;
		
		for (Party p : aParties) {
			firstAtk += p.getAtk();
			firstSize += p.getHealthySize();
		}
		int secondAtk = 0;
		int secondSize = 0;
		for (Party p : dParties) {
			secondAtk += p.getAtk();
			secondSize += p.getHealthySize();
		}
		double balanceFirst = firstAtk + firstSize; // method for computing balance
		double balanceSecond = secondAtk + secondSize;
		double total = balanceFirst + balanceSecond;
		balanceA = balanceFirst / total; // balanceA + balanceD = 1
		balanceD = 1-balanceA;
		
		if (!firstTimeInit) {
			initBalanceA = balanceA;
			initBalanceD = balanceD;
//			System.out.println(initBalanceA + " " + initBalanceD);
			firstTimeInit = true;
		}
	}
}
