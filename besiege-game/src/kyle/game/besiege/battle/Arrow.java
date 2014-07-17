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
	private final float SCALE = 3;
	private final float THRESHOLD = .5f;
	private final float COLLISION_HEIGHT = .5f;
	private final float GRAVITY = 1;
	
	private final float ACCURACY_FACTOR = .06f;
	
	private boolean stopped;
	private Unit stuck;
	
	private float temp_offset_x;
	private float temp_offset_y;
	private float rotation_offset;
	
	public int damage;
	
	public float speed = 25f;
//	public float speed = 10f;
	
	public Vector2 velocity;
	public float rotation;
	public int team;

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

		// set up initial values
		this.stage = firing.stage;
		this.team = firing.team;
		
		this.time_since_shot = 0;
		
		this.pos_x = firing.pos_x;
		this.pos_y = firing.pos_y;
		
		setX(firing.getCenterX());
		setY(firing.getCenterY());
		
		this.setWidth(texture.getRegionWidth()*SCALE);
		this.setHeight(texture.getRegionHeight()*SCALE);

		this.setOriginX(this.getWidth()/2);
		this.setOriginY(this.getHeight()/2);
		
		
		// calculate damage
		this.damage = firing.atk;
		
		// modified to make arrows stronger
		this.damage *= 2;
		
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
		float dist_to_closest = (float) firing.distanceTo(target);
		
		
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
		vz = time_to_collision*GRAVITY/2;
								
		rotation = velocity.angle()+270;
		this.setRotation(rotation);
	}
	
	@Override
	public void act(float delta) {
		if (this.stopped) return;
		time_since_shot += delta;
		// update height based on time
		vz -= GRAVITY*delta;
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
		if (this.height < COLLISION_HEIGHT && inMap() && stage.map[pos_y_int][pos_x_int] != null && stage.map[pos_y_int][pos_x_int].team != this.team)
			collision(stage.map[pos_y_int][pos_x_int]);
		
//		if (Math.abs(pos_x-this.dest_x) < THRESHOLD && Math.abs(pos_y-this.dest_y) < THRESHOLD)
//			this.stopped = true;
		if (height < -.1 || (inMap() && stage.closed[pos_y_int][pos_x_int])) this.stopped = true;
	}
	
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {	
		if (!this.inMap()) return;
		//if (!this.stopped) this.toFront();
		
		this.setX(stage.scale * pos_x * stage.unit_width);
		this.setY(stage.scale * pos_y * stage.unit_height);
		
		this.setScaleX((1+height)*stage.scale/2);
		this.setScaleY((1+height)*stage.scale);
		
		//change scale based on how high arrow is! TODO
//		System.out.println("height: " + this.height);

		if (this.stuck == null)
			batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(),getScaleY(), getRotation());	

		
		else {
			// adjust based on orientation of unit. -- actually utilize scene2d
			setX(stuck.getOriginX() + temp_offset_x);
			setY(stuck.getOriginY() + temp_offset_y);
			this.setRotation(-rotation_offset);
			
			batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());	
		}
	}
	
	public void collision(Unit that) {
		that.hurt(Math.max(0, damage - that.def*Math.random()), null);
		
		// stick in target, don't destroy TODO
		this.stopped = true;
		this.stuck = that;
		
		this.temp_offset_x = (float) Math.random() - .5f;
		this.temp_offset_y = (float) Math.random() - .5f;

//		this.temp_offset_x = that.getX() - this.getX();
//		this.temp_offset_y = that.getY() - this.getY();
//		
//		if (that.orientation == Orientation.LEFT) {
//			this.temp_offset_x = that.getY() - this.getY();
//			this.temp_offset_y = -(that.getX() - this.getX());
//		} 
//		else if (that.orientation == Orientation.RIGHT){
//			this.temp_offset_x = -(that.getY() - this.getY());
//			this.temp_offset_y = that.getX() - this.getX();
//		} 
//		else if (that.orientation == Orientation.DOWN) {
//			this.temp_offset_x = -(that.getX() - this.getX());
//		}
//		
		this.temp_offset_x *= 3;
		this.temp_offset_y *= 3;
		
		this.rotation_offset = that.getRotation() - this.getRotation();
		this.remove();
		that.addActor(this);
	//	this.destroy();
	}
	
	public void destroy() {
		stage.removeActor(this);
	}
	
	private boolean inMap() {
		return pos_x_int < stage.size_x &&
				pos_y_int < stage.size_y && 
				pos_x_int > 0 && 
				pos_y_int > 0;
	}
}
