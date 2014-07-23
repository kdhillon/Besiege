package kyle.game.besiege.battle;

import kyle.game.besiege.battle.Unit.Orientation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Projectile extends Actor {
	public BattleStage stage;	
	private TextureRegion texture;
	private TextureRegion halfArrow;
	private final float SCALE = 3;
	//	private final float THRESHOLD = .5f;
	//	private final float UNIT_HEIGHT = .1f;
	private final float GRAVITY = -1;
	
	private final float SCALE_X = .3f;
	private final float MAX_DRAW_HEIGHT = 3;

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
//		public float speed = 1f; // this is hilarious

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
	public Projectile(Unit firing, Unit target) {
		texture = new TextureRegion(new Texture("objects/arrow.png"));
		halfArrow = new TextureRegion(new Texture("objects/half_arrow.png"));
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

		// calculate destination (based on accuracy) 
		dest_x = target.pos_x + .5f;
		dest_y = target.pos_y + .5f;


		distanceToTravel = (float) firing.distanceTo(target);
		float time_to_collision = distanceToTravel/speed;
		
		boolean shouldLead = true;
		// lead the enemy unit a bit based on distance?
		if (target.moveSmooth && shouldLead) {
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
		if (firing.onWall()) accuracy_factor += 1;
//		if (stage.isRaining) accuracy_factor -= 1;
		
		accuracy_factor = 10 - accuracy_factor;

		// add a bit of randomness based on accuracy
		float random_x = (float) ((accuracy_factor) * (Math.random()-.5) * (10 + distanceToTravel)) * ACCURACY_FACTOR;
		float random_y = (float) ((accuracy_factor) * (Math.random()-.5) * (10 + distanceToTravel)) * ACCURACY_FACTOR;	
		this.dest_x += random_x;
		this.dest_y += random_y;

		// calculate velocity vector
		velocity = new Vector2(dest_x - pos_x, dest_y - pos_y);
		float dist_to_closest = velocity.len();
		time_to_collision = dist_to_closest/speed;

		velocity.nor();
		velocity.scl(speed); //(Math.random()-.5)*speed/2));

		// goal is to find vertical velocity needed to get to goal
		// object must hit ground after the amount of time (d = 0)

		// using d = vi*t + 1/2 * a*t^2

		float initialHeight = INITIAL_HEIGHT + firing.getFloorHeight();

		float accel_distance = 1.0f/2.0f*GRAVITY*time_to_collision*time_to_collision;
		// aim for halfway up the unit's body
		vz = ((-initialHeight+(target.getZHeight()-Unit.UNIT_HEIGHT_GROUND*.1f)) - accel_distance)/time_to_collision;

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
			if (collided != null && collided.team != firing.team && this.height < collided.getZHeight() && this.height > collided.getZHeight() - Unit.UNIT_HEIGHT_GROUND)
				collision(stage.units[pos_y_int][pos_x_int]);
		}
		
		// if standing above 0, don't intersect with anything at that height... dicey but works, kind of OP can fix later TODO
		if (inMap() && (stage.heights[pos_y_int][pos_x_int] != firing.getFloorHeight() || firing.getFloorHeight() == 0f) && 
				(height < stage.heights[pos_y_int][pos_x_int]
				|| (stage.battlemap.objects[pos_y_int][pos_x_int] != null && 
				height < stage.battlemap.objects[pos_y_int][pos_x_int].height + stage.heights[pos_y_int][pos_x_int]))) {
			this.stopped = true;

			// move forward a bit if stuck in an object
			this.pos_x += velocity.x*.2f*delta;
			this.pos_y += velocity.y*.2f*delta;

			if (Math.random() < .8) this.broken = true;
		}
	}


	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {	
		if (!this.inMap()) return;
		//if (!this.stopped) this.toFront();

		this.setX(stage.scale * pos_x * stage.unit_width);
		this.setY(stage.scale * pos_y * stage.unit_height);

		float stoppedScale = .8f;
		if (stopped || stuck != null) stoppedScale = .4f;
		
		float drawHeight = Math.min(height, MAX_DRAW_HEIGHT);
	
		if (height < 0) drawHeight = 0;
		this.setScaleX((1+drawHeight*stage.scale) * SCALE_X);
		this.setScaleY(1+drawHeight*stage.scale * stoppedScale);

		
		//change scale based on how high arrow is! TODO
		//		System.out.println("height: " + this.height);

		if (this.stuck == null) {
			TextureRegion toDraw = texture;
			if (this.stopped && this.broken) toDraw = halfArrow;
			batch.draw(toDraw, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(),getScaleY(), getRotation());	
		}
		else if (!stuck.isDying || stuck.timeSinceDeath > .75f){
//			setScaleY(getScaleY()*1f);
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
