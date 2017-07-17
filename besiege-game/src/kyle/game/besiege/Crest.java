package kyle.game.besiege;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Crest extends Actor {
	private static TextureRegion basic = Assets.atlas.findRegion("crestBase");;

	public static Crest BANDIT_CREST;
	public static Crest ROGUE_CREST;
	
	int overlay;
	int cOverlay;
	int detail;
	int cDetail;
		
	private Color orig;
	// for now, base color is always white.
	Color base = Color.WHITE;

	transient TextureRegion overlayRegion;
	transient TextureRegion details;
	transient Color overlayColor;
	transient Color detailColor;
	
	// for kryo
	public Crest() {
		
	}
	
	
	public Crest(int overlay, int cOverlay, int detail, int cDetail) {
		this.overlay = overlay;
		this.cOverlay = cOverlay;
		this.detail = detail;
		this.cDetail = cDetail;
	}
	
	public void loadFromInts(RandomCrestGenerator rcg) {
		this.overlayRegion = rcg.singleOverlays[overlay];
		this.details = rcg.details[detail];
		this.overlayColor = rcg.getColor(cOverlay);
		if (overlayColor == null) {
			throw new java.lang.AssertionError();
		}
		this.detailColor = rcg.getColor(cDetail);
		if (detailColor == null) {
			throw new java.lang.AssertionError();
		}
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		// for now, until I can figure out blending...
		parentAlpha = 1;
		
		orig = batch.getColor();
		
		batch.setColor(base.r, base.g, base.b, parentAlpha);
		batch.draw(basic, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		
		if (overlay >= 0) {
			batch.setColor(overlayColor.r, overlayColor.g, overlayColor.b, parentAlpha);
			batch.draw(overlayRegion, this.getX(), this.getY(), this.getWidth(), this.getHeight());
			if (overlayRegion == null) throw new java.lang.AssertionError();
		}
		
		if (detail >= 0) {
			batch.setColor(detailColor.r, detailColor.g, detailColor.b, parentAlpha);

			batch.draw(details, this.getX(), this.getY(), this.getWidth(), this.getHeight());
			if (details == null) throw new java.lang.AssertionError();
		}
		
		batch.setColor(orig);
	}
}
