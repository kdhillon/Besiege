package kyle.game.besiege;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// Factions possess a crest. Locations and Armies hold a CrestDraw, which is a crest drawn at a specific position.
public class Crest {
	int overlay;
	int cOverlay;
	String detail; // detail name
	int cDetail;

	transient TextureRegion overlayRegion;
	transient TextureRegion details;
	transient Color overlayColor;
	transient Color detailColor;

	// default crest draw
	public CrestDraw defaultCrestDraw;

	// for kryo
	public Crest() {
		
	}

	public Crest(int overlay, int cOverlay, String detail, int cDetail) {
		this.overlay = overlay;
		this.cOverlay = cOverlay;
		this.detail = detail;
		this.cDetail = cDetail;

		defaultCrestDraw = new CrestDraw(this);
	}

	public void loadFromInts(RandomCrestGenerator rcg) {
//		this.overlayRegion = rcg.singleOverlays[overlay];
		this.details = Assets.crests.findRegion(detail);
		this.overlayColor = rcg.getColor(cOverlay);
		if (overlayColor == null) {
			throw new java.lang.AssertionError();
		}
		this.detailColor = rcg.getColor(cDetail);
		if (detailColor == null) {
			throw new java.lang.AssertionError();
		}
	}

	static Crest getBlank(Color color) {
//		int hash = color.hashCode();
//		rcg.addSpecialColor(color, hash);
		Crest c = new Crest(-1, -1, null, -1);
		c.overlayColor = color;
		c.defaultCrestDraw = new CrestDraw(c);
		return c;
//		return new Crest(0, hash, null, 0);
	}
}
