package kyle.game.besiege;

import java.util.List;
import java.util.Stack;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;


// Code from  http://stackoverflow.com/questions/43044/algorithm-to-randomly-generate-an-aesthetically-pleasing-color-palette
public class RandomCrest extends Actor {
	static Stack<Color> colors;
	static Color orig;
	static boolean initialized;
	static TextureRegion basic;
	static TextureRegion[] singleOverlays;
	static TextureRegion[][] doubleOverlays;
	static TextureRegion[] details;
	static boolean detailUsed[];
	
	static int SINGLE_OVERLAYS = 13;
//	static int DOUBLE_OVERLAYS = 0;
	static int DETAILS = 12;

	Color base;
	int overlay = -1;
	
//	Color accent;
	Color cOverlay;
//	Color cOverlay2;
	int detail = -1;
	Color cDetail;
	
	boolean doubleOverlay;
	
	public RandomCrest() { //StrictArray<RandomCrest> alreadyGenerated
//		if (Math.random() < 0.3) {
			base = Color.WHITE;
			
//		}
//		else 
//			base = Color.WHITE;
		
		if (Math.random() < .5) {
			// do a single overlay
//			if (Math.random() * (SINGLE_OVERLAYS + DOUBLE_OVERLAYS) < SINGLE_OVERLAYS) {
				overlay = (int) (Math.random() * SINGLE_OVERLAYS);
				if (Math.random() < 0.3) overlay = 0;
				cOverlay = colors.pop();
//			}
			// do a double overlay
//			else {
//				overlay = (int) (Math.random() * DOUBLE_OVERLAYS);
//				cOverlay = colors.pop();
//				cOverlay2 = colors.pop();
//				doubleOverlay = true;
//			}
		}
		
		if (overlay < 0 || Math.random() < 0.6) {
			do {
				detail = (int) (Math.random() * DETAILS);
			}
			while (detailUsed[detail]);
			detailUsed[detail] = true;
			cDetail = colors.pop();
		}
	}
	
	// call this only once.
	public static void initialize() {
		if (initialized) return;
		int coloursToGenerate = 50;

		// The colours at the start that you don't want (White and Black are the first 2)
		int coloursToSkip = 1;

		List<Color> colours = ColorGenerator.generate(coloursToGenerate, coloursToSkip);
		colors = new Stack<Color>();
		for (Color c: colours) {
			if (c == null ) throw new java.lang.AssertionError();
			System.out.println("creating color: " + c.r + ", " + c.g + ", " + c.b);
			colors.push(c);
		}
		
		basic = Assets.atlas.findRegion("crestBase");
		
		singleOverlays = new TextureRegion[SINGLE_OVERLAYS];
		for (int i = 0; i < SINGLE_OVERLAYS; i++) {
			singleOverlays[i] = Assets.atlas.findRegion("over" + (i + 1));
			if (singleOverlays[i] == null) throw new java.lang.AssertionError();
		}
		
//		doubleOverlays = new TextureRegion[DOUBLE_OVERLAYS][2];
//		for (int i = 0; i < DOUBLE_OVERLAYS; i++) {
//			doubleOverlays[i][0] = Assets.atlas.findRegion("overd" + (i+1) + "a");
//			doubleOverlays[i][1] = Assets.atlas.findRegion("overd" + (i+1) + "b");
//		}
		
		details = new TextureRegion[DETAILS];
		for (int i = 0; i < DETAILS; i++) {
			details[i] = Assets.atlas.findRegion("detail" + (i + 1));
			if (details[i] == null) throw new java.lang.AssertionError("cant find detail: " + (i+1));
		}
		detailUsed = new boolean[DETAILS];
		
		initialized = true;
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		if (!initialized) return;
		
		// for now, until I can figure out blending...
		parentAlpha = 1;
		
		orig = batch.getColor();
		
		batch.setColor(base.r, base.g, base.b, parentAlpha);
		batch.draw(basic, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		
		if (overlay >= 0) {
//			if (doubleOverlay) {
//				batch.setColor(cOverlay.r, cOverlay.g, cOverlay.b, parentAlpha);
//
//				batch.draw(doubleOverlays[overlay][0], this.getX(), this.getY(), this.getWidth(), this.getHeight());
//				batch.setColor(cOverlay2.r, cOverlay2.g, cOverlay2.b, parentAlpha);
//
//				batch.draw(doubleOverlays[overlay][1], this.getX(), this.getY(), this.getWidth(), this.getHeight());
//			}
//			else {
				batch.setColor(cOverlay.r, cOverlay.g, cOverlay.b, parentAlpha);
				batch.draw(singleOverlays[overlay], this.getX(), this.getY(), this.getWidth(), this.getHeight());
				if (singleOverlays[overlay] == null) throw new java.lang.AssertionError();
//			}
		}
		
		if (detail >= 0) {
			batch.setColor(cDetail.r, cDetail.g, cDetail.b, parentAlpha);

			batch.draw(details[detail], this.getX(), this.getY(), this.getWidth(), this.getHeight());
			if (details[detail] == null) throw new java.lang.AssertionError();
		}
		
		batch.setColor(orig);
	}
}
