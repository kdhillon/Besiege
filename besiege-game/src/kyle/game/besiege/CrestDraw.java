package kyle.game.besiege;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import kyle.game.besiege.panels.SidePanel;

// Factions possess a crest. Locations and Armies hold a CrestDraw, which is a crest drawn at a specific position.
public class CrestDraw extends Actor {
	private static TextureRegion basic = Assets.crests.findRegion("crestbase");;
	private static final Color clear_white = new Color(1f, 1f, 1f, .6f);

//	private Crest parentCrest;
	private Color orig;
	public Destination destination;

	// for now, base color is always white.
	private Color base = Color.WHITE;

	private Crest parentCrest;

	// for kryo
	public CrestDraw() {

	}

	public CrestDraw(Destination destination) {
		this.destination = destination;
//		if (destination.getFaction() != null)
//			this.parentCrest = destination.getFaction().crest;
		calcSize();
//		this.setPosition(destination.getCenterX() - this.getWidth()/2, 0.7f);
		if (getLocation() != null)
			this.addListener(getNewInputListener());
	}

	// For use with side panel only.
	public CrestDraw(Crest crest) {
		this.parentCrest = crest;
	}

//	public void notifyFactionChanged() {
//		if (destination.getFaction() != null) {
//			this.parentCrest = destination.getFaction().crest;
//		}
//	}

	private Crest getFactionCrest() {
		if (parentCrest != null) return parentCrest;
		if (destination.getFaction() != null)
			return destination.getFaction().crest;
		return null;
	}

	private Army getArmy() {
		if (destination.getType() == Destination.DestType.ARMY) {
			Army army = ((Army) destination);
			return army;
		}
		return null;
	}
	private Location getLocation() {
		if (destination.getType() == Destination.DestType.LOCATION) {
			Location loc = ((Location) destination);
			return loc;
		}
		return null;
	}

	private void calcSize() {
		// Already been set.
		if (this.getY() > 0) return;
		Location loc = getLocation();
		Army army = getArmy();

		if (loc != null) {
//			this.setPosition(-15 * loc.getScaleX(), 15 * loc.getScaleY());
			this.setSize(1.4f, 1.4f);
			this.setPosition( loc.getWidth() / 2 - this.getWidth()/2, loc.getHeight() * 1.2f);
		} else if (army != null) {
//			this.setPosition(-15 * army.getScaleX(), 15 * army.getScaleY());
			this.setPosition(army.getCenterX() - this.getWidth()/2, 0.7f);
			this.setSize(1.4f, 1.4f);
		} // TODO add battleactor
		else throw new AssertionError();
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		Crest parentCrest = getFactionCrest();
		if (this.parentCrest == null) {
			if (getArmy() != null) {
				if (!getArmy().shouldDrawCrest()) return;
			} else if (getLocation() != null) {
				if (!getLocation().shouldDrawCrest()) return;
			}

			if (parentCrest == null) return;
			calcSize();
		} else {
			parentCrest = this.parentCrest;
		}
		// for now, until I can figure out blending...
		parentAlpha = 1;

		orig = batch.getColor();

		base = parentCrest.overlayColor;
		batch.setColor(base.r, base.g, base.b, parentAlpha);
		batch.draw(basic,  this.getX(),  this.getY(), this.getWidth(), this.getHeight());

//		if (overlay >= 0) {
//			batch.setColor(overlayColor.r, overlayColor.g, overlayColor.b, parentAlpha);
//			batch.draw(overlayRegion, this.getX(), this.getY(), this.getWidth(), this.getHeight());
//			if (overlayRegion == null) throw new java.lang.AssertionError();
//		}

		if (parentCrest.detail != null) {
			batch.setColor(parentCrest.detailColor.r, parentCrest.detailColor.g, parentCrest.detailColor.b, parentAlpha);

			batch.draw(parentCrest.details, this.getX(), this.getY(), this.getWidth(), this.getHeight());
			if (parentCrest.details == null) throw new AssertionError();
		}
		
		batch.setColor(orig);
		super.draw(batch, parentAlpha);
	}

	public InputListener getNewInputListener() {
		return new InputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y,
								int pointer, int button) {
				boolean touchdown = true;
				//do your stuff
				//it will work when finger is released..
				System.out.println("Touched up " + getName());
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y,
									 int pointer, int button) {
				boolean touchdown = false;
				//do your stuff it will work when u touched your actor
				return true;
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				System.out.println("Mousing over " + getName());
				System.out.println("Setting panel crest! " + getName());
				// TODO make this work with crestdraw
				Army army = getArmy();
				Location location = getLocation();
				if (location != null) location.getKingdom().setPanelTo(location);
				else if (army != null) army.getKingdom().setPanelTo(army);
				else throw new AssertionError();
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				System.out.println("returning to previous (crest)");
				Army army = getArmy();
				if (army != null)
					army.getKingdom().mouseOverCurrentPoint();
				Location loc = getLocation();
				if (loc != null)
					loc.getKingdom().mouseOverCurrentPoint();
			}
		};
	}
}
