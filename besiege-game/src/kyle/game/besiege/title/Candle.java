package kyle.game.besiege.title;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Candle {

	Array<Particle> particles;
	//	Array<Letter> letters;

	// var FLAME_HEIGHT = 1;
	// var FLAME_WIDTH = 1;

	// var FLICKER_RATE_X = .03;
	// var MAX_FLICKER = .4;

	// var PARTICLE_COUNT = 100*FLAME_WIDTH*FLAME_HEIGHT;

	private static float MS = 10; // ms per frame

	private static float FLICKER_RATE_X = .03f;
	private static float MAX_FLICKER = .6f;
	// var PARTICLE_COUNT = 100*FLAME_WIDTH*FLAME_HEIGHT;
	private static float ACCEL_Y = -.05f;

	// var INIT_VX = .6*FLAME_WIDTH;
	// var INIT_VY = .2*FLAME_HEIGHT;
	// var ACCEL_Y = -.05;

	// var INIT_LIFETIME = .2*FLAME_HEIGHT*FLAME_WIDTH;
	// var MAX_LIFETIME = .5*FLAME_HEIGHT;
	// var LIFETIME_RANDOMNESS = .05*FLAME_WIDTH*FLAME_HEIGHT;

	// colors
	private static int INIT_G = 255;
	private static int FINAL_G = 80;
	private static int counter = 0;
	private static int wind_x = 0;

	int _x;
	int _y;

	float INIT_VX;
	float INIT_VY;

	float INIT_LIFETIME;
	float MAX_LIFETIME;
	float LIFETIME_RANDOMNESS;

	float current_flicker;

	public Candle(int _x, int _y, float _width, float _height) {
		this._x = _x;
		this._y = _y;

		float FLAME_WIDTH = _width;
		float FLAME_HEIGHT = _height;

		this.INIT_VX = .6f*FLAME_WIDTH;
		this.INIT_VY = .2f*FLAME_HEIGHT;

		this.INIT_LIFETIME = .15f*FLAME_HEIGHT*FLAME_WIDTH;
		this.MAX_LIFETIME = .3f*FLAME_HEIGHT;
		this.LIFETIME_RANDOMNESS = .05f*FLAME_WIDTH*FLAME_HEIGHT;

		this.current_flicker = 0;
	}

	private class Particle {
		int _x;
		int _y;
		float vx;
		float vy;
		float lifetime;
		float init_lifetime;
		int r, g, b;
		boolean isSmoke;
		
		TextureRegion todraw;
		
		private Particle(Candle candle) {
			counter++;
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

			this.vx = (float) (candle.INIT_VX*Math.random() - candle.INIT_VX/2 + candle.current_flicker);
			this.vy = (float) (candle.INIT_VY*Math.random() - candle.INIT_VY/2);

			this.lifetime = candle.INIT_LIFETIME + Math.min(candle.LIFETIME_RANDOMNESS/Math.abs(this.vx - candle.current_flicker), candle.MAX_LIFETIME);
			this.init_lifetime = candle.INIT_LIFETIME;

			this.r = 255; // between 0 and 255
			this.g = 0;
			this.b = 0;

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
	private void generateParticles(candle) {
		// if (particles.length < PARTICLE_COUNT) 
		if (Math.random() < 1)
			particles[particles.length] = new Particle(candle);
	}

	private void removeParticle(p) {
		if (Math.random() < .2 && !p.isSmoke) {
			p.isSmoke = true;
			p.lifetime = Math.random()*3;
			p.init_lifetime = p.lifetime;
			p.vy *= 1.5;

			var element = document.getElementById(p.id);
			// element.innerHTML = "~";
			p._y -= 10;
			var gray = 255;
			p.r = gray;
			p.g = gray;
			p.b = gray;
		}
		else {
			var index = particles.indexOf(p);
			if (index > -1) particles.splice(index, 1);
			var element = document.getElementById(p.id);
			element.parentNode.removeChild(element);
		}
	}

	private void  calculateNewPosition(p) {
		if (!p.isSmoke)
			p.vy += ACCEL_Y*MS/20;
		else p.vx = wind_x;
		if (p.vy < -.7) {
			if (p.vx > 0) p.vx -= .05;
			if (p.vx < 0) p.vx += .05;
		} 

		p._x += p.vx*MS/20;
		p._y += p.vy*MS/20;
	}

	private void  doMove(p) {
		var div = document.getElementById(p.id); 
		div.style.left = p._x + 'px';
		div.style.top = p._y + 'px';
		div.style.color = "#" + decimalToHexString(p.r) + decimalToHexString(p.g) + decimalToHexString(p.b);
	}

	private void updateColor(p) {
		if (!p.isSmoke)
			p.g = parseInt((p.lifetime / p.init_lifetime) * INIT_G) + FINAL_G;
		else {
			var gray = parseInt((p.lifetime / p.init_lifetime) * 255) + 0;

			p.g = gray;
			p.r = gray;
			p.b = gray;
		}
		// p.g = parseInt((p._y / (INIT_TOP+50)) * 255);
	}

	private void updateLifetimes(p) {
		p.lifetime -= MS/1000.0;

		if (p.lifetime < 0) removeParticle(p);
	}

	private void updateFlicker(candle) {
		var scale = 1;
		if (Math.random() < .02) scale = 10; 
		candle.current_flicker += (Math.random()*FLICKER_RATE_X*scale)-FLICKER_RATE_X*scale/2;
		if (candle.current_flicker > MAX_FLICKER) candle.current_flicker = MAX_FLICKER;
		if (candle.current_flicker < -MAX_FLICKER) candle.current_flicker = -MAX_FLICKER;
	}

	private void updatePositions(candle) {
		for (var i = 0; i < particles.length; i++) {
			calculateNewPosition(particles[i]);
			updateColor(particles[i]);
			doMove(particles[i]);
			updateLifetimes(particles[i]);
		}
	}

	private void updateTextLighting() {
		// letters[3] is the middle letter
		var center = 3;
		center += candles[0].current_flicker;
		if (center < 2.5) center = 2.5 + Math.random()/4;
		if (center > 3.5) center = 3.5 - Math.random()/4;

		var darkness = Math.random()*80 + 30;

		for (var i = 0; i < letters.length; i++) {
			// if (i != 3) {
			var diff = Math.abs(i-center);
			var gray = parseInt(180-diff*50-darkness/((diff*diff)+1));

			letters[i].style.color = "#" + decimalToHexString(gray+90) + decimalToHexString(gray+30) + decimalToHexString(0);
			// }
			// else letters[i].style.color = "#" + decimalToHexString(gray-100) + decimalToHexString(gray-100) + decimalToHexString(gray-100);
		}
	}

	private void render() {
		wind_x += Math.random()*.1 - .05;
		if (wind_x > 1) wind_x = 1;
		if (wind_x < -1) wind_x = -1;
		wind_x += 2*candles[0].current_flicker;

		// update text lighting
		if (Math.random() < .15)
			updateTextLighting();

		for (var i = 0; i < candles.length; i++) {
			generateParticles(candles[i]);
			updateFlicker(candles[i]);
		}
		updatePositions();
		setTimeout(render, MS); // msecs
	}

	private void init() {
		var candivs = document.querySelectorAll("div.candle");

		for (var i = 0; i < candivs.length; i++) {
			var candiv = candivs[i];
			var width = parseInt(180);
			var height = parseInt(500);
			candles[i] = new Candle(parseInt(0)+238, parseInt(0)+15, width/150, 1, candiv);
			// console.log(parseInt(candiv.style.left));
		}

		letters = document.querySelectorAll("font.besiege");
		console.log(letters.length);

		render();
	}

}
