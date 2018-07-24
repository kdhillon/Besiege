package kyle.game.besiege.title;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class Candle extends Actor {
	private static TextureRegion circle = new TextureRegion(new Texture("whitecircle.png"));

	private TextureRegion region;
	Array<Particle> particles;
	
	private static float RATE = 40;
	private static float MS = 75; // ms per frame

	private static float FLICKER_RATE_X = .03f;
	private static float MAX_FLICKER = .6f;
	// var PARTICLE_COUNT = 100*FLAME_WIDTH*FLAME_HEIGHT;
	private static float ACCEL_Y = -.005f;
	private static final float SMOKE_LIFETIME = 30;
	
	private static final float INIT_SMOKE_SPEED = -0.75f;

	// colors
	private static int INIT_G = 1;
	private static float FINAL_G = .2f;
	private static int counter = 0;
	public static float wind_x = 0;
	
	// circle
	float circleWidth = 20;
	float circleHeight = 10;
	float flickerMin = 0.1f;
	float flickerMax = 0.3f;
	float flickerChangeProb = 0.075f;
	float flicker = 0.5f;
	
	int _x;
	int _y;

	float INIT_VX;
	float INIT_VY;

	float INIT_LIFETIME;
	float MAX_LIFETIME;
	float LIFETIME_RANDOMNESS;

	float current_flicker;
	float scale;

	public Candle(int _x, int _y, float _width, float _height, float scale) {
		this.region = new TextureRegion(new Texture("whitepixel.png"));
		this._x = _x;
		this._y = _y;

		float FLAME_WIDTH = _width;
		float FLAME_HEIGHT = _height;
		this.scale = scale;
		
		this.INIT_VX = .11f*FLAME_WIDTH * scale;
		this.INIT_VY = .3f*FLAME_HEIGHT * scale;

		this.INIT_LIFETIME = .65f*FLAME_HEIGHT*FLAME_WIDTH;
		this.MAX_LIFETIME = .9f*FLAME_HEIGHT;
		this.LIFETIME_RANDOMNESS = .05f*FLAME_WIDTH*FLAME_HEIGHT;

		this.current_flicker = 0;
		
		this.particles = new Array<Particle>();
	}
	
	public void move(int x, int y) {
		this._x = x;
		this._y = y;
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
		
		private Particle(Candle candle) {
			counter++;
//			System.out.println("generating particle");
			// newdiv.innerHTML = "B";
			
			// TODO initialize texture
			
//			newdiv.innerHTML = "&#149;";
//			newdiv.style.position = "absolute";
//			console.log(candle.current_flicker);
//			newdiv.style.color = "#" + decimalToHexString(255) + decimalToHexString(255) + decimalToHexString(0);
//			candle.div.appendChild(newdiv);
//			// properties
//			this.id = newdiv.id;
			
			this._x = candle._x;
			this._y = candle._y;
//
//			float direction = 1;
//			if (Math.random() < .5) direction = -1f; 
			
			this.vx = (float) (Math.random() - .5)*candle.INIT_VX;// * INIT_VX * direction;// + candle.current_flicker);
			this.vx *= scale;
			//			System.out.println(vx);
			this.vy = (float) (candle.INIT_VY*1.1f*Math.random() - candle.INIT_VY);
//			this.vx = 1;
//			this.vy = 1;

//			System.out.println(vy);
//			this.vx = 
			
//			this.lifetime = candle.INIT_LIFETIME + Math.min(candle.LIFETIME_RANDOMNESS/(Math.abs(this.vx) - candle.current_flicker), candle.MAX_LIFETIME);
//			this.lifetime = .9f+(float) (Math.random()*.1);
			this.lifetime = 1;
			this.init_lifetime = candle.INIT_LIFETIME;

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
			p.lifetime = (float) (Math.random()*SMOKE_LIFETIME);
			p.init_lifetime = p.lifetime;
			
			p.vy = INIT_SMOKE_SPEED * (float) (Math.random() * 0.3 + 1);
			p.vx = 0;
			float windSpeedVariance = 0.2f;
			float windSpeedChange = (float) (windSpeedVariance * Math.random() - windSpeedVariance);
			
			p.vx = wind_x * scale + windSpeedChange;

			// element.innerHTML = "~";
			p._y -= 5 * scale;
			int gray = 1;
			p.color.r = gray;
			p.color.g = gray;
			p.color.b = gray;
		}
		else {
			this.particles.removeValue(p, true);
		}
	}

	private void  calculateNewPosition(Particle p) {
		if (!p.isSmoke)
			p.vy += ACCEL_Y*MS/20;
		else {
//			p.vx = wind_x * scale;
		}
		
		if (p.vy < .2 && !p.isSmoke) {
			if (p.vx > 0) p.vx -= .05 * scale;
			if (p.vx < 0) p.vx += .05 * scale;
		} 
		 
		p._x += p.vx*MS/RATE;
		p._y += p.vy*MS/RATE;
		
		if (!p.isSmoke) {
			p._x += (wind_x/2) * scale / RATE;
		}
	}

//	private void  doMove(p) {
//		var div = document.getElementById(p.id); 
//		div.style.left = p._x + 'px';
//		div.style.top = p._y + 'px';
//		div.style.color = "#" + decimalToHexString(p.r) + decimalToHexString(p.g) + decimalToHexString(p.b);
//	}

	public void updateColor(Particle p) {
		if (!p.isSmoke)
			p.color.g = (float) (((p.lifetime / p.init_lifetime) * INIT_G) + FINAL_G);
		else {
			float gray = (float) ((p.lifetime / p.init_lifetime) * 1) + 0;
			p.color.a = gray/2f + 0.5f;
			p.color.g = gray;
			p.color.r = gray;
			p.color.b = gray;
		}
		// p.g = parseInt((p._y / (INIT_TOP+50)) * 255);
	}

	private void updateLifetimes(Particle p) {
		p.lifetime -= MS/1000.0;
		

		if (p.lifetime < 0) removeParticle(p);
	}

	public void updatePositions() {
		for (Particle p : particles) {
			calculateNewPosition(p);
			updateColor(p);
//			doMove(p);
			updateLifetimes(p);
		}
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		// draw the light circle
		if (Math.random() < flickerChangeProb) {
			flicker = (float) Math.random() * (flickerMax - flickerMin) + flickerMin;
		}
		batch.setColor(0, 0, 0f, 1f);
		// Draw fixed black circle
		batch.draw(circle, _x - circleWidth/3f * scale - 3, 14 + -_y - circleHeight/3f * scale, circleWidth * scale / 1.5f, circleHeight * scale / 1.5f );
		batch.setColor(1, 0.95f, 0.9f, 0.2f);
		batch.setColor(1, 0.95f, 0.9f, flicker/4 + 0.1f);
		float firstCircleScale = 2;
//		float flickerSizeEffect = (flicker + 0.5f);
		float flickerSizeEffect = 0.75f;
		batch.draw(circle, _x - circleWidth/2f * scale * firstCircleScale * flickerSizeEffect - 1*firstCircleScale, 13 * firstCircleScale / 2 + -_y - circleHeight/2f * flickerSizeEffect * scale * firstCircleScale, circleWidth * scale * firstCircleScale *flickerSizeEffect, circleHeight *firstCircleScale * scale * flickerSizeEffect);
//		batch.draw(circle, _x - circleWidth/2f * scale * (flicker + 0.5f) - 3, 17 + -_y - circleHeight/2f * (flicker + 0.5f) * scale, circleWidth * scale * (flicker + 0.5f), circleHeight * scale * (flicker + 0.5f));
		float secondCircleScale = 4;
		batch.draw(circle, _x - circleWidth/2f * scale * secondCircleScale * flickerSizeEffect - 1*secondCircleScale, 17 * secondCircleScale / 4 + -_y - circleHeight/2f * flickerSizeEffect * scale * secondCircleScale, circleWidth * scale * secondCircleScale * flickerSizeEffect, circleHeight *secondCircleScale * scale * flickerSizeEffect);

//		System.out.println("drawing");
		for (Particle p : particles) {
//			System.out.println("drawing");
//			Color c = new Color();
			batch.setColor(p.color);
		
			batch.draw(region, p._x - 4 * scale, -p._y + 4 * scale, 8 * scale * 0.75f, 8 * scale * 0.75f);
		}
	}
}
