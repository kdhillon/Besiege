package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Random;
import kyle.game.besiege.battle.Unit.Orientation;
import kyle.game.besiege.party.AmmoType;
import kyle.game.besiege.party.RangedWeaponType.Type;

public class Projectile extends Group {
	public BattleStage stage;	
	private TextureRegion texture;
	private TextureRegion halfTexture;
	//	private final float THRESHOLD = .5f;
	//	private final float UNIT_HEIGHT = .1f;
	private final float GRAVITY = -1;
	private final float CROSSBOW_BONUS = 2; // bonus factor against shields
	private final float SHIELD_BLOCK_PROB = 0.8f; // bonus factor against shields
    private final double STICK_IN_ENEMY_PROB = 0.5;

	private final static boolean FRIENDLY_FIRE = true; // if true, arrows can hurt own team. note that siegeOrRaid hurts own team by default
	
	private float SCALE = 3;
	private float SCALE_SIEGE = 3f;
	private float SIEGE_STOPPED_SCALE = .7f;
	private final float ARROW_SCALE_X = .3f;
	
	private final float SPIN = 15;
	
	private final float MAX_DRAW_HEIGHT = 3;
	private static final float BASE_SCALE = 0.5f;
	private final float HEIGHT_SCALE_FACTOR = .5f;

	private final float ACCURACY_FACTOR = .06f;
	private final float DISTANCE_EXP_FACTOR = .1f;
	private final float UNIT_COVER_DIST_CHANGE = .5f; // amount to multiply damage by to add to the near cover dist of a unit hit

	public static final float INITIAL_HEIGHT = .075f;
	private static final float SIEGE_INITIAL_HEIGHT = .25f;
	@SuppressWarnings("unused")
	private static final float SIEGE_TARGET_HEIGHT = .8f;
	private static final float SPEED_SCALE_SIEGE = .4f; // how much does speed decrease when unit is hit.
	
	private static int MAX_BOUNCES = 5;
	private final float DAMAGE_FACTOR = 3f;
	private final float STUCK_Y = -6f;

	float BOUNCE_DEPRECIATION = .4f; // can randomize for more fun

	private boolean broken;
	private boolean stopped;
	private WeaponDraw stuckShield;
	private Unit stuck;
	private Unit firing;
	private SiegeUnit siegeFiring;

	private AmmoType ammoType;

	private float temp_offset_x;
	private float temp_offset_y;
	private float rotation_offset;

	public int bounceCount;
	public float damage;
	public float distanceToTravel;

//	public float SPEED = 10f;
	public float SPEED = 30f;
    public float THROWN_SPEED = 22f;
    public float FIREARM_SPEED = 100f;
//			public float SPEED = 200f; // basically gunfire

	public Vector2 velocity;
	public float rotation;

	public Orientation orientation;

	public float pos_x;
	public float pos_y;

	public float time_since_shot;
	public float height; // implement this somehow... time
	public float vz; // initial vertical velocity
	
	public float spin;

	public int pos_x_int;
	public int pos_y_int;

	public float dest_x;
	public float dest_y;
	
	public FireContainer fc;

	// create new arrow with target
	public Projectile(Unit firing, Unit target) {
		this.ammoType = firing.ammoType;
		if (ammoType == null) throw new AssertionError();
		this.firing = firing;

        texture = ammoType.getRegion();
        if (texture == null) throw new AssertionError();
        halfTexture = ammoType.getBrokenRegion();
        if (halfTexture == null) throw new AssertionError();

		initializePosition();

		// calculate damage
		this.damage = firing.getRangeDmg();
		this.damage *= DAMAGE_FACTOR;

		// calculate destination (based on accuracy) 
		dest_x = target.pos_x + .5f;
		dest_y = target.pos_y + .5f;

		distanceToTravel = (float) firing.distanceTo(target);

//		SPEED += (float) (Math.random()*SPEED/5f);
		
		if (firing.rangedWeapon.type == Type.FIREARM) {
			SPEED = FIREARM_SPEED;
			texture = Assets.map.findRegion("arrow");
		} else if (firing.rangedWeapon.type == Type.THROWN_AXE || firing.rangedWeapon.type == Type.THROWN) {
		    SPEED = THROWN_SPEED;
        }
		
		float time_to_collision = distanceToTravel/SPEED;

		boolean shouldLead = true;
		// lead the enemy unit a bit based on distance?
		if (target.moveSmooth && shouldLead) {
			if (firing.team == 0)
				System.out.println("Leading enemy with shot: " + target.soldier.getName());
			float leadBy = target.getSpeed() * time_to_collision;

			//	System.out.println(leadBy);
			if (target.orientation == Orientation.DOWN)
				dest_y -= leadBy;
			if (target.orientation == Orientation.UP)
				dest_y += leadBy;
			if (target.orientation == Orientation.RIGHT)
				dest_x += leadBy;
			if (target.orientation == Orientation.LEFT)
				dest_x -= leadBy;
		}
		
		int accuracy_factor = firing.rangedWeapon.accuracy;
		float initialHeight = firing.getFloorHeight() + INITIAL_HEIGHT;
		float targetHeight = target.getZHeight() - Unit.UNIT_HEIGHT_GROUND*.1f;
		//		if (stage.isRaining) accuracy_factor -= 1;

		accuracy_factor = 10 - accuracy_factor;
		
		initializeMovement(accuracy_factor, time_to_collision, initialHeight, targetHeight);

		// TODO with Ammo
		if (ammoType.isOnFire()) {
//			System.out.println("creating fire");
			fc = new FireContainer();
            Fire fire = new Fire(400, 500, firing.stage.getMapScreen(), null);
//			System.out.println("adding fire: " + this.getWidth()/2 + " " + this.getHeight());
			fc.setPosition(this.getWidth()/2, this.getHeight() * 1.5f);
			fc.addFire(fire);
			fc.setRotation(180);
			fc.toFront();
			this.addActor(fc);
		}
        this.spin = SPIN;
		if (!ammoType.shouldSpin()) {
		    spin = 0;
        }
    }

	// create new siegeOrRaid projectile with target
	public Projectile(SiegeUnit siegeFiring, BPoint target) {
		texture = Assets.map.findRegion("rock");
		halfTexture = Assets.map.findRegion("half rock");
		
		SCALE = SCALE_SIEGE;
		
		this.SPEED = siegeFiring.type.projectileSpeed;
		
		this.siegeFiring = siegeFiring;

		initializePosition();

		// calculate damage
		this.damage = siegeFiring.type.damage;
		this.damage *= DAMAGE_FACTOR;

		// calculate destination (based on accuracy) 
		dest_x = target.pos_x + .5f;
		dest_y = target.pos_y + .5f;

		distanceToTravel = (float) siegeFiring.distanceTo(target);
		float time_to_collision = distanceToTravel/SPEED;

		int accuracy_factor = siegeFiring.type.accuracy;
		//		if (stage.isRaining) accuracy_factor -= 1;

		accuracy_factor = 10 - accuracy_factor;

		float initialHeight = SIEGE_INITIAL_HEIGHT;
		
		float targetHeight = stage.heights[(int) dest_y][(int) dest_x];
//		float targetHeight = SIEGE_TARGET_HEIGHT;

		initializeMovement(accuracy_factor, time_to_collision, initialHeight, targetHeight);
		
		this.bounceCount = (int) (Math.random() * MAX_BOUNCES);
		this.spin = SPIN;
		if (!ammoType.shouldSpin()) {
		    spin = 0;
        }
	}

	private void initializePosition() {
		boolean siege = this.siegeFiring != null;
		if (!siege) {
			this.orientation = firing.orientation;
			this.stage = firing.stage;

			// spawn in middle of square
			this.pos_x = firing.pos_x + .5f;
			this.pos_y = firing.pos_y + .5f;

			setX(firing.getCenterX());
			setY(firing.getCenterY());
		}
		else {
			this.orientation = siegeFiring.orientation;
			this.stage = siegeFiring.stage;

			this.pos_x = siegeFiring.pos_x + siegeFiring.size_x/2;
			this.pos_y = siegeFiring.pos_y + siegeFiring.size_y/2;

			setX(siegeFiring.getCenterX());
			setY(siegeFiring.getCenterY());
		}		
		
		this.time_since_shot = 0;

		this.setWidth(texture.getRegionWidth()*SCALE);
		this.setHeight(texture.getRegionHeight()*SCALE);

		this.setOriginX(this.getWidth()/2);
		this.setOriginY(this.getHeight()/2);
	}

	private void initializeMovement(float accuracy_factor, float time_to_collision, float initialHeight, float targetHeight) {
		// add a bit of randomness based on accuracy
		float random_x = (float) ((accuracy_factor) * (Math.random()-.5) * (10 + distanceToTravel)) * ACCURACY_FACTOR;
		float random_y = (float) ((accuracy_factor) * (Math.random()-.5) * (10 + distanceToTravel)) * ACCURACY_FACTOR;	
		this.dest_x += random_x;
		this.dest_y += random_y;

		// calculate velocity vector
		velocity = new Vector2(dest_x - pos_x, dest_y - pos_y);
		float dist_to_closest = velocity.len();
		time_to_collision = dist_to_closest/SPEED;

		velocity.nor();
		velocity.scl(SPEED); //(Math.random()-.5)*speed/2));

		// goal is to find vertical velocity needed to get to goal
		// object must be at target height after the amount of time (d = 0)

		// using d = vi*t + 1/2 * a*t^2
		float accel_distance = 1.0f/2.0f*GRAVITY*time_to_collision*time_to_collision;
		// aim for halfway up the unit's body
		vz = ((-initialHeight+(targetHeight)) - accel_distance)/time_to_collision;

		// add a bit so it goes a bit further
		//vz += .02f; // .02

		//vz = time_to_collision*GRAVITY/2;
		height = initialHeight;

		rotation = velocity.angle()+270;
		this.setRotation(rotation);
	}

	@Override
	public void act(float delta) {
		if (this.stopped) return;
		super.act(delta);

		time_since_shot += delta;
		// update height based on time
		vz += GRAVITY*delta;

		//		System.out.println(vz);
		height += vz*delta;
		//		System.out.println(height);

		// move towards target;

		pos_x += velocity.x*delta;
		pos_y += velocity.y*delta;

		//		setX(getX() + velocity.x);
		//		setY(getY() + velocity.y);

		// update containing position
		pos_x_int = (int) (pos_x);
		pos_y_int = (int) (pos_y);

		// check for collision
		Unit collided = null; 
		if (inMap()) {
			collided = stage.units[pos_y_int][pos_x_int];
			if (collided != null && this.height < collided.getZHeight() && this.height > collided.getZHeight() - Unit.UNIT_HEIGHT_GROUND) {
				if (!goingUp())
					collision(stage.units[pos_y_int][pos_x_int]);
			}
		}

		if (inMap() && (wallHeightCheck()) &&
				(height < stage.heights[pos_y_int][pos_x_int]) && stage.battlemap.objects[pos_y_int][pos_x_int] == null) {
			
			if (this.isSmallProjectile() || this.bounceCount <= 0 || this.stage.battlemap.objects[pos_y_int][pos_x_int] != null) {
				this.stopped = true;
				
				if (Math.random() < .2) this.broken = true;
			}
			else {
				if (Math.random() < .2) this.broken = true;

				//bounce
				this.damage *= BOUNCE_DEPRECIATION; // scale damage with speed
				this.bounceCount--;
				this.height = stage.heights[pos_y_int][pos_x_int] + .001f;
				this.vz = vz*-BOUNCE_DEPRECIATION;
				this.velocity.scl(BOUNCE_DEPRECIATION);

				if (ammoType.shouldSpin())
    				this.spin *= BOUNCE_DEPRECIATION;
			}
		}
		// collide with object
		else if (inMap() && stage.battlemap.objects[pos_y_int][pos_x_int] != null && !goingUp() && 
				height < stage.battlemap.objects[pos_y_int][pos_x_int].height + stage.heights[pos_y_int][pos_x_int]) {
			
			BattleMap.Object object = stage.battlemap.objects[pos_y_int][pos_x_int];
			if (!this.isSmallProjectile()) {
				if (object == BattleMap.Object.CASTLE_WALL || object == BattleMap.Object.CASTLE_WALL_FLOOR) {
					stage.damageWallAt(pos_x_int, pos_y_int, damage);
				}
			}
			this.stopped = true;
			
			// move forward a bit if stuck in an object
			if (this.isSmallProjectile()) {
				this.pos_x += velocity.x*.018f;
				this.pos_y += velocity.y*.019f;

//                this.pos_x += 0.5;
//                this.pos_y += 0.5;
			}
			else { 
				if (Math.random() < .3) this.destroy();
				this.broken = true;
				this.SCALE = SCALE_SIEGE / 2f;
			}
			if (Math.random() < .2) this.broken = true;

			// Tree collision, maybe generate fire
			if (object == BattleMap.Object.PALM || object == BattleMap.Object.TREE) {
                if (firing.ammoType != null && firing.ammoType.isOnFire()) {
                    if (Math.random() < 0.2)
                        stage.battlemap.createFireAt(pos_x_int, pos_y_int);
                }
            }
		}
	}
	
	public boolean goingUp() {
		return vz > 0;
	}
	
	// if standing above 0, don't intersect with anything at that height... dicey but works, kind of OP can fix later TODO
	private boolean wallHeightCheck() {
		if (firing == null) return true;
		return stage.heights[pos_y_int][pos_x_int] != firing.getFloorHeight() || firing.getFloorHeight() == 0f;
	}

	// Get default small
	public static float getDefaultSmallScale() {
        return (BASE_SCALE);
    }

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		if (!this.inMap()) return;
		if (!this.stopped && this.stuck == null) this.toFront();
		if (!this.stopped) super.draw(batch, parentAlpha);

		this.setX(pos_x * stage.unit_width);
		this.setY(pos_y * stage.unit_height);

		float stoppedScale = 0.5f;
		if (stopped || stuck != null) stoppedScale = .5f;

		float drawHeight = Math.min(height, MAX_DRAW_HEIGHT);

		if (height < 0) drawHeight = 0;
		
		float scaleX = (BASE_SCALE+drawHeight*HEIGHT_SCALE_FACTOR);
		float scaleY = (BASE_SCALE+drawHeight*HEIGHT_SCALE_FACTOR);
		
		if (this.isSmallProjectile()) {
//			scaleX *= ARROW_SCALE_X;
//			scaleY *= stoppedScale;
		} else if (stopped) {
			scaleX *= SIEGE_STOPPED_SCALE;
			scaleY *= SIEGE_STOPPED_SCALE;
		}

		if (ammoType.shouldSpin() && !stopped) {
//		    System.out.println("Rotating: " + getKingdomRotation());
			setRotation(getRotation() + spin);
		}
	
		this.setScaleX(scaleX);
		this.setScaleY(scaleY);

        // Regular draw
		if (this.stuck == null) {
			TextureRegion toDraw = texture;
			if ((this.stopped && this.isSmallProjectile()) || this.broken) toDraw = halfTexture;
			if (toDraw != null)
				batch.draw(toDraw, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(),getScaleY(), getRotation());	
		}
		else if (stuckShield != null && !stuck.isDying) {
//			setScaleY(getScaleY()*2f);
            setX(stuck.getOriginX() + temp_offset_x + stuckShield.shieldOffset);
            setY(stuck.getOriginY() + temp_offset_y + stuckShield.shieldOffset);
            if (ammoType.shouldSpin()) {
                if (this.getRotation() == -rotation_offset)
                    this.setRotation(randomStuckAngle());
            }
            else
			    this.setRotation(-rotation_offset);

			if (texture != null)
				batch.draw(halfTexture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
		}
		else if (!stuck.isDying || stuck.timeSinceDeath > .75f){
//						setScaleY(getScaleY()*2f);
			setX(stuck.getOriginX() + temp_offset_x);
			setY(stuck.getOriginY() + temp_offset_y);

			if (ammoType.shouldSpin()) {
			    // Just use some metric to see whether we've already set this or not.
			    if (this.getRotation() == -rotation_offset)
                    this.setRotation(randomStuckAngle());
            }
            else
			    this.setRotation(-rotation_offset);

			if (texture != null)
				batch.draw(halfTexture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
		}
		
//		if (fc != null && !this.stopped) this.fc.draw(batch, parentAlpha);
	}

	// Returns a reasonable angle for a stuck projectile
	private float randomStuckAngle() {
	    System.out.println("Using random stuck angle");
	    return (float) Random.getRandomInRange(90, 270);
    }

	public void collision(Unit that) {
		if (that == firing) return;
		
		if (this.isSmallProjectile()) {
			if (!FRIENDLY_FIRE && that.team == firing.team) {
//				this.destroy();
				return;
			}
			if (that.team == firing.team && this.vz > 0) return; // don't collide with friends on the way up
		}
		
		if (this.isSmallProjectile()) {
		// test killing horses
			if (that.shieldUp() && that.getOppositeOrientation() == this.orientation && Math.random() < SHIELD_BLOCK_PROB) {
				if (this.firing.rangedWeapon.type == Type.CROSSBOW) {
					damage *= CROSSBOW_BONUS;
				}
				that.shield_hp -= damage;
				if (that.shield_hp <= 0) that.destroyShield();
				else {
					this.stopped = true;
					this.stuckShield = that.weaponDraw;
					this.stuck = that;
					//				this.temp_offset_x = (float) Math.random() - .5f;h
					//				this.temp_offset_y = (float) Math.random() - .5f;
//					this.setPosition(100, 0);
//					this.setKingdomRotation(0);
					//				//		
					//				this.temp_offset_x *= 5;
//					this.temp_offset_y *= 5;

					this.temp_offset_y += STUCK_Y / 2;
					this.temp_offset_x += STUCK_Y * 1.5f;

					this.rotation_offset = that.getRotation() - this.getRotation();
					that.weaponDraw.addActor(this);
					this.toBack();

				}
//				this.destroy();
			}
			else {
				that.hurt(Math.max(0, damage - that.def*Math.random()), firing);

				// get a bit of EXP for hitting someone based on distance
				firing.soldier.addExp((int) (distanceToTravel * DISTANCE_EXP_FACTOR));

				// EXP is taken care of by that.hurt.
				
				// more exp for killing someone based on their level
//				if (that.isDying) {
//					firing.soldier.registerKillOrWoundEnemy(that.soldier, true);
//				}

//				that.NEAR_COVER_DISTANCE += damage * UNIT_COVER_DIST_CHANGE;

				this.stopped = true;

//				this.temp_offset_x = (float) Math.random() - .5f;
//				this.temp_offset_y = (float) Math.random() - .5f;
//				//
//				this.temp_offset_x *= 5;
//				this.temp_offset_y *= 10;

                this.temp_offset_x = -3f + (float) Random.getRandomInRange(-3, 3);
                this.temp_offset_y = -7f + (float) Random.getRandomInRange(-1, 1);

				this.rotation_offset = that.getRotation() - this.getRotation();
				this.setRotation(-rotation_offset);
				this.remove();

                if (Math.random() < STICK_IN_ENEMY_PROB) {
                    this.stuck = that;
                    that.addActor(this);
                    this.toBack();
                }
			}
		}
		else {
			// slow down but keep going
			this.velocity.scl(SPEED_SCALE_SIEGE);
			that.hurt(Math.max(0, damage - that.def*Math.random()), null);
		}
	}

	public void destroy() {
		stage.removeActor(this);
	}

	public boolean isSmallProjectile() {
		return this.firing != null;
	}

	private boolean inMap() {
		return pos_x_int < stage.size_x &&
				pos_y_int < stage.size_y && 
				pos_x_int >= 0 && 
				pos_y_int >= 0;
	}
}
