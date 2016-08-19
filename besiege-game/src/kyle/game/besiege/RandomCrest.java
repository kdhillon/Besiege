package kyle.game.besiege;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;


// Code from  http://stackoverflow.com/questions/43044/algorithm-to-randomly-generate-an-aesthetically-pleasing-color-palette
public class RandomCrest extends Actor {
//	static Stack<Color> colors;
	static Color[] colors;
	static int colorsPopped;
	static Color orig;
	static boolean initialized;
	static TextureRegion basic;
	static TextureRegion[] singleOverlays;
	static TextureRegion[][] doubleOverlays;
	static TextureRegion[] details;
	static boolean detailUsed[];
	static Color[] colorsUsed;
	
	static int COLORS = 50;
	static int OVERLAYS = 1;
//	static int DOUBLE_OVERLAYS = 0;
	static int DETAILS = 19;

	static Color base = Color.WHITE;

	int overlay = -1;
	
//	Color accent;
	int cOverlay;
//	Color cOverlay2;
	int detail = -1;
	int cDetail;
	
	boolean doubleOverlay;
	
	public RandomCrest() { //StrictArray<RandomCrest> alreadyGenerated
		colorsPopped += (int) (Math.random() * 2);
		
//		overlay = (int) (Math.random() * OVERLAYS);
//		if (Math.random() < 0.4) overlay = 0;
		overlay = 0;
		
		cOverlay = getNextColor();
//		if (overlay == 0 && Math.random() < 0.0) cOverlay = -1;

//		if (Math.random() < 0.6 || (cOverlay == -1 && overlay == 0)) {
			detail = getUnusedDetail();
//			detail = -1;
			cDetail = getNextColor();
//		}
		
//		if (detail < 0) {
//			overlay = (int) (Math.random() * OVERLAYS);
//		}
	}
	
	// Create variant
	public RandomCrest(RandomCrest original) { //StrictArray<RandomCrest> alreadyGenerated
		this.detail = original.detail;
		this.cDetail = original.cDetail;
		this.overlay = original.overlay;
		this.cOverlay = original.cOverlay;

		// change detail color or change detail
		if (Math.random() < 0.6) {
			if (detail < 0) {
				this.detail = getUnusedDetail();				
				this.cDetail = getNextColor();
			}
			else {
				// change color
				if (Math.random() < 0.5) {
					this.cDetail = getNextColor();
				}
				// change detail
				else {
					this.detail = getUnusedDetail();
				}
			}
		}
		else {
			if (Math.random() < 0.5) {
				// change overlay color
				this.cOverlay = getNextColor();
			}
			else {
				do {
					this.overlay = (int) (Math.random() * OVERLAYS);
				} while (this.overlay == original.overlay);
			}
		}
	}
	
	public static int getNextColor() {
		System.out.println("using color: " + colorsPopped);
		return colorsPopped++;
	}
	
	// call this only once.
	public static void initialize() {
		if (initialized) return;
		int coloursToGenerate = 50;

		// The colours at the start that you don't want (White and Black are the first 2)
		int coloursToSkip = 0;

		List<Color> colours = ColorGenerator.generate(coloursToGenerate, coloursToSkip);
		colors = new Color[COLORS];
		int j = 0;
		for (Color c: colours) {
			if (c == null ) throw new java.lang.AssertionError();
			System.out.println("creating color: " + c.r + ", " + c.g + ", " + c.b);
			colors[j] = c;
			j++;
		}
		
		compareAllColors();
		
		basic = Assets.atlas.findRegion("crestBase");
		
		singleOverlays = new TextureRegion[OVERLAYS];
		for (int i = 0; i < OVERLAYS; i++) {
			singleOverlays[i] = Assets.atlas.findRegion("over" + (i + 1));
			if (singleOverlays[i] == null) throw new java.lang.AssertionError();
		}
		
		details = new TextureRegion[DETAILS];
		for (int i = 0; i < DETAILS; i++) {
			details[i] = Assets.atlas.findRegion("detail" + (i + 1));
			if (details[i] == null) throw new java.lang.AssertionError("cant find detail: " + (i+1));
		}
		detailUsed = new boolean[DETAILS];
		
		initialized = true;
	}
	
	public static int getUnusedDetail() {
		boolean allUsed = true;
		for (int i = 0; i < details.length; i++) {
			if (!detailUsed[i]) allUsed = false;
		}
		if (allUsed) return -1;
		
		int detail;
		do {
			detail = (int) (Math.random() * DETAILS);
		}
		while (detailUsed[detail]);
		detailUsed[detail] = true;
		return detail;
	}
	
	public static Color getColor(int i) {
		if (i < 0) return Color.WHITE;
		return colors[i];
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
			batch.setColor(getColor(cOverlay).r, getColor(cOverlay).g, getColor(cOverlay).b, parentAlpha);
			batch.draw(singleOverlays[overlay], this.getX(), this.getY(), this.getWidth(), this.getHeight());
			if (singleOverlays[overlay] == null) throw new java.lang.AssertionError();
		}
		
		if (detail >= 0) {
			batch.setColor(getColor(cDetail).r, getColor(cDetail).g, getColor(cDetail).b, parentAlpha);

			batch.draw(details[detail], this.getX(), this.getY(), this.getWidth(), this.getHeight());
			if (details[detail] == null) throw new java.lang.AssertionError();
		}
		
		batch.setColor(orig);
	}
	
	public static boolean tooSimilar(Color a, Color b) {
//		Color aY = toYUV(a);
//		Color bY = toYUV(b);
		Color aY = a;
		Color bY = b;
		
		float d = (float) Math.sqrt((aY.r-bY.r) * (aY.r-bY.r) + (aY.g-bY.g) *(aY.g-bY.g) + (aY.b-bY.b) * (aY.b-bY.b));
		System.out.println("distance: " + d);
		return d < 0.4f;
	}
	
	public static void compareAllColors() {
		for (int i = 0; i < COLORS; i++) {
			for (int j = 0; j < COLORS; j++) {
				if (i == j) continue;
				if (tooSimilar(colors[i], colors[j])) {
					System.out.println("colors: " + colors[i].toString() + " " + i + ", " + colors[j].toString() + " " + j + " are too similar");
				}
			}
		}
	}
	
	public static Color toYUV(Color in) {
		Color out = new Color();
		// y
		out.r = 0.299f * in.r + 0.587f * in.g + 0.114f * in.b;
		
		// u
		out.g = -0.14713f * in.r + -0.28886f * in.g + 0.436f * in.b;
		
		// v
		out.b = 0.615f * in.r + -0.51499f * in.g + -0.10001f * in.b;
		return out;
	}
}
