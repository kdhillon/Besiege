package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.BattleMap.Ladder;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.*;

public class Unit extends Group {

	static final int FRAME_COLS = 2;
	static final int FRAME_ROWS = 1;

	static final int DEFENSE_DISTANCE = 5;
	static final int SAFE_DISTANCE = 8; // how far away an enemy should be from friendly before shooting at them
	static final int ATTACK_EVERY = 1;
	static final float NEAREST_UPDATE_TIME = 2;

	static final int RETREAT_THRESHOLD = 2;

	static final float RELOADING_THRESHHOLD = 0.5f;

	static public float NEAR_COVER_DISTANCE = 6;
	static public float HEIGHT_RANGE_FACTOR = 6;
	static public float MAN_SIEGE_DISTANCE = 40;
	static public float HIDE_DISTANCE = 40;

	static public int RETREAT_POS = 10000;

	static final float DEATH_TIME = 300;
	static final float BASE_SPEED = .2f;

	static final float UNIT_HEIGHT_GROUND = .1f;
	static final float UNIT_HEIGHT_HORSE = .2f;

	static final float CLIMB_HEIGHT = .1f; // how high can units climb

	final static float CAVALRY_BONUS = 4f;
	
	final static float BASE_FIRE_RATE = 1.5f;
	final static float INIT_RELOAD = 2f;

	public BattleStage stage;	
	public Unit attacking;
	public SiegeUnit attackingSiege;

	public BattleSubParty bsp;
	public Party party;
	public Soldier soldier;
	public WeaponType weapon;
	public RangedWeaponType rangedWeapon;
	public AmmoType ammoType;
    public ShieldType shield;
    public boolean shieldBroken;

    public SiegeUnit siegeUnit;

	public boolean isMounted = false;
//	public boolean canRetreat = true;
	
	//	private boolean inCover;
	private BPoint nearestCover;

	boolean outOfBattle = false;

	public int quiver;
	//	public int height; // height off the ground

	//	public int baseAtk;
	//	public int baseDef;
	//	public int baseSpd;

	public int original_x;
	public int original_y;

	// these should be set at the beginning of battle and not changed.
	public float atk;
	public float def;
	public float spd;

	boolean rotationFixed;

	float timer = ATTACK_EVERY;
	float reloading;
	//float lastFace = 0f;
	public float hp;

	//	public float speed = .35f;
	public float UNIT_BASE_SPEED = .75f; // .45

	public float currentSpeed = 0;
	public int team;
	//	public Array<Unit> enemyArray;
	public BattleParty enemyParty;
	public boolean isHit; // is unit hit
	public boolean isDying;
	boolean isHidden;
	public float timeSinceDeath = 0;

	public int pos_x = -1;
	public int pos_y = -1;

	public int prev_x; 
	public int prev_y;
	public boolean moving;
	public boolean forceTwoMoves;
	float ladder_height;

	public float percentComplete; // between 0 and 1, used for moving?
	public enum Orientation {LEFT, UP, RIGHT, DOWN};
	public enum Stance {AGGRESSIVE, DEFENSIVE, INLINE};
	public Orientation orientation;
	public Stance stance;
	public Unit nearestEnemy;
	public Unit nearestTarget;

    public float stateTime;
    public float firingStateTime;

	public float rotation;
	private boolean retreating;

	public float updateNearestEnemy;

    public WeaponDraw weaponDraw;
    public UnitDraw unitDraw;
	public ThrownItem thrownItem;

	// this prevents the unit from spazzing
	private float lastAct = 0;

	// This will be added to Unit, so that rotation/position is tied to that.
	private static class ThrownItem extends Actor {
        public Animation sling;
        public TextureRegion textureRegion;

        public ThrownItem(RangedWeaponType.Type type) {
            this.setX(6);
            this.setY(-4);
            this.setScale(2);

            if (type == RangedWeaponType.Type.SLING) {
                sling = UnitDraw.createAnimation("sling", 8, 0.08f);
                sling.setPlayMode(Animation.LOOP);
                this.setWidth(sling.getKeyFrame(0).getRegionWidth());
                this.setHeight(sling.getKeyFrame(0).getRegionHeight());
            }

            if (textureRegion != null) {
                this.setWidth(textureRegion.getRegionWidth());
                this.setHeight(textureRegion.getRegionHeight());
            }
        }

        // Let the width of this be the same as the textureRegion or animation.
	    @Override
	    public void draw(SpriteBatch batch, float parentAlpha) {
            this.toFront();
            super.draw(batch, parentAlpha);
            Unit parent = (Unit) getParent();
            if (parent.unitDraw.isDrawingRangedLoadedAnimation()) {
                UnitDraw.drawAnimationTint(batch, sling, parent.firingStateTime, true, UnitLoader.colors.get("DK_BROWN"), this);
            }
        }
    }

	private boolean firingLoop;
    public boolean drawAmmo;
    public boolean drawAmmoReversed;

	public Color armorTint;
	public Color skinTint;

	// shouldn't be used that much, mostly for drawing horses in battles
	public Equipment horse;
	public Equipment head;

	// Includes all equipment listed above
	public StrictArray<Equipment> equipment = new StrictArray<>();

	public int shield_hp;
	public int horse_hp;

	public boolean moveSmooth; // use this to do animation so it's smoother
	private boolean moveToggle; // only set to true for the frame that moving is set to false;
	//	Animation knightIdle;
	//	Animation swordWalk;
	//	Animation swordAttack;

	public Unit(BattleStage parent, int team, Soldier soldier, BattleSubParty bp) {
		stage = parent;

		//		texture = new TextureRegion(new Texture("red.png"));
		this.bsp = bp;
		this.party = soldier.party;
		this.team = team;
		if (this.team == 0) enemyParty = stage.enemies;
		else enemyParty = stage.allies;
		
		this.original_x = 5000;
		this.original_y = 5000;
		this.setX( pos_x * stage.unit_width);
		this.setY( pos_y * stage.unit_height);

		this.setWidth(stage.unit_width);
		this.setHeight(stage.unit_height);
		//		this.setWidth(texture.getRegionWidth());
		//		this.setHeight(texture.getRegionHeight());

		this.setOriginX(this.getWidth()/2);
		this.setOriginY(this.getHeight()/2);

		this.horse = soldier.getHorse();
		this.shield = soldier.getShield();
		this.head = soldier.getHead();
		if (shield != null) shield_hp = shield.hp;
		if (horse != null) horse_hp = horse.hp;
        if (horse != null) equipment.add(horse);
        if (head != null) equipment.add(head);

		ladder_height = 0;

		// TODO check if position already occupied before creating
		this.orientation = Orientation.DOWN;
		if (this.team == 0) this.orientation = Orientation.UP;
		this.stance = Stance.AGGRESSIVE;

		this.soldier = soldier;
		this.weapon = soldier.unitType.melee;
		this.rangedWeapon = soldier.unitType.ranged;
        this.ammoType = soldier.unitType.ammoType;

        // calculate number of arrows unit gets

		if (rangedWeapon != null)
			quiver = rangedWeapon.quiver;

		//		if (rangedWeapon != null) 
		//			this.stance = Stance.DEFENSIVE;

		// check if in cover
		checkIfInCover();

		calcStats();

        if (this.spd == 0) System.out.println("speed is 0");

		if (rangedWeapon != null) {
		    reloading = INIT_RELOAD; // initial reload time

            if (this.rangedWeapon.type == RangedWeaponType.Type.SLING) {
                thrownItem = new Unit.ThrownItem(RangedWeaponType.Type.SLING);
                this.addActor(thrownItem);
            }
        }

		//		this.height = 0;

		this.weaponDraw = new WeaponDraw(this);
		this.addActor(weaponDraw);

		unitDraw = new UnitDraw(this);
        this.addActor(unitDraw);


		if (this.horse != null) {
			isMounted = true;
		}

        firingStateTime = 0f;
        stateTime = 0f;

//		if (this.team == 0 && stage.siegeOrRaid && stage.playerDefending) this.canRetreat = false;
//		if (this.team == 1 && stage.siegeOrRaid && !stage.playerDefending) this.canRetreat = false;
	}


	@Override
	public void act(float delta) {
		if (!this.inMap() && !isDying) {
		    this.retreatDone();
		    return;
        }
        stateTime += delta;
        firingStateTime += delta;

		if (nearestTarget != null && nearestTarget.outOfBattle) nearestTarget = null;
        if (nearestEnemy != null && nearestEnemy.outOfBattle) nearestEnemy = null;

		if (nearestEnemy == null || updateNearestEnemy < 0) {
			this.nearestEnemy = this.getNearestEnemy();
			updateNearestEnemy = NEAREST_UPDATE_TIME;
		}

		updateNearestEnemy -= delta;

		// flip flop logic kind of
		if (moveToggle) {
			moveSmooth = false;
			moveToggle = false;
		}
		if (this.isDying) {
			this.die(delta);
			return;
		}

		// Here is where we prevent the unit from spazzing.
        // Only allow them to change action every X seconds.
        // TODO isolate the decision making actions from the other ones and put them before.
//        float actEvery = 0.5f;
//		if (lastAct > 0) {
//            lastAct -= delta;
//            return;
//        } else
//            lastAct = actEvery;

        if (this.hp <= 0) {
			System.out.println("Still here not dying?");
			throw new AssertionError();
		}
		else if (this.hp <= RETREAT_THRESHOLD && canRetreat()) {
		    startRetreating();
			//System.out.println("unit retreating");
			//			this.retreat();
		}
		if (this.attacking != null) {
		    if (!this.attacking.inMap()) {
                attacking = null;
		    } else {
                timer += delta;
                if (timer > ATTACK_EVERY) {
                    attack();
                    timer = 0;
                }
            }
		}
		else if (this.moving) {
			//System.out.println("moving");
			this.percentComplete += getSpeed() * delta;				

			if (percentComplete > 1) {
				moveToggle = true;
				moving = false;
				percentComplete = 1;
			}
		}
		else if (forceTwoMoves) {
			this.moveForward();
			this.forceTwoMoves = false;
		}
		else if (this.retreating || this.bsp.retreating) {
			this.retreating = true;
			retreat();
		}
		else if (this.siegeUnit != null) {
			// check if manning
			if (!siegeUnit.adjacent(this)) {
				BPoint nearSiegePoint = siegeUnit.getOperatorPoint(this);
				if (nearSiegePoint != null) {
					this.moveToPoint(nearSiegePoint);
				}
			}
			else this.orientation = siegeUnit.orientation; // do nothing
		}
		else {
			checkIfShouldManSiege();
			//						System.out.println("moving2");
			//			getRandomDirection(delta);
			if (this.nearestCover != null && !this.inCover()) {
				// refresh cover to make sure still empty
				this.nearestCover = getNearestCover();
				if (nearestCover != null)
					moveToPoint(nearestCover);
			}
			else if (stance != Stance.DEFENSIVE && (!(this.isRanged() && distanceTo(getNearestEnemy()) < this.getCurrentRange()) || this.quiver <= 0)) {
			    reloading = 0;
				moveToEnemy();
			}
			else { // either defensive stance or aggressive but ranged within range
				// just use attack every for convenience
				timer += delta;
				//				if (timer > ATTACK_EVERY){
				//					faceEnemy();
				//					timer = 0;
				//				}
				// if enemy is within one unit and fighting, can move to them.
				if (nearestEnemy != null && (nearestEnemy.distanceTo(this) < DEFENSE_DISTANCE && nearestEnemy.attacking != null && !this.rangedWeaponOut()))
					moveToEnemy();
				else if (isFiring() && nearestTarget != null) {
					if (nearestTarget.isDying || nearestTarget.outOfBattle) {
						nearestTarget = getNearestTarget();
						this.updateRotation();
					}
					// makes archers freak out
					//					if (nearestTarget != null)
					//						face(nearestTarget);
					reload(delta);
				}
				// if ranged, fire weapon
				else {
					nearestTarget = getNearestTarget();

					if (this.rangedWeaponOut() && !shouldMove()) {
						if (nearestTarget != null) {
							if (reloading < RELOADING_THRESHHOLD && nearestTarget.distanceTo(this) < this.getCurrentRange() && nearestTarget.distanceTo(nearestTarget.getNearestEnemy()) > SAFE_DISTANCE && nearestTarget.attacking == null) {
								fireAtEnemy();
							}
							else {
								this.nearestTarget = getNearestTarget();
								if (this.nearestTarget == null) moveToEnemy();
							}
						}
						else {
							if (this.stance == Stance.AGGRESSIVE)
								moveToEnemy();
						}
					}
					// move to orignial position for infantry
					else if (!this.rangedWeaponOut() && this.stance == Stance.DEFENSIVE) {
						if ((this.pos_x != original_x || this.pos_y != original_y) && canMove(pos_x, pos_y)) this.moveToPoint(new BPoint(original_x, original_y));
					}
				}
			}
		}
	}

	public void startRetreating() {
        this.retreating = true;
        this.attacking = null;
    }

    public boolean isRetreating() {
	    return retreating;
    }

	private boolean canRetreat() {
        return bsp.parent.canRetreat();
    }

	private void reload(float delta) {
		reloading -= delta;
		if (reloading < 0f)
			firingStateTime = 0;
	}

	public void checkIfShouldManSiege() {
		// for now, no defensive units should siegeOrRaid
		if (this.rangedWeaponOut() || this.isMounted() || (stage.siegeOrRaid && this.stance == Stance.DEFENSIVE)) return;

		for (SiegeUnit s : stage.siegeUnitsArray) {
			if (!s.hasMen() && s.enemyTeam() != this.team) {
				BPoint closest = s.getOperatorPoint(this);
				if (this.distanceTo(closest) < MAN_SIEGE_DISTANCE) {
					this.man(s);

					return;
				}
			}
		}
	}

	public void checkIfInCover() {
		//		for (BPoint p : stage.battlemap.cover) {
		//			if (p.pos_x == this.pos_x && p.pos_y == this.pos_y) {
		//				if (p.orientation == this.orientation)
		//					this.nearestCover = p;
		//			}
		//		}
	}

	public boolean inCover() {
		if (nearestCover == null) {
			nearestCover = getNearestCover();
			if (nearestCover == null) return false;
		}
		if (this.pos_x == nearestCover.pos_x && this.pos_y == nearestCover.pos_y) return true;
		return false;

	}

	//	// if still in manning position, return true. if not, unman and return false;
	//	public boolean stillManning() {
	//		if (this.siegeUnit == null) return false;
	//		if (this.pos_x != siegeUnit.getOperatorPoint().pos_x || this.pos_y != siegeUnit.getOperatorPoint().pos_y) {
	//			unman();
	//			return false;
	//		}
	//		return true;
	//	}

	public float getSpeed() {
		return currentSpeed;
	}

	public float getCurrentRange() {
		if (this.rangedWeapon == null) return -1;
		//		System.out.println(this.rangedWeapon.range + this.getFloorHeight()*HEIGHT_RANGE_FACTOR);
		return this.getBaseRange() + this.getFloorHeight()*HEIGHT_RANGE_FACTOR;
	}


	public String getStatus() {
		if (this.isDying) 				return "Fallen";
		if (this.siegeUnit != null)		return "Manning " + siegeUnit.type.name;
		if (this.retreating)		 	return "Retreating";
		if (this.attacking != null) 	return "Attacking " + attacking.soldier.getTypeName();
		if (this.moveSmooth && 
				nearestCover != null) 	return "Moving to cover";
		if (this.moveSmooth) 			return "Charging";
		if (this.isRanged() && 
				this.reloading > 0 && 
				inCover()) 				return "Firing from cover";
		if (this.rangedWeaponOut() &&
				this.reloading > 0) 		return "Firing";
		else return "Idle";
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {		
//		if (this.isHidden() && !this.isDying && this.team != 0) return;
        // First draws weapondraw (everything under Unit)
        // Then draws Unitdraw (Unit armor etc)
        // Then draws ThrownItem (on top of unit)

//		if (isDying) {
		    // For now, don't draw equipment.
//             TODO draw equipment (specifically headdress)
//		    drawUnit(this, batch, dieArmor, dieSkin, armorTint, skinTint, timeSinceDeath, false, equipment);

        if (moving && !isDying) {
            setX(currentX() * stage.unit_width);
            setY(currentY() * stage.unit_height);
        } else if (!isDying) {
            setX(pos_x * stage.unit_width);
            setY(pos_y * stage.unit_height);
        }

        if (!rotationFixed) {
            updateRotation();
        }

        if (!this.isDying) {
            this.toFront();
        }
        this.setScale(1 + this.getFloorHeight() / 5f);
        super.draw(batch, parentAlpha);
//		}
//		else {


			//			batch.setColor(this.tintColor);

//		}
	}

	public boolean isFiring() {
	    return reloading > 0 && nearestTarget != null;
    }

//	public int calcHP() {
//		//		if (this.soldier.getType() == Soldier.SoldierType.ARCHER) return 10 + this.def*2;
//		//		else 
//		return (int) (15 + this.def*3 + soldier.subparty.getGeneral().getHPBonus());
//	}

	private void moveToEnemy() {
	    // This is clearly messed up...
//		if (nearestEnemy != null && !nearestEnemy.inMap()) {
//			nearestEnemy = getNearestEnemy();
//		}
//		if (nearestEnemy == null)  {
			nearestEnemy = getNearestEnemy();
			if (nearestEnemy == null) return;
//		}
//		nearestEnemy = getNearestEnemy();
//        System.out.println("going to nearest enemy: " + nearestEnemy.pos_x + " " + nearestEnemy.pos_y);

        if (nearestEnemy.retreating && nearestEnemy.moveSmooth) {
			moveToPoint(nearestEnemy.getAdjacentPoint());
		}
		else
			moveToPoint(nearestEnemy.getPoint());
	}

	//	private void moveToEnemy() {
	//		nearestCover = null;
	//		if (enemyArray.size == 0) return;
	//		this.faceEnemy();
	//
	//		if (this.onWall() != nearestEnemy.onWall()) {
	//			if (this.isMounted()) this.dismount();
	//			this.moveToPoint(getNearestLadder());
	//			return;
	//		}
	//
	//		Orientation original = this.orientation;
	//
	//		if (!this.moveForward()) {
	//			if (Math.random() > .5) this.forceTwoMoves = true;
	//
	//			// move in a different direction
	//			faceEnemyAlt();
	//			if (!this.moveForward()) {
	//
	//
	//				// try the last two directions as a last resort
	//				this.orientation = getOppositeOrientation(this.orientation);
	//				if (!this.moveForward()) {
	//					this.orientation = getOppositeOrientation(original);
	//					if (!this.moveForward()) {
	//						// this actually seems to work!
	//						//System.out.println("stuck!");
	//					}
	//				}
	//			}
	//		}
	//	}

	private void moveToPoint(BPoint point) {
		if (point == null) return;
		// check if on same height
		if (this.getFloorHeight() != stage.heights[point.pos_y][point.pos_x] && !stage.ladderAt(point.pos_x, point.pos_y)) {
			if (this.isMounted()) this.dismount();
			BPoint nearLadder;
			if (this.onWall())
				nearLadder = getNearestLadderTo(this.getPoint());
			else nearLadder = getNearestLadderTo(point); // slightly better but not perfect - must guarantee that there are always ladders close enough
			if (stage.selectedUnit == this) System.out.println("moving to ladder");
			//			if (!(point.pos_x == nearLadder.pos_x && point.pos_y == nearLadder.pos_y) && !stage.ladderAt(this.pos_x, this.pos_y))
			this.moveToPoint(nearLadder);
			return;
		} 
		//		// check if on same side of wall (also make sure to not check sides with entrances)
		else if (stage.insideWall(this.pos_x, this.pos_y) != stage.insideWall(point.pos_x, point.pos_y) && !stage.entranceAt(point.pos_x, point.pos_y) && !stage.entranceAt(this.pos_x, this.pos_y) && !this.rangedWeaponOut()) {
			BPoint nearestEntrance;
			if (stage.insideWall(this.pos_x, this.pos_y))
				nearestEntrance = getNearestEntranceTo(point);
			else nearestEntrance = getNearestEntranceTo(this.getPoint());
			this.moveToPoint(nearestEntrance);
			// once it gets to nearestEntrance, it's not getting the right target...
			if (stage.selectedUnit == this) System.out.println("needs to move inside wall");
			return;
		}

		if (stage.selectedUnit == this) if (stage.entranceAt(point.pos_x, point.pos_y)) System.out.println("target is entrance");

		this.face(point, true);

		Orientation original = this.orientation;

		if (!this.moveForward() && !this.unitMovingOutOfWay()) {

			if (Math.random() > .5) this.forceTwoMoves = true;

			this.faceAlt(point);

			// this happens way more than it should for some reason
			// changed from "or" to "and" to be stricter
			if (Math.random() < .01 && (!this.moveForward() && !this.unitMovingOutOfWay())) {	
				//				System.out.println("desperate move");
				// try the last two directions as a last resort
				this.orientation = getOppositeOrientation(this.orientation);
				if (Math.random() < .1 || !this.moveForward()) {
					this.orientation = getOppositeOrientation(original);
					if (!this.moveForward()) {
						// this actually seems to work!
						//System.out.println("stuck!");
					}
				}
			}
		}
	}

	public boolean unitMovingOutOfWay() {
		if (stage == null || stage.units == null || this.getAdjacentPoint() == null) return false;
		Unit inWay = stage.units[this.getAdjacentPoint().pos_y][this.getAdjacentPoint().pos_x];
		if (inWay == null) return false;
		if (inWay.party != this.party) return false; // should attack if a foe
		if (inWay.moveSmooth && inWay.orientation == this.orientation) {

			//			System.out.println("unit moving out of way");
			return true;
		}
		return false;
	}

	private void retreat() {
		this.bsp.handleUnitRetreating(this);

		nearestCover = null;
		this.unman();
		moveToPoint(getNearestExit());

		// TODO this is not a great way to do this...
		// effectively wound soldier until after battle
		if (this.pos_x <= 0 || this.pos_y <= 0 || this.pos_x >= stage.size_x-1 || this.pos_y >= stage.size_y-1) {
			//			leaveField();
			soldier.subparty.wound(soldier);
			retreatDone();
			//			System.out.println("Safe");
		}
	}

	// should move checks if archer needs to move before shooting, then moves them away from an obstruction or to cover
	private boolean shouldMove() {
		BPoint facing = getAdjacentPoint();
		if (facing == null) return false; // facing off stage
		BattleMap.Object object = stage.battlemap.objects[facing.pos_y][facing.pos_x];

		// check if object in front is too height
		if (object != null && (object.height+stage.heights[facing.pos_y][facing.pos_x] > Projectile.INITIAL_HEIGHT+this.getFloorHeight())) {
			//			System.out.println("should move");
			this.startMove(getRandomDirection());
			return true;
		}

		//		if (nearestEnemy != null && !inCover()) {
		//			this.nearestCover = this.getNearestCover();
		//			if (!inCover()) {
		//				if (this.nearestCover != null && Math.random() < .6) {
		//					moveToPoint(nearestCover);
		//					System.out.println("moving to cover");
		//					//					return true;
		//				}
		//			}
		//		}
		return false;
	}
	
	public boolean isGeneral() {
		return soldier.isGeneral();
	}

	private BPoint getAdjacentPoint() {
		BPoint point = null;
		if (this.orientation == Orientation.LEFT)
			point = new BPoint(this.pos_x-1, this.pos_y);
		if (this.orientation == Orientation.RIGHT)
			point = new BPoint(this.pos_x+1, this.pos_y);
		if (this.orientation == Orientation.UP)
			point = new BPoint(this.pos_x, this.pos_y+1);
		if (this.orientation == Orientation.DOWN)
			point = new BPoint(this.pos_x, this.pos_y-1);
		if (point.pos_x < 0 || point.pos_x >= stage.size_x || point.pos_y < 0 || point.pos_y >= stage.size_y) point = null;
		return point;
	}

	private void fireAtEnemy() {
		this.quiver -= 1;

		this.reloading = rangedWeapon.rate * BASE_FIRE_RATE + RELOADING_THRESHHOLD;
		Unit enemy = getNearestTarget();
		face(enemy, true);
		Projectile projectile = new Projectile(this, enemy);

		stage.addActor(projectile);
	}

	//	private void faceEnemy() {
	//		Unit nearest = this.getNearestEnemy();
	//		if (nearest == null) return;
	//		this.face(nearest);
	//	}

	//	private void faceEnemyAlt() {
	//		Unit nearest = this.getNearestEnemy();
	//		if (nearest == null) return;
	//		this.faceAlt(nearest);
	//	}

	private BPoint getNearestCover() {
		if (this.nearestEnemy == null) return null;
		this.face(nearestEnemy, false);
		Orientation thisOrientation = this.orientation;


		// check to see if should move to cover
		BPoint closest = null;
		float closestDistance = Float.MAX_VALUE;

		for (BPoint p : stage.battlemap.cover) { // && nearestEnemy.distanceTo(p) < rangedWeapon.range  ?
			if (p.orientation == thisOrientation && Math.abs(stage.heights[p.pos_y][p.pos_x] - this.getFloorHeight()) < Unit.CLIMB_HEIGHT) {
				if (nearestEnemy.distanceTo(p) < this.getCurrentRange()) {
					float dist = (float) distanceTo(p);
					if (dist < closestDistance && stage.units[p.pos_y][p.pos_x] == null) {
						closest = p;
						closestDistance = dist;
					}

				}
			}
		}

		if (closestDistance > NEAR_COVER_DISTANCE) return null;
//		System.out.println("Closest cover is " + closestDistance);

		return closest;
	}

	// return true if that is surrounded by units or not accessible
	private boolean notAccessible(Unit that) {
		BPoint current = new BPoint(that.pos_x, that.pos_y + 1);

		if (stage.inMap(current)) {
			if (stage.units[current.pos_y][current.pos_x] == null || stage.units[current.pos_y][current.pos_x] == this) {
				if (!stage.closed[current.pos_y][current.pos_x])
					return false;
			}
		}
		current = new BPoint(that.pos_x, that.pos_y - 1);
		if (stage.inMap(current)) {
			if (stage.units[current.pos_y][current.pos_x] == null || stage.units[current.pos_y][current.pos_x] == this) {
				if (!stage.closed[current.pos_y][current.pos_x])
					return false;
			}
		}
		current = new BPoint(that.pos_x - 1, that.pos_y);
		if (stage.inMap(current)) {
			if (stage.units[current.pos_y][current.pos_x] == null || stage.units[current.pos_y][current.pos_x] == this) {
				if (!stage.closed[current.pos_y][current.pos_x])
					return false;
			}
		}
		current = new BPoint(that.pos_x + 1, that.pos_y);
		if (stage.inMap(current)) {
			if (stage.units[current.pos_y][current.pos_x] == null || stage.units[current.pos_y][current.pos_x] == this) {
				if (!stage.closed[current.pos_y][current.pos_x])
					return false;
			}
		}
		return true;
	}

	
	// can make this super fast using a few simple ways
	// if infantry, just check nearby area (16 squares or so)
	// if nothing there, shoot line straight ahead to see if enemy is there.
	// if nothing there, do expanding search (or use neighbor's nearest enemy)
	private Unit getNearestEnemy() {
		Unit closest = null;
		Unit closestRetreating = null;
		double closestDistance = 99999;

		// just fix search now.
		for (Unit that : enemyParty.units) {
			if (that.team == this.team) System.out.println("TEAM ERROR!!!");
			if (that.isHidden) continue;
			if (notAccessible(that)) continue;
			if (that.pos_x <= 0 || that.pos_y <= 0) continue;
			double dist = this.distanceTo(that);
			if (dist < closestDistance) {
				if (that.retreating)
					closestRetreating = that;
				else {
					closest = that;
					closestDistance = dist;
				}
			}
		}

		if (closest != null) {
			nearestEnemy = closest;
			if (closest.pos_x == 0 && closest.pos_y == 0) {
			    throw new AssertionError();
            }
			return closest;
		}
		else if (closestRetreating == null) {
		    // This is the bug causing 0,0 chasing!
			// in this case, all units are out of line of sight
			Unit toChase = enemyParty.units.random();
			if (toChase != null && !toChase.inMap()) {
                toChase.retreatDone();
            }
            return null;
		}
		else {
			nearestEnemy = closestRetreating;
			return closestRetreating;
		}
	}

	// returns nearest enemy that's a certain distance away
	private Unit getNearestTarget() {
		Unit closest = null;
		Unit closestRetreating = null;
		Unit closestNormal = null; // prioritize archers

		double closestDistance = 			9999999;
		double closestNormalDistance = 		9999999;
		double closestRetreatingDistance = 	9999999;

		double MIN_DIST = 0f; // arbitrary

		for (Unit that : enemyParty.units) {
			if (that.team == this.team) System.out.println("TEAM ERROR!!!");
			if (that.isHidden) continue;
			if (that.soldier.isWounded()) continue;
			if (that.outOfBattle) continue;
			if (!that.inMap()) continue;

			double dist = this.distanceTo(that);
			// note - make sure not attacking, because you might hit a teammate
			if (dist > MIN_DIST && dist < this.getCurrentRange() && that.attacking == null && that.distanceTo(that.getNearestEnemy()) > SAFE_DISTANCE) {
				//				System.out.println(that.distanceTo(that.nearestEnemy));
				if (!that.retreating && that.rangedWeaponOut() && dist < closestDistance) {
					closestDistance = dist;
					closest = that;
				}
				else if (!that.retreating && dist < closestNormalDistance) {
					closestNormalDistance = dist;
					closestNormal = that;
				}
				else if (dist < closestRetreatingDistance){
					closestRetreating = that;
					closestRetreatingDistance = dist;
				}
			}
		}


		// IF false then prioritize closest
		boolean PRIORITIZE_ARCHERS = this.onWall();

		if (PRIORITIZE_ARCHERS || closestDistance < closestNormalDistance){
			if (closest != null) {
				nearestTarget = closest;
				return closest;
			}
			else if (closestNormal != null) {
				nearestTarget = closestNormal;
				return closestNormal;
			}
			else {
				nearestTarget = closestRetreating;
				return closestRetreating;
			}
		}
		else {
			if (closestNormal != null) {
				nearestTarget = closestNormal;
				return closestNormal;
			}
			if (closest != null) {
				nearestTarget = closest;
				return closest;
			}
			else {
				nearestTarget = closestRetreating;
				return closestRetreating;
			}
		}
	}

	private BPoint getNearestExit() {
		int point_x = 0;
		int point_y = 0;

		int dist_to_right = stage.size_x-pos_x;
		int dist_to_top = stage.size_y-pos_y;
		int dist_to_left = pos_x;
		int dist_to_bottom = pos_y;

		// don't allow to retreat into enemies (hacky)
		if (team == 0) dist_to_top = Integer.MAX_VALUE;
		if (team == 1) dist_to_bottom = Integer.MAX_VALUE;

		boolean done = false;

		// right
		if (dist_to_right < dist_to_left && dist_to_right < dist_to_top && dist_to_right < dist_to_bottom) {
			point_x = stage.size_x-1;
			point_y = pos_y;
			if (canMove(point_x, point_y)) done = true;
		}
		// left
		if (!done && dist_to_left < dist_to_right && dist_to_left < dist_to_top && dist_to_left < dist_to_bottom) {
			point_x = 0;
			point_y = pos_y;
			if (canMove(point_x, point_y)) done = true;
		} 
		// top
		if (!done && dist_to_top < dist_to_left && dist_to_top < dist_to_right && dist_to_top < dist_to_bottom) {
			point_x = pos_x;
			point_y = stage.size_y-1;
			if (canMove(point_x, point_y)) done = true;
		}
		// bottom
		if (!done) {
			point_x = pos_x;
			point_y = 0;
			if (canMove(point_x, point_y)) done = true;
		}

		BPoint closest = new BPoint(point_x, point_y);

		if (this.onWall()) {
			closest = getNearestLadderTo(this.getPoint());
		}

		return closest;
	}

	public BPoint getNearestLadderTo(BPoint p) {
		BPoint closest = null;

		double closestDistance = 999999;
		// get closest ladder
		for (Ladder l : stage.battlemap.ladders) {
			BPoint ladderPoint = new BPoint(l.pos_x, l.pos_y);
			double dist = p.distanceTo(ladderPoint);
			if (dist < closestDistance) {
				closest = ladderPoint;
				closestDistance = dist;
			}
		}
		return closest;
	}

	public BPoint getNearestEntranceTo(BPoint that) {
		BPoint closest = null;
		double closestDistance = 999999;
		// get closest ladder
		for (BPoint p : stage.battlemap.entrances) {
			BPoint point = new BPoint(p.pos_x, p.pos_y);
			double dist = that.distanceTo(point);
			if (dist < closestDistance) {
				closest = point;
				closestDistance = dist;
			}
		}
		return closest;
	}

	//	public Point getNearestSiegePoint() {
	//		if (stage.siegeUnitsArray.size == 0) return null;
	//
	//		Point closest = null;
	//
	//		double closestDistance = 99999;
	//		
	//		for (SiegeUnit s : stage.siegeUnitsArray) {
	//			if (s)
	//			Point point = s.getOperatorPoint();
	//			if (point == null) continue;
	//			
	//			double dist = this.distanceTo(point);
	//			if (dist < closestDistance) {
	//				closest = point;
	//				closestDistance = dist;
	//			}
	//		}
	//		
	////		if (closest == null) System.out.println("null closest");
	////		else System.out.println("closest not null");
	//		
	//		return closest;
	//	}

	// get closest siegeOrRaid unit that doesn't have enough men
	//	public SiegeUnit getNearestUnmannedSiegeUnit() {
	//		if (stage.siegeUnitsArray.size == 0) return null;
	//
	//		SiegeUnit closest = null;
	//
	//		double closestDistance = 999999;
	//		
	//		for (SiegeUnit s : stage.siegeUnitsArray) {
	//			if (s.hasMen()) continue;
	//			Point point = s.getOperatorPoint();
	//			if (point == null) continue;
	//			
	//			double dist = this.distanceTo(point);
	//			if (dist < closestDistance) {
	//				closest = s;
	//				closestDistance = dist;
	//			}
	//		}
	//		
	//		return closest;
	//	}

	public void man(SiegeUnit siegeUnit) {
		if (siegeUnit.hasMen()) return;

		siegeUnit.units.add(this);
		this.siegeUnit = siegeUnit;
	}

	public void unman() {
		if (siegeUnit == null) {
			return;
		}

		siegeUnit.units.removeValue(this, true);
		this.siegeUnit = null;
	}

	public boolean onWall() {
		return this.getFloorHeight() > 0;
	}

	public double distanceTo(Unit that) {
		if (that == null) return Double.POSITIVE_INFINITY;
		return Math.sqrt((that.pos_x-this.pos_x)*(that.pos_x-this.pos_x) + (that.pos_y-this.pos_y)*(that.pos_y-this.pos_y));
	}

	public double distanceTo(BPoint that) {
		if (that == null) return Double.POSITIVE_INFINITY;
		return Math.sqrt((that.pos_x-this.pos_x)*(that.pos_x-this.pos_x) + (that.pos_y-this.pos_y)*(that.pos_y-this.pos_y));
	}

	private void attack() {
		if (!this.attacking.isAdjacent(this) || this.attacking.hp <= 0){
			this.attacking = null;
			return;
		}
		//System.out.println("attack phase: " + attacking.hp);
		this.face(attacking, true);
		attacking.face(this, false);

		double damage = this.atk-Math.random()*attacking.def;

		// calculate bonus damage
		double bonusDamage = 0;

		// polearm against cavalry
		if (weapon.isPolearm() && attacking.horse != null) {
			bonusDamage += CAVALRY_BONUS;
		}

		attacking.hurt(damage + bonusDamage, this);
	}

	public void hurt(double damage, Unit attacker) {
		if (damage <= 0) return;

		// shield splits damage in half
		if (this.shieldUp() && attacker != null && this.orientation == attacker.getOppositeOrientation()) {
			this.shield_hp -= damage/2.0;
			damage /= 2.0;
			if (shield_hp <= 0) destroyShield();
		}

		if (this.isMounted()) {
			this.horse_hp -= damage/2.0;
			damage /= 2.0;
			if (horse_hp <= 0) killHorse();
		}

		this.hp -= (int) (damage + .5);

		this.isHit = true;
		if (this.hp <= 0 && !this.outOfBattle) {
			if (this.soldier != null && attacker != null && attacker.soldier != null) {
				this.soldier.killedBy = attacker.soldier;
				attacker.bsp.handleKilledEnemy();
			}
			this.isDying = true;
			
			// this takes care of exp
			this.kill();
			
			//			this.destroy();
//			if (attacker != null) {
//				if (attacker.attacking == this) attacker.attacking = null;
//				// usually full level, but spread some out to playerPartyPanel
//				// this happens in 
////				attacker.soldier.registerKill(this.soldier, false);
//			}
		}
		//		System.out.println(this.hp);
	}

	public void die(float delta) {
		if (!this.rotationFixed) {
			this.setRotation(this.rotation + (float) (Math.random() * 120 - 60));
			this.rotationFixed = true;
		}

		// don't need to go all the way to the back, just behind the other units.
        // Is there a way to bring units to the front, but bring thrown items *more* to the front?
//		this.toBack();
        timeSinceDeath += delta;
		
		if (timeSinceDeath > DEATH_TIME) {
			
		}
	}

	public boolean hasWorkingShield() {
        return this.shield != null && !shieldBroken;
    }

    // Does unit have a shield, but it's broken
    public boolean isShieldBroken() {
        return this.shield != null && shieldBroken;
    }

	public boolean shieldUp() {
		if (hasWorkingShield() && !this.rangedWeaponOut()) return true;
		return false;
	}

	// shield on back
	public boolean shieldDown() {
		if (this.shield != null && this.rangedWeaponOut()) return true;
		return false;
	}

	public boolean isMounted() {
		return this.horse != null && isMounted;
	}

	public void dismount() {
		//		System.out.println("dismounting");
		this.isMounted = false;
		this.calcStats();
	}

	public boolean hasHorseButDismounted() {
		//		if (this.horse != null) System.out.println("mounted: " + isMounted);
		return (this.horse != null && !isMounted);
	}

	public static Orientation getRandomDirection() {
		System.out.println("moving in random direction");
		double random = Math.random();
		if (random < .25)
			return Orientation.LEFT;
		else if (random < .5)
			return Orientation.UP;
		else if (random < .75)
			return Orientation.RIGHT;
		else
			return Orientation.DOWN;
	}


	public void updateRotation() {
		rotation = 0;
		if (orientation == Orientation.DOWN) rotation = 180;
		if (orientation == Orientation.LEFT) rotation = 90;
		if (orientation == Orientation.RIGHT) rotation = 270;

		if (this.nearestTarget != null && this.rangedWeaponOut() && !this.moveSmooth) {
			rotation = angleToEnemy(this.nearestTarget);
		}

		this.setRotation(rotation);
	}

	public float angleToEnemy(Unit nearestTarget) {
		float angle = (float)Math.atan2(this.pos_x - nearestTarget.getX()/stage.unit_width, this.pos_y - nearestTarget.getY()/stage.unit_height) * MathUtils.radiansToDegrees;
		if (angle < 0) angle += 360;
		return -angle + 180;
	}

	public static Orientation getOppositeOrientation(Orientation orientation) {
		if (orientation == Orientation.UP) return Orientation.DOWN;
		if (orientation == Orientation.DOWN) return Orientation.UP;
		if (orientation == Orientation.LEFT) return Orientation.RIGHT;
		else return Orientation.LEFT;
	}

	public Orientation getOppositeOrientation() {
		return getOppositeOrientation(this.orientation);
	}

//	public void drawAnimation(SpriteBatch batch, Animation animation, float stateTime, boolean loop) {
//		TextureRegion region = animation.getKeyFrame(stateTime, loop);
//		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
//	}

	public float currentX() {
		return (prev_x + percentComplete*(pos_x-prev_x));
	}

	public float currentY() {
		return (prev_y + percentComplete*(pos_y-prev_y));
	}

	public boolean moveForward() {
		return startMove(this.orientation);
	}
	
	// return -1 if failure, 0 if success, 1 if enemy, 2 if friend
	public int tryMoveTo(int x, int y) {
		if (!canMove(x, y)) return -1;
		Unit other = stage.units[y][x];
		if (other != null) {
			boolean enemy = collision(other);
			if (enemy) return 1;
			else {
				// see if should push friend forward:
				if (other.orientation == this.orientation && other.canMoveForward() && !other.moving) {
					//					System.out.println("should move");
					other.moveForward();
				}
				return 2;
			}
		}
		if (pos_y < stage.units.length && pos_x < stage.units[0].length)
			stage.units[pos_y][pos_x] = null;
		pos_y = y;
		pos_x = x;
		stage.units[pos_y][pos_x] = this;

		return 0;
	}
	
	// returns false if move failed, true otherwise
	public boolean startMove(Orientation direction) {
		if (this.hp < 0) return false;
		prev_x = pos_x;
		prev_y = pos_y;
		this.orientation = direction;

		int moveResult = -1;
		if (direction == Orientation.DOWN) {
			moveResult = tryMoveTo(pos_x, pos_y-1);
		}
		else if (direction == Orientation.UP) {
			moveResult = tryMoveTo(pos_x, pos_y+1);
		}
		else if (direction == Orientation.LEFT) {
			moveResult = tryMoveTo(pos_x-1, pos_y);
		}
		else if (direction == Orientation.RIGHT) {
			moveResult = tryMoveTo(pos_x+1, pos_y);	
		}
		if (moveResult == -1) return false;
		if (moveResult == 1) return true;
		if (moveResult == 2) return false;

		moving = true;
		moveSmooth = true;
		percentComplete = 0;

		this.calcSpeed();

		if (this.team == 0) stage.enemies.updateHiddenAll();
		else if (this.team == 1) stage.allies.updateHiddenAll();
		this.updateHidden();

		if (prev_y != pos_y && prev_x != pos_x) System.out.println("error!");

		this.orientation = direction;
		return true;
	}

	private void calcSpeed() {
		this.currentSpeed = (UNIT_BASE_SPEED) * (float)(1-stage.slow[pos_y][pos_x]);
		this.currentSpeed *= stage.getStageSlow();

		// better yet, move as a unit. don't keep moving until everyone is in their right place!
		// either use this speed or everyone's speed
		if (!retreating && bsp.stance == Stance.INLINE)
			this.currentSpeed *= bsp.minSpeed;
		else this.currentSpeed *= spd;
	}

	public boolean canMoveForward() {
		int next_x = this.pos_x;
		int next_y = this.pos_y;
		if (this.orientation == Orientation.DOWN) {
			next_y--;
		}
		else if (this.orientation == Orientation.UP) {
			next_y++;
		}
		else if (this.orientation == Orientation.LEFT) {
			next_x--;
		}
		else if (this.orientation == Orientation.RIGHT) {
			next_x++;
		}
		if (!stage.inMap(next_x, next_y)) return false;
		if (stage.units[next_y][next_x] != null) return false;
		return canMove(next_x, next_y);
	}
	
	public boolean canMove(int pos_x, int pos_y) {
	    // Just for testing
	    if (pos_x == 0 && pos_y == 0) return false;

		if (pos_x < 0 || pos_y < 0 || pos_x >= stage.size_x || pos_y >= stage.size_y) return false;
		if (stage.closed[pos_y][pos_x]) return false;
		if (Math.abs(this.getFloorHeight() - stage.heights[pos_y][pos_x]) > Unit.CLIMB_HEIGHT && (!stage.ladderAt(pos_x, pos_y) || this.isMounted())) return false;
		if (stage.ladderAt(pos_x, pos_y) && (this.isMounted() || (this.retreating && this.getOppositeOrientation() != stage.battlemap.getLadderAt(pos_x, pos_y).orientation))) return false;
		if (stage.siegeUnits[pos_y][pos_x] != null) return false;
		return true;
	}

	// return true if enemy, false if friend
	public boolean collision(Unit that) {
		if (this.team != that.team) {
			if (!that.moving) 
				attack(that);
			return true;
		}
		return false;
	}

	public void attack(Unit that) {
		// if (should fight back / not already attacking)
		// 		fight back
		this.attacking = that;
		this.face(that, true);

		// change this later
		that.attacking = this;
	}

	public void destroyShield() {
		shieldBroken = true;
		this.weaponDraw.shield = null;
		this.weaponDraw.clear();
		soldier.calcStats();
		calcStats();
	}

	public void killHorse() {
		equipment.removeValue(horse, true);
		//		this.horse = null;
		this.dismount();
		//		this.weaponDraw.horseWalk = null;
		//		soldier.calcStats();
		//		soldier.calcBonus();
		calcStats();
	}

	private void calcStats() {
		this.atk = soldier.getAtk();
		this.def = soldier.getDef();
		this.spd = soldier.getSpd() + 1 + (float) (BASE_SPEED*Math.random());
		// adjust speed and def
		if (shieldDown())  {
			this.spd -= shield.spdMod;
		}
		if (hasHorseButDismounted()) {
			this.spd -= horse.spdMod;
			this.def -= horse.defMod;
			this.atk -= horse.atkMod;
		}
		this.hp = soldier.getHp();
	}
	
	public void setStance(Stance s) {
		this.stance = s;
	}

	public boolean rangedWeaponOut() {
		return isRanged() && quiver > 0 && attacking == null;
	}

	public boolean isAdjacent(Unit that) {
		int distance_x = Math.abs(that.pos_x - this.pos_x);
		int distance_y = Math.abs(that.pos_y - this.pos_y);

		if (distance_x > 1 || distance_y > 1) return false;
		if (distance_x == 1 && distance_y == 1) return false;
		return true;
	}

	// call this when a soldier's health goes to 0
	public void kill() {
	    if (this.outOfBattle) return;

		this.unman();
		this.bsp.handleUnitKilled(this);
//		if (this.isDying)

		if (this.team == 0)
			stage.allies.removeUnit(this, true);
		else stage.enemies.removeUnit(this, true);
		if (pos_y >= 0 && pos_x >= 0)
			stage.units[pos_y][pos_x] = null;
		
		this.pos_x = RETREAT_POS;
		this.pos_y = RETREAT_POS;
		this.outOfBattle = true;

		if (this.team == 0) stage.allies.units.removeValue(this, true);
		if (this.team == 1) stage.enemies.units.removeValue(this, true);
		this.removeActor(weaponDraw);
        this.removeActor(thrownItem);

		boolean wasAttacker = (this.team == 0) == (stage.playerDefending);
		stage.casualty(this.soldier, wasAttacker);
	}

	// call this when a soldier retreats
	public void retreatDone() {
//		System.out.println("leaving battle");

        if (this.inMap())
		    stage.units[pos_y][pos_x] = null;
		this.pos_x = RETREAT_POS;
		this.pos_y = RETREAT_POS;
		if (this.team == 0) stage.allies.removeUnit(this, false);
		if (this.team == 1) stage.enemies.removeUnit(this, false);

		this.outOfBattle = true;

		stage.retreated.add(this);

		String status = soldier.getTypeName();
		String color = "white";

		status += " retreated!";

		if (team == 0)
			color = "yellow";
		else // if (dArmies.contains(army, true)) {
			color = "blue";
		BottomPanel.log(status, color);
	}

	public void face(Unit that, boolean forceInstant) {
	    if (!that.inMap()) return;

		//		int x_dif = that.pos_x - this.pos_x;
		//		int y_dif = that.pos_y - this.pos_y;
		//
		//		if (Math.abs(x_dif) > Math.abs(y_dif)) {
		//			if (x_dif > 0) this.orientation = Orientation.RIGHT;
		//			else this.orientation = Orientation.LEFT;
		//		}
		//		else if (Math.abs(x_dif) < Math.abs(y_dif)) {
		//			if (y_dif > 0) this.orientation = Orientation.UP;
		//			else this.orientation = Orientation.DOWN;
		//		}
		//		else if (y_dif > 0) this.orientation = Orientation.UP;
		//		else this.orientation = Orientation.DOWN;
		this.face(that.getPoint(), forceInstant);
	}

	// same as above but with point
	public void face(BPoint that, boolean forceInstant) {
	    // For now, just return
	    if (!forceInstant) return;

		int x_dif = that.pos_x - this.pos_x;
		int y_dif = that.pos_y - this.pos_y;

		double move_hor_prob = Math.abs(x_dif) / (double) (Math.abs(x_dif) + Math.abs(y_dif));
		if (move_hor_prob < 0.5) move_hor_prob = move_hor_prob/10;
		else if (move_hor_prob >= 0.5) move_hor_prob = move_hor_prob * 1.5;

		if (this.stance == Stance.INLINE) 
			move_hor_prob = .01;
		
		// should move right
		if (x_dif > 0) {
			// up
			if (y_dif > 0) {
				// up or right
				if (Math.random() < (move_hor_prob)) {
					this.orientation = Orientation.RIGHT;
				}
				else {
					this.orientation = Orientation.UP;
				}
			}
			else {
				// down or right
				if (Math.random() < move_hor_prob) {
					this.orientation = Orientation.RIGHT;
				}
				else {
					this.orientation = Orientation.DOWN;
				}
			}
		}
		// should move left
		else {
			if (y_dif > 0) {
				// up or left
				if (Math.random() < move_hor_prob && x_dif != 0) {
					this.orientation = Orientation.LEFT;
				}
				else {
					this.orientation = Orientation.UP;
				}
			}
			else {
				// down or left
				if (Math.random() < move_hor_prob && x_dif != 0) {
					this.orientation = Orientation.LEFT;
				}
				else {
					this.orientation = Orientation.DOWN;
				}
			}
		}

		//		if (Math.abs(x_dif) > Math.abs(y_dif)) {
		//			if (x_dif > 0) this.orientation = Orientation.RIGHT;
		//			else this.orientation = Orientation.LEFT;
		//		}
		//		else if (Math.abs(x_dif) < Math.abs(y_dif)) {
		//			if (y_dif > 0) this.orientation = Orientation.UP;
		//			else this.orientation = Orientation.DOWN;
		//		}
		//		else if (y_dif > 0) this.orientation = Orientation.UP;
		//		else this.orientation = Orientation.DOWN;
	}


	// still tries to face the enemy, but in none of the same ways as the other face method
	public void faceAlt(Unit that) {
		this.faceAlt(that.getPoint());
	}

	public void faceAlt(BPoint that) {
		int x_dif = that.pos_x - this.pos_x;
		int y_dif = that.pos_y - this.pos_y;

		if (Math.abs(x_dif) < Math.abs(y_dif)) {
			if (x_dif > 0) this.orientation = Orientation.RIGHT;
			else if (x_dif < 0) this.orientation = Orientation.LEFT;
			else if (Math.random() < .5) this.orientation = Orientation.RIGHT;
			else this.orientation = Orientation.LEFT;
		}
		else if (Math.abs(x_dif) > Math.abs(y_dif)) {
			if (y_dif > 0) this.orientation = Orientation.UP;
			else if (y_dif < 0) this.orientation = Orientation.DOWN;
			else if (Math.random() < .5) this.orientation = Orientation.UP;
			else this.orientation = Orientation.UP;
		}
		else if (x_dif > 0) this.orientation = Orientation.RIGHT;
		else this.orientation = Orientation.LEFT;
	}

	public float getZHeight() {
		if (horse != null) return UNIT_HEIGHT_HORSE + getFloorHeight();
		else return UNIT_HEIGHT_GROUND + getFloorHeight();
	}

	public float getFloorHeight() {
		if (!this.inMap()) return 0;

		if (stage.ladderAt(pos_x, pos_y)) {
			// moving up
			if (!stage.wallAt(prev_x, prev_y)) {
				//				System.out.println("moving up");

				if (moving)
					ladder_height = BattleMap.CASTLE_WALL_HEIGHT_DEFAULT * this.percentComplete;
				else if (ladder_height > BattleMap.CASTLE_WALL_HEIGHT_DEFAULT/2) ladder_height =  BattleMap.CASTLE_WALL_HEIGHT_DEFAULT;
				else if (ladder_height < BattleMap.CASTLE_WALL_HEIGHT_DEFAULT/2) ladder_height =  0;

				//				else ladder_height = BattleMap.CASTLE_WALL_HEIGHT_DEFAULT;
			}
			// moving down
			else {
				//				System.out.println("moving down");
				if (moving)
					ladder_height = BattleMap.CASTLE_WALL_HEIGHT_DEFAULT * (1-this.percentComplete);
				else if (ladder_height < .5f) ladder_height =  0;
				else if (ladder_height > .5f) ladder_height =  BattleMap.CASTLE_WALL_HEIGHT_DEFAULT;

				//				else ladder_height = 0;
			}
		} else ladder_height = 0;

		return stage.heights[pos_y][pos_x] + ladder_height;
	}


	public boolean isRanged() {
		return this.rangedWeapon != null;// || this.quiver <= 0;
	}

	public float getCenterX() {
		return getX() + getOriginX();
	}
	public float getCenterY() {
		return getY() + getOriginY();
	}

	public boolean inMap() {
		return pos_x < stage.size_x &&
				pos_y < stage.size_y && 
				pos_x >= 0 && 
				pos_y >= 0;
	}

	public BPoint getPoint() {
	    if (!inMap()){
	        throw new AssertionError();
        }
		return new BPoint(pos_x, pos_y);
	}

	// only 1-handed units and non-mounted
	public boolean canHide() {
		if (this.isMounted()) return false;
		return !this.weapon.isPolearm();
	}

	public void updateHidden() {
	    // THIS WORKS, just commenting for now
//		if (this.canHide()) {
//			this.nearestEnemy = this.getNearestEnemy();
//			if (this.nearestEnemy == null) this.isHidden = false;
//			else if (this.nearestEnemy.distanceTo(this) > HIDE_DISTANCE*this.stage.battlemap.obscurity_factor) {
//				this.isHidden = true;
//			}
//			else this.isHidden = false;
//		}
//		else this.isHidden = false;
	}

	// return true if enemy can't see this unit
	public boolean isHidden() {
		return isHidden;
	}
	
	public float getBaseRange() {
		if (!this.isRanged()) return 0;
		return this.rangedWeapon.range + soldier.subparty.getBonusGeneralRange();
	}
	
	public float getRangeDmg() {
		if (!this.isRanged()) return 0;
		return this.ammoType.dmg;
	}
	
	// probably shouldn't be here
	public boolean shouldDrawPlacementRegion() {
		return this == stage.selectedUnit;
	}
}
