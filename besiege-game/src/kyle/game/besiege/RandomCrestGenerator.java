package kyle.game.besiege;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Generates random crests. Keeps a state of which crests have been generated.
 * @author Kyle
 *
 */
// Code from  http://stackoverflow.com/questions/43044/algorithm-to-randomly-generate-an-aesthetically-pleasing-color-palette
public class RandomCrestGenerator extends Actor {
	// static Stack<Color> colors;
	Color[] colors;
	int colorsPopped;
	TextureRegion[] singleOverlays;
	TextureRegion[][] doubleOverlays;
	TextureRegion[] details;
	boolean detailUsed[];
	Color[] colorsUsed;

	int COLORS = 50;
	int OVERLAYS = 1;
	// static int DOUBLE_OVERLAYS = 0;
	int DETAILS = 19;

	public RandomCrestGenerator() {
		int coloursToGenerate = 50;

		// The colours at the start that you don't want (White and Black are the
		// first 2)
		int coloursToSkip = 0;

		List<Color> colours = ColorGenerator.generate(coloursToGenerate, coloursToSkip);
		colors = new Color[COLORS];
		int j = 0;
		for (Color c : colours) {
			if (c == null)
				throw new java.lang.AssertionError();
			System.out.println("creating color: " + c.r + ", " + c.g + ", " + c.b);
			colors[j] = c;
			j++;
		}

		compareAllColors();

		singleOverlays = new TextureRegion[OVERLAYS];
		for (int i = 0; i < OVERLAYS; i++) {
			singleOverlays[i] = Assets.atlas.findRegion("over" + (i + 1));
			if (singleOverlays[i] == null)
				throw new java.lang.AssertionError();
		}

		details = new TextureRegion[DETAILS];
		for (int i = 0; i < DETAILS; i++) {
			details[i] = Assets.atlas.findRegion("detail" + (i + 1));
			if (details[i] == null)
				throw new java.lang.AssertionError("cant find detail: " + (i + 1));
		}
		detailUsed = new boolean[DETAILS];
	}

	public Crest create() {
		colorsPopped += (int) (Math.random() * 2);

		int overlay = 0;
		int cOverlay = getNextColor();
		int detail = getUnusedDetail();
		int cDetail = getNextColor();
		Crest crest = new Crest(overlay, cOverlay, detail, cDetail);

		return crest;
	}

	public int getUnusedDetail() {
		boolean allUsed = true;
		for (int i = 0; i < details.length; i++) {
			if (!detailUsed[i])
				allUsed = false;
		}
		if (allUsed)
			return -1;

		int detail;
		do {
			detail = (int) (Math.random() * DETAILS);
		} while (detailUsed[detail]);
		detailUsed[detail] = true;
		return detail;
	}

	// Create variant
	public Crest createVariant(Crest original) { 							
		int detail = original.detail;
		int cDetail = original.cDetail;
		int overlay = original.overlay;
		int cOverlay = original.cOverlay;

		// change detail color or change detail
		if (Math.random() < 0.6) {
			if (detail < 0) {
				detail = getUnusedDetail();
				cDetail = getNextColor();
			} else {
				// change color
				if (Math.random() < 0.5) {
					cDetail = getNextColor();
				}
				// change detail
				else {
					detail = getUnusedDetail();
				}
			}
		} else {
			if (Math.random() < 0.5) {
				// change overlay color
				cOverlay = getNextColor();
			} else {
				do {
					overlay = (int) (Math.random() * OVERLAYS);
				} while (overlay == original.overlay);
			}
		}
		Crest crest = new Crest(overlay, cOverlay, detail, cDetail);

		return crest;
	}

	public Color getColor(int i) {
		if (i < 0)
			return Color.WHITE;
		return colors[i];
	}

	public int getNextColor() {
		System.out.println("using color: " + colorsPopped);
		return colorsPopped++;
	}

	public boolean tooSimilar(Color a, Color b) {
		// Color aY = toYUV(a);
		// Color bY = toYUV(b);
		Color aY = a;
		Color bY = b;

		float d = (float) Math
				.sqrt((aY.r - bY.r) * (aY.r - bY.r) + (aY.g - bY.g) * (aY.g - bY.g) + (aY.b - bY.b) * (aY.b - bY.b));
		System.out.println("distance: " + d);
		return d < 0.4f;
	}

	public void compareAllColors() {
		for (int i = 0; i < COLORS; i++) {
			for (int j = 0; j < COLORS; j++) {
				if (i == j)
					continue;
				if (tooSimilar(colors[i], colors[j])) {
					System.out.println("colors: " + colors[i].toString() + " " + i + ", " + colors[j].toString() + " "
							+ j + " are too similar");
				}
			}
		}
	}

	public Color toYUV(Color in) {
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
