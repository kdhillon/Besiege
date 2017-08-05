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
import kyle.game.besiege.Character;
import kyle.game.besiege.Crest;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.MiniMap;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.ArmyPlayer;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.battle.BattleActor;
import kyle.game.besiege.battle.OldBattle;
import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.Party;
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
	
	private MiniMap minimap;
	private BottomPanel bottom;
	
	private transient Panel activePanel;
	private transient Panel previousPanel;
	
	public Character character;
	public PanelParty party;
	public PanelUpgrades upgrades;
	public PanelInventory inventory;
		
	private boolean hardStay; // stay on current panel until set false
		
	// 5767 lines in Panels. \n[\s]* in *panel*.java
	// lets see how much we can reduce.
	
	public SidePanel(MapScreen mapScreen) {
		this.mapScreen = mapScreen;
		this.camera = (OrthographicCamera) mapScreen.getUICamera();	
		
		this.region = new TextureRegion(Assets.atlas.findRegion(textureRegion));

		bottom = new BottomPanel(this);
		this.addActor(bottom);
		
		minimap = new MiniMap(this);
		this.addActor(minimap);
		
		this.setHeight(camera.viewportHeight);
		hardStay = false;
	}

	public void initializePanels() {
		if (Soldier.WEAPON_NEEDED)
			inventory = new PanelInventory(this);
		party = new PanelParty(this, kingdom.getPlayer().party);
		upgrades = new PanelUpgrades(this, kingdom.getPlayer());
		this.setActive(party);
	}
	
	// For battlestage
	public void initializePanels(Party partyType) {
		if (Soldier.WEAPON_NEEDED)
			inventory = new PanelInventory(this);
		party = new PanelParty(this, partyType);
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
		super.draw(batch, parentAlpha);
		Table.drawDebug(getMapScreen().getUIStage());
	}
	
	public void clean() {
		this.clearChildren();
		this.addActor(this.minimap);
		this.addActor(this.bottom);
		this.previousPanel = null;
		this.activePanel = null;
	}
	
	public void returnToPrevious() {
		if (!hardStay) {
			setActive(previousPanel);
//			System.out.println("Returning to previous " + ("" + previousPanel == null) + " " + hardStay + " " + previousPanel.getClass());
//			if (previousPanel.getClass() == PanelParty.class) {
//				System.out.println(((PanelParty) previousPanel).party.getName());
//			}
		}
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
		System.out.println("Setting new active panel");
		if (newActivePanel == null) {
			System.out.println("Returning to null panel");
			setActive(party);
			return;
		}
		if (this.activePanel == newActivePanel && this.activePanel != party) {
			System.out.println("Resetting active panel");
//			setActive(/party);
			return;
		};
		if (newActivePanel == upgrades) upgrades.updateSoldierTable();
		if (newActivePanel.getClass() == PanelParty.class) {
			((PanelParty) newActivePanel).updateSoldierTable();
			System.out.println("just setactive");
		}
		if (newActivePanel.getClass() != PanelCenter.class && kingdom != null) {
			kingdom.map.selectedCenter = null;
		}
		
//		if (!hardStay) {
			this.removeActor(this.activePanel);
			this.previousPanel = this.activePanel;
			this.activePanel = newActivePanel;

		
			this.addActor(activePanel);
//		}s
	}
	public void setActiveDestination(Destination destination) {
		Destination.DestType type = destination.getType();
		if (type == Destination.DestType.POINT) { returnToPrevious();
		System.out.println("setting active destination to a point...");
		}
		else if (type == Destination.DestType.LOCATION) setActiveLocation((Location) destination);
		else if (type == Destination.DestType.ARMY) setActiveArmy((Army) destination);
		else if (type == Destination.DestType.BATTLE) setActiveBattle(((BattleActor) destination).getBattle());
	}
	public void setActiveArmy(Army army) {
		if (army.getParty().player) setActive(party);
		else {
			PanelParty pp = new PanelParty(this, army.party);
			setActive(pp);
		}
	}
	
	public void setActiveLocation(Location location) {
		if (location.panel == null) 
			location.panel = new PanelLocation(this, location);
		setActive(location.panel);
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
	}
	public void setActiveCenter(Center center) {
		PanelCenter pc = new PanelCenter(this, center);
		kingdom.map.selectedCenter = center;
		setActive(pc);
	}
	
	public void setPanelInventory() {
		if (Soldier.WEAPON_NEEDED) return;
		setActive(inventory);
	}
	
	public void setDefault(boolean force) {
		if (force) this.setHardStay(false);
			
		if (mapScreen.battle != null)
			setActiveBattle(mapScreen.getKingdom().getPlayer().getBattle());
		else setActive(party); // can change
	}
	public void press(int button) {
			if (button == 1)
				activePanel.button1();
			else if (button == 2)
				activePanel.button2();
			else if (button == 3)
				activePanel.button3();
			else if (button == 4)
				activePanel.button4();
			}
	public void setKingdom(Kingdom kingdom) {
		if (kingdom == null) {
			throw new java.lang.AssertionError();
		}
		this.kingdom = kingdom;
	}
	public Kingdom getKingdom() {
		return kingdom;
	}
	public ArmyPlayer getPlayer() {
		if (kingdom != null) {
			return kingdom.getPlayer();
		} else return null;
	}
	
	// it shuldn't really be a boolean, should be handled with previousPanel
	// if you click a panel, it becomes active and previous
//	public void setSoftStay(boolean b) {
//		System.out.println("setting soft stay");
//		this.previousPanel = this.activePanel;
//	}
//	
	public void setHardStay(boolean b) {
		System.out.println("setting hard stay");		
		hardStay = b;
	}
	public boolean getStay() {
		return hardStay;
	}
	public Crest getActiveCrest() {
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
