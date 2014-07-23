package kyle.game.besiege.battle;

import kyle.game.besiege.battle.BattleMap.Ladder;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.Equipment;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.RangedWeapon;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public class Unit extends Group {
	final int FRAME_COLS = 2;
	final int FRAME_ROWS = 1;

	final int DEFENSE_DISTANCE = 5;
	final int ATTACK_EVERY = 1;

	final int RETREAT_THRESHOLD = 2;

	public float NEAR_COVER_DISTANCE = 3;
	public float HEIGHT_RANGE_FACTOR = 6;
	public float MAN_SIEGE_DISTANCE = 10;

	final float DEATH_TIME = 300;
	final float BASE_SPEED = .2f;

	static final float UNIT_HEIGHT_GROUND = .1f;
	static final float UNIT_HEIGHT_HORSE = .2f;

	static final float CLIMB_HEIGHT = .1f; // how high can units climb

	final float POLARM_BONUS = 3.5f;

	public BattleStage stage;	
	public Unit attacking;
	public SiegeUnit attackingSiege;

	public Party party;
	public Soldier soldier;
	public Weapon weapon;
	public RangedWeapon rangedWeapon;
	public SiegeUnit siegeUnit;

	public boolean isMounted = true;
//	private boolean inCover;
	private Point nearestCover;

	public int quiver;
	//	public int height; // height off the ground

	//	public int baseAtk;
	//	public int baseDef;
	//	public int baseSpd;

	public int original_x;
	public int original_y;

	public int atk;
	public int def;
	public float spd;

	float timer = ATTACK_EVERY;
	float reloading = 0f;
	//float lastFace = 0f;
	public int hp;

	//	public float speed = .35f;
	public float speed = .35f;

	public float currentSpeed = 0;
	public int team;
	public Array<Unit> enemyArray;
	boolean isHit; // is unit hit
	boolean isDying;
	float timeSinceDeath = 0;

	public int pos_x;
	public int pos_y;

	public int prev_x; 
	public int prev_y;
	public boolean moving;
	public boolean forceTwoMoves;
	float ladder_height;

	public float percentComplete; // between 0 and 1, used for moving?
	public enum Orientation {LEFT, UP, RIGHT, DOWN};
	public enum Stance {AGGRESSIVE, DEFENSIVE};
	public Orientation orientation;
	public Stance stance;
	public Unit nearestEnemy;
	public Unit nearestTarget;

	public WeaponDraw weaponDraw;

	public float rotation;
	public boolean retreating;

	float stateTime;
	float firingStateTime;
	public Animation animationWalk;
	public Animation animationAttack;
	public Animation animationDie;
	public Animation animationFiring;

	// shouldn't be used that much, mostly for drawing horses in battles
	public Equipment horse;
	public Equipment shield;
	public int shield_hp;
	public int horse_hp;

	public boolean moveSmooth; // use this to do animation so it's smoother
	private boolean moveToggle; // only set to true for the frame that moving is set to false;
	//	Animation knightIdle;
	//	Animation swordWalk;
	//	Animation swordAttack;

	public Unit(BattleStage parent, int pos_x, int pos_y, int team, Soldier soldier, Party party) {
		stage = parent;

		//		texture = new TextureRegion(new Texture("red.png"));
		this.party = party;
		this.team = team;
		if (this.team == 0) enemyArray = stage.enemies;
		else enemyArray = stage.allies;

		this.original_x = pos_x;
		this.original_y = pos_y;
		this.setX(pos_x);
		this.setY(pos_y);

		this.setWidth(stage.scale*stage.unit_width);
		this.setHeight(stage.scale*stage.unit_height);
		//		this.setWidth(texture.getRegionWidth());
		//		this.setHeight(texture.getRegionHeight());

		this.setOriginX(this.getWidth()/2);
		this.setOriginY(this.getHeight()/2);

		this.horse = soldier.getHorse();
		this.shield = soldier.getShield();
		if (shield != null) shield_hp = shield.hp;
		if (horse != null) horse_hp = horse.hp;

		quiver = soldier.level; // number of arrows this unit gets

		ladder_height = 0;

		this.pos_x = pos_x;
		this.pos_y = pos_y;

		// TODO check if position already occupied before creating
		stage.units[pos_y][pos_x] = this;

		this.orientation = Orientation.DOWN;
		this.stance = Stance.AGGRESSIVE;

		this.soldier = soldier;
		this.weapon = soldier.weapon;
		this.rangedWeapon = soldier.rangedWeapon;
		//		if (rangedWeapon != null) 
		//			this.stance = Stance.DEFENSIVE;

		// check if in cover
		checkIfInCover();
		

		calcStats();

		if (this.spd == 0) System.out.println("speed is 0");

		//		if (this.team == 1)  stance = Stance.DEFENSIVE;

		// TODO change to texture region once I have more
		String walkFile;
		String attackFile;
		String dieFile;
		String firingFile;
		if (soldier.tier < 3) {
			walkFile = "farmer_walk.png";
			attackFile = "farmer_walk.png";
			dieFile = "farmer_die.png";
			firingFile = "bandit_firing.png";
		}
		else if (soldier.tier < 6) {
			walkFile = "bandit_walk.png";
			attackFile = "bandit_walk.png";
			dieFile = "bandit_die.png";
			firingFile = "bandit_firing.png";
		}
		else if (soldier.tier < 8) {
			walkFile = "bandit_walk.png";
			attackFile = "bandit_walk.png";
			dieFile = "bandit_die.png";
			firingFile = "bandit_firing.png";
		}
		else {
			walkFile = "knight_walk.png";
			attackFile = "knight_walk.png";
			dieFile = "knight_die.png";
			firingFile = "bandit_firing.png";
		}

		animationWalk = createAnimation(walkFile, 2, .25f);
		animationAttack = createAnimation(attackFile, 2, .25f);
		animationDie = createAnimation(dieFile, 4, .25f);
		animationDie.setPlayMode(Animation.NORMAL);
		animationFiring = createAnimation(firingFile, 2, 2f);
		animationFiring.setPlayMode(Animation.NORMAL);

		firingStateTime = 0f;
		stateTime = 0f;
		if (rangedWeapon != null) reloading = rangedWeapon.rate/2; // initial reload time

		//		this.height = 0;

		this.weaponDraw = new WeaponDraw(this);
		this.addActor(weaponDraw);


		// mounted by default
		if (this.horse != null) {
			isMounted = true;
		}
		//
		//		// TODO REMOVE
		//		if (this.team == 0) this.retreating = true;
	}

	// create animation with speed .25f assuming one row, loops by default
	private Animation createAnimation(String filename, int columns, float time) {
		Texture walkSheet = new Texture(Gdx.files.internal("units/"+filename)); 
		TextureRegion[][] textureArray = TextureRegion.split(walkSheet, walkSheet.getWidth()/columns, walkSheet.getHeight()/1);
		Animation animation = new Animation(time, textureArray[0]);
		animation.setPlayMode(Animation.LOOP);
		return animation;
	}

	@Override
	public void act(float delta) {

		if (nearestEnemy == null) this.getNearestEnemy();
		
		// flip flop logic kind of
		if (moveToggle) {
			moveSmooth = false;
			moveToggle = false;
		}
		if (this.isDying) {
			this.die(delta);
			return;
		}
		if (this.hp <= 0) { 
			System.out.println("Still here not dying?");
			return;
		}
		else if (this.hp <= RETREAT_THRESHOLD) {
			this.retreating = true;
			this.attacking = null;
			//System.out.println("unit retreating");
			//			this.retreat();
		}
		if (this.attacking != null) {
			timer += delta;
			if (timer > ATTACK_EVERY) 
			{
				attack();
				timer = 0;
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
		else if (this.retreating) {
			retreat();
		}
		else if (this.siegeUnit != null) {
			// check if manning
			if (!siegeUnit.adjacent(this)) {
				Point nearSiegePoint = siegeUnit.getOperatorPoint(this);
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
			

			if (stance == Stance.AGGRESSIVE && (!(this.isRanged() && distanceTo(getNearestEnemy()) < this.getCurrentRange()) || this.quiver <= 0)) {				
				moveToEnemy();
			}
			else { // either defensive stance or aggressive but ranged within range
				// just use attack every for convenience
				timer += delta;
				if (timer > ATTACK_EVERY){
					faceEnemy();
					timer = 0;
				}
				// if enemy is within one unit and fighting, can move to them.
				if (nearestEnemy != null && (nearestEnemy.distanceTo(this) < DEFENSE_DISTANCE && nearestEnemy.attacking != null && !this.bowOut()))
					moveToEnemy();
				else if (this.reloading > 0) {
					if (nearestTarget != null) face(nearestTarget);
					reloading -= delta;
					if (reloading < 0f)
						firingStateTime = 0;
				}
				// if ranged, fire weapon
				else {
					nearestTarget = getNearestTarget();

					if (this.bowOut() && !shouldMove()) {
						if (nearestTarget != null) {
							if (nearestTarget.distanceTo(this) < this.getCurrentRange()) {
								fireAtEnemy();
							}
						}
					}
					else if (!this.bowOut() && this.stance == Stance.DEFENSIVE) {
						if ((this.pos_x != original_x || this.pos_y != original_y) && canMove(pos_x, pos_y)) this.moveToPoint(new Point(original_x, original_y));
					}
				}
			}
		}
	}
	
	public void checkIfShouldManSiege() {
		// for now, no defensive units should siege
		if (this.bowOut() || this.isMounted() || (stage.siegeAttack && this.stance == Stance.DEFENSIVE)) return;
		
		for (SiegeUnit s : stage.siegeUnitsArray) {
			if (!s.hasMen() && s.enemyTeam() != this.team) {
				Point closest = s.getOperatorPoint(this);
				if (this.distanceTo(closest) < MAN_SIEGE_DISTANCE) {
					this.man(s);
					
					return;
				}
			}
		}
	}
	
	public void checkIfInCover() {
		for (Point p : stage.battlemap.cover) {
			if (p.pos_x == this.pos_x && p.pos_y == this.pos_y) {
				this.nearestCover = p;
			}
		}
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
		return currentSpeed * spd;
	}

	public float getCurrentRange() {
		if (this.rangedWeapon == null) return -1;
		//		System.out.println(this.rangedWeapon.range + this.getFloorHeight()*HEIGHT_RANGE_FACTOR);
		return this.rangedWeapon.range + this.getFloorHeight()*HEIGHT_RANGE_FACTOR;
	}


	public String getStatus() {
		if (this.isDying) 				return "Dying";
		if (this.siegeUnit != null)		return "Manning " + siegeUnit.type.name;
		if (this.retreating)		 	return "Retreating";
		if (this.attacking != null) 	return "Attacking " + attacking.soldier.name;
		if (this.moveSmooth && 
				nearestCover != null) 			return "Moving to cover";
		if (this.moveSmooth) 			return "Charging";
		if (this.isRanged() && 
				this.reloading > 0 && 
				inCover()) 				return "Firing from cover";
		if (this.bowOut() && 
				this.reloading > 0) 		return "Firing";
		else return "Idle";
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {		

		stateTime += Gdx.graphics.getDeltaTime();
		firingStateTime += Gdx.graphics.getDeltaTime();

		if (isDying) {
			drawAnimation(batch, animationDie, timeSinceDeath, false);
			super.draw(batch,  parentAlpha);
		}
		else {
			this.setScale(1+this.getFloorHeight()/5f);
			super.draw(batch, parentAlpha);

			this.toFront();
			Color c = new Color(batch.getColor());

			updateRotation();

			if (moving) {
				setX(stage.scale * currentX() * stage.unit_width);
				setY(stage.scale * currentY() * stage.unit_height);
			}
			else {
				setX(stage.scale * pos_x * stage.unit_width);
				setY(stage.scale * pos_y * stage.unit_height);
			}

			if (this.isHit)
				batch.setColor(1, 0, 0, 1); 


			else if (attacking != null) {
				// maybe remove later
				this.face(attacking);
				drawAnimation(batch, animationAttack, stateTime, true);
				//			drawAnimation(batch, swordAttack, stateTime);
			}
			else if (moveSmooth) {
				drawAnimation(batch, animationWalk, stateTime, true);
				//			drawAnimation(batch, swordWalk, stateTime);
			}	
			else if (reloading > 0) {
				drawAnimation(batch, animationFiring, firingStateTime, false);
			}
			else {
				drawAnimation(batch, animationWalk, 0, false);
				//			drawAnimation(batch, swordWalk, 0);
			}

			//		if (team == 0) 
			//			batch.draw(texture, pos_x_world, pos_y_world, getWidth(), getHeight());

			if (this.isHit){
				batch.setColor(c);
				this.isHit = false;
			}
		}
	}

	public int calcHP() {
		//		if (this.soldier.getType() == Soldier.SoldierType.ARCHER) return 10 + this.def*2;
		//		else 
		return 15 + this.def*3;
	}
	private void moveToEnemy() {
		nearestCover = null;
		if (enemyArray.size == 0) return;
		this.faceEnemy();

		if (this.onWall() != nearestEnemy.onWall()) {
			if (this.isMounted()) this.dismount();
			this.moveToPoint(getNearestLadder());
			return;
		}

		Orientation original = this.orientation;

		if (!this.moveForward()) {
			if (Math.random() > .5) this.forceTwoMoves = true;

			// move in a different direction
			faceEnemyAlt();
			if (!this.moveForward()) {

				
				// try the last two directions as a last resort
				this.orientation = getOppositeOrientation(this.orientation);
				if (!this.moveForward()) {
					this.orientation = getOppositeOrientation(original);
					if (!this.moveForward()) {
						// this actually seems to work!
						//System.out.println("stuck!");
					}
				}
			}
		}
	}

	private void moveToPoint(Point point) {
		this.face(point);
		Orientation original = this.orientation;

		if (!this.moveForward()) {
			
			if (Math.random() > .5) this.forceTwoMoves = true;

			faceAlt(point);
			if (!this.moveForward()) {					
				// try the last two directions as a last resort
				this.orientation = getOppositeOrientation(this.orientation);
				if (!this.moveForward()) {
					this.orientation = getOppositeOrientation(original);
					if (!this.moveForward()) {
						// this actually seems to work!
						//System.out.println("stuck!");
					}
				}
			}
		}
	}

	private void retreat() {
		nearestCover = null;
		this.unman();
		moveToPoint(getNearestExit());

		// effectively wound soldier until after battle
		if (this.pos_x == 0 || this.pos_y == 0 || this.pos_x == stage.size_x-1 || this.pos_y == stage.size_y-1) {
			//			leaveField();
			soldier.party.wound(soldier);
			leaveBattle();
			//			System.out.println("Safe");
		}
	}

	// should move checks if archer needs to move before shooting, then moves them away from an obstruction or to cover
	private boolean shouldMove() {
		Point facing = getAdjacentPoint();
		if (facing == null) return false; // facing off stage
		BattleMap.Object object = stage.battlemap.objects[facing.pos_y][facing.pos_x];

		// check if object in front is too heigh
		if (object != null && (object.height+stage.heights[facing.pos_y][facing.pos_x] > Projectile.INITIAL_HEIGHT+this.getFloorHeight())) {
//			System.out.println("should move");
			this.startMove(getRandomDirection());
			return true;
		}

		if (!inCover() && nearestEnemy != null) {
			if (this.nearestCover != null) {
				moveToPoint(nearestCover);
			}
		}
		return false;
	}

	private Point getAdjacentPoint() {
		Point point = null;
		if (this.orientation == Orientation.LEFT)
			point = new Point(this.pos_x-1, this.pos_y);
		if (this.orientation == Orientation.RIGHT)
			point = new Point(this.pos_x+1, this.pos_y);
		if (this.orientation == Orientation.UP)
			point = new Point(this.pos_x, this.pos_y+1);
		if (this.orientation == Orientation.DOWN)
			point = new Point(this.pos_x, this.pos_y-1);
		if (point.pos_x < 0 || point.pos_x >= stage.size_x || point.pos_y < 0 || point.pos_y >= stage.size_y) point = null;
		return point;
	}

	private void fireAtEnemy() {
		this.quiver -= 1;

		this.reloading = rangedWeapon.rate;
		Unit enemy = getNearestTarget();
		face(enemy);
		Projectile projectile = new Projectile(this, enemy);

		stage.addActor(projectile);
	}

	private void faceEnemy() {
		Unit nearest = this.getNearestEnemy();
		if (nearest == null) return;
		this.face(nearest);
	}

	private void faceEnemyAlt() {
		Unit nearest = this.getNearestEnemy();
		if (nearest == null) return;
		this.faceAlt(nearest);
	}
	
	private Point getNearestCover() {
		// check to see if should move to cover
		Point closest = null;
		float closestDistance = Float.MAX_VALUE;
		
		for (Point p : stage.battlemap.cover) { // && nearestEnemy.distanceTo(p) < rangedWeapon.range  ?
			if (p.orientation == this.orientation && Math.abs(stage.heights[p.pos_y][p.pos_x] - this.getFloorHeight()) < Unit.CLIMB_HEIGHT) {
				if (Math.abs(this.pos_x - p.pos_x) < NEAR_COVER_DISTANCE && Math.abs(this.pos_y - p.pos_y) < NEAR_COVER_DISTANCE) {
					float dist = (float) distanceTo(p);
					if (dist < closestDistance && stage.units[p.pos_y][p.pos_x] == null) {
						closest = p;
						closestDistance = dist;
					}
				}
			}
		}

		return closest;
	}

	private Unit getNearestEnemy() {
		Unit closest = null;
		Unit closestRetreating = null;
		double closestDistance = 99999;

		for (Unit that : enemyArray) {
			if (that.team == this.team) System.out.println("TEAM ERROR!!!");
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
			return closest;
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

		for (Unit that : enemyArray) {
			if (that.team == this.team) System.out.println("TEAM ERROR!!!");
			double dist = this.distanceTo(that);
			if (dist > MIN_DIST && dist < this.getCurrentRange()) {
				if (!that.retreating && that.bowOut() && dist < closestDistance) {
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
				nearestEnemy = closest;
				return closest;
			}
			else if (closestNormal != null) {
				nearestEnemy = closestNormal;
				return closestNormal;
			}
			else {
				nearestEnemy = closestRetreating;
				return closestRetreating;
			}
		}
		else {
			if (closestNormal != null) {
				nearestEnemy = closestNormal;
				return closestNormal;
			}
			if (closest != null) {
				nearestEnemy = closest;
				return closest;
			}
			else {
				nearestEnemy = closestRetreating;
				return closestRetreating;
			}
		}
	}

	private Point getNearestExit() {
		int point_x = 0;
		int point_y = 0;

		int dist_to_right = stage.size_x-pos_x;
		int dist_to_top = stage.size_y-pos_y;
		int dist_to_left = pos_x;
		int dist_to_bottom = pos_y;

		if (dist_to_right < dist_to_left && dist_to_right < dist_to_top && dist_to_right < dist_to_bottom) {
			point_x = stage.size_x-1;
			point_y = pos_y;
		}
		else if (dist_to_left < dist_to_right && dist_to_left < dist_to_top && dist_to_left < dist_to_bottom) {
			point_x = 0;
			point_y = pos_y;
		} 
		else if (dist_to_top < dist_to_left && dist_to_top < dist_to_right && dist_to_top < dist_to_bottom) {
			point_x = pos_x;
			point_y = stage.size_y-1;
		}
		else {
			point_x = pos_x;
			point_y = 0;
		}

		Point closest = new Point(point_x, point_y);

		if (this.onWall()) {
			closest = getNearestLadder();
		}

		return closest;
	}


	public Point getNearestLadder() {
		Point closest = null;

		double closestDistance = 999999;
		// get closest ladder
		for (Ladder l : stage.battlemap.ladders) {
			Point ladderPoint = new Point(l.pos_x, l.pos_y);
			double dist = this.distanceTo(ladderPoint);
			if (dist < closestDistance) {
				closest = ladderPoint;
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
	
	// get closest siege unit that doesn't have enough men
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

	public double distanceTo(Point that) {
		if (that == null) return Double.POSITIVE_INFINITY;
		return Math.sqrt((that.pos_x-this.pos_x)*(that.pos_x-this.pos_x) + (that.pos_y-this.pos_y)*(that.pos_y-this.pos_y));
	}

	private void attack() {
		if (!this.attacking.isAdjacent(this) || this.attacking.hp <= 0){
			this.attacking = null;
			return;
		}
		//System.out.println("attack phase: " + attacking.hp);
		this.face(attacking);
		attacking.face(this);

		double damage = this.atk-Math.random()*attacking.def;

		// calculate bonus damage
		double bonusDamage = 0;

		// polearm against cavalry
		if (!weapon.oneHand && attacking.isMounted()) {
			bonusDamage += POLARM_BONUS;
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
		if (this.hp <= 0) {
			this.isDying = true;
			this.kill();
			//			this.destroy();
			if (attacker != null) {
				if (attacker.attacking == this) attacker.attacking = null;
				// usually full level, but spread some out to party
				attacker.soldier.addExp(this.soldier.getExpForKill());;
			}
		}
		//		System.out.println(this.hp);
	}

	public void die(float delta) {
		timeSinceDeath += delta;
		if (timeSinceDeath > DEATH_TIME) {
			stage.removeUnit(this);
		}
	}

	public boolean shieldUp() {
		if (this.shield != null && !this.bowOut()) return true;
		return false;
	}

	// shield on back
	public boolean shieldDown() {
		if (this.shield != null && this.bowOut()) return true;
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
		this.setRotation(rotation);
	}

	public Orientation getOppositeOrientation(Orientation orientation) {
		if (orientation == Orientation.UP) return Orientation.DOWN;
		if (orientation == Orientation.DOWN) return Orientation.UP;
		if (orientation == Orientation.LEFT) return Orientation.RIGHT;
		else return Orientation.LEFT;
	}

	public Orientation getOppositeOrientation() {
		return getOppositeOrientation(this.orientation);
	}

	public void drawAnimation(SpriteBatch batch, Animation animation, float stateTime, boolean loop) {
		TextureRegion region = animation.getKeyFrame(stateTime, loop);
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());		
	}


	public float currentX() {
		return (prev_x + percentComplete*(pos_x-prev_x));
	}

	public float currentY() {
		return (prev_y + percentComplete*(pos_y-prev_y));
	}

	public boolean moveForward() {
		return startMove(this.orientation);
	}

	// returns false if move failed, true otherwise
	public boolean startMove(Orientation direction) {
		if (this.hp < 0) return false;
		prev_x = pos_x;
		prev_y = pos_y;
		if (direction == Orientation.DOWN) {
			if (!canMove(pos_x, pos_y-1)) return false;
			if (stage.units[pos_y-1][pos_x] != null) {
				this.orientation = direction;
				return collision(stage.units[pos_y-1][pos_x]);
			}
			stage.units[pos_y][pos_x] = null;
			pos_y -= 1;
		}
		else if (direction == Orientation.UP) {
			if (!canMove(pos_x, pos_y+1)) return false;
			if (stage.units[pos_y+1][pos_x] != null) {
				this.orientation = direction;
				return collision(stage.units[pos_y+1][pos_x]);
			}
			stage.units[pos_y][pos_x] = null;
			pos_y += 1;
		}
		else if (direction == Orientation.LEFT) {
			if (!canMove(pos_x-1, pos_y)) return false;
			if (stage.units[pos_y][pos_x-1] != null) {
				this.orientation = direction;
				return collision(stage.units[pos_y][pos_x-1]);
			}
			stage.units[pos_y][pos_x] = null;
			pos_x -= 1;
		}
		else if (direction == Orientation.RIGHT) {
			if (!canMove(pos_x+1, pos_y)) return false;
			if (stage.units[pos_y][pos_x+1] != null) { 
				this.orientation = direction;
				return collision(stage.units[pos_y][pos_x+1]);
			}
			stage.units[pos_y][pos_x] = null;
			pos_x += 1;
		}

		stage.units[pos_y][pos_x] = this;

		moving = true;
		moveSmooth = true;
		percentComplete = 0;
		this.currentSpeed = (speed) * (float)(1-stage.slow[pos_y][pos_x]);

		if (prev_y != pos_y && prev_x != pos_x) System.out.println("error!");

		this.orientation = direction;
		return true;
	}

	boolean canMove(int pos_x, int pos_y) {
		if (pos_x < 0 || pos_y < 0 || pos_x >= stage.size_x || pos_y >= stage.size_y) return false;
		if (stage.closed[pos_y][pos_x]) return false;
		if (Math.abs(this.getFloorHeight() - stage.heights[pos_y][pos_x]) > Unit.CLIMB_HEIGHT && (!stage.ladderAt(pos_x, pos_y) || this.isMounted())) return false;
		if (stage.ladderAt(pos_x, pos_y) && this.isMounted()) return false;
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
		this.face(that);

		// change this later
		that.attacking = this;
	}

	public void destroyShield() {
		soldier.equipment.removeValue(shield, true);
		this.shield = null;
		this.weaponDraw.shield = null;
		soldier.calcStats();
		soldier.calcBonus();
		calcStats();
	}

	public void killHorse() {
		soldier.equipment.removeValue(horse, true);
		this.horse = null;
		this.weaponDraw.horseWalk = null;
		soldier.calcStats();
		soldier.calcBonus();
		calcStats();
	}

	private void calcStats() {
		this.atk = soldier.getAtk();
		this.def = soldier.getDef();
		this.spd = soldier.getSpd() + 1 + (float) (BASE_SPEED*Math.random());
		// adjust speed and def
		if (shieldDown())  {
			this.spd -= shield.spdMod;
			this.def -= shield.defMod;
		} 
		if (hasHorseButDismounted()) {
			this.spd -= horse.spdMod;
			this.def -= horse.defMod;
			this.atk -= horse.atkMod;
		}
		this.hp = calcHP();
	}

	public boolean bowOut() {
		return isRanged() && quiver > 0 && attacking == null;
	}

	public boolean isAdjacent(Unit that) {
		int distance_x = Math.abs(that.pos_x - this.pos_x);
		int distance_y = Math.abs(that.pos_y - this.pos_y);

		if (distance_x > 1 || distance_y > 1) return false;
		if (distance_x == 1 && distance_y == 1) return false;
		return true;
	}

	// call this when a soldier dies from wounds
	public void kill() {
		this.unman();;

		stage.units[pos_y][pos_x] = null;
		this.pos_x = -100;
		this.pos_y = -100;
		if (this.team == 0) stage.allies.removeValue(this, true);
		if (this.team == 1) stage.enemies.removeValue(this, true);
		this.removeActor(weaponDraw);

		stage.battle.casualty(this.soldier, (this.team == 0) == (stage.playerDefending));

		//		System.out.println("DESTROYED");
		//party.casualty(soldier);
	}

	// call this when a soldier retreats
	public void leaveBattle() {
		stage.units[pos_y][pos_x] = null;
		this.pos_x = -100;
		this.pos_y = -100;
		if (this.team == 0) stage.allies.removeValue(this, true);
		if (this.team == 1) stage.enemies.removeValue(this, true);

		stage.removeUnit(this);
		stage.retreated.add(this);
		stage.battle.calcBalancePlayer();

		String status = soldier.name;
		String color = "white";

		status += " retreated!";

		if (party == stage.player)
			color = "yellow";
		else // if (dArmies.contains(army, true)) {
			color = "blue";
		BottomPanel.log(status, color);
	}

	public void face(Unit that) {
		int x_dif = that.pos_x - this.pos_x;
		int y_dif = that.pos_y - this.pos_y;

		if (Math.abs(x_dif) > Math.abs(y_dif)) {
			if (x_dif > 0) this.orientation = Orientation.RIGHT;
			else this.orientation = Orientation.LEFT;
		}
		else if (Math.abs(x_dif) < Math.abs(y_dif)) {
			if (y_dif > 0) this.orientation = Orientation.UP;
			else this.orientation = Orientation.DOWN;
		}
		else if (y_dif > 0) this.orientation = Orientation.UP;
		else this.orientation = Orientation.DOWN;
	}

	// same as above but with point
	public void face(Point that) {
		int x_dif = that.pos_x - this.pos_x;
		int y_dif = that.pos_y - this.pos_y;

		if (Math.abs(x_dif) > Math.abs(y_dif)) {
			if (x_dif > 0) this.orientation = Orientation.RIGHT;
			else this.orientation = Orientation.LEFT;
		}
		else if (Math.abs(x_dif) < Math.abs(y_dif)) {
			if (y_dif > 0) this.orientation = Orientation.UP;
			else this.orientation = Orientation.DOWN;
		}
		else if (y_dif > 0) this.orientation = Orientation.UP;
		else this.orientation = Orientation.DOWN;
	}


	// still tries to face the enemy, but in none of the same ways as the other face method
	public void faceAlt(Unit that) {
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

	public void faceAlt(Point that) {
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
				else if (ladder_height > .5f) ladder_height =  BattleMap.CASTLE_WALL_HEIGHT_DEFAULT;
				else if (ladder_height < .5f) ladder_height =  0;

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
		return this.rangedWeapon != null || this.quiver <= 0;
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
}
