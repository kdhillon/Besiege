/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;


import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.*;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.battle.BattleActor;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.Panel;
import kyle.game.besiege.party.General;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.UnitDraw;
import kyle.game.besiege.voronoi.Center;

import static kyle.game.besiege.location.Location.getAdjustedZoom;

public class Army extends Group implements Destination {
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
	private static final float COLLISION_FACTOR = 5; // higher means must be closer
	public static final float ORIGINAL_SPEED_FACTOR = .020f;

	public static final int TIME_TO_SET_AMBUSH = 1;

	public static final int A_STAR_FREQ = 200; // army may only set new target every x frames
	private static final float SIZE_FACTOR = .025f; // amount that playerPartyPanel size detracts from total speed
	private static final float BASE_LOS = 200;
	private static final int MAX_STACK_SIZE = 10;
	private static final float LOS_FACTOR = 1; // times troops in playerPartyPanel
	private static final float momentumDecay = 6; // every N hours, momentum -= 1
	private static final int offset = 30;
	private static final double REPAIR_FACTOR = .5; // if a playerPartyPanel gets below this many troops it will go to repair itself.
	private static final float RUN_EVERY = .5f;
	protected static final double WEALTH_FACTOR = 2;
	private static final double ACTING_RANGE_FACTOR = 2; // times player LOS, units within this range will act. otherwise they won't
	private static final Color clear_white = new Color(1f, 1f, 1f, .6f);
	private static float ANIMATION_LENGTH = 0.25f;

	public boolean passive; // passive if true (won't attack) aggressive if false;
    float stateTime;

    Animation walkArmor, walkSkin;

    // for font kingdomRotation
	private Matrix4 mx4Font = new Matrix4();
	
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
	public boolean player;
	public Party party;

	private int morale;
	private int momentum; //changes with recent events

	private int updatePolygon;

	protected int lastPathCalc;
	private boolean stopped;
	protected boolean normalWaiting;
	private double waitUntil;// seconds goal
	public boolean forceWait;
	public boolean isGarrison;

	private boolean startedRunning;

	// This is what sets the unitdraw kingdomRotation. Keeping this separate from getKingdomRotation()
	// allows us to draw the unit and its crest at different rotations
	private float actualRotation;

	private Location garrisonedIn;

//	public boolean shouldEject; // useful for farmers, who don't need to F during nighttime.
	//	protected boolean isNoble;
	public enum ArmyType {PATROL, NOBLE, MERCHANT, BANDIT, FARMER, MILITIA, HUNTER}; // 3 for patrol,
	public ArmyType type;

	private BattleActor battleActor;
	public float retreatCounter; // needed in battles
	private Siege siege;
	private Destination target;
	private transient Destination defaultTarget;
	//	private Location defaultTarget;
	protected Stack<Destination> targetStack;
	public Path path;
	protected Army runFrom;
//	public Destination runTo; // use for running
	public Array<Army> targetOf; // armies that have this army as a target
	public int containingCenter = -1;
	public transient Array<Army> closeArmies;
	public Array<Integer> closeCenters; 
	
	private boolean ambushStarted = false;
	public float timeSinceAmbushSet;
	
	private float timeSinceRunFrom = 0;

	Vector2 toTarget;

	private int currentHour; // used for decreasing momentum every hour
	public boolean playerTouched; // kinda parallel to location.playerIn

	private boolean justLoaded;

	public CrestDraw crestDraw;

	public Army() {
//		this.playerPartyPanel = PartyType.generateDefault(PartyType.Type.FARMERS);
//		this.playerPartyPanel.army = this;
		//for loading
		// restore kingdom, texture region, 
		// not sure if this will fix saving bug
		this.closeArmies = new Array<Army>();
		this.justLoaded = true;
	}
	
	public Army(Kingdom kingdom, String name, Faction faction, float posX, float posY, PartyType.Type type, Location location) {
		this(kingdom, name, faction, posX, posY, type, false, location);
	}
	
	public Army(Kingdom kingdom, String name, Faction faction, float posX, float posY, PartyType.Type type, boolean player) {
		this(kingdom, name, faction, posX, posY, type, player, null);
	}

	public Army(Kingdom kingdom, String name, Faction faction, float posX, float posY, PartyType.Type type, boolean player, Location location) {
		this.kingdom = kingdom;
		this.name = name;
		this.faction = faction;
//		this.partyType = pt;
		this.player = player;
		
//		else this.playerPartyPanel = new Party();

		this.stopped = true;
		this.normalWaiting = false;
		this.waitUntil = 0;

		this.battleActor = null;
		this.siege = null;	
		this.runFrom = null;
		this.garrisonedIn = null;
//		this.shouldEject = true;

		this.lastPathCalc = 0;
		this.targetStack = new Stack<Destination>();

		this.targetOf = new Array<Army>();

		this.closeArmies = new Array<Army>();
		this.closeCenters = new Array<Integer>();

		this.path = new Path(this, kingdom);

        restoreAnimation();
	
		this.setPosition(posX, posY);
//		this.setKingdomRotation(90);

		kingdom.updateArmyPolygon(this);

		if (type != null) {
			Center containing = kingdom.getMap().getCenter(containingCenter);
			if (containing == null) {
				containing = kingdom.getMap().reference;
//				 throw new java.lang.NullPointerException();
			}
			if (location != null) {
				if (location.cultureType == null) {
				    System.out.println(location.getName() + " hasn't had its biomes set " + (this.getName()));
				    throw new AssertionError();
                }
				if (this.isBandit()) System.out.println("Generating party type for bandit with location");
				this.partyType = PartyType.generatePT(type, location);
			} else {
				if (this.isBandit()) System.out.println("Generating party type for bandit with containing");
				// Initialize this playerPartyPanel with the location's garrison type.
				this.partyType = PartyType.generatePT(type, containing);
			}
			generateParty(player);
		} else {
			System.out.println("NULL TYPE!!!");
			 throw new java.lang.NullPointerException();
	}

		if (party == null) throw new java.lang.NullPointerException();
		
		this.speedFactor = ORIGINAL_SPEED_FACTOR;
		this.speed = calcSpeed();
		this.lineOfSight = calcLOS();

		this.morale = calcMorale();
		this.currentHour = getKingdom().getTotalHour();
		

		this.toTarget = new Vector2();

//		setTextureRegion(DEFAULT_TEXTURE); // default texture

		playerTouched = false;
		this.setVisible(false);

        this.addListener(getNewInputListener());
		crestDraw = new CrestDraw(this);
		this.addActor(crestDraw);

		calcInitWealth();
	}

    private InputListener getNewInputListener() {
        return new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                boolean touchdown=true;
                //do your stuff
                //it will work when finger is released..
                System.out.println("Touched up " + getName());
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                boolean touchdown=false;
                //do your stuff it will work when u touched your actor
                return true;
            }

            @Override
            public void enter(InputEvent event,  float x, float y, int pointer, Actor fromActor) {
                System.out.println("Mousing over " + getName());
                System.out.println("Setting panel! " + getName());
                kingdom.setPanelTo((Army) event.getListenerActor());
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                System.out.println("returning to previous (exit)");
                getKingdom().mouseOverCurrentPoint();
            }
        };
    }

	// do this after added to kingdom
	public void postAdd() {
	}
	private void initializeBox() {
		this.setScale(calcScale());

//		this.setWidth(region.getRegionWidth()*getScaleX());
//		this.setHeight(region.getRegionHeight()*getScaleY());
		this.setOrigin(getWidth()/2, getHeight()/2);
	}
	
	public void generateParty(boolean player) {
		this.party = partyType.generate(player);		
		this.party.army = this;
	}

	protected void updateVisibility() {
        if (losOn()) {
            if (!withinLOSRange())
                this.setVisible(false);
            else if (!this.isGarrisoned() && !this.isInBattle()) this.setVisible(true);
        }
        else if (!this.isGarrisoned() && !this.isInBattle()) this.setVisible(true);
    }

    private void stopWaiting() {
	    normalWaiting = false;
	}

	public void setActualRotation(float rotation) {
		this.actualRotation = rotation;
	}

	public void startAmbush() {
//		System.out.println("Starting ambush: " + timeSinceAmbushSet);
		if (!ambushStarted) {
			this.ambushStarted = true;
			timeSinceAmbushSet = 0;
		}
	}

	public void endAmbush() {
//		System.out.println("Ending ambush: " + timeSinceAmbushSet);
		this.ambushStarted = false;
		timeSinceAmbushSet = 0;
	}

	@Override
	public void act(float delta) {
	    if (isRunning()) {
	        stopWaiting();
        }

        if (!isWaiting())
            stateTime += delta;

//		System.out.println(this.getName() + " is acting");
		// Why do we do this?
		if (this.justLoaded) {
			this.defaultTarget = this.faction.getRandomCity();
		}
		
		if (this.party == null) {
			throw new java.lang.NullPointerException();
		}

		// TODO replace with efficient process
        this.lastPathCalc = 0;
//		if (this.lastPathCalc > 0) this.lastPathCalc--;
		//		setLineOfSight();
		// Player's Line of Sight:
		updateVisibility();

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
					//						System.out.println(getName() + " detectNearby() = " + result); // 0 none, 1 setAppropriateRunTarget, 2 attack
					if (isRunning()) {
                        if (shouldStopRunning()) {
                            stopRunning();
//                            System.out.println(this.getName() + "Stopping running");
                        } else {
                        	// If a good distance away, hide until enemy is gone.
							// TODO allow army to hide (defensively).
                            setAppropriateRunTarget();
                            if (getTarget() == null) throw new AssertionError(this.getName() + " has no target while running");
                        }
                        path.travel();
                    }
					else if (isWaiting()) {
						incrementAmbush(delta);
						wait(delta);
					}
					else if (isInSiege())
						siegeAct(delta);
					else {
						uniqueAct();
						if (!path.isEmpty()) {
							path.travel();
						} else {
//							System.out.println("Path is empty");
							nextTarget();
							if (getTarget() == null) throw new AssertionError();
//							if (path.isEmpty()) throw new AssertionError();
						}
						if (this.hasTarget()) {
							//							if (this.type == ArmyType.FARMER) System.out.println(getName() + " here"); 
							detectCollision();
						}
						if (targetLost()) nextTarget(); // forgot to do this before...
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
		//							setAppropriateRunTarget();
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
		//					//decide if should leave siegeOrRaid
		//					detectNearbyRunOnly();
		//				}
		//			}
		//			else if (isGarrisoned()) {
		//
		//				playerPartyPanel.checkUpgrades();
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
		//playerPartyPanel.distributeExp(60);

		// This is a hack so that the crestdraw detects touches in the right location
		super.act(delta);
	}

	protected void incrementAmbush(float delta) {
		if (ambushStarted && timeSinceAmbushSet <= TIME_TO_SET_AMBUSH) {
//							System.out.println(timeSinceAmbushSet + " ambush time");
			timeSinceAmbushSet += delta;

			// Ambush is set. Stop any followers
			if (timeSinceAmbushSet > TIME_TO_SET_AMBUSH) {
				changeTargetOfAnyFollowers();
			}
		}
	}

	public boolean isInAmbush() {
		return ambushStarted && timeSinceAmbushSet > TIME_TO_SET_AMBUSH;
	}
	
	private boolean losOn() {
		if (getKingdom() == null) return false;
		return getKingdom().getMapScreen().losOn;
	}

	public void uniqueAct() {
		//actions contained in extensions
	}

	private Center getCenter() {
		if (containingCenter < 0) return null;
		return kingdom.map.vg.centers.get(containingCenter);
	}

	private boolean inFog() {
		if (getCenter() == null) return false;
		if (!getCenter().discovered && kingdom.getMapScreen().fogOn) return true;
		return false;
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
//		if (this.getFaction() == null) {
//			return;
//		}
		if (inFog()) return;

		// Don't draw while in battle.
		if (isInBattle()) return;

		if (isNonFriendlyAmbush(kingdom.getPlayer())) return;

		float animationTime = 0;
		if (isArmyMoving()) {
			animationTime = stateTime;
		}

		float oldRotation = getRotation();
		setRotation(actualRotation);

		float scale = 1.25f;
		scale *= this.getSizeFactor();
		scale *= getAdjustedZoom(getKingdom());
		this.setScale(scale * 10);

		// TODO one way to make this look better might be to change the scale of the crest, but not the unit.

        UnitDraw.drawUnit(this, batch, walkArmor, walkSkin, getGeneralArmorColor(), getGeneralSkinColor(), animationTime, getGeneral().getEquipment());
		setRotation(oldRotation);

		// We're going to force kingdomRotation to be upright -- that way crest (a child) will be drawn upright
		// This is the same way location works.
		setRotation(kingdom.getMapScreen().kingdomRotation);

        // draw los
		drawLOS();

		// This is a hack so that the crestdraw is drawn upright.
//		float kingdomRotation = this.getKingdomRotation();
//		this.setKingdomRotation(0);
		super.draw(batch, parentAlpha);
//		this.setKingdomRotation(kingdomRotation);
		//if (mousedOver()) drawInfo(batch, parentAlpha);
	}

	// TODO make scale limits apply only to zoom level
	public float getSizeFactor() {
		float scale = 2f;
		return scale;
	}

	// Is this army in an ambush and unfriendly towards the faction of the given army
	public boolean isNonFriendlyAmbush(Army army) {
		return this.isInAmbush() && this.faction != army.getFaction();
	}

	 boolean isArmyMoving() {
		if (this.isInSiege()) return false;
		if (this.isWaiting()) return false;
		if (kingdom.isPaused()) return false;
		if (target == null) return false;
		if (path.isEmpty()) return false;
		return true;
	}

	public void drawLOS() {
		
	}

	public boolean shouldDrawCrest() {
        if (this.isInBattle() || this.isGarrisoned() || !this.isVisible()) return false;
        return true;
    }

	public void drawCrest(SpriteBatch batch) {
        if (!shouldDrawCrest()) return;

		float size_factor = .7f;

		size_factor +=  .004*this.party.getTotalSize();

		Color temp = batch.getColor();
//		float zoom = getKingdom().getMapScreen().getCamera().zoom;
		float scale = 1.5f;
//        scale *= size_factor;
        scale *= getAdjustedZoom(kingdom);

		batch.setColor(clear_white);
		
		mx4Font.idt();
		mx4Font.rotate(new Vector3(0, 0, 1), getKingdom().getMapScreen().getKingdomRotation());
		mx4Font.trn(getCenterX(), getCenterY(), 0);
		Matrix4 tempMatrix = batch.getTransformMatrix();
		batch.setTransformMatrix(mx4Font);

		// TODO draw crest.
//		if (faction.crest != null) {
//            faction.crest.setPosition(-15*scale , 5 + 5*scale);
//            faction.crest.setSize(30*scale, 30*scale);
////		batch.draw(this.getFaction().crest, -15*zoom, 5 + 5*zoom, 30*zoom, 45*zoom);
//            faction.crest.draw(batch, clear_white.a);
//        }
		
		batch.setTransformMatrix(tempMatrix);
		batch.setColor(temp);
	}

	public String getAction() {
		// testing
		//if (this.type == ArmyType.NOBLE && ((Noble) this).specialTarget != null) return "Special target: " + ((Noble) this).specialTarget.getName();
		
		if (isInBattle()) return "In battle";
		else if (forceWait) return "Regrouping (" + Panel.format(this.waitUntil-getKingdom().clock() + "", 2) + ")";
		else if (isWaiting()&& isRunning()) return "Waiting and Running?";
//        else if (isWaiting()) return "Waiting";
        else if (isRunning()) return "Running from " + getRunFrom().getName(); // + " (Speed: " + Panel.format(getSpeed()*SPEED_DISPLAY_FACTOR + "", 2) + ")";
		//		else if (shouldRepair) return "SHOULD REPAIR";
		else if (isInSiege()) return "Besieging " + siege.location.getName();
		else if (getTarget() != null && getTarget().getType() == Destination.DestType.LOCATION) return "Travelling to " + getTarget().getName(); // + " (Speed: " + Panel.format(getSpeed()*SPEED_DISPLAY_FACTOR + "", 2) + ")";
		else if (getTarget() != null && getTarget().getType() == Destination.DestType.ARMY) return "Following " + getTarget().getName(); // + " (Speed: " + Panel.format(getSpeed()*SPEED_DISPLAY_FACTOR+"", 2) + ")";
		else return getUniqueAction();
	}

	public String getUniqueAction() {
		//contained in extensions;
		if (this.getTarget() == null) return "Travelling (Null Target)";
		return "Travelling " + this.getTarget().getName(); // + " (Speed: " + Panel.format(getSpeed()*SPEED_DISPLAY_FACTOR+"", 2)+")";
	}

	public boolean detectCollision() {
//	    if (type == ArmyType.FARMER) System.out.println(getName() + " target  = " + target.getName());
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

		// first check if army is even visible or available to go to
		if (!hasVisibilityOf(targetArmy)) {
			System.out.println(this.getName() + " no longer has visibility of " + targetArmy.getName() + ", setting next target");
			nextTarget();
			return false;
		}

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
			if (targetArmy.getBattle().shouldJoinAttackers(this)) {
				targetArmy.getBattle().addToAttackers(this);
				this.setBattleActor(targetArmy.getBattleActor());
			} else if (targetArmy.getBattle().shouldJoinDefenders(this)) {
				targetArmy.getBattle().addToDefenders(this);
				this.setBattleActor(targetArmy.getBattleActor());
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
		if (this.battleActor != null) return;
		if (targetArmy == null && siegeOf == null) {
		    throw new AssertionError();
        }

        // If targetArmy is null, we are attacking a location (garrison is a party not an army)
        Party targetParty;
		if (targetArmy != null) targetParty = targetArmy.party;
        else {
            targetParty = siegeOf.garrison;
        }
        if (targetParty == null) throw new AssertionError(this.getName() + " " + siegeOf.getName());

		if (this == getKingdom().getPlayer()) {
			BottomPanel.log("Attacking " + targetParty.getName() + "!");
			
			// get nearby armies and make them join battle
			Array<Party> nearAllies = new Array<Party>();
			nearAllies.add(this.party);
		
			Array<Party> nearEnemies = new Array<Party>();
			nearEnemies.add(targetParty);

			// TODO: this is O(N)
			for (Army a : kingdom.getArmies()) {
				if (a == targetArmy || a == this || a.distToCenter(this) > this.lineOfSight || (a.isGarrisoned() && siegeOf == null) || 
						nearEnemies.contains(a.party, true) || nearAllies.contains(a.party, true)) continue;
				if (a.isAtWar(targetParty.getFaction()) && !a.isAtWar(this) && !a.passive)
					nearAllies.add(a.party);
				else if (!a.isAtWar(targetParty.getFaction()) && a.isAtWar(this) && !a.passive)
					nearEnemies.add(a.party);
			}

			getKingdom().getPlayer().createPlayerBattleWith(nearAllies, nearEnemies, false, siegeOf);

			//			getKingdom().getMapScreen().getSidePanel().setActiveBattle(b);
			//			getKingdom().getMapScreen().getSidePanel().setStay(true);
		}
		else if (targetArmy == getKingdom().getPlayer()) {
			BottomPanel.log("Attacked by " + this.getName() + "!");
			
			// get nearby armies and join them
			Array<Party> nearAllies = new Array<Party>();
			nearAllies.add(targetArmy.party);
			
			Array<Party> nearEnemies = new Array<Party>();
			nearEnemies.add(this.party);
			
			for (Army a : kingdom.getArmies()) {
				if (a == targetArmy || a == this || a.distToCenter(targetArmy) > this.lineOfSight || (a.isGarrisoned() && siegeOf == null) || 
						nearEnemies.contains(a.party, true) || nearAllies.contains(a.party, true)) continue;
				if (a.isAtWar(targetArmy) && !a.isAtWar(this))
					nearEnemies.add(a.party);
				else if (!a.isAtWar(targetArmy) && a.isAtWar(this))
					nearAllies.add(a.party);
			}

			getKingdom().getPlayer().createPlayerBattleWith(nearAllies, nearEnemies, false, siegeOf);
			
			//			getKingdom().getMapScreen().getSidePanel().setActiveBattle(b);
			//			getKingdom().getMapScreen().getSidePanel().setStay(true);
		}
		else {
			if (this.party == null) throw new AssertionError();
			BattleActor b = new BattleActor(getKingdom(), this.party, targetParty, siege);
			this.setBattleActor(b);

			if (targetArmy != null)
			    targetArmy.setBattleActor(b);
			getKingdom().addBattle(b);
		}
		//shouldJoinBattle();
	}

	public boolean detectBattleCollision() {
		if (distToCenter(getTarget()) < battleCollisionDistance) {
			BattleActor targetBattle = (BattleActor) target;
			this.joinBattle(targetBattle);
			return true;
		}
		return false;
	}

	public void joinBattle(BattleActor battleActor) {
		if (this.party.player) {
			
			Array<Party> allies = new Array<Party>();
			Array<Party> enemies = new Array<Party>();
			Battle battle = battleActor.getBattle();
			
			allies.add(this.party);
			
			boolean defending = false;
			
			// should join defenders
			if (battle.shouldJoinDefenders(this)) {
				defending = true;
				StrictArray<Party> defendingParties = battle.getDefendingParties();
				StrictArray<Party> attackingParties = battle.getAttackingParties();
				for (Party party : attackingParties)
					enemies.add(party);
				for (Party party : defendingParties)
					allies.add(party);
			}
			else if (battle.shouldJoinAttackers(this)) {
				defending = false;
				StrictArray<Party> defendingParties = battle.getDefendingParties();
				StrictArray<Party> attackingParties = battle.getAttackingParties();
				for (Party party : defendingParties)
					enemies.add(party);
				for (Party party : attackingParties)
					allies.add(party);
			}
			else return;
			Location siegeOf = battleActor.getSiege() == null ? null : battleActor.getSiege().location;

			((ArmyPlayer) this).createPlayerBattleWith(allies, enemies, defending, siegeOf);
			
			BottomPanel.log("Joining " + battleActor.getName());
		}
		else {
			if (battleActor == null || battleActor.getBattle() == null) {
				System.out.println("joining null battle");
				return;
			}
			Battle battle = battleActor.getBattle();
			if (this.battleActor != null) {
				System.out.println("already in battle");
				return;
			}
			if (battle.shouldJoinAttackers(this)) {
				battle.addToAttackers(this);				
			} else if (battle.shouldJoinDefenders(this)) {
				battle.addToDefenders(this);
			} else {
				System.out.println("shouldn't join battle");
				return;
			}
			this.setVisible(false);
			this.setBattleActor(battleActor);
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
		changeTargetOfAnyFollowers();

		// wait/pause AFTER garrisoning!
		if (party.player) {
			System.out.println("garrisoning player");
			getKingdom().setPaused(true);
			getKingdom().getMapScreen().getSidePanel().setActive(targetCity.panel);
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
			Army army = closestHostileArmy();
			// if Noble with faction containing only one city
			if (type == ArmyType.NOBLE) {
//				if (this.getFaction().cities.size <= 1) {
					// only eject for special reasons
					if (army != null && shouldAttack(army) && !isRunning())  {
						setTarget(army);

						if (isGarrisoned())
							eject();
					}
//				}
			}
			//			else if (army == null || !shouldRunFrom(army)) {
			if (this.isRunning() && shouldStopRunning() && safeToEject()) {
//				System.out.println("player should stop running...");
				runFrom = null;
				eject();
				setTarget(null);
				//					System.out.println("ejecting " + this.getName() + " with no target");
			}
			if (!isRunning()) {
	    		uniqueAct();
			}
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

	private boolean safeToEject() {
		for (Army army : closeArmies) {
			if (shouldRunFrom(army)) return false;
		}
		return true;
	}

	/** do this while sieging
	 * 
	 * @param delta time elapsed since last frame
	 */
	public void siegeAct(float delta) {
		// Decide if should leave siege.

		// Siege itself handles whether should attack.
	}

	public void eject() {
		
		if (isGarrisoned())
			garrisonedIn.eject(this);
		else {
			throw new AssertionError("trying to eject from nothing");
		}
	}

	// returns 0 if no army nearby, 1 if shouldRun (runFrom != null), and 2 if shouldAttack nearby army (target == army)
	public int detectNearby() {
		// Problem with using "closest" -- if the closest army is a baby, and you want to kill it,
		// but there's a big boy nearby who wants to kill you, you'll prioritize the baby.
		// Wait -- that might not be true. API for closest hostile army specifies it prioritizes running.
		Army army = closestHostileArmy();
		//		if (this.type == ArmyType.MERCHANT) {
		//			if (army != null) System.out.println(getName() + " has cha " + army.getName());
		//			else System.out.println(getName() + " has cha null");
		//		}

		if (army != null) {
			if (shouldRunFrom(army) && runFrom != army)  {
				runFrom(army);
				//				System.out.println(this.getName() + " starting to setAppropriateRunTarget from " + army.getName());
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

	// TODO investigate the performance of this method. This may be one of the most expensive logic calls.
	// returns closest army should setAppropriateRunTarget from, else closest army should attack, or null if no armies are close.
	// Will not return garrisons.
	public Army closestHostileArmy() {
		//		if (this.type == ArmyType.PATROL) System.out.println(getName() + " in closest hostile army"); 
		// can (slightly) optimize by maintaining close Centers until this army's center changes! TODO
		// commented for testing
		double closestDistance = Float.MAX_VALUE;
		Army currentArmy = null;
		boolean shouldRun = false; // true if should setAppropriateRunTarget from CHA, false otherwise

		// only within 2 levels of adjacent, can expand later
		closeArmies.clear();
		closeCenters.clear();

		if (kingdom.getMap().getCenter(containingCenter) != null) {
			Center containing = kingdom.getMap().getCenter(containingCenter);
			// central one
			closeCenters.add(containingCenter);
			for (Center levelOne : containing.neighbors) {
				if (!closeCenters.contains(levelOne.index, false) && !levelOne.water) // level one
					closeCenters.add(levelOne.index);
				for (int i = 0; i < levelOne.neighbors.size(); i++) {
					Center levelTwo = levelOne.neighbors.get(i);
					if (!closeCenters.contains(levelTwo.index, false) && !levelTwo.water) // level two
						closeCenters.add(levelTwo.index);
				}
			}

			for (int index: closeCenters){ 
				Center containing2 = getKingdom().getMap().getCenter(index);
				if (containing2.armies != null) closeArmies.addAll(containing2.armies);
				closeArmies.removeValue(this, true);
			}

			//			System.out.println("Total Armies length: " + getKingdom().getArmies().size);
			//			if (this.type == ArmyType.PATROL) System.out.println(getName() + " CloseArmies Length: " + closeArmies.size);

			for (Army army : closeArmies) {
				if (!hasVisibilityOf(army)) continue;
				double distToCenter = this.distToCenter(army);
				// hostile troop
				if (isAtWar(army)) {
					if (shouldRunFrom(army)) {
						if (distToCenter < closestDistance) {
							shouldRun = true;
							closestDistance = distToCenter;
							currentArmy = army;
						}
					} else if (!shouldRun) {
						if (distToCenter < closestDistance) {
							closestDistance = distToCenter;
							currentArmy = army;
						}
					}
				}
			}
			return currentArmy;
		} else {
			//			System.out.println(this.getName() + " containing = null");
			return null;
		}
	}

	// Is the given army visible to this guy
	private boolean hasVisibilityOf(Army that) {
		double distToCenter = this.distToCenter(that);
		if (that.isNonFriendlyAmbush(this)) return false;
		if (that.isGarrisoned()) return false;
		if (that.isDestroyed()) return false;

		// This makes it so poor villagers who leave the acting range around the player won't get attacked by armies who aren't affected by that.
		// Need to make sure the acting range is large enough that the player won't see any weird effects this may cause.
		if (OPTIMIZED_MODE && !that.withinActingRange()) return false;
		return distToCenter < lineOfSight;
	}

	public void updatePolygon() {
		if (updatePolygon == UPDATE_POLYGON_FREQ) {
			getKingdom().updateArmyPolygon(this);
			updatePolygon = 0;
		}
		else updatePolygon++;
	}
	
	public void updatePolygonForce() {
		getKingdom().updateArmyPolygon(this);
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
				this.stopped = false;
			}
			if (this.isPlayer()) getKingdom().setPaused(true);
		}
	}

	public float getForceWait() {
		return 6 / party.getAvgSpd();
	}
	
	public void forceWait(float seconds) {
		this.forceWait = true; 
		this.waitFor(seconds);
	}
	// should this army attack that army?
	public boolean shouldAttack(Army that) {
//		if ((this.getTroopCount() - that.getTroopCount() >= 1) && (this.getTroopCount() <= that.getTroopCount()*4) && (that.getBattle() == null || that.getBattle().shouldJoin(this) != 0))
//			return true;

		float partyMaxDifferenceFactor = 2; // if a party is less than this fraction of the other one, won't attack.
		// TODO take into account nearby parties
		if ((this.getParty().getAtk() - that.getParty().getAtk() >= 1)
				&& (this.getTroopCount() <= that.getTroopCount() * partyMaxDifferenceFactor || this.isBandit())
				&& (that.getBattle() == null || that.getBattle().shouldJoinAttackers(this)))
			return true; 
		return false;
	}

	public boolean shouldRunFrom(Army that) {
		if (that.isGarrison) return false;
		//		if (this.type == ArmyType.PATROL)System.out.println(getName() + " in shouldRun method"); 

		if (this.getTroopCount() < that.getTroopCount())
			return true;
		return false;
	}

	public boolean shouldRepair() {
		if (defaultTarget != null && defaultTarget.getType() == DestType.LOCATION) {
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
			if (!hasVisibilityOf(targetArmy) || !targetArmy.hasParent() || targetArmy.isGarrisoned())
				return true;
			if (targetArmy.isInBattle()) {
				if (!targetArmy.getBattle().shouldJoinAttackers(this) && !targetArmy.getBattle().shouldJoinDefenders(this)) // shouldn't join
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

		changeTargetOfAnyFollowers();

		getKingdom().removeArmy(this);
		this.remove();
	}

	private void changeTargetOfAnyFollowers() {
		for (Army followedBy : this.targetOf) {
			if (followedBy.getTarget() == this) followedBy.nextTarget();
			followedBy.targetStack.remove(this);
			System.out.println(this.getName() + " is no longer visible, setting next target for " + followedBy.getName());
		}
	}

	public boolean isDestroyed() {
		return !this.kingdom.getArmies().contains(this, true);
	}

	//	public void verifyTarget() {
	//		if (getTarget().getType() == 2) { // army
	//			Army targetArmy = (Army) getTarget();
	//			if (!getKingdom().getArmies().contains(targetArmy, true))
	//				nextTarget();
	//		}
	//	}

	public void runFrom(Army runFrom) {
		// only let armies setAppropriateRunTarget from a new army every .5 seconds
		if (runFrom != null) {
			if (timeSinceRunFrom < RUN_EVERY) {
				timeSinceRunFrom += Gdx.graphics.getDeltaTime();
				return;
			}
			else timeSinceRunFrom = 0;
		}

		setWaiting(false);
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

	// What this does is ensure the army has a valid target.
	public void setAppropriateRunTarget() { // for now, find a spot far away and set path there
        if (shouldStopRunning()) throw new AssertionError(); // This check happens outside this method
		if (isWaiting()) stopWaiting();

        // this is the problem. path is not empty, but it's not getting empty;
        // Problem is units are running to a town, but not actually moving...
//		if (startedRunning && this.hasTarget() && !this.path.isEmpty() ) {
//			//this.detectCollision();
//			//if (this.type == ArmyType.FARMER) System.out.println(this.getName() + " is running");
//            return;
//		}

		// Note that all this code will setAppropriateRunTarget o matter what now... even if army already has a target.

        // NOTE: this is only cities! not villages...
			Location goTo = detectNearbyFriendlyLocationForRunning();
			if (goTo != null) {
//                System.out.println(getName() + " should go to " + goTo.getName());
                if (goTo == this.getTarget()) {
//                    System.out.println(getName() + " already has target,  " + goTo.getName() + " ...");
//                    setSpeed(calcSpeed());   // update speed
                    startedRunning = true;
                    return;
                }
				if (!setTarget(goTo)) throw new AssertionError(goTo.getName() + " cant be set as target for " + getName());
				setSpeed(calcSpeed());   // update speed
			//	this.detectCollision();
				startedRunning = true;
                //	System.out.println(this.getName() + " is travelling to target");
			}
			// find new target an appropriate distance away, travel there.
			else { //if (!this.hasTarget()) {
//                System.out.println(getName() + " no nearby city");
                //			System.out.println(getName() + " is running");
				//			setTarget(getKingdom().getCities().get(0));
				//				System.out.println(getName() + " getting new random setAppropriateRunTarget target");
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
                if (!setTarget(p)) throw new AssertionError(p.getName() + " cant be set as target for " + getName());
				startedRunning = true;
            }
    }

    // By default, only allow parties to run and hide in cities.
	public Location detectNearbyFriendlyLocationForRunning() {
		for (City city : getKingdom().getCities()) {
			if (!isAtWar(city) && this.distToCenter(city) < getLineOfSight() && this.distToCenter(city) < runFrom.distToCenter(city)) {
				return city;
			}
		}
		return null;
	}

	public void raid(Village village) {
		//System.out.println(this.name + " is raiding " + village.getName());
//		Militia militia = village.createMilitia();
//		getKingdom().addArmy(militia);
        // using null target army defaults to siege battle against garrison
		createBattleWith(null, village);
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
		// make speed related to playerPartyPanel's speed, morale, and army's unique speed factor,
		// simplified version for testing
		//return (BASE_SPEED + playerPartyPanel.getAvgSpd()*PARTY_SPEED_FACTOR)*speedFactor;
		return (BASE_SPEED + morale/30 + party.getAvgSpd()*PARTY_SPEED_FACTOR - party.getTotalSize()*SIZE_FACTOR)*speedFactor;
	}

	public float calcScale() {
		return (scale + scale*getTroopCount()/SCALE_FACTOR) * 20;
	}
	@Override
	public void setScale(float scale) {
		super.setScale(scale);
		setWidth(1);
		setHeight(1);
//		this.setWidth(region.getRegionWidth()*getScaleX());
//		this.setHeight(region.getRegionHeight()*getScaleY());
		this.setOrigin(getWidth()/2, getHeight()/2);
	}
	public void setMorale(int morale) {
		this.morale = morale;
	}
	
	
	public int calcMorale() {
		// 100 =  25     			  +  25      + 50;
		
		int base = 0;
		int party_bonus = Math.min(25, 100 - getTroopCount());
		party_bonus = 25;
		int free_bonus = 25;		float ani = 0.25f;

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
	public ArmyType getArmyType() {
	    return type;
    }
	public DestType getType() {
		return Destination.DestType.ARMY; // army type
	}
	public void setBattleActor(BattleActor battle) {
		this.battleActor = battle;
	}
	public BattleActor getBattleActor() {
		return battleActor;
	}
	public Battle getBattle() {
		if (battleActor == null) return null;
		return battleActor.getBattle();
	}
	public void endBattle() {
		if (siege != null) leaveSiege();
		battleActor = null;
        setStopped(false);
        // Not sure about this one! what if you already had a target? TODO
        setTarget(null);
		nextTarget();
//        if (!((p.getHealthySize() <= DEPRECATED_THRESHOLD && !p.player) || p.getHealthySize() <= 0))
        setVisible(true);
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
		//		System.out.println(this.getName() + " is ending siegeOrRaid");
		// force wait to see if it fixes slow siegeOrRaid end
//		this.forceWait(this.getForceWait());
		
		nextTarget();
		if (siege != null) {
			if (siege.location == this.getTarget()) nextTarget();
			//			while (siegeOrRaid.location == this.getTarget()) {
			//				System.out.println(getName() + " ending siegeOrRaid and going to new target");
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
			// figure out how to reconcile this with path?
			//System.out.println(getName() + " has null target");
			path.forceClear();
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
		// Allow the player to travel to water (they will stop at the border).
        // This prevents the player from clicking in the fog to figure out where water is
        if (isPlayer()) isInWater = false;
		if (!isInWater && !(newTarget.getType() == Destination.DestType.ARMY && ((Army) newTarget).isGarrisoned())) {
			if ((this.target != newTarget && this.lastPathCalc == 0) || this.isPlayer()) {
				if (!this.isWaiting() && this.isGarrisoned() && canEject()) this.eject();
				
				// don't add a bunch of useless point and army targets
				if (this.target != null && this.target.getType() != Destination.DestType.ARMY && this.target.getType() != Destination.DestType.POINT && targetStack.size() < MAX_STACK_SIZE) {
					targetStack.push(this.target);
					//					System.out.println(getName() + " pushing " + this.target.getName() + " stack size: " + this.targetStack.size() + " new target " + newTarget.getName());
				}
				this.target = newTarget;
				//				if (newTarget != null && this.path.isEmpty()) {
				if (this.path.finalGoal != newTarget) {
					if (this.path.calcPathTo(newTarget, this.isPlayer())) {
						this.lastPathCalc = A_STAR_FREQ;
						path.next();
					}
					else {
						System.out.println(getName() + " failed A*");
						this.path.calcStraightPathTo(newTarget);
					}
				}
//				else if (newTarget == this.path.finalGoal) System.out.println("new goal is already in path");
			}
			else {
			    // Not sure if this is the best solution. This was causing bugs when farmers were running. TODO remove "lastPathCalc" and optimize in a smarter way.
			    if (lastPathCalc != 0) {
			        this.target = newTarget;
			        return true;
                }
                if (this.target == newTarget) {
//                    System.out.println(getName() + " adding same target twice");
//                    throw new AssertionError();
					return false;
//
                }
                System.out.println("wtf?");
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
		else {
            uniqueAct();
            if (getTarget() == null) {
				System.out.println(getName() + " setting target failed");
				return false;
            }
            return true;
        }
	}
	private boolean canEject() {
		if (this.garrisonedIn == null) throw new AssertionError();

		// For now, don't allow ejecting if under siege.
		if (this.garrisonedIn.getSiege() != null) {
			return false;
		}
		return true;
	}
	public boolean hasTarget() {
		return getTarget() != null;
	}
	public Destination getTarget() {
		return target;
	}
	/* fix this */
	public void nextTarget() {
		this.target = null;
		if (!targetStack.isEmpty()) {
			// Check if null or destroyed, clear if so.
			if (targetStack.peek() == null || targetStack.peek() == target) {
				targetStack.pop(); // clean stack
				nextTarget();
				return;
			}
			// Check if no longer visible, clear if so.
			if (targetStack.peek().getType() == Destination.DestType.ARMY) {
				Army army = (Army) targetStack.peek();
				// If not visible to this,
				if (!this.hasVisibilityOf(army) || ((Army) getTarget()).isDestroyed()) {
					targetStack.pop(); // clean stack
					nextTarget();
					return;
				}
			}

			if (!targetStack.isEmpty() && targetStack.peek() != null) {
				setTarget(targetStack.pop());
			}
			else findTarget();
		}
		else {
			findTarget();
		}
//		if (target == null) throw new AssertionError();
	}
	public void newTarget(Destination target) {
		targetStack.removeAllElements();
		setTarget(target);
	}
	public void findTarget() {
		this.setTarget(null);
		//		if (this.type == ArmyType.BANDIT) System.out.println("bandit finding target");
        if (defaultTarget != getTarget())
		    setTarget(defaultTarget);

        if (!hasTarget()) {
			System.out.println(this.getName() + " unique acting to find a target");
			// Remove any remaining targets.
			path.forceClear();
			if (this.isFarmer()) ((Farmer) this).resetWaitToggle();
			if (this.isHuntingParty()) ((HuntingParty) this).resetWaitToggle();
			uniqueAct();
		}

        if (!hasTarget()) {
        	System.out.println(this.getName() + " can't find target");
        	//        	throw new AssertionError(this.getName() + " can't find target");
		}
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
		if (this.isNoble()) return this.getGeneral().getOfficialName();
//		return "army name";
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
		if (destination.getFaction() == null || this.faction == null) {
		    if (destination.getFaction() == null && this.faction == null) {
//				System.out.println(this.getName() + " is not at war with " + destination.getName());
				return false;
			}
//		    System.out.println(this.getName() + " is at war with " + destination.getName());
		    return true;
        }
//		System.out.println(this.getName() + " not at war with " + destination.getName());

		if (destination.getType() == DestType.LOCATION && ((Location) destination).isRuin()) return false;

//		System.out.println(this.getName() + " depends on faction " + destination.getName());
		return faction.atWar(destination.getFaction());
	}

    public boolean isAtWar(Faction faction) {
        if (faction == null) return true;
        if (this.faction == null) return true;
        return faction.atWar(faction);
    }

	// laziness sake
	// this is called a fuckton and uses the most time.
	protected double distToCenter(Destination d) {
		return getKingdom().distBetween(this, d);
	}
	
	public boolean isPlayer() {
		return player;
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
		// This is a hack attempt
//		if (isWaiting()) stopWaiting();
		return runFrom != null;
	}
	public boolean isInBattle() {
		return battleActor != null && battleActor.getBattle() != null;
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
//	public void setTextureRegion(String textureRegion) {
//		this.textureName = textureRegion;
//		this.region = Assets.atlas.findRegion(textureRegion);
//		this.initializeBox();
//	}
//
//	public TextureRegion getTextureRegion() {
//		return region;
//	}
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
	    if (faction == null) return Faction.INDEPENDENT_NAME;
		return faction.getName();
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
//
//    protected void drawAnimationTint(SpriteBatch batch, Animation animation, float stateTime, boolean loop, Color tint) {
//        Color c = batch.getColor();
//        batch.setColor(tint);
//        TextureRegion region = animation.getKeyFrame(stateTime, loop);
//        batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getKingdomRotation() - 90);
//        batch.setColor(c);
//    }

    protected Color getGeneralArmorColor() {
		if (ambushStarted) {
			Color c = new Color(party.getGeneral().getArmor().color);
			float ambushColor = 0.5f;
			if (!isInAmbush()) {
				// Interpolate the color of the party as it hides, but add a small fudge factor so it's clear when it's done.
				c.a = Math.min(1, ambushColor + 0.1f + (1 - timeSinceAmbushSet / TIME_TO_SET_AMBUSH) * (1 - ambushColor));
			} else {
				c.a = ambushColor;
			}
			return c;
		}
		else
			return party.getGeneral().getArmor().color;
	}

    protected Color getGeneralSkinColor() {
		if (ambushStarted) {
			Color c = new Color(party.getGeneral().skinColor);
			float ambushColor = 0.5f;
			if (!isInAmbush()) {
				// Interpolate the color of the party as it hides, but add a small fudge factor so it's clear when it's done.
				c.a = Math.min(1, ambushColor + 0.1f + (1 - timeSinceAmbushSet / TIME_TO_SET_AMBUSH) * (1 - ambushColor));
			} else {
				c.a = ambushColor;
			}
			return c;
		}
		else
			return party.getGeneral().skinColor;
    }
	
	public boolean isNoble() {
		return (this.type == ArmyType.NOBLE);
	}
	public boolean isFarmer() {
		return (this.type == ArmyType.FARMER);
	}
	public boolean isHuntingParty() {
		return (this.type == ArmyType.HUNTER);
	}
	public boolean isMerchant() {
		return (this.type == ArmyType.MERCHANT);
	}
	public boolean isPatrol() {
		return (this.type == ArmyType.PATROL);
	}
	
	public boolean isBandit() {
		if (this.faction == null) return false;
		return this.faction.isBandit();
	}
	
	public boolean withinLOSRange() {
		return (Kingdom.sqDistBetween(this, getKingdom().getPlayer()) < getKingdom().getPlayer().losSquared);
	}
	
	public boolean withinActingRange() {
		if (this.isNoble() || this.isMerchant() || this.isBandit() || getKingdom().getPlayer() == null) return true;
		return (Kingdom.sqDistBetween(this, getKingdom().getPlayer()) < getKingdom().getPlayer().losSquared*ACTING_RANGE_FACTOR*ACTING_RANGE_FACTOR);
	}
	public Center getContaining() {
		return kingdom.getMap().getCenter(containingCenter);
	}
	public void restoreAnimation() {
        walkArmor	= UnitDraw.createAnimation("walk-armor", 2, ANIMATION_LENGTH);
        walkSkin 	= UnitDraw.createAnimation("walk-skin", 2, ANIMATION_LENGTH);
	}
	public General getGeneral() {
		return this.party.getGeneral();
	}
}
