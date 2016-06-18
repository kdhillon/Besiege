/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import kyle.game.besiege.Assets;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.MiniMap;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.ArmyPlayer;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.voronoi.Center;

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
	
	private transient Panel activePanel;
	private transient Panel previousPanel;
	
	//public PanelMain main;
	public PanelCharacter character;
	public PanelAttributes attributes;
	public PanelParty party;
	public PanelUpgrades upgrades;
	public PanelInventory inventory;
		
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

	// hopefully will prevent stupid kryo errors
	// nope was totally different
//	public void purgeBeforeSave() {
//		this.clearChildren();
//	}
//	
//	public void restoreAfterSave() {
//		this.addActor(bottom);
//		this.addActor(minimap);
//		this.addActor(activePanel);
//	}
	
	public void initializePanels() {
		//main = new PanelMain(this);
		character = new PanelCharacter(this);
		inventory = new PanelInventory(this);
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
		Table.drawDebug(getMapScreen().getUIStage());

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
		if (newActivePanel == upgrades) upgrades.updateSoldierTable();
		if (newActivePanel.getClass() == PanelParty.class) {
			((PanelParty) newActivePanel).updateSoldierTable();
//			System.out.println("just updated party panel!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		if (newActivePanel.getClass() != PanelCenter.class && kingdom != null) {
			kingdom.map.selectedCenter = null;
		}
		
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
		if (unit.isGeneral()) {
			PanelGeneral pg = new PanelGeneral(this, unit, unit.soldier);
			setActive(pg);
		}
		else {
			PanelUnit pu = new PanelUnit(this, unit, unit.soldier);
			setActive(pu);
		}
//		PanelFaction pf = new PanelFaction(this, faction);
//		setActive(pf);
	}
	public void setActiveUnit(Soldier s) {
		if (s.isGeneral()) {
			PanelGeneral pg = new PanelGeneral(this, null, s);
			setActive(pg);
		}
		else {
			PanelUnit pu = new PanelUnit(this, null, s);
			setActive(pu);
		}
//		PanelFaction pf = new PanelFaction(this, faction);
//		setActive(pf);
	}
	public void setActiveCenter(Center center) {
		PanelCenter pc = new PanelCenter(this, center);
		kingdom.map.selectedCenter = center;
		setActive(pc);
	}
	public void setPanelInventory() {
		setActive(inventory);
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
		if (activePanel == null) return null;
		return activePanel.getCrest();
	}
	public TextureRegion getSecondCrest() {
		if (activePanel == null) return null;
		return activePanel.getSecondCrest();
	}
	public Soldier getSoldierInstead() {
		if (activePanel == null) return null;
		return activePanel.getSoldierInsteadOfCrest();
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
