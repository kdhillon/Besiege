package kyle.game.besiege.battle;

import kyle.game.besiege.battle.Unit.Orientation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Arrow extends Actor {
	public BattleStage stage;	
	private TextureRegion texture;
	private TextureRegion halfArrow;
	private final float SCALE = 3;
	//	private final float THRESHOLD = .5f;
	//	private final float UNIT_HEIGHT = .1f;
	private final float GRAVITY = -1;

	private final float ACCURACY_FACTOR = .06f;
	private final float DISTANCE_EXP_FACTOR = .1f;
	private final float UNIT_COVER_DIST_CHANGE = .5f; // amount to multiply damage by to add to the near cover dist of a unit hit

	public static final float INITIAL_HEIGHT = .25f;
	private final float DAMAGE_FACTOR = 10f;
	private final float STUCK_Y = -4f;

	private boolean broken;
	private boolean stopped;
	private Unit stuck;
	private Unit firing;

	private float temp_offset_x;
	private float temp_offset_y;
	private float rotation_offset;

	public int damage;
	public float distanceToTravel;

	public float speed = 25f;
	//	public float speed = 10f;

	public Vector2 velocity;
	public float rotation;
	
	public Orientation orientation;

	public float pos_x;
	public float pos_y;

	public float time_since_shot;
	public float height; // implement this somehow... time
	public float vz; // initial vertical velocity

	public int pos_x_int;
	public int pos_y_int;

	public float dest_x;
	public float dest_y;

	// create new arrow with target
	public Arrow(Unit firing, Unit target) {
		texture = new TextureRegion(new Texture("arrow.png"));
		halfArrow = new TextureRegion(new Texture("half_arrow.png"));
		this.firing = firing;
		this.orientation = firing.orientation;
		this.stage = firing.stage;

		this.time_since_shot = 0;

		// spawn in middle of square
		this.pos_x = firing.pos_x + .5f;
		this.pos_y = firing.pos_y + .5f;

		setX(firing.getCenterX());
		setY(firing.getCenterY());

		this.setWidth(texture.getRegionWidth()*SCALE);
		this.setHeight(texture.getRegionHeight()*SCALE);

		this.setOriginX(this.getWidth()/2);
		this.setOriginY(this.getHeight()/2);


		// calculate damage
		this.damage = firing.rangedWeapon.atkMod;

		// modified to make arrows stronger
		this.damage *= DAMAGE_FACTOR;

		// calculate destination (based on accuracy) -- try to overshoot target
		dest_x = target.pos_x + .5f;
		dest_y = target.pos_y + .5f;

		//		// just add this to make it shoot a bit further?
		//		dest_x += velocity.x;
		//		dest_y += velocity.y;

		// add a bit of randomness based on accuracy
		//		if (Math.random()*10 < firing.rangedWeapon.accuracy) {
		//			dest_x += Math.random()*firing.distanceTo(target)/2;
		//			dest_y += Math.random()*firing.distanceTo(target)/2;
		//			System.out.println("misfire");
		//		}
		// different from dist to target
		distanceToTravel = (float) firing.distanceTo(target);
		float dist_to_closest = distanceToTravel;


		float random_x = (float) ((10-firing.rangedWeapon.accuracy) * (Math.random()-.5) * (10 + dist_to_closest)) * ACCURACY_FACTOR;
		float random_y = (float) ((10-firing.rangedWeapon.accuracy) * (Math.random()-.5) * (10 + dist_to_closest)) * ACCURACY_FACTOR;	
		this.dest_x += random_x;
		this.dest_y += random_y;

		// maybe add something to account for edge cases?

		//		System.out.println(random_x + " " + random_y);

		// calculate velocity vector
		velocity = new Vector2(dest_x - pos_x, dest_y - pos_y);
		float dist_to_target = velocity.len();

		velocity.nor();
		velocity.scl((float)(speed)); //(Math.random()-.5)*speed/2));


		// given dist to target and initial horizontal velocity (speed), calculate necessary vertical velocity?
		float time_to_collision = dist_to_target/velocity.len();

		// goal is to find vertical velocity needed to get to goal
		// object must hit ground after the amount of time (d = 0)

		// using d = vi*t + 1/2 * a*t^2

		float accel_distance = 1.0f/2.0f*GRAVITY*time_to_collision*time_to_collision;
		// aim for halfway up the unit's body
		vz = ((-INITIAL_HEIGHT+Unit.UNIT_HEIGHT_GROUND*.75f) - accel_distance)/time_to_collision;

		// add a bit so it goes a bit further
		vz += .03f;

		//vz = time_to_collision*GRAVITY/2;
		height = INITIAL_HEIGHT;

		rotation = velocity.angle()+270;
		this.setRotation(rotation);
	}

	@Override
	public void act(float delta) {
		if (this.stopped) return;
		time_since_shot += delta;
		// update height based on time
		vz += GRAVITY*delta;
		height += vz*delta;

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
			collided = stage.map[pos_y_int][pos_x_int];
			if (collided != null && collided.team != firing.team && this.height < collided.getZHeight())
				collision(stage.map[pos_y_int][pos_x_int]);
		}


		if (height < -.0f
				|| (inMap() && stage.battlemap.objects[pos_y_int][pos_x_int] != null && height < stage.battlemap.objects[pos_y_int][pos_x_int].height)) {
			this.stopped = true;
			if (Math.random() < .8) this.broken = true;
		}
	}


	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {	
		if (!this.inMap()) return;
		//if (!this.stopped) this.toFront();

		this.setX(stage.scale * pos_x * stage.unit_width);
		this.setY(stage.scale * pos_y * stage.unit_height);

		this.setScaleX((1+height)*stage.scale/2);
		this.setScaleY((1+height)*stage.scale);

		if (stopped) this.setScaleY((1+height)*stage.scale*.8f);


		//change scale based on how high arrow is! TODO
		//		System.out.println("height: " + this.height);

		if (this.stuck == null) {
			TextureRegion toDraw = texture;
			if (this.stopped && this.broken) toDraw = halfArrow;
			batch.draw(toDraw, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(),getScaleY(), getRotation());	
		}
		else if (!stuck.isDying || stuck.timeSinceDeath > .75f){
			if (!stuck.isDying) this.setScaleY(((1+height)*stage.scale)*1.2f);
			setX(stuck.getOriginX() + temp_offset_x);
			setY(stuck.getOriginY() + temp_offset_y);
			this.setRotation(-rotation_offset);

			batch.draw(halfArrow, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());	
		}
	}

	public void collision(Unit that) {
		// test killing horses
		if (that.shieldUp() && that.getOppositeOrientation() == this.orientation) {
			that.shield_hp -= damage;
			if (that.shield_hp <= 0) that.destroyShield();
			this.destroy();
		}
		else {
			that.hurt(Math.max(0, damage - that.def*Math.random()), firing);

			// get a bit of EXP for hitting someone based on distance
			firing.soldier.addExp((int) (distanceToTravel * DISTANCE_EXP_FACTOR));

			// get more exp for killing someone based on their level
			if (that.isDying) {
				firing.soldier.addExp(that.soldier.getExpForKill());
			}

			that.NEAR_COVER_DISTANCE += damage * UNIT_COVER_DIST_CHANGE;

			this.stopped = true;
			this.stuck = that;

			this.temp_offset_x = (float) Math.random() - .5f;
			this.temp_offset_y = (float) Math.random() - .5f;
			//		
			this.temp_offset_x *= 5;
			this.temp_offset_y *= 5;

			this.temp_offset_y += STUCK_Y * stage.scale;

			this.rotation_offset = that.getRotation() - this.getRotation();
			this.remove();
			that.addActor(this);
		}
	}

	public void destroy() {
		stage.removeActor(this);
	}

	private boolean inMap() {
		return pos_x_int < stage.size_x &&
				pos_y_int < stage.size_y && 
				pos_x_int >= 0 && 
				pos_y_int >= 0;
	}
}
