/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import kyle.game.besiege.Assets;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.SidePanel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class BottomPanel extends Group {
	private static final Color RED = new Color(230/255f, 0, 15/255f, 1);
	private static final Color ORANGE = new Color(1, 60/255f, 0, 1);
	private static final Color BLUE = new Color(0, 70/255f, 1, 1);
	private static final Color PURPLE = new Color(168/255f, 0, 1, 1);
	public static final int HEIGHT = 80;
	public final float PAD = 4;
	public final float NEG = -4;
	public final float MINI_ZOOM = 12;
	private final String textureRegion = "panelBG";
	private final String textureBlack = "black9";
	private final String barTexture = "grey-d9";
	private final String knobTexture = "grey-med9";
	private final int r = 3;
	private SidePanel panel;
	private TextureRegion region;
	private TextureRegion black;
	private ScrollPane sp;
	private ScrollPane.ScrollPaneStyle spStyle;
	private static Label logText;
	private static Label.LabelStyle ls;
	private static Table logTable;
	private static float logging; // workaround because scrolling to max right way wasn't working
	private static float logtime = .2f; // must be greater than ~.2f
	
	public BottomPanel(SidePanel panel) {
		this.region = new TextureRegion(Assets.atlas.findRegion(textureRegion));
		this.black = new TextureRegion(Assets.atlas.findRegion(textureBlack));
		
		this.panel = panel;

		this.setHeight(HEIGHT);
		this.setWidth(BesiegeMain.WIDTH - SidePanel.WIDTH);
		this.setX(-this.getWidth());
		this.setY(0);

		logging = 0;
		
		ls = new LabelStyle();
		ls.font = Assets.pixel15;
		
		logText = new Label("", ls);
		logText.setWrap(true);
		spStyle = new ScrollPane.ScrollPaneStyle();
		logTable = new Table();
		logTable.defaults().bottom().left().padTop(NEG);
		logTable.add(logText);
		logTable.bottom().left();
		//logTable.debug();
		
		spStyle.vScroll = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(barTexture), r,r,r,r));
		spStyle.vScrollKnob = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(knobTexture), r,r,r,r));
		sp = new ScrollPane(logTable, spStyle);
		sp.setScrollingDisabled(true, false);
		sp.setFadeScrollBars(false);
		sp.setBounds(PAD*2, PAD, this.getWidth()-PAD*4, BottomPanel.HEIGHT-PAD*2);
		sp.setScrollbarsOnTop(true);
		
		this.addActor(sp);
		
//		log("hey", "purple");
//		log("look", "magenta");
//		log("this", "blue");
//		log("thing", "cyan");
//		log("is", "green");
//		log("a", "yellow");
//		log("log", "orange");
//		log("yum", "red");
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
		batch.draw(black, getX() + PAD, getY() + PAD, getOriginX(), getOriginY(), getWidth()-2*PAD, getHeight()-2*PAD, 1, 1, getRotation());
		super.draw(batch, parentAlpha);
	}
	
	@Override
	public void act(float delta) {
		this.setX(0);
		// if resized
		if (this.getWidth() != panel.getMapScreen().getCamera().viewportWidth - SidePanel.WIDTH) {
			this.setWidth(panel.getMapScreen().getCamera().viewportWidth - SidePanel.WIDTH);
			resize();
		}
		this.setX(-this.getWidth());
		super.act(delta);
		if (logging<logtime) {
			sp.setScrollY(sp.getMaxY());
			logging += delta;
		}
	}
	
	public void resize() {
		this.removeActor(sp);
		sp = new ScrollPane(logTable, spStyle);
		sp.setScrollingDisabled(true, false);
		sp.setFadeScrollBars(false);
		sp.setBounds(PAD*2, PAD, this.getWidth()-PAD*2, BottomPanel.HEIGHT-PAD*2);
		sp.setScrollbarsOnTop(true);
		this.addActor(sp);
	}
	
	public static void log (String text) {
		log(text, "white");
	}
	
	public static void log(String text, String color) {
		Color fontColor;
		if (color == "red")
			fontColor = RED;
		else if (color == "orange")
			fontColor = ORANGE;
		else if (color == "yellow")
			fontColor = Color.YELLOW;
		else if (color == "cyan")
			fontColor = Color.CYAN;
		else if (color == "magenta")
			fontColor = Color.MAGENTA;
		else if (color == "white")
			fontColor = Color.WHITE;
		else if (color == "green")
			fontColor = Color.GREEN;
		else if (color == "blue")
			fontColor = BLUE;
		else if (color == "purple")
			fontColor = PURPLE;
		else fontColor = Color.WHITE;

		// We do need a new LS for every color.
		LabelStyle lsNew = new LabelStyle(ls);
		lsNew.fontColor = fontColor;
		Label temp = new Label(text,lsNew); // Minor memory leak, should change to have 4 or 5 fixed colors so new one is not created everytime.
		logTable.row();
		logTable.add(temp);
		logging = 0;
		ls.fontColor = Color.WHITE;
	}

}
