package kyle.game.besiege.battle;

import kyle.game.besiege.battle.Unit.Orientation;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.panels.BottomPanel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public class SiegeUnit extends Group {
	final int FRAME_COLS = 2;
	final int FRAME_ROWS = 1;
	
	final int DEATH_TIME = 60;

	static final int size_x = 3; // 2x2 for realism?
	static final int size_y = 3;

	final float SPEED_FACTOR = 1f;
	final int OPERATORS_NEEDED = 2;

	BattleStage stage;
	
	Array<Unit> units; // units who SHOULD be manning this engine
//	boolean isManned = false; // is this currently being manned

	int prev_x;
	int prev_y;

	int pos_x; // bottom left corner of catapult
	int pos_y; // bottom left corner

//	int currentTeam;
	
	float currentSpeed;
	float rotation;

	boolean moving;
	public boolean forceTwoMoves;
	boolean moveToggle;
	boolean moveSmooth;
	boolean isDying;
	boolean isHit;

	float percentComplete;

	int hp = 15; // TODO change?

	Orientation orientation;

	float stateTime;
	float firingStateTime;
	public Animation animationWalk;
	public Animation animationDie;
	public Animation animationFiring;

	public SiegeType type;

	float reloading;
	float timeSinceDeath;

	BPoint nearestTarget;


	public enum SiegeType {
		CATAPULT(20f, 50, 10, 15, 6, "catapult.png", "catapult_firing.png", "Catapult");

		float projectileSpeed;
		int range; 
		int damage;
		int rate;
		int accuracy;
		String moveFile;
		String firingFile;
		String name;

		private SiegeType(float projectileSpeed, int range, int damage, int rate, int accuracy, String moveFile, String firingFile, String name) {
			this.projectileSpeed = projectileSpeed;
			this.range = range;
			this.damage = damage;
			this.rate = rate;
			this.accuracy = accuracy;
			this.moveFile = moveFile;
			this.firingFile = firingFile;
			this.name = name;
		}
	}


	public SiegeUnit(BattleStage parent, SiegeType type, int pos_x, int pos_y) {
		stage = parent;

		this.setX(pos_x);
		this.setY(pos_y);

		this.setWidth(stage.scale*stage.unit_width*size_x);
		this.setHeight(stage.scale*stage.unit_height*size_y);
		//		this.setWidth(texture.getRegionWidth());
		//		this.setHeight(texture.getRegionHeight());

		this.setOriginX(this.getWidth()/2);
		this.setOriginY(this.getHeight()/2);

		this.pos_x = pos_x;
		this.pos_y = pos_y;

		this.prev_x = pos_x;
		this.prev_y = pos_y;

		this.type = type;
		
		this.units = new Array<Unit>();

		// TODO check if position already occupied before creating
		this.updateArray();

		this.orientation = Orientation.DOWN;

		//		if (this.team == 1)  stance = Stance.DEFENSIVE;

		// TODO change to texture region once I have more

		animationWalk = createAnimation(type.moveFile, 1, .25f);
		//		animationDie = createAnimation(type., 4, .25f);
		//		animationDie.setPlayMode(Animation.NORMAL);
		animationFiring = createAnimation(type.firingFile, 4, 1f);
		animationFiring.setPlayMode(Animation.NORMAL);

		firingStateTime = 5f; // don't want animation to start with reloading
		stateTime = 0f;

		reloading = type.rate/2; // initial reload time
		
		
	}

	// create animation with speed .25f assuming one row, loops by default
	private Animation createAnimation(String filename, int columns, float time) {
		Texture walkSheet = new Texture(Gdx.files.internal("objects/"+filename)); 
		TextureRegion[][] textureArray = TextureRegion.split(walkSheet, walkSheet.getWidth()/columns, walkSheet.getHeight()/1);
		Animation animation = new Animation(time, textureArray[0]);
		animation.setPlayMode(Animation.LOOP);
		return animation;
	}

	@Override
	public void act(float delta) {
		stateTime += delta;
		firingStateTime += delta;

		// flip flop logic kind of
		if (moveToggle) {
			moveSmooth = false;
			moveToggle = false;
		}
		if (this.isDying) {
//			this.die(delta);
			return;
		}
		if (this.hp <= 0) { 
			System.out.println("Still here not dying?");
			return;
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
		else {
			if (this.isManned()) {
				if (this.reloading > 0) {
					if (nearestTarget != null) face(nearestTarget);
					reloading -= delta;
					if (reloading < 0f)
						firingStateTime = 0;
				}
				else {
					nearestTarget = getNearestTarget(this.getCurrentRange());

					if (nearestTarget != null) {
						if (this.distanceTo(nearestTarget) < this.getCurrentRange()) {
							fireAtEnemy();
						}
					}
					else {
						moveToTarget();
					}
				}
			}
//			else System.out.println("Unmanned");
		}
	}

	// updates stage.siegeunits
	public void updateArray() {
		for (int i = 0; i < this.size_x; i++) {
			for (int j = 0; j < this.size_y; j++) {
				stage.siegeUnits[prev_y+j][prev_x+i] = null;				
			}
		}

		for (int i = 0; i < this.size_x; i++) {
			for (int j = 0; j < this.size_y; j++) {
				stage.siegeUnits[pos_y+j][pos_x+i] = this;				
			}
		}
	}
	
	// return the closer point to unit that's calling
	public BPoint getOperatorPoint(Unit calling) {
		if (isManned()) return null;
		
		int back_left_x, back_left_y, back_right_x, back_right_y; 
		
		if (orientation == Orientation.UP) {
			back_left_x = pos_x - 1;
			back_left_y = pos_y;
			
			back_right_x = pos_x + size_x;
			back_right_y = pos_y;
		}
		else if (orientation == Orientation.DOWN) {
			back_right_x = pos_x -1;
			back_right_y = pos_y + size_y - 1;

			back_left_x = pos_x + size_x;
			back_left_y = pos_y + size_y - 1;
		}
		else if (orientation == Orientation.LEFT) {
			back_left_x = pos_x + size_x - 1;
			back_left_y = pos_y - 1;

			back_right_x = pos_x + size_x - 1;
			back_right_y = pos_y + size_y;
		}
		else {
			back_left_x = pos_x;
			back_left_y = pos_y + size_y;

			back_right_x = pos_x;
			back_right_y = pos_y - 1;
		}
		
		boolean right_needed = true;
		boolean left_needed = true;
		
		for (Unit unit : units) {
			if (unit.pos_x == back_left_x && unit.pos_y == back_left_y) {
				left_needed = false;
			} else if (unit.pos_x == back_right_x && unit.pos_y == back_right_y) {
				right_needed = false;
			} else {
//				System.out.println("unit not manning!");
			}
		}
		// return the closer of the two if both vacant
		if (right_needed && left_needed) {
			BPoint right = new BPoint(back_right_x, back_right_y);
			BPoint left = new BPoint(back_left_x, back_left_y);
			double right_dist = calling.distanceTo(right);
			double left_dist = calling.distanceTo(left);
			if (right_dist > left_dist) return left;
			else return right;
		}
		else if (right_needed) return new BPoint(back_right_x, back_right_y);
		else if (left_needed) return new BPoint(back_left_x, back_left_y);
		else return null;		
	}
	
	public boolean adjacent(Unit unit) {
		int back_left_x, back_left_y, back_right_x, back_right_y; 
		
		if (orientation == Orientation.UP) {
			back_left_x = pos_x - 1;
			back_left_y = pos_y;
			
			back_right_x = pos_x + size_x;
			back_right_y = pos_y;
		}
		else if (orientation == Orientation.DOWN) {
			back_right_x = pos_x -1;
			back_right_y = pos_y + size_y - 1;

			back_left_x = pos_x + size_x;
			back_left_y = pos_y + size_y - 1;
		}
		else if (orientation == Orientation.LEFT) {
			back_left_x = pos_x + size_x - 1;
			back_left_y = pos_y - 1;

			back_right_x = pos_x + size_x - 1;
			back_right_y = pos_y + size_y;
		}
		else {
			back_left_x = pos_x;
			back_left_y = pos_y + size_y;

			back_right_x = pos_x;
			back_right_y = pos_y - 1;
		}
		if ((unit.pos_x == back_left_x && unit.pos_y == back_left_y) || (unit.pos_x == back_right_x && unit.pos_y == back_right_y))
			return true;
		return false;
	}
	
	public int currentTeam() {
		if (this.units.size > 0)
			return units.first().team;
		else return -1;
	}
	
	// return 0 if enemy, 1 if player ,and -1 if neither
	public int enemyTeam() {
		if (this.units.size > 0) {
			if (units.first().team == 0) return 1;
			else return 0;
		}
		else return -1;
	}
	
	// currently has men operating it
	public boolean isManned() {
		if (!hasMen()) return false;
		
		for (Unit unit: units) {
			if (!this.adjacent(unit)) return false;
		}
		
		return true;
	}
	
	// are there units assigned to this guy
	public boolean hasMen() {
		if (this.units.size == this.OPERATORS_NEEDED) return true;
		else return false;
	}

	public float getSpeed() {
		return currentSpeed * SPEED_FACTOR;
	}

	public float getCurrentRange() {
		return this.type.range;
	}

	public String getStatus() {
		if (this.isDying) 				return "Destroyed";
		if (this.moveSmooth) 			return "Moving";
		if (this.reloading > 0) 		return "Firing";
		else return "Idle";
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {		

		if (isDying) {
			drawAnimation(batch, animationDie, timeSinceDeath, false);
			super.draw(batch,  parentAlpha);
		}
		else {
			this.setScale(1);
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

			if (this.isHit){
				batch.setColor(c);
				this.isHit = false;
			}
		}
	}
	
	public int calcHP() {
		return this.hp;
	}


//	private void moveToPoint(BPoint point) {
//		this.face(point);
//		if (!this.moveForward()) {
//			faceAlt(point);
//			if (!this.moveForward()) {
//				this.orientation = getRandomDirection();
//				this.moveForward();
//			}
//		}
//	}


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
//		//		this.quiver -= 1;
//
		this.reloading = type.rate;
		BPoint target = getNearestTarget(this.type.range);
		face(target);
		Projectile projectile = new Projectile(this, target);

		stage.addActor(projectile);
	}

	// returns nearest enemy that's a certain distance away
	// get nearest wall to start off
	private BPoint getNearestTarget(float distance) {
		Unit closest = null;
		Unit closestRetreating = null;
		Unit closestNormal = null; // prioritize archers

		double closestDistance = 			9999999;
		double closestNormalDistance = 		9999999;
		double closestRetreatingDistance = 	9999999;

		double MIN_DIST = 10f; // arbitrary

		
		Array<Unit> enemy;
		if (this.currentTeam() == 0) enemy = stage.enemies;
		else enemy = stage.allies;
		
		
		// check for nearby units
		for (Unit that : enemy) {
			double dist = this.distanceTo(that);
			if (dist > MIN_DIST && dist < distance) {
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
		boolean PRIORITIZE_ARCHERS = false;

		if (PRIORITIZE_ARCHERS || closestDistance < closestNormalDistance){
			if (closest != null) {
				return new BPoint(closest);
			}
			else if (closestNormal != null) {
				return new BPoint(closestNormal);
			}
			else if (closestRetreating != null) {
				return new BPoint(closestRetreating);
			}
			else return null;
		}
		else {
			if (closestNormal != null) {
				return new BPoint(closestNormal);
			}
			if (closest != null) {
				return new BPoint(closest);
			}
			else if (closestRetreating != null){
				return new BPoint(closestRetreating);
			}
			else return null;
		}
	}

	public double distanceTo(Unit that) {
		if (that == null) return Double.POSITIVE_INFINITY;
		return Math.sqrt((that.pos_x-this.pos_x)*(that.pos_x-this.pos_x) + (that.pos_y-this.pos_y)*(that.pos_y-this.pos_y));
	}

	public double distanceTo(BPoint that) {
		if (that == null) return Double.POSITIVE_INFINITY;
		return Math.sqrt((that.pos_x-this.pos_x)*(that.pos_x-this.pos_x) + (that.pos_y-this.pos_y)*(that.pos_y-this.pos_y));
	}

//	public void hurt(double damage, Unit attacker) {
//		if (damage <= 0) return;
//
//		this.hp -= (int) (damage + .5);
//
//		this.isHit = true;
//		if (this.hp <= 0) {
//			this.isDying = true;
//			this.kill();
//			//			this.destroy();
//			if (attacker != null) {
//				if (attacker.attackingSiege == this) attacker.attackingSiege = null;
//				// usually full level, but spread some out to party
////				attacker.soldier.addExp(this.soldier.getExpForKill());;
//			}
//		}
//		//		System.out.println(this.hp);
//	}

//	public void die(float delta) {
//		timeSinceDeath += delta;
//		if (timeSinceDeath > DEATH_TIME) {
//			stage.removeSiegeUnit(this);
//		}
//	}
	
	
	private void moveToTarget() {
		System.out.println("moving to target");
		BPoint target = getNearestTarget(Float.MAX_VALUE);
		if (target == null) {
			System.out.println("null target");
			return;
		}
		this.face(target);
		
		Orientation original = this.orientation;

		if (!this.moveForward()) {
			if (Math.random() > .5) this.forceTwoMoves = true;

			// move in a different direction
			faceAlt(target);
			if (!this.moveForward()) {

//				// try the last two directions as a last resort
				this.orientation = Unit.getOppositeOrientation(this.orientation);
				if (!this.moveForward()) {
					this.orientation = Unit.getOppositeOrientation(original);
					if (!this.moveForward()) {
						// this actually seems to work!
						System.out.println("stuck!");
					}
				}
			}
		}
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
			pos_y -= 1;
		}
		else if (direction == Orientation.UP) {
			if (!canMove(pos_x, pos_y+1)) return false;
			pos_y += 1;
		}
		else if (direction == Orientation.LEFT) {
			if (!canMove(pos_x-1, pos_y)) return false;
			pos_x -= 1;
		}
		else if (direction == Orientation.RIGHT) {
			if (!canMove(pos_x+1, pos_y)) return false;
			pos_x += 1;
		}

		updateArray();
		
		moving = true;
		moveSmooth = true;
		percentComplete = 0;
		this.currentSpeed = (float)(1-stage.slow[pos_y][pos_x]);

		if (prev_y != pos_y && prev_x != pos_x) System.out.println("error!");

		this.orientation = direction;
		return true;
	}


	public boolean canMove(int pos_x, int pos_y) {
		if (pos_x < 0 || pos_y < 0 || pos_x >= stage.size_x-size_x || pos_y >= stage.size_y-size_y) return false;
		
		// surround with spacing to make sure units can fit
		for (int i = -1; i < size_x+1; i++) {
			for (int j = -1; j < size_y+1; j++) {
				if (stage.units[pos_y + j][pos_x + i] != null && !this.units.contains(stage.units[pos_y + j][pos_x + i], true)) return false;
				if (stage.siegeUnits[pos_y + j][pos_x + i] != null && stage.siegeUnits[pos_y + j][pos_x + i] != this) return false;
			
				if (pos_y + j < 0 || pos_x + i < 0 || pos_y + j >= stage.size_y || pos_x + i >= stage.size_x) return false;
				if (stage.closed[pos_y + j][pos_x + i]) return false;
				// right now 
//				if (stage.units[pos_y + j][pos_x + i] != null) return false;
			}
		}
		return true;
	}
	
	public static boolean canPlace(BattleStage stage, int pos_x, int pos_y) {
	// surround with spacing to make sure units can fit
		for (int i = -1; i < size_x+1; i++) {
			for (int j = -1; j < size_y+1; j++) {
				if (pos_y + j < 0 || pos_x + i < 0 || pos_y + j >= stage.size_y || pos_x + i >= stage.size_x) return false;
				if (stage.closed[pos_y + j][pos_x + i]) return false;
				// right now 
				if (stage.units[pos_y + j][pos_x + i] != null) return false;
				if (stage.siegeUnits[pos_y + j][pos_x + i] != null) return false;
			}
		}
		return true;
	}


//	public boolean isAdjacent(Unit that) {
//		int distance_x = Math.abs(that.pos_x - this.pos_x);
//		int distance_y = Math.abs(that.pos_y - this.pos_y);
//
//		if (distance_x > 1 || distance_y > 1) return false;
//		if (distance_x == 1 && distance_y == 1) return false;
//		return true;
//	}

//	// call this when a soldier dies from wounds
//	public void kill() {
//		stage.units[pos_y][pos_x] = null;
//		this.pos_x = -100;
//		this.pos_y = -100;
//		this.removeActor(weaponDraw);
//
//		stage.battle.casualty(this.soldier, (this.team == 0) == (stage.playerDefending));
//
//		//		System.out.println("DESTROYED");
//		//party.casualty(soldier);
//	}


	// same as above but with point
	public void face(BPoint that) {
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

	public float getCenterX() {
		return getX() + getOriginX();
	}
	public float getCenterY() {
		return getY() + getOriginY();
	}

	public boolean inMap() {
		for (int i = 0; i < this.size_x; i++) {
			for (int j = 0; j < this.size_y; j++) {
				if (pos_x+i >= stage.size_x ||
				pos_y+j >= stage.size_y ||
				pos_x < 0 || 
				pos_y < 0) return false;
			}
		}
		return true;
	}
}
