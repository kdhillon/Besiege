package kyle.game.besiege.battle;

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

	final int DEFENSE_DISTANCE = 3;
	final int ATTACK_EVERY = 1;
	
	final int RETREAT_THRESHOLD = 2;

	final float DEATH_TIME = 300;

	public BattleStage stage;	
	public Unit attacking;

	public Party party;
	public Soldier soldier;
	public Weapon weapon;
	public RangedWeapon rangedWeapon;

	public int quiver;

	//	public int baseAtk;
	//	public int baseDef;
	//	public int baseSpd;

	public int atk;
	public int def;
	public float spd;

	float timer = 0;
	float reloading = 0;
	public int hp;

	public float speed = .5f;
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
	public float percentComplete; // between 0 and 1, used for moving?
	public enum Orientation {LEFT, UP, RIGHT, DOWN};
	public enum Stance {AGGRESSIVE, DEFENSIVE};
	public Orientation orientation;
	public Stance stance;
	public Unit nearestEnemy;

	public WeaponDraw weaponDraw;

	public float rotation;
	public boolean retreating = false;

	float stateTime;
	public Animation animationWalk;
	public Animation animationAttack;
	public Animation animationDie;
	
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
 
		this.setX(pos_x);
		this.setY(pos_y);

		this.setWidth(stage.scale*stage.unit_width);
		this.setHeight(stage.scale*stage.unit_height);
		//		this.setWidth(texture.getRegionWidth());
		//		this.setHeight(texture.getRegionHeight());

		this.setOriginX(this.getWidth()/2);
		this.setOriginY(this.getHeight()/2);

		//		this.setScale(texture.getRegionWidth()/map.unit_width);

		//		this.setScale(1);
		
		quiver = soldier.level; // number of arrows this unit gets
//		quiver = 1;
		
		calcHP();

		this.pos_x = pos_x;
		this.pos_y = pos_y;

		// TODO check if position already occupied before creating
		stage.map[pos_y][pos_x] = this;

		this.orientation = Orientation.DOWN;
		this.stance = Stance.AGGRESSIVE;
		
		this.soldier = soldier;
		this.weapon = soldier.weapon;
		this.rangedWeapon = soldier.rangedWeapon;
//		if (rangedWeapon != null) 
//			this.stance = Stance.DEFENSIVE;
		
		
		this.soldier.retreated = false;

		//		this.atk = soldier.getAtk();
		//		this.def = soldier.getDef();
		//		this.spd = Math.max(1, soldier.getSpd());

		
		this.atk = soldier.getAtk();
		this.def = soldier.getDef();
		this.spd = soldier.getSpd() + 1 + (float) (1*Math.random());

		this.hp = calcHP();
		
		if (this.spd == 0) System.out.println("speed is 0");

//		if (this.team == 1)  stance = Stance.DEFENSIVE;

		// TODO change to texture region once I have more
		String walkFile;
		String attackFile;
		String dieFile;
		if (soldier.tier < 3) {
			walkFile = "farmer_walk.png";
			attackFile = "farmer_walk.png";
			dieFile = "farmer_die.png";
		}
		else if (soldier.tier < 6) {
			walkFile = "bandit_walk.png";
			attackFile = "bandit_walk.png";
			dieFile = "bandit_die.png";
		}
		else if (soldier.tier < 8) {
			walkFile = "bandit_walk.png";
			attackFile = "bandit_walk.png";
			dieFile = "bandit_die.png";
		}
		else {
			walkFile = "knight_walk.png";
			attackFile = "knight_walk.png";
			dieFile = "knight_die.png";
		}

		Texture walkSheet = new Texture(Gdx.files.internal("units/"+walkFile)); 
		TextureRegion[][] textureArray = TextureRegion.split(walkSheet, walkSheet.getWidth()/FRAME_COLS, walkSheet.getHeight()/FRAME_ROWS);
		animationWalk = new Animation(0.25f, textureArray[0]);
		animationWalk.setPlayMode(Animation.LOOP);

		Texture walkSheet2 = new Texture(Gdx.files.internal("units/"+attackFile)); 
		TextureRegion[][] textureArray2 = TextureRegion.split(walkSheet2, walkSheet2.getWidth()/FRAME_COLS, walkSheet2.getHeight()/FRAME_ROWS);
		animationAttack = new Animation(0.25f, textureArray2[0]);
		animationAttack.setPlayMode(Animation.LOOP);

		Texture walkSheet3 = new Texture(Gdx.files.internal("units/"+dieFile)); 
		TextureRegion[][] textureArray3 = TextureRegion.split(walkSheet3, walkSheet3.getWidth()/4, walkSheet3.getHeight()/1);
		animationDie = new Animation(0.25f, textureArray3[0]);
		animationDie.setPlayMode(Animation.NORMAL);

		//		Texture walkSheet3 = new Texture(Gdx.files.internal("swordAttack.png")); 
		//		TextureRegion[][] textureArray3 = TextureRegion.split(walkSheet3, walkSheet2.getWidth()/FRAME_COLS, walkSheet2.getHeight()/FRAME_ROWS);
		//		swordAttack = new Animation(0.25f, textureArray3[0]);
		//
		//		Texture walkSheet4 = new Texture(Gdx.files.internal("swordWalk.png")); 
		//		TextureRegion[][] textureArray4 = TextureRegion.split(walkSheet4, walkSheet2.getWidth()/FRAME_COLS, walkSheet2.getHeight()/FRAME_ROWS);
		//		swordWalk = new Animation(0.25f, textureArray4[0]);

		stateTime = 0f;

		this.weaponDraw = new WeaponDraw(this);
		this.addActor(weaponDraw);
	}

	@Override
	public void act(float delta) {
		
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
			this.percentComplete += currentSpeed * spd * delta;
			if (percentComplete > 1) {
				moveToggle = true;
				moving = false;
				percentComplete = 1;
			}
		}
		else if (this.retreating) {
			retreat();
		}
		else {
			//			System.out.println("moving2");
			//			getRandomDirection(delta);

			if (stance == Stance.AGGRESSIVE && (!(this.isRanged() && distanceTo(getNearestEnemy()) < this.rangedWeapon.range) || this.quiver <= 0)) {
				moveToEnemy();
			}
			else { // either defensive stance or aggressive but ranged within range
				faceEnemy();
				// if enemy is within one unit and fighting, can move to them.
				if (nearestEnemy != null && (nearestEnemy.distanceTo(this) < DEFENSE_DISTANCE && nearestEnemy.attacking != null))
					moveToEnemy();
				else if (this.reloading > 0) {
					reloading -= delta;
				}
				// if ranged, fire weapon
				else if (this.isRanged() && nearestEnemy != null) {
					if (nearestEnemy.distanceTo(this) < this.rangedWeapon.range)
						fireAtEnemy();
				}
			}
		}
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {		

		stateTime += Gdx.graphics.getDeltaTime();

		if (isDying) {
			drawAnimation(batch, animationDie, timeSinceDeath, false);
		}
		else {
			super.draw(batch, parentAlpha);
			//TESTING
//			if (stateTime > 5 && this.team == 1) retreating = true;


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
				drawAnimation(batch, animationAttack, stateTime, true);
				//			drawAnimation(batch, swordAttack, stateTime);
			}
			else if (moveSmooth) {
				drawAnimation(batch, animationWalk, stateTime, true);
				//			drawAnimation(batch, swordWalk, stateTime);
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
		 return 15 + this.def*3;
	}
	private void moveToEnemy() {
		if (enemyArray.size == 0) return;
		this.faceEnemy();
		if (!this.moveForward()) {
			// move in a different direction
			faceEnemyAlt();
			if (!this.moveForward()) {
				// try a random direction as a last resort
				this.orientation = getRandomDirection();
				this.moveForward();
			}
		}
	}

	private void retreat() {
		this.faceExit();
		if (!this.moveForward()) {
			faceExitAlt();
			if (!this.moveForward()) {
				this.orientation = getRandomDirection();
				this.moveForward();
			}
		}
		
		// effectively wound soldier until after battle
		if (this.pos_x == 0 || this.pos_y == 0 || this.pos_x == stage.size_x-1 || this.pos_y == stage.size_y-1) {
			//			leaveField();
			soldier.retreated = true;
			soldier.party.wound(soldier);
			leaveBattle();
			//			System.out.println("Safe");
		}
	}

	private void fireAtEnemy() {
		this.quiver -= 1;
		faceEnemy();
		this.reloading = rangedWeapon.rate;
		Unit enemy = nearestEnemy;
		Arrow arrow = new Arrow(this, enemy);

		stage.addActor(arrow);
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

	private void faceExit() {
		Point nearest = this.getNearestExit();
		if (nearest == null) return;
		this.face(nearest);
	}

	private void faceExitAlt() {
		Point nearest = this.getNearestExit();
		if (nearest == null) return;
		this.faceAlt(nearest);
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

		return closest;
	}

	public double distanceTo(Unit that) {
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

		attacking.hurt(Math.max(0, this.atk-Math.random()*attacking.def), this);
	}

	public void hurt(double damage, Unit attacker) {
		if (damage <= 0) return;
		
		this.hp -= (int) (damage + .5);

		this.isHit = true;
		if (this.hp <= 0) {
			this.isDying = true;
			this.kill();
			//			this.destroy();
			if (attacker != null) {
				attacker.attacking = null;
				// usually full level, but spread some out to party
				attacker.soldier.exp += this.soldier.level;
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
			if (pos_y == 0 || stage.closed[pos_y-1][pos_x]) return false;
			if (stage.map[pos_y-1][pos_x] != null) {
				this.orientation = direction;
				return collision(stage.map[pos_y-1][pos_x]);
			}
			stage.map[pos_y][pos_x] = null;
			pos_y -= 1;
		}
		else if (direction == Orientation.UP) {
			if (pos_y == stage.size_y-1 || stage.closed[pos_y+1][pos_x]) return false;
			if (stage.map[pos_y+1][pos_x] != null) {
				this.orientation = direction;
				return collision(stage.map[pos_y+1][pos_x]);
			}
			stage.map[pos_y][pos_x] = null;
			pos_y += 1;
		}
		else if (direction == Orientation.LEFT) {
			if (pos_x == 0 || stage.closed[pos_y][pos_x-1]) return false;
			if (stage.map[pos_y][pos_x-1] != null) {
				this.orientation = direction;
				return collision(stage.map[pos_y][pos_x-1]);
			}
			stage.map[pos_y][pos_x] = null;
			pos_x -= 1;
		}
		else if (direction == Orientation.RIGHT) {
			if (pos_x == stage.size_x-1 || stage.closed[pos_y][pos_x+1]) return false;
			if (stage.map[pos_y][pos_x+1] != null) { 
				this.orientation = direction;
				return collision(stage.map[pos_y][pos_x+1]);
			}
			stage.map[pos_y][pos_x] = null;
			pos_x += 1;
		}

		stage.map[pos_y][pos_x] = this;
	
		moving = true;
		moveSmooth = true;
		percentComplete = 0;
		this.currentSpeed = speed * (float)(1-stage.slow[pos_y][pos_x]);

		if (prev_y != pos_y && prev_x != pos_x) System.out.println("error!");

		this.orientation = direction;
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

		// change this later
		that.attacking = this;
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
		stage.map[pos_y][pos_x] = null;
		this.pos_x = -100;
		this.pos_y = -100;
		if (this.team == 0) stage.allies.removeValue(this, true);
		if (this.team == 1) stage.enemies.removeValue(this, true);

		//		System.out.println("DESTROYED");
		party.casualty(soldier);
	}

	// call this when a soldier retreats
	public void leaveBattle() {
		stage.map[pos_y][pos_x] = null;
		this.pos_x = -100;
		this.pos_y = -100;
		if (this.team == 0) stage.allies.removeValue(this, true);
		if (this.team == 1) stage.enemies.removeValue(this, true);

		stage.removeUnit(this);
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

	public boolean isRanged() {
		return this.rangedWeapon != null || this.quiver <= 0;
	}

	public float getCenterX() {
		return getX() + getOriginX();
	}
	public float getCenterY() {
		return getY() + getOriginY();
	}
}
