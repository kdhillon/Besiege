package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.MapScreen;
import kyle.game.besiege.location.Location;

public class Fire extends Actor {

	private TextureRegion region;
	
	Color o;
	MapScreen mapScreen;
	
	Array<Particle> particles;
	
//	private static float MS = 0; // ms per frame

	boolean battle;
	private float size = 400;
	
	private static float FLICKER_RATE_X = .03f;
	private static float MAX_FLICKER = .6f;
	// var PARTICLE_COUNT = 100*FLAME_WIDTH*FLAME_HEIGHT;
	private static float ACCEL_Y = -.3f;
	
	private float _width, _height; // to store init width/height

	// colors
	private static int INIT_G = 1;
	private static float FINAL_G = .2f;
	private static int counter = 0;
	    
	float FLAME_WIDTH;
	float FLAME_HEIGHT;
		
	float INIT_VX;
	float INIT_VY;

	float INIT_LIFETIME;
	float MAX_LIFETIME;
	float LIFETIME_RANDOMNESS;

	float current_flicker;
	
	Location loc;

	public Fire(float _width, float _height, MapScreen mapScreen, Location loc) {
		this.mapScreen = mapScreen;
		this.region = new TextureRegion(new Texture("whitepixel.png"));
		
		this._width = _width;
		this._height = _height;
		
		if (loc == null) battle = true;
		else this.loc = loc;
		
		updateZoom(mapScreen.getZoom());
		// need to be able to update this when zoom changes

//		this.INIT_LIFETIME = .65f*2;
//		this.MAX_LIFETIME = .9f*2;
//		this.LIFETIME_RANDOMNESS = .05f*2;

		this.current_flicker = 0;
		
		this.particles = new Array<Particle>();
	}
	
	public void updateZoom(float zoom) {
		float scale = zoom;
		if (loc != null) {
			scale = loc.getSizeFactor() * zoom;
			scale = loc.adjustScale(scale);
//			System.out.println("scale: " + scale + " min_ZOOm: " + loc.MIN_ZOOM);
			if (scale < loc.MIN_ZOOM) scale = loc.MIN_ZOOM;
			if (scale > loc.MAX_ZOOM) scale = loc.MAX_ZOOM;
		}
		
		FLAME_WIDTH = _width * 2f * scale / 10;
		FLAME_HEIGHT = _height * 2f * scale / 10;
		
		this.INIT_VX = .06f*FLAME_WIDTH;
		this.INIT_VY = .08f*FLAME_HEIGHT;
	}

	private class Particle {
		float _x;
		float _y;
		float vx;
		float vy;
		float lifetime;
		float init_lifetime;
		Color color;
		boolean isSmoke;
		
		TextureRegion todraw;
		
		private Particle(Fire fire) {
			counter++;
//			System.out.println("generating particle");
			// newdiv.innerHTML = "B";
			
			// TODO initialize texture
			
//			newdiv.innerHTML = "&#149;";
//			newdiv.style.position = "absolute";
//			console.log(fire.current_flicker);
//			newdiv.style.color = "#" + decimalToHexString(255) + decimalToHexString(255) + decimalToHexString(0);
//			fire.div.appendChild(newdiv);
//			// properties
//			this.id = newdiv.id;
			
			this._x = 0;
			this._y = 0;
//
//			float direction = 1;
//			if (Math.random() < .5) direction = -1f; 
			
			this.vx = (float) (Math.random() - .5)  *  INIT_VX * 5;// * INIT_VX * direction;// + fire.current_flicker);
//			System.out.println("vx: " + vx);
			//			System.out.println(vx);
			this.vy = (float) (fire.INIT_VY*Math.random() - fire.INIT_VY);
			
//			this.vx = 1;
//			this.vy = 1;

//			System.out.println(vy);
//			this.vx = 
			
//			this.lifetime = fire.INIT_LIFETIME + Math.min(fire.LIFETIME_RANDOMNESS/(Math.abs(this.vx) - fire.current_flicker), fire.MAX_LIFETIME);
//			this.lifetime = .9f+(float) (Math.random()*.1);
			this.lifetime = .7f;
			this.init_lifetime = lifetime * 1.5f;

			this.color = new Color();
			this.color.r = 1; // between 0 and 255
			this.color.g = 0;
			this.color.b = 0;
			this.color.a = 1;

			this.isSmoke = false;
		}
	}

//	private String decimalToHexString(String number)
//	{
//		int num;
//		num = Integer.parseInt(number);
//		if (num < 0) num = 0xFFFFFFFF + num + 1;
//		var string = num.toString(16).toUpperCase();
//		if (string.length == 1) string = "0" + string;
//		return string;
//	}

	// don't limit particle count
	public void generateParticles() {
		// if (particles.length < PARTICLE_COUNT) 
		if (Math.random() < 1)
			particles.add(new Particle(this));
	}

	private void removeParticle(Particle p) {
		if (Math.random() < .1 && !p.isSmoke) {
			p.isSmoke = true;
			p.lifetime = (float) (Math.random()*10);
			p.init_lifetime = p.lifetime;
			p.vy *= 1;

			// element.innerHTML = "~";
//			p._y -= FLAME_HEIGHT / 10;
			int gray = 1;
			p.color.r = gray;
			p.color.g = gray;
			p.color.b = gray;
		}
		else {
			this.particles.removeValue(p, true);
		}
	}

	private void  calculateNewPosition(Particle p, float MS) {
		if (!p.isSmoke)
			p.vy += ACCEL_Y* FLAME_HEIGHT *MS;
		else p._x += MapScreen.wind * FLAME_WIDTH / 100 * 10f * MS;
		if (p.vy < -FLAME_HEIGHT/10 && !p.isSmoke) {
			if (p._x > 0) p.vx -= 3 * MS;
			if (p._x < 0) p.vx += 3 * MS;
			
			if (p.vx > 0) p.vx -= FLAME_WIDTH/2 * MS;
			if (p.vx < 0) p.vx += FLAME_WIDTH/2 * MS;
//			p.vx += wind_x / 10;
		} 

//			System.out.println("vx: " + p.vx);
		p._x += p.vx*MS;
		p._y += p.vy*MS;
		
		p._x += MapScreen.wind*5f*MS * FLAME_WIDTH / 100;
	}

	public void updateColor(Particle p) {
//		System.out.println("p lifetime: " +  p.lifetime);
		if (!p.isSmoke)
			p.color.g = (float) (((p.lifetime / p.init_lifetime) * INIT_G) + FINAL_G);
		else {
			float gray = (float) ((p.lifetime / p.init_lifetime) * 1) + 0;

			p.color.g = gray;
			p.color.r = gray;
			p.color.b = gray;
		}
		// p.g = parseInt((p._y / (INIT_TOP+50)) * 255);
	}

	private void updateLifetimes(Particle p, float MS) {
		p.lifetime -= MS;
		
		if (p.lifetime < 0) removeParticle(p);
	}

	public void updateFlicker() {
//		float scale = 1;
//		if (Math.random() < .02) scale = 2; 
//		current_flicker += (Math.random()*FLICKER_RATE_X*scale)-FLICKER_RATE_X*scale/2;
//		if (current_flicker > MAX_FLICKER) current_flicker = MAX_FLICKER;
//		if (current_flicker < -MAX_FLICKER) current_flicker = -MAX_FLICKER;
	}

	@Override
	public void act(float delta) {

		if (!battle) updateZoom(mapScreen.getZoom());
		delta = 0.02f; //.05 is a little fast, .08 is faster, .03 is good for now
//		System.out.println("candle acting");
		generateParticles();
		updateFlicker();
		updatePositions(delta);
	}
	
	public void updatePositions(float delta) {
//		System.out.println("delta: " + delta);
//		delta *= 10;
//		delta = 10;
		for (Particle p : particles) {
			calculateNewPosition(p, delta);
			updateColor(p);
//			doMove(p);
			updateLifetimes(p, delta);
		}
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
//		System.out.println("drawing fire: " + this.getX() + this.getY());
		
//		wind_x += 2*fires[0].current_flicker;
		
//		System.out.println("candle drawing");
//		System.out.println("drawing");
		for (Particle p : particles) {
//			System.out.println("drawing");
			o = batch.getColor();
			batch.setColor(p.color);
			
			batch.draw(region, this.getX() + p._x - FLAME_HEIGHT/50, this.getY() + -p._y - FLAME_HEIGHT/50, FLAME_HEIGHT/25, FLAME_HEIGHT/25);
			batch.setColor(o);
		}
	}
}
