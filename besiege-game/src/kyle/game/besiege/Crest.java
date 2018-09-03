package kyle.game.besiege;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;


// Factions possess a crest. Locations and Armies hold a CrestDraw, which is a crest drawn at a specific position.
public class Crest {
	public static Crest BANDIT_CREST;
	public static Crest ROGUE_CREST;
	
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
}
