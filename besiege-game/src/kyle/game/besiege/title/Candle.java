package kyle.game.besiege.title;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class Candle extends Actor {

	private TextureRegion region;
	Array<Particle> particles;
	
	private static float MS = 100; // ms per frame

	private static float FLICKER_RATE_X = .03f;
	private static float MAX_FLICKER = .6f;
	// var PARTICLE_COUNT = 100*FLAME_WIDTH*FLAME_HEIGHT;
	private static float ACCEL_Y = -.005f;

	// colors
	private static int INIT_G = 1;
	private static float FINAL_G = .2f;
	private static int counter = 0;
	public static float wind_x = 0;

	int _x;
	int _y;

	float INIT_VX;
	float INIT_VY;

	float INIT_LIFETIME;
	float MAX_LIFETIME;
	float LIFETIME_RANDOMNESS;

	float current_flicker;

	public Candle(int _x, int _y, float _width, float _height) {
		
		this.region = new TextureRegion(new Texture("whitepixel.png"));
		this._x = _x;
		this._y = _y;

		float FLAME_WIDTH = _width;
		float FLAME_HEIGHT = _height;

		this.INIT_VX = .1f*FLAME_WIDTH;
		this.INIT_VY = .4f*FLAME_HEIGHT;

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
		int _x;
		int _y;
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
			
			this.vx = (float) (Math.random() - .5)*.7f + .15f;// * INIT_VX * direction;// + candle.current_flicker);

			//			System.out.println(vx);
			this.vy = (float) (candle.INIT_VY*Math.random() - candle.INIT_VY);
			
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
			p.lifetime = (float) (Math.random()*15);
			p.init_lifetime = p.lifetime;
			p.vy *= 1;

			// element.innerHTML = "~";
			p._y -= 10;
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
		else p.vx = wind_x;
		if (p.vy < .2 && !p.isSmoke) {
			if (p.vx > 0) p.vx -= .05;
			if (p.vx < 0) p.vx += .05;
//			p.vx += wind_x / 10;
		} 

//			System.out.println(p.vx);
		p._x += p.vx*MS/20;
		p._y += p.vy*MS/20;
		
		p._x += wind_x/2;
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

	public void updateFlicker() {
//		float scale = 1;
//		if (Math.random() < .02) scale = 2; 
//		current_flicker += (Math.random()*FLICKER_RATE_X*scale)-FLICKER_RATE_X*scale/2;
//		if (current_flicker > MAX_FLICKER) current_flicker = MAX_FLICKER;
//		if (current_flicker < -MAX_FLICKER) current_flicker = -MAX_FLICKER;
	}

	public void updatePositions() {
		for (Particle p : particles) {
			calculateNewPosition(p);
			updateColor(p);
//			doMove(p);
			updateLifetimes(p);
		}
	}

//	private void updateTextLighting() {
//		// letters[3] is the middle letter
//		var center = 3;
//		center += candles[0].current_flicker;
//		if (center < 2.5) center = 2.5 + Math.random()/4;
//		if (center > 3.5) center = 3.5 - Math.random()/4;
//
//		var darkness = Math.random()*80 + 30;
//
//		for (var i = 0; i < letters.length; i++) {
//			// if (i != 3) {
//			var diff = Math.abs(i-center);
//			var gray = parseInt(180-diff*50-darkness/((diff*diff)+1));
//
//			letters[i].style.color = "#" + decimalToHexString(gray+90) + decimalToHexString(gray+30) + decimalToHexString(0);
//			// }
//			// else letters[i].style.color = "#" + decimalToHexString(gray-100) + decimalToHexString(gray-100) + decimalToHexString(gray-100);
//		}
//	}

//	private void render() {
//		wind_x += Math.random()*.1 - .05;
//		if (wind_x > 1) wind_x = 1;
//		if (wind_x < -1) wind_x = -1;
//		wind_x += 2*candles[0].current_flicker;
//
//		// update text lighting
//		if (Math.random() < .15)
//			updateTextLighting();
//
//		for (var i = 0; i < candles.length; i++) {
//			generateParticles(candles[i]);
//			updateFlicker(candles[i]);
//		}
//		updatePositions();
//		setTimeout(render, MS); // msecs
//	}

//	private void init() {
//		var candivs = document.querySelectorAll("div.candle");
//
//		for (var i = 0; i < candivs.length; i++) {
//			var candiv = candivs[i];
//			var width = parseInt(180);
//			var height = parseInt(500);
//			candles[i] = new Candle(parseInt(0)+238, parseInt(0)+15, width/150, 1, candiv);
//			// console.log(parseInt(candiv.style.left));
//		}
//
//		letters = document.querySelectorAll("font.besiege");
//		console.log(letters.length);
//
//		render();
//	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
//		System.out.println("drawing");
		for (Particle p : particles) {
//			System.out.println("drawing");
			batch.setColor(p.color);
			
			batch.draw(region, p._x, -p._y, 8, 8);
		}
	}
}
