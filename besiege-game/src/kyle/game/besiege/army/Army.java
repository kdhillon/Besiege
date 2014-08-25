/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;


import java.util.Stack;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.Path;
import kyle.game.besiege.Point;
import kyle.game.besiege.Siege;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.Panel;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.voronoi.Center;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class Army extends Actor implements Destination {
	private static final boolean OPTIMIZED_MODE = true;
	private static final int UPDATE_POLYGON_FREQ = 100; // update polygon every x frames
	public final static int SPEED_DISPLAY_FACTOR = 10; // what you multiply by to display speed
	private static final float SCALE_FACTOR = 600f; // smaller is bigger
	private static final float PARTY_SPEED_FACTOR = 3f; // smaller is bigger
	private static final float BASE_SPEED = 5;
	private static final float WAIT = 3; // 3 second wait after garrisoning
	private static final float scale = .6f;
	private static final float cityCollisionDistance = 25;
	private static final float battleCollisionDistance = 15;
	private static final float COLLISION_FACTOR = 10; // higher means must be closer
	public static final float ORIGINAL_SPEED_FACTOR = .020f;
	public static final int A_STAR_FREQ = 20; // army may only set new target every x frames
	private static final float SIZE_FACTOR = .025f; // amount that party size detracts from total speed
	private static final float BASE_LOS = 80;
	private static final int MAX_STACK_SIZE = 10;
	private static final float LOS_FACTOR = 1; // times troops in party
	private static final float momentumDecay = 6; // every N hours, momentum -= 1
	private static final int offset = 30;
	private static final String DEFAULT_TEXTURE = "Player";
	private static final double REPAIR_FACTOR = .5; // if a party gets below this many troops it will go to repair itself.
	private static final float RUN_EVERY = .5f;
	protected static final double WEALTH_FACTOR = 2;
	private static final double ACTING_RANGE_FACTOR = 2; // times player LOS, units within this range will act. otherwise they won't
	
	private boolean mouseOver;
	protected boolean passive; // passive if true (won't attack) aggressive if false;
	
	protected int wealthFactor = 1; // set in children

	private Kingdom kingdom; // parent actor, kingdom
	private String name;
	private Faction faction;

	transient private TextureRegion region;
	public String textureName;

	private float speed;
	public float speedFactor;
	private float lineOfSight;

	private PartyType partyType;
	public Party party;

	private int morale;
	private int momentum; //changes with recent events

	private int updatePolygon;

	protected int lastPathCalc;
	private boolean stopped;
	protected boolean normalWaiting;
	private double waitUntil;// seconds goal
	public boolean forceWait;
	private boolean shouldRepair;

	private boolean startedRunning;

	private Location garrisonedIn;

	public boolean shouldEject; // useful for farmers, who don't need to F during nighttime.
	//	protected boolean isNoble;
	public enum ArmyType {PATROL, NOBLE, MERCHANT, BANDIT, FARMER, MILITIA}; // 3 for patrol, 
	public ArmyType type;

	private Battle battle;
	public float retreatCounter; // needed in battles
	private Siege siege;
	private Destination target;
	private Destination defaultTarget;
	//	private Location defaultTarget;
	protected Stack<Destination> targetStack;
	public Path path;
	protected Army runFrom;
//	public Destination runTo; // use for running
	public Array<Army> targetOf; // armies that have this army as a target
	public int containingCenter;
	public Array<Army> closeArmies;
	public Array<Integer> closeCenters; 
	
	private float timeSinceRunFrom = 0;

	Vector2 toTarget;

	private int currentHour; // used for decreasing momentum every hour
	public boolean playerTouched; // kinda parallel to location.playerIn

	public Army() {
		this.party = PartyType.BANDIT.generate();
		this.party.army = this;
		//for loading
		// restore kingdom, texture region, 
	}
	
	public Army(Kingdom kingdom, String name, Faction faction, float posX, float posY, PartyType pt) {
		this.kingdom = kingdom;
		this.name = name;
		this.faction = faction;
		this.partyType = pt;

		if (pt != null)
			this.party = pt.generate();
		else this.party = new Party();
		this.party.army = this;

		this.speedFactor = ORIGINAL_SPEED_FACTOR;
		this.speed = calcSpeed();
		this.lineOfSight = calcLOS();

		this.morale = calcMorale();
		this.currentHour = getKingdom().getTotalHour();

		this.stopped = true;
		this.normalWaiting = false;
		this.waitUntil = 0;

		this.battle = null;
		this.siege = null;	
		this.runFrom = null;
		this.garrisonedIn = null;
		this.shouldEject = true;

		this.lastPathCalc = 0;
		this.targetStack = new Stack<Destination>();

		this.targetOf = new Array<Army>();

		this.closeArmies = new Array<Army>();
		this.closeCenters = new Array<Integer>();

		this.path = new Path(this);
		
		this.setPosition(posX, posY);
		this.setRotation(0);

		this.toTarget = new Vector2();

		setTextureRegion(DEFAULT_TEXTURE); // default texture

		playerTouched = false;
		
		calcInitWealth();
	}
	// do this after added to kingdom
	public void postAdd() {
	}
	private void initializeBox() {
		this.setScale(calcScale());
		this.setWidth(region.getRegionWidth()*getScaleX());
		this.setHeight(region.getRegionHeight()*getScaleY());
		this.setOrigin(region.getRegionWidth()*getScaleX()/2, region.getRegionWidth()*getScaleY()/2);
	}

	@Override
	public void act(float delta) {


//		System.out.println(this.getName() + " is acting");
		
		if (this.lastPathCalc > 0) this.lastPathCalc--;
		//		setLineOfSight();
		// Player's Line of Sight:
		if (losOn()) {
			if (!withinLOSRange())
				this.setVisible(false);
			else if (!this.isGarrisoned() && !this.isInBattle()) this.setVisible(true);
		}
		else if (!this.isGarrisoned() && !this.isInBattle()) this.setVisible(true);

		if (OPTIMIZED_MODE && !withinActingRange()) return;
		
		if (!getKingdom().isPaused()) {
			playerTouched = false; // only can be selected when game is paused;
		}

		lineOfSight = calcLOS();
		setMorale(calcMorale()); // update morale
		setScale(calcScale()); // set scale;

//		if (this.type == ArmyType.FARMER) System.out.println("farmer acting");
		
		// simple army control flow
		if (isForcedWaiting()) {
			if (getKingdom().currentPanel == this) System.out.println(getName() + " forced wait");

			//if (get) System.out.println(this.getName() + "forced wait");
			wait(delta);
		}
		else {
			if (!isInBattle()) {
				if (isGarrisoned()) 
					garrisonAct(delta); 
				else {
					setSpeed(calcSpeed());
					detectNearby();
					// int result = detectNearby();
					//					if (result != 0)
					//						System.out.println(getName() + " detectNearby() = " + result); // 0 none, 1 run, 2 attack
					if (isRunning()) {
						run();
					}
					else if (isWaiting())
						wait(delta);
					else if (isInSiege())
						siegeAct(delta);
					else {
						uniqueAct();
						if (!path.isEmpty()) {
							path.travel();
							if (targetLost()) nextTarget(); // forgot to do this before...
						}
						else if (this.hasTarget()) {
							//							if (this.type == ArmyType.FARMER) System.out.println(getName() + " here"); 
							detectCollision();
						}
					}
				}
			} 
		}

		//		if (forceWait) { // forces player to wait
		//			wait(delta);
		//			if (!isWaiting())  
		//				forceWait = false;
		//		}
		//		else {
		//			if (!isGarrisoned()) {
		//				if (!isInSiege()) {
		//					if (shouldRepair() && !shouldRepair) shouldRepair = true;
		//					else if ((!shouldRepair() || defaultTarget == null) && shouldRepair) shouldRepair = false;
		//					if (shouldRepair && this.getTarget() != defaultTarget) this.setTarget(defaultTarget);
		//					if (!isInBattle()) {
		//						setStopped(false);
		//						setSpeed(calcSpeed());   // update speed
		//						detectNearby();
		//						if (isRunning()) {
		//							run();
		//							// wait(delta);
		//						}
		//						else {
		//							if (isWaiting()) {
		//								wait(delta);
		//							}
		//							else {
		//								uniqueAct();
		//								if (getTarget() != null) {
		//									path.travel();
		//									if (targetLost()) {
		//										nextTarget();
		//									}
		//								}
		//								else nextTarget();
		//							}
		//						}
		//					}
		//				}
		//				else if (isInSiege()) {
		//					//decide if should leave siege
		//					detectNearbyRunOnly();
		//				}
		//			}
		//			else if (isGarrisoned()) {
		//
		//				party.checkUpgrades();
		//				// if garrisoned and waiting, wait
		//				if (isWaiting()) {
		//					//					System.out.println(this.getName() + " waiting " + this.waitUntil);
		//					wait(delta);
		//				}
		//				// if garrisoned and patrolling, check if coast is clear
		//				else if (hasTarget() || type == ArmyType.NOBLE) {
		//					Army army = closestHostileArmy();
		//					if (army == null || !shouldRunFrom(army)) {
		//						if (shouldEject) {
		//							eject();
		//							setTarget(null);
		//						}
		//						uniqueAct();
		//					}
		//				}
		//			}
		//		}
		party.act(delta);
		momentumDecay();
		//party.distributeExp(60);
	}
	
	private boolean losOn() {
		if (getKingdom() == null) return false;
		return getKingdom().getMapScreen().losOn;
	}

	public void uniqueAct() {
		//actions contained in extensions
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		if (this.getFaction() == null) {
			System.out.println("no faction!");
			return;
		}
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), 1, 1, getRotation());
		//if (mousedOver()) drawInfo(batch, parentAlpha);
	}

	public void drawCrest(SpriteBatch batch) {
		if (this.isInBattle() || this.isGarrisoned() || !this.isVisible() || (this.getFaction() == null)) return;
		float size_factor = .4f;

		size_factor +=  .005*this.party.getTotalSize();

		Color temp = batch.getColor();
		float zoom = getKingdom().getMapScreen().getCamera().zoom;
		zoom *= size_factor; 

		Color clear_white = new Color();
		clear_white.b = 1;	clear_white.r = 1;	clear_white.g = 1;
		clear_white.a = .6f;
		batch.setColor(clear_white);
		
		Matrix4 mx4Font = new Matrix4();
		mx4Font.rotate(new Vector3(0, 0, 1), getKingdom().getMapScreen().getRotation());
		mx4Font.trn(getCenterX(), getCenterY(), 0);
		Matrix4 tempMatrix = batch.getTransformMatrix();
		batch.setTransformMatrix(mx4Font);
		
		batch.draw(this.getFaction().crest, -15*zoom, 5 + 5*zoom, 30*zoom, 45*zoom);
		
		batch.setTransformMatrix(tempMatrix);
		batch.setColor(temp);
	}

	public String getAction() {
		
		// testing
		//if (this.type == ArmyType.NOBLE && ((Noble) this).specialTarget != null) return "Special target: " + ((Noble) this).specialTarget.getName();
		
		if (isInBattle()) return "In battle";
		else if (forceWait) return "Regrouping (" + Panel.format(this.waitUntil-getKingdom().clock() + "", 2) + ")";
		else if (isWaiting()) return "Waiting";
		else if (isRunning()) return "Running from " + getRunFrom().getName(); // + " (Speed: " + Panel.format(getSpeed()*SPEED_DISPLAY_FACTOR + "", 2) + ")";
		//		else if (shouldRepair) return "SHOULD REPAIR";
		else if (isInSiege()) return "Besieging " + siege.location.getName();
		else if (getTarget() != null && getTarget().getType() == Destination.DestType.LOCATION) return "Travelling to " + getTarget().getName(); // + " (Speed: " + Panel.format(getSpeed()*SPEED_DISPLAY_FACTOR + "", 2) + ")";
		else if (getTarget() != null && getTarget().getType() == Destination.DestType.ARMY) return "Following " + getTarget().getName(); // + " (Speed: " + Panel.format(getSpeed()*SPEED_DISPLAY_FACTOR+"", 2) + ")";
		else return getUniqueAction();
	}

	public String getUniqueAction() {
		//contained in extensions;
		return "Travelling"; // + " (Speed: " + Panel.format(getSpeed()*SPEED_DISPLAY_FACTOR+"", 2)+")";
	}

	public boolean detectCollision() {
		//		if (type == ArmyType.FARMER) System.out.println(getName() + " target  = " + target.getName());
		switch (target.getType()) {
		case POINT: // point reached
			return detectPointCollision();
		case LOCATION: // location reached
			return detectLocationCollision();
		case ARMY: // army reached
			return detectArmyCollision();
		case BATTLE: // battle reached
			return detectBattleCollision();
		default:
			return false;
		}
	}

	public boolean detectPointCollision() {
		if (distToCenter(target) < 1) {
			target = null;
			setStopped(true);
			return true;
		}
		return false;
	}

	public boolean detectArmyCollision() {
		Army targetArmy = (Army) target;
		//		System.out.println("collision dist " + (getTroopCount() + targetArmy.getTroopCount())/COLLISION_FACTOR);
		if (distToCenter(targetArmy) < ((getTroopCount() + targetArmy.getTroopCount()))/COLLISION_FACTOR && !targetArmy.isGarrisoned()) {			
			if (isAtWar(targetArmy)) 
				enemyArmyCollision(targetArmy);	
			else 
				friendlyArmyCollision(targetArmy);
			targetArmy.targetOf.removeValue(this, true);
			return true;
		}
		return false;
	}

	public void enemyArmyCollision(Army targetArmy) {
		if (targetArmy.getBattle() == null) {
			createBattleWith(targetArmy, null);
		}
		else {
			// join battle
			if (targetArmy.getBattle().shouldJoin(this) != 0) {
				targetArmy.getBattle().add(this);
				this.setBattle(targetArmy.getBattle());
			}
			else {
				this.nextTarget();
			}
		}
	}

	public void friendlyArmyCollision(Army targetArmy) {
		//follow
	}

	public void createBattleWith(Army targetArmy, Location siegeOf) {
		//		System.out.println(this.getName() + " creating battle");
		if (this == getKingdom().getPlayer()) {
			BottomPanel.log("Attacking " + targetArmy.getName() + "!");
			getKingdom().getPlayer().createPlayerBattleWith(targetArmy, false, siegeOf);
			
			// get nearby armies and make them join battle
			
			//			getKingdom().getMapScreen().getSidePanel().setActiveBattle(b);
			//			getKingdom().getMapScreen().getSidePanel().setStay(true);
		}
		else if (targetArmy == getKingdom().getPlayer()) {
			BottomPanel.log("Attacked by " + this.getName() + "!");
			getKingdom().getPlayer().createPlayerBattleWith(this, true, siegeOf);
			//			getKingdom().getMapScreen().getSidePanel().setActiveBattle(b);
			//			getKingdom().getMapScreen().getSidePanel().setStay(true);
		}
		else {
			Battle b = new Battle(getKingdom(), this, targetArmy);
			this.setBattle(b);
			targetArmy.setBattle(b);
			getKingdom().addBattle(b);
			getKingdom().addActor(b);
		}
		//shouldJoinBattle();
	}

	public boolean detectBattleCollision() {
		if (distToCenter(getTarget()) < battleCollisionDistance) {
			Battle targetBattle = (Battle) target;
			this.joinBattle(targetBattle);
			return true;
		}
		return false;
	}

	public void joinBattle(Battle battle) {
		if (this.party.player) {
			BottomPanel.log("sorry, joining battles has not been implemented yet");
		}
		else {
			if (battle == null) {
				System.out.println("joining null battle");
				return;
			}
			if (this.battle != null) {
				System.out.println("already in battle");
				return;
			}
			battle.add(this);
			this.setVisible(false);
			this.setBattle(battle);
		}
	}

	public boolean detectLocationCollision() {
		if (distToCenter(getTarget()) < cityCollisionDistance) {
			Location targetLocation = (Location) target;
			if (isAtWar(targetLocation)) {
				enemyLocationCollision(targetLocation);
				//				if (type == ArmyType.FARMER) System.out.println(getName() + " detectLocationCollision");
			}
			else friendlyLocationCollision(targetLocation);
			return true;
		}
		return false;
	}

	public void enemyLocationCollision(Location targetLocation) {
		if (!this.passive) {
			if (targetLocation.isVillage()) {
				raid((Village) targetLocation);
			}
			else {
				if (type == ArmyType.BANDIT) 
					this.nextTarget();
				else {
					setStopped(true);
					if (targetLocation.underSiege())
						targetLocation.getSiege().add(this);
					else {
						targetLocation.beginSiege(this);
					}
				}
			}
		}
	}

	public void friendlyLocationCollision(Location targetLocation) {
		//		System.out.println(getName() + " friendslyCollision");
		if (targetLocation != null)
			garrisonIn(targetLocation);
	}

//	public Location getGarrisonedIn() {
//		return this.garrisonedIn;
//	}
	
	public void garrisonIn(Location targetCity) {
		//		System.out.println(getName() + " garrisoning");
		if (targetCity == null) {
			System.out.println("null targetcity");
			return;
		}
		// test to see if should garrison goes here

		targetCity.garrison(this); 
		garrisonedIn = targetCity;

		setTarget(null);
		for (Army followedBy : this.targetOf) {
			if (followedBy.getTarget() == this) followedBy.nextTarget();
			followedBy.targetStack.remove(this);
		}

		// wait/pause AFTER garrisoning!
		if (party.player) {
			getKingdom().setPaused(true);
			this.setWaiting(false);
		}
		else if (type == ArmyType.MERCHANT && ((Merchant) this).goal == targetCity) waitFor(Merchant.MERCHANT_WAIT);
		else if (type != ArmyType.NOBLE) waitFor(WAIT); //arbitrary
	}

	/** do this while garrisoned
	 * 
	 * @param delta time elapsed since last frame
	 */
	public void garrisonAct(float delta) {
		party.checkUpgrades();
		// if garrisoned and waiting, wait
		if (isWaiting()) {
			wait(delta);
		}
		// if garrisoned and patrolling, check if coast is clear
		else if (hasTarget() || type == ArmyType.NOBLE) {
//			Army army = closestHostileArmy();
			// if Noble with faction containing only one city
//			if (type == ArmyType.NOBLE) {
//				if (this.getFaction().cities.size <= 1) {
//					// only eject for special reasons
//					if (army != null && shouldAttack(army))  {
//						setTarget(army);
//						eject(); 
//					}
//				}
//			}
			//			else if (army == null || !shouldRunFrom(army)) {
			if (this.isRunning() && shouldStopRunning()) {
				System.out.println("player should stop running...");
				runFrom = null;
				eject();
				setTarget(null);
				//					System.out.println("ejecting " + this.getName() + " with no target");
			}
			uniqueAct();
			//			}
		}
	}
	
	protected boolean shouldStopRunning() {
		if (this.runFrom == null) return true;
		if (distToCenter(runFrom) >= this.getLineOfSight()) {
			//stopRunning();
			return true;
		}
		return false;
	}

	/** do this while sieging
	 * 
	 * @param delta time elapsed since last frame
	 */
	public void siegeAct(float delta) {

	}

	public void eject() {
		
		if (isGarrisoned())
			garrisonedIn.eject(this);
		else System.out.println("trying to eject from nothing");
	}

	// returns 0 if no army nearby, 1 if shouldRun (runFrom != null), and 2 if shouldAttack nearby army (target == army)
	public int detectNearby() {
		Army army = closestHostileArmy();
		//		if (this.type == ArmyType.MERCHANT) {
		//			if (army != null) System.out.println(getName() + " has cha " + army.getName());
		//			else System.out.println(getName() + " has cha null");
		//		}

		if (army != null) {
			if (shouldRunFrom(army) && runFrom != army)  {
				runFrom(army);
				//				System.out.println(this.getName() + " starting to run from " + army.getName());
				return 1;
			}
			else if (!passive && shouldAttack(army) && (!hasTarget() || target != army)) {
				runFrom = null;
				if (this.isInSiege()) this.leaveSiege();
				setTarget(army);
				return 2;
			}
		}
		return 0;
	}

	//	public void detectNearbyRunOnly() {
	//		//naive approach (N^2)
	//		Army army = closestHostileArmy();
	//		if (army != null) {
	//			if (shouldRunFrom(army) && (!isRunning() || runFrom != army))  {
	//				if (isInSiege())
	//					endSiege();
	//				runFrom(army);
	//			}
	//		}
	//	}


	// returns closest army should run from, else closest army should attack, or null if no armies are close.
	public Army closestHostileArmy() {
		//		if (this.type == ArmyType.PATROL) System.out.println(getName() + " in closest hostile army"); 
		// can (slightly) optimize by maintaining close Centers until this army's center changes! TODO
		// commented for testing
		double closestDistance = Float.MAX_VALUE;
		Army currentArmy = null;
		boolean shouldRun = false; // true if should run from CHA, false otherwise

		// only within 2 levels of adjacent, can expand later
		closeArmies.clear();
		closeCenters.clear();

		if (kingdom.getMap().getCenter(containingCenter) != null) {
			Center containing = kingdom.getMap().getCenter(containingCenter);
			// central one
			closeCenters.add(containingCenter);
			for (Center levelOne : containing.neighbors) {
				if (!closeCenters.contains(levelOne.index, false)) // level one
					closeCenters.add(levelOne.index);
				for (int i = 0; i < levelOne.neighbors.size(); i++) {
					Center levelTwo = levelOne.neighbors.get(i);
					if (!closeCenters.contains(levelTwo.index, false)) // level two
						closeCenters.add(levelTwo.index);
				}
			}

			for (int index: closeCenters){ 
				Center containing2 = getKingdom().getMap().getCenter(index);
				if (containing2.armies != null) closeArmies.addAll(containing2.armies);
			}

			//			System.out.println("Total Armies length: " + getKingdom().getArmies().size);
			//			if (this.type == ArmyType.PATROL) System.out.println(getName() + " CloseArmies Length: " + closeArmies.size);

			for (Army army : closeArmies) {
				if (this.distToCenter(army) < lineOfSight && !army.isGarrisoned()) {
					// hostile troop
					if (isAtWar(army)) {
						if (shouldRunFrom(army)) {
							if (this.distToCenter(army) < closestDistance) {
								shouldRun = true;
								closestDistance = this.distToCenter(army);
								currentArmy = army;
							}
						}
						else if (!shouldRun) {
							if (this.distToCenter(army) < closestDistance) {
								closestDistance = this.distToCenter(army);
								currentArmy = army;
							}
						}
					}
				}
			}
			return currentArmy;
		}
		else {
			//			System.out.println(this.getName() + " containing = null");
			return null;
		}
	}

	public void updatePolygon() {
		if (updatePolygon == UPDATE_POLYGON_FREQ) {
			getKingdom().updateArmyPolygon(this);
			updatePolygon = 0;
		}
		else updatePolygon++;
	}

	public void waitFor(double seconds) {
		normalWaiting = true;
		if (getKingdom() != null)
			this.waitUntil = seconds + getKingdom().clock();
	}
	public void wait(float delta) {
		if (getKingdom().clock() >= waitUntil) {
			//			if (type == ArmyType.MERCHANT) System.out.println(getName() + "stopping wait");
//			System.out.println("stopping wait");
			normalWaiting = false;
			waitUntil = 0;
			if (forceWait) { 
				forceWait = false;
				//				this.stopped = false;
			}
		}
	}

	public void forceWait(float seconds) {
		this.forceWait = true; 
		this.waitFor(seconds);
	}
	// should this army attack that army?
	public boolean shouldAttack(Army that) {
//		if ((this.getTroopCount() - that.getTroopCount() >= 1) && (this.getTroopCount() <= that.getTroopCount()*4) && (that.getBattle() == null || that.getBattle().shouldJoin(this) != 0))
//			return true; 
		// TODO take into account nearby parties
		if ((this.getParty().getAtk() - that.getParty().getAtk() >= 1) && (this.getTroopCount() <= that.getTroopCount()*2) && (that.getBattle() == null || that.getBattle().shouldJoin(this) != 0))
			return true; 
		return false;
	}

	public boolean shouldRunFrom(Army that) {
		//		if (this.type == ArmyType.PATROL)System.out.println(getName() + " in shouldRun method"); 

		if (this.getTroopCount() < that.getTroopCount())
			return true;
		return false;
	}

	public boolean shouldRepair() {
		if (defaultTarget != null) {
			boolean repair = this.getParty().getHealthySize() <= this.partyType.getMinSize()*REPAIR_FACTOR;
			//			if (repair) System.out.println(this.getName() + " should repair (min size is " + this.partyType.getMinSize() + ") and defaultTarget is " + getDefaultTarget());
			return (repair);
		}
		else {
			return false;
		}
	}
	public boolean targetLost() {
		if (target != null && target.getType() == Destination.DestType.ARMY) {
			Army targetArmy = (Army) target;
			if (distToCenter(target) > lineOfSight || !targetArmy.hasParent() || targetArmy.isGarrisoned())
				return true;
			if (targetArmy.isInBattle()) {
				if (targetArmy.getBattle().shouldJoin(this) == 0) // shouldn't join
					return true;
			}
		}
		//		if ()
		return false;
	}

	public void momentumDecay() {
		if (momentum >= 1) {
			//			System.out.println("total: " + getKingdom().getTotalHour() + " current: " + currentHour);
			if (getKingdom().getTotalHour() - currentHour >= momentumDecay) {
				momentum -= 1;
				currentHour = getKingdom().getTotalHour();
			}
		}
	}

	public void destroy() {
		if (isGarrisoned()) {
			getGarrisonedIn().eject(this);
		}
//		System.out.println("army destroyed");
		
		
		if (this.type == ArmyType.NOBLE) {
			// 50 percent chance to move to random city
//			if (Math.random() < .5) {
//				if (this.defaultTarget != null && this.defaultTarget.getType() == Destination.DestType.LOCATION && this.defaultTarget.getFaction() == this.getFaction()) {
//					this.setPosition(this.defaultTarget.getCenterX(), this.defaultTarget.getCenterY());
//					this.garrisonIn((Location) this.defaultTarget);
//					return;
//				}
//			}
	
			// do a noble kill -- either escaped, ransomed, or executed! Allow execution of nobles D:
			this.faction.removeNoble((Noble) this);
		}
		
		getKingdom().removeArmy(this);
		this.remove();
	}

	//	public void verifyTarget() {
	//		if (getTarget().getType() == 2) { // army
	//			Army targetArmy = (Army) getTarget();
	//			if (!getKingdom().getArmies().contains(targetArmy, true))
	//				nextTarget();
	//		}
	//	}

	public void runFrom(Army runFrom) {
		// only let armies run from a new army every .5 seconds
		if (runFrom != null) {
			if (timeSinceRunFrom < RUN_EVERY) {
				timeSinceRunFrom += Gdx.graphics.getDeltaTime();
				return;
			}
			else timeSinceRunFrom = 0;
		}
		
		if (runFrom != null) setTarget(null);
		this.runFrom = runFrom;
	}

	public void stopRunning() {
		//		System.out.println(getName() + " stopping running");
		runFrom = null;
		startedRunning = false;
		nextTarget();
	}
	public Army getRunFrom() {
		return runFrom;
	}

	public void run() { // for now, find a spot far away and set path there
		if (normalWaiting) normalWaiting = false;
		
		// this is the problem. path is not empty, but it's not getting empty;
		if (startedRunning && this.hasTarget() && !this.path.isEmpty() ) {
			//this.detectCollision();
			//if (this.type == ArmyType.FARMER) System.out.println(this.getName() + " is running");
			path.travel(); 
		}
		else {
			Location goTo = detectNearbyFriendlyCity();
			if (shouldStopRunning()) stopRunning();
			else if (goTo != null) {
				setTarget(goTo);
				setSpeed(calcSpeed());   // update speed
			//	this.detectCollision();
				path.travel();
				startedRunning = true;
			//	System.out.println(this.getName() + " is travelling to target");
			}
			// find new target an appropriate distance away, travel there.
			else { //if (!this.hasTarget()) {
				//			System.out.println(getName() + " is running");
				//			setTarget(getKingdom().getCities().get(0));
				//				System.out.println(getName() + " getting new random run target");
				float distance = getLineOfSight();

				toTarget.x = getCenterX() - runFrom.getCenterX();
				toTarget.y = getCenterY() - runFrom.getY();
				toTarget.scl(1/toTarget.len()); // set vector length to 1
				toTarget.scl(distance);

				// TODO make memory efficient by keeping only one point
				Point p = new Point(getCenterX() + toTarget.x, getCenterY() + toTarget.y);

				float rotation = 10;
				while (getKingdom().getMap().isInWater(p) && rotation < 360) {
					toTarget.rotate(rotation);
					rotation += 10;
					p.setPos(getX() + toTarget.x, getY() + toTarget.y);
					//					System.out.println("rotating to find new target");
				}
				if (rotation > 360) {// no escape, probably in water
					p.setPos(getCenterX(), getCenterY());
					//				System.out.println("rotated all the way");
				}
				setTarget(p);
				//				this.runTo = p;
				startedRunning = true;
			}
		}
	}

	public Location detectNearbyFriendlyCity() {
		for (City city : getKingdom().getCities()) {
			if (!isAtWar(city) && this.distToCenter(city) < getLineOfSight() && this.distToCenter(city) < runFrom.distToCenter(city)) {
				return city;
			}
		}
		return null;
	}

	public void raid(Village village) {
		//System.out.println(this.name + " is raiding " + village.getName());
		Militia militia = village.createMilitia();
		getKingdom().addArmy(militia);
		createBattleWith(militia, village);
	}
	
	public void calcInitWealth() {
		this.party.wealth = (int) (this.wealthFactor * this.party.getAtk() * WEALTH_FACTOR);
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	public float getSpeed() {
		return speed;
	}
	public float calcSpeed() {
		// make speed related to party's speed, morale, and army's unique speed factor, 
		// simplified version for testing
		//return (BASE_SPEED + party.getAvgSpd()*PARTY_SPEED_FACTOR)*speedFactor;
		return (BASE_SPEED + morale/30 + party.getAvgSpd()*PARTY_SPEED_FACTOR - party.getTotalSize()*SIZE_FACTOR)*speedFactor;
	}
	public float getScale() {
		return scale;
	}
	public float calcScale() {
		return scale + scale*getTroopCount()/SCALE_FACTOR;
	}
	@Override
	public void setScale(float scale) {
		if (this.getTextureRegion() == null) {
			this.setTextureRegion(textureName);
			return;
		}
		
		super.setScale(scale);
		this.setWidth(region.getRegionWidth()*getScaleX());
		this.setHeight(region.getRegionHeight()*getScaleY());
		this.setOrigin(region.getRegionWidth()*getScaleX()/2, region.getRegionHeight()*getScaleY()/2);
	}
	public void setMorale(int morale) {
		this.morale = morale;
	}
	
	
	public int calcMorale() {
		// 100 =  25     			  +  25      + 50;
		
		int base = 0;
		int party_bonus = Math.min(25, 100 - getTroopCount());
		int free_bonus = 25;
		int momentum_bonus = Math.min(50, momentum);
		
		return base + party_bonus + free_bonus + momentum_bonus;
		// return (100 - getTroopCount())/4 + momentum;
	}
	public int getMorale() {
		return morale;
	}
	public String getMoraleString() {
		if (morale < 10) return "Awful";
		if (morale < 35) return "Low";
		if (morale < 50) return "Okay";
		if (morale < 65) return "Good";
		if (morale < 90) return "Great";
		else return "Jolly";
	}
	public void setMomentum(int momentum) {
		if (momentum >= 50) {
			this.momentum = 50;
		}
		else if (momentum <= 0) {
			this.momentum = 0;
		}
		else this.momentum = momentum;
	}
	public int getMomentum() {
		return momentum;
	}
	public float calcLOS() {
		if (getKingdom() == null) return 0;
			//		return BASE_LOS + LOS_FACTOR * this.getTroopCount(); // * getKingdom().currentDarkness
		return BASE_LOS + LOS_FACTOR * this.getTroopCount() * getKingdom().currentDarkness;
	}
	public void setLOS(float lineOfSight) {
		this.lineOfSight = lineOfSight;
	}

	public Faction getFaction() {
		return faction;
	}
	public void setFaction(Faction faction) {
		this.faction = faction;
	}
	public DestType getType() {
		return Destination.DestType.ARMY; // army type
	}
	public void setBattle(Battle battle) {
		this.battle = battle;
	}
	public Battle getBattle() {
		return battle;
	}
	public void endBattle() {
		//		path.travel();
		if (siege != null) leaveSiege();
		battle = null;
		//		if (type == ArmyType.MERCHANT) System.out.println(getName() + " ending battle");
	}
	public void besiege(Location location) {
		if (location.getSiege() == null) { 
			location.beginSiege(this);
		}
		else if (location.getSiege().besieging != this.faction) {
			this.nextTarget();
		}
		else location.joinSiege(this);
	}
	public void setSiege(Siege siege) {
		this.siege = siege;
	}
	public Siege getSiege() {
		return siege;
	}
	public void leaveSiege() {
		//		System.out.println(this.getName() + " is ending siege");
		nextTarget();
		if (siege != null) {
			if (siege.location == this.getTarget()) nextTarget();
			//			while (siege.location == this.getTarget()) {
			//				System.out.println(getName() + " ending siege and going to new target");
			//				nextTarget();
			//			}
			siege.remove(this);
		}
		siege = null;           
	}
	public boolean isInSiege() {
		return siege != null;
	}
	public int getTroopCount() {
		return party.getTotalSize();
	}

	public boolean setTarget(Destination newTarget) {
		if (newTarget == null) {
			path.dStack.clear();
			path.nextGoal = null;
			// figure out how to reconcile this with path?
			//System.out.println(getName() + " has null target");
			return false;
		}	
		// replace old targetof
		if (getTarget() != null && getTarget().getType() == Destination.DestType.ARMY) {
			((Army) getTarget()).targetOf.removeValue(this, true);
		}

		//		if (this.type == ArmyType.FARMER)System.out.println("farmer in setTarget"); 
		// don't add same target twice in a row... this is a problem.
		//		if (newTarget.getType() == 2 && ((Army) newTarget).isGarrisoned()) System.out.println("***** TARGET GARRISONED! *****");


		boolean isInWater = getKingdom().getMap().isInWater(newTarget);
		if (!isInWater && !(newTarget.getType() == Destination.DestType.ARMY && ((Army) newTarget).isGarrisoned())) {
			if (this.target != newTarget && (this.lastPathCalc == 0 || this.isPlayer())) {

				if (!this.isWaiting() && this.isGarrisoned()) this.eject(); 
				
				// don't add a bunch of useless point and army targets
				if (this.target != null && this.target.getType() != Destination.DestType.ARMY && this.target.getType() != Destination.DestType.POINT && targetStack.size() < MAX_STACK_SIZE) {
					targetStack.push(this.target);
					//					System.out.println(getName() + " pushing " + this.target.getName() + " stack size: " + this.targetStack.size() + " new target " + newTarget.getName());
				}
				this.target = newTarget;
				//				if (newTarget != null && this.path.isEmpty()) {
				if (newTarget != null && this.path.finalGoal != newTarget) {
					if (this.path.calcPathTo(newTarget)) {
						this.lastPathCalc = A_STAR_FREQ;
						path.next();
					}
					else {
						System.out.println(getName() + " failed A*");
					}
				}
				else if (newTarget == this.path.finalGoal) System.out.println("new goal is already in path");
			}
			else {
				//				System.out.println(getName() + "adding same target twice");
				return false;
			}
			if (newTarget.getType() == Destination.DestType.ARMY) ((Army) newTarget).targetOf.add(this);
			return true;
		}
		else if (defaultTarget != null &&
				!getKingdom().getMap().isInWater(defaultTarget)){
			//			System.out.println(getName() + " trying to go to the water (setting target to center of land)");
			setTarget(defaultTarget);
			return true;
		}
		else if (!isInWater) {
			setTarget(getKingdom().getMap().referencePoint);
			return true;
		}
		else return false;
	}
	public boolean hasTarget() {
		return getTarget() != null;
	}
	public Destination getTarget() {
		return target;
	}
	/* fix this */
	public void nextTarget() {
		if (!targetStack.isEmpty()) {
			if (targetStack.peek() == null || targetStack.peek() == target || 
					(targetStack.peek().getType() == Destination.DestType.ARMY && !getKingdom().getArmies().contains((Army) getTarget(), true))) 
				targetStack.pop(); // clean stack 
			if (!targetStack.isEmpty() && targetStack.peek() != null) setTarget(targetStack.pop());
			else findTarget();
		}
		else {
			findTarget();
		}
	}
	public void newTarget(Destination target) {
		targetStack.removeAllElements();
		setTarget(target);
	}
	public void findTarget() {
		//		if (this.type == ArmyType.BANDIT) System.out.println("bandit finding target"); 
		setTarget(defaultTarget);
	}
	public void setDefaultTarget(Destination defaultTarget) {
		this.defaultTarget = defaultTarget;
	}
	//	public void setDefaultTarget(Location defaultTarget) {
	//		this.defaultTarget = defaultTarget;
	//	}
	public Destination getDefaultTarget() {
		return defaultTarget;
	}
	public Kingdom getKingdom() {
		return kingdom;
		//return (Kingdom) getParent();
	}
	
	public int getWealth() {
		return this.getParty().wealth;
	}
	public void changeWealth(int delta) {
		this.getParty().wealth += delta;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public float getLineOfSight() {
		return lineOfSight;
	}
	//	public void addMoney(int money) {
	//		this.money += money;
	//	}
	//	public void loseMoney(int money) {
	//		this.money -= money;
	//	}
	//	public int getMoney() { // lol
	//		return money;
	//	}
	//	public void setMoney(int money) {
	//		if (money >= 100) {
	//			this.money = 100;
	//		}
	//		if (money <= 0) {
	//			this.money = 0;
	//		}
	//		else this.money = money;
	//	}
	public boolean isAtWar(Destination destination) {
		return kingdom.isAtWar(faction, destination.getFaction());
	}

	// laziness sake
	protected double distToCenter(Destination d) {
		return getKingdom().distBetween(this, d);
	}
	
	private boolean isPlayer() {
		return this.getParty().player;
	}

	//	@Override
	//	public double distToCenter(Destination d) {
	//		float thisX = getX() + getOriginX();
	//		float thisY = getY() + getOriginY();
	//		float dX = d.getX() + d.getOriginX();
	//		float dY = d.getY() + d.getOriginY();
	//		return Math.sqrt((dX-thisX)*(dX-thisX) + (dY-thisY)*(dY-thisY));
	//	}
	//
	//	@Override
	//	public double distTo(Destination d) {
	////		return Math.sqrt((d.getX()-getCenterX())*(d.getX()-getCenterX())+(d.getY()-getCenterY())*(d.getY()-getCenterY()));
	//		return Math.sqrt((d.getCenterX()-getCenterX())*(d.getCenterX()-getCenterX())+(d.getCenterY()-getCenterY())*(d.getCenterY()-getCenterY()));
	//	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
	public void setWaiting(boolean waiting) {
		this.normalWaiting = waiting;
	}
	public void setForceWait(boolean forceWait) {
		this.forceWait = forceWait;
	}
	public boolean isStopped() {
		return stopped;
	}
	public boolean isWaiting() {
		return normalWaiting || forceWait;
	}
	public boolean isForcedWaiting() {
		return forceWait;
	}
	public boolean isRunning() {
		return runFrom != null;
	}
	public boolean isInBattle() {
		return battle != null;
	}
	@Override
	public void setMouseOver(boolean mouseOver) {
		if (this.mouseOver) {
			if (!mouseOver)
				getKingdom().getMapScreen().getSidePanel().returnToPrevious();
		}
		else if (mouseOver)
			getKingdom().getMapScreen().getSidePanel().setActiveArmy(this);

		this.mouseOver = mouseOver;
	}
	public boolean mousedOver() {
		return mouseOver;
	}
	public boolean isGarrisoned() {
		return (garrisonedIn != null);
	}
	public void setGarrisonedIn(Location city) {
		this.garrisonedIn = city;
	}

	public boolean isGarrisonedIn(Location city) {
		if (this.garrisonedIn == city)
			return true;
		return false;
	}
	public Location getGarrisonedIn() {
		return garrisonedIn;
	}
	public void setTextureRegion(String textureRegion) {
		this.textureName = textureRegion;
		this.region = Assets.atlas.findRegion(textureRegion);
		this.initializeBox();
	}
	public TextureRegion getTextureRegion() {
		return region;
	}
	public float getCityCollisionDistance() {
		return cityCollisionDistance;
	}
	public int getOffset() {
		return offset;
	}
	public double getWaitUntil() {
		return waitUntil;
	}
	public void setWaitUntil(double waitUntil) {
		this.waitUntil = waitUntil;
	}
	public float getCenterX() {
		// modified
		return getX() + getOriginX();
	}
	public float getCenterY() {
		// modified
		return getY() + getOriginY();
	}
	public String getFactionName() {
		return faction.name;
	}
	public void setParty(Party party) {
		party.army = this;
		this.party = party;
	}
	public Party getParty() {
		return party;
	}
	public PartyType getPartyType() {
		return partyType;
	}
	public Point toPoint() {
		return new Point(getCenterX(), getCenterY());
	}
	public kyle.game.besiege.Character getCharacter() {
		return getKingdom().getMapScreen().getCharacter();
	}
	// prepares this object for saving, removing unnecessary references
	public void nullify() {
		this.remove();
//		this.path.map = null;
//		this.kingdom = null;
		//for now nullify path
//		this.path = null;
		//this.faction = null;
		
//		this.closeCenters.clear();
		//this.containing = null;
	}
	public void restore(Kingdom kingdom) {
		kingdom.addActor(this);
//		this.kingdom = kingdom;
//		this.path = new Path(this);
//		this.path.map = kingdom.getMap();
//		this.faction = Faction.BANDITS_FACTION;
	}
	
	public boolean isNoble() {
		return (this.type == ArmyType.NOBLE);
	}
	public boolean isFarmer() {
		return (this.type == ArmyType.FARMER);
	}
	public boolean isMerchant() {
		return (this.type == ArmyType.MERCHANT);
	}
	public boolean isPatrol() {
		return (this.type == ArmyType.PATROL);
	}
	
	public boolean withinLOSRange() {
		return (Kingdom.sqDistBetween(this, getKingdom().getPlayer()) < getKingdom().getPlayer().losSquared);
	}
	
	public boolean withinActingRange() {
		if (this.isNoble()) return true;
		return (Kingdom.sqDistBetween(this, getKingdom().getPlayer()) < getKingdom().getPlayer().losSquared*ACTING_RANGE_FACTOR);
	}
	public Center getContaining() {
		return kingdom.getMap().getCenter(containingCenter);
	}
	public void restoreTexture() {
		this.setTextureRegion(textureName);
	}
}
