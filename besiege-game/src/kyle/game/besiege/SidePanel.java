/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.ArmyPlayer;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.Panel;
import kyle.game.besiege.panels.PanelAttributes;
import kyle.game.besiege.panels.PanelBattle;
import kyle.game.besiege.panels.PanelCharacter;
import kyle.game.besiege.panels.PanelFaction;
import kyle.game.besiege.panels.PanelLocation;
import kyle.game.besiege.panels.PanelParty;
import kyle.game.besiege.panels.PanelUnit;
import kyle.game.besiege.panels.PanelUpgrades;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class SidePanel extends Group {
	public static final int WIDTH = 190; // 180 works well
	public final float MINI_ZOOM = 12;
	public static float HEIGHT = BesiegeMain.HEIGHT;
	private final String textureRegion = "panelBG";
	private TextureRegion region;
	private MapScreen mapScreen;
	private Kingdom kingdom;
	private OrthographicCamera camera;
	
//	private OrthographicCamera miniCam;
//	private Stage miniStage;
//	private SpriteBatch miniBatch;
	private MiniMap minimap;
	private BottomPanel bottom;
	
	private Panel activePanel;
	private Panel previousPanel;
	
	//public PanelMain main;
	public PanelCharacter character;
	public PanelAttributes attributes;
	public PanelParty party;
	public PanelUpgrades upgrades;
	
	private boolean stay; // stay on current panel until set false
		
	public SidePanel(MapScreen mapScreen) {
		this.mapScreen = mapScreen;
		this.camera = (OrthographicCamera) mapScreen.getUICamera();	
		
		this.region = new TextureRegion(Assets.atlas.findRegion(textureRegion));
		
//		this.miniCam = new OrthographicCamera(BesiegeMain.WIDTH,BesiegeMain.HEIGHT);
//		miniCam.zoom = MINI_ZOOM;
//		this.miniStage = new Stage();
//		miniStage.setCamera(miniCam);
//		this.miniBatch = new SpriteBatch();
//		System.out.println("gutter" + miniStage.getGutterHeight());
//		// orient miniStage
//		miniCam.position.set(-3700, -2500, 0);
		
		bottom = new BottomPanel(this);
		this.addActor(bottom);
		
		minimap = new MiniMap(this);
		this.addActor(minimap);
		
		this.setHeight(camera.viewportHeight);
		stay = false;
	}
	
	public void initializePanels() {
		//main = new PanelMain(this);
		character = new PanelCharacter(this);
		party = new PanelParty(this, kingdom.getPlayer());
		attributes = new PanelAttributes(this);
		upgrades = new PanelUpgrades(this, kingdom.getPlayer());
		
		this.setActive(party);
	}
	
	@Override
	public void act(float delta) {
		this.setWidth(WIDTH);
		this.setHeight(camera.viewportHeight);
		this.setPosition(camera.viewportWidth-WIDTH, 0);
		this.setOrigin(getCamera().position.x - getX(), getCamera().position.y - getY());

		// the second leak is here! it's in one of the children.
		super.act(delta);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
		// draw minimap
		//minimap.clipBegin(100, 100, 100, 100);
		super.draw(batch, parentAlpha);
		//minimap.clipEnd();
//		Table.drawDebug(getMapScreen().getUIStage());

//		miniCam.update();
//		miniStage.addActor(kingdom);
//		kingdom.clipBegin(kingdom.getX(), kingdom.getY(),kingdom.getWidth(), kingdom.getHeight());
//		miniBatch.setProjectionMatrix(miniCam.combined);
//		miniBatch.begin();
//		kingdom.draw(miniBatch, 1);
//		miniBatch.end();
//		miniStage.clear();
//		mapScreen.getStage().addActor(kingdom);
	}
	public void clean() {
		this.clearChildren();
		this.addActor(this.minimap);
		this.addActor(this.bottom);
		this.previousPanel = null;
		this.activePanel = null;
	}
	
	public void setPreviousPanel(Panel previousPanel) {
			this.previousPanel = previousPanel;
	}
	public void returnToPrevious() {
		if (previousPanel != null && !stay)
			setActive(previousPanel);
	}
	public OrthographicCamera getCamera() {
		return camera;
	}
	public MiniMap getMiniMap() {
		return minimap;
	}
	public MapScreen getMapScreen() {
		return mapScreen;
	}
	public void setActive(Panel newActivePanel) {
		if (this.activePanel == newActivePanel) return;
		if (!stay) {
			if (activePanel != null) {
				setPreviousPanel(activePanel);
				this.removeActor(this.activePanel);
			}
			this.activePanel = newActivePanel;

			this.addActor(activePanel);
		}
	}
	public void setActiveDestination(Destination destination) {
		Destination.DestType type = destination.getType();
		if (type == Destination.DestType.POINT) { returnToPrevious();
//		System.out.println("trying to return to prev");
		}
		else if (type == Destination.DestType.LOCATION) setActiveLocation((Location) destination);
		else if (type == Destination.DestType.ARMY) setActiveArmy((Army) destination);
		else if (type == Destination.DestType.BATTLE) setActiveBattle((Battle) destination);
	}
	public void setActiveArmy(Army army) {
		if (army.getParty().player) setActive(party);
		else {
			PanelParty pp = new PanelParty(this, army);
			setActive(pp);
		}
	}
	public void setActiveLocation(Location location) {
		PanelLocation lp = new PanelLocation(this, location);
		setActive(lp);
	}
	public void setActiveBattle(Battle battle) {
		PanelBattle pb = new PanelBattle(this, battle);
		setActive(pb);
	}
	public void setActiveFaction(Faction faction) {
		PanelFaction pf = new PanelFaction(this, faction);
		setActive(pf);
	}
	public void setActiveUnit(Unit unit) {
		PanelUnit pu = new PanelUnit(this, unit);
		setActive(pu);
//		PanelFaction pf = new PanelFaction(this, faction);
//		setActive(pf);
	}
	public void setDefault() {
		if (mapScreen.battle != null)
			setActiveBattle(mapScreen.getKingdom().getPlayer().getBattle());
		else setActive(party); // can change
		
		// testing for leaks?
		// leaks still occur with character!
//		setActive(character);
	}
	public void press(int button) {
//		if (activePanel.getButton(button).isVisible()) {
			if (button == 1)
				activePanel.button1();
			else if (button == 2)
				activePanel.button2();
			else if (button == 3)
				activePanel.button3();
			else if (button == 4)
				activePanel.button4();
//		}
	}
	public void setKingdom(Kingdom kingdom) {
		this.kingdom = kingdom;
	}
	public Kingdom getKingdom() {
		return kingdom;
	}
	public ArmyPlayer getPlayer() {
		return kingdom.getPlayer();
	}
	public void setStay(boolean b) {
		stay = b;
	}
	public boolean getStay() {
		return stay;
	}
	public TextureRegion getActiveCrest() {
		return activePanel.getCrest();
	}
	public TextureRegion getSecondCrest() {
		return activePanel.getSecondCrest();
	}
	public Panel getActivePanel() {
		return activePanel;
	}
	public void beginSaving() {
		this.activePanel.beginSaving();
	}
	public void endSaving() {
		this.activePanel.endSaving();
	}
}
