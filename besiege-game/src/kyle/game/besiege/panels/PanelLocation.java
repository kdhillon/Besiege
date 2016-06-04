/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import kyle.game.besiege.Assets;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.esotericsoftware.tablelayout.Cell;

public class PanelLocation extends Panel { // TODO organize soldier display to consolidate same-type soldiers
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 530;
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private SidePanel panel;
	private Location location;
	private Party party;
	
	private Table text;
	private Label title;
	private Label faction;
	private Label type;
	
	private Label garrisonSize;
	private Label population;
	private Label wealth;

	private Label garrisonC;
	private Label emptyC;
	private Label prisonersC;
	
	private Table stats;
	private Label nameS;
	private Label levelS;
	private Label expS;
	private Label nextS;
	private Label atkS;
	private Label defS;
	private Label spdS;
	private Label weaponS;
	private Label equipmentS;
	
	
	private Table soldierTable;
	private ScrollPane soldierPane;
	
	private LabelStyle ls;
	private LabelStyle lsMed;
	private LabelStyle lsG;
	
	private boolean playerIn;
	private PanelHire panelHire;
	private boolean playerTouched;
	private boolean playerWaiting;
	private boolean playerBesieging;

	public PanelLocation(SidePanel panel, Location location) {
		this.panel = panel;
		this.location = location;
		this.party = location.getParty();
		this.addParentPanel(panel);
		this.playerTouched = false;
		
		LabelStyle lsBig = new LabelStyle();
		lsBig.font = Assets.pixel22;
		
		lsMed = new LabelStyle();
		lsMed.font = Assets.pixel18;
		
		ls = new LabelStyle();
		ls.font = Assets.pixel16;
		
		lsG = new LabelStyle();
		lsG.font = Assets.pixel16;
		lsG.fontColor = Color.GRAY;
		
		Label garrisonSizeC = new Label("Garrison:", ls);
		Label populationC = new Label("Pop:",ls);
		Label wealthC = new Label("Wealth:",ls);
		
		title = new Label(location.getName(), lsBig);
		title.setAlignment(0,0);
//		title.setWrap(true); // wrapping messes up click listeners... WTF?
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
		title.addListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				centerCamera();
			}
		});
		faction = new Label("", ls);
		faction.setAlignment(0,0);
		faction.setText(location.getFactionName());
		type = new Label("",ls);
		type.setAlignment(Align.center); // large city, village, etc
		type.setText(location.getTypeStr());
		
		garrisonSize = new Label("", ls);
		garrisonSize.setWrap(false);
		population = new Label("", ls);
		wealth = new Label("", ls);

		garrisonC = new Label("Garrison", ls);
		emptyC = new Label("No troops garrisoned!",ls);
		prisonersC = new Label("Captured", ls);
		
		// Create text
		text = new Table();
//		text.debug();
		text.defaults().padTop(NEG).left();
		faction.addListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				setActiveFaction();
			}
		});
		text.add(title).colspan(4).center().padBottom(0);
		text.row();
		text.add().width((SidePanel.WIDTH-PAD*2)/4);
		text.add().width((SidePanel.WIDTH-PAD*2)/4);
		text.add().width((SidePanel.WIDTH-PAD*2)/4);
		text.add().width((SidePanel.WIDTH-PAD*2)/4);
		text.row();
		text.add(faction).colspan(4).fillX().expandX();
		text.row();
		text.add(type).colspan(4).fillX().expandX();
		text.row();
		text.add(garrisonSizeC).colspan(2).padLeft(MINI_PAD);
		text.add(garrisonSize).colspan(2).center();
		text.row();
		text.add(populationC).padLeft(MINI_PAD);
		text.add(population).center();
		text.add(wealthC).padLeft(PAD);
		text.add(wealth).center();

		soldierTable = new Table();
		//soldierTable.debug();
		soldierTable.defaults().padTop(NEG);
		soldierTable.top();
		soldierTable.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));
		text.row();
		text.add().colspan(4).padBottom(PAD);
		text.row();
		
		soldierPane = new ScrollPane(soldierTable);
		soldierPane.setScrollbarsOnTop(true);
		soldierPane.setFadeScrollBars(false);
		text.add(soldierPane).colspan(4).top().padTop(0);
		
		text.row();
		
		// Soldier's stats
		stats = new Table();
		stats.setVisible(false);
		
		Label levelSC = new Label("Level:", ls);
		Label expSC = new Label("Exp:",ls);
		Label nextSC = new Label("Next:",ls);
		Label atkSC = new Label("Atk:", ls);
		Label defSC = new Label("Def:", ls);
		Label spdSC = new Label("Spd:", ls); 
		Label weaponSC = new Label("Weapon: ", ls);
		Label equipmentSC = new Label("Armor: ", ls);

		nameS = new Label("", ls);
		nameS.setAlignment(0,0);
		levelS = new Label("", ls);
		expS = new Label("", ls);
		nextS = new Label("", ls);
		atkS = new Label("" ,ls);
		defS = new Label("", ls);
		spdS = new Label("", ls);
		weaponS = new Label("", ls);
		equipmentS = new Label("", ls);
		
		stats.defaults().left().padTop(NEG);
		stats.add(nameS).colspan(4).width(SidePanel.WIDTH-PAD*2).fillX().expandX().padBottom(MINI_PAD);
		stats.row();
		stats.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		stats.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		stats.row();
		stats.add(levelSC).padLeft(MINI_PAD);
		stats.add(levelS);
		stats.add(atkSC).padLeft(PAD);
		stats.add(atkS);
		stats.row();
		stats.add(expSC).padLeft(MINI_PAD);
		stats.add(expS);
		stats.add(defSC).padLeft(PAD);
		stats.add(defS);
		stats.row();
		stats.add(nextSC).padLeft(MINI_PAD);
		stats.add(nextS);
		stats.add(spdSC).padLeft(PAD);
		stats.add(spdS);
		stats.row();
		stats.add(weaponSC).colspan(2).padLeft(MINI_PAD).padTop(0);
		stats.add(weaponS).colspan(2).padTop(0);
		stats.row();
		stats.add(equipmentSC).colspan(2).padLeft(MINI_PAD).padTop(0).top();
		stats.add(equipmentS).colspan(2).padTop(0);
		
		//stats.debug();
		
		text.add(stats).colspan(4).padTop(PAD);

		text.padLeft(MINI_PAD);
		this.addTopTable(text);

		playerIn = false;
//		this.hireMode = false;
		this.panelHire = new PanelHire(panel, location);
		
		this.setButton(4, "Back");
	}
	
	@Override
	public void act(float delta) {
		// hostile player touches
		if (location.hostilePlayerTouched && !playerTouched) {
			if (location.underSiege()) {
				if (location.isVillage())
					setButton(1, "Continue Raid");
				else setButton(1, "Resume Siege");
			}
			else if (!location.underSiege()) {
				if (location.isVillage())
					setButton(1, "Raid");
				else setButton(1, "Besiege");
			}
			setButton(2, "Withdraw");
			setButton(4, null);
			playerTouched = true;
		}
		// hostile player leaves
		else if (!location.hostilePlayerTouched && playerTouched && !location.playerBesieging)  {
			setButton(1, null);
			setButton(2, null);
			setButton(4, "Back");
			playerTouched = false;
		}
		else if (location.playerBesieging && !playerBesieging) {
			 // turn on siege panel
			setButton(1, "Charge!");
			setButton(2, "Wait");
			setButton(4, "Withdraw");
			playerBesieging = true;
//			System.out.println("siege panel on");
		}
		else if (!location.playerBesieging && playerBesieging) {
			// turn off siege panel
			setButton(1, null);
			setButton(2, null);
			setButton(4, "Back");
			playerBesieging = false;
//			System.out.println("siege panel off");
		}
		// friendly player touches
		else if (location.playerIn && !playerIn) {
			if (location.toHire != null && location.toHire.getHealthySize() > 0) 
				setButton(2, "Hire");
			playerIn = true;
		}
		// friendly player leaves
		else if (!location.playerIn && playerIn){
			setButton(1, null);
			setButton(2, null);
			playerIn = false;
		}
		// if hostile player is waiting at a siege
		else if (location.playerBesieging && playerBesieging) {
			// start wait
			if (location.playerWaiting && playerWaiting) {
				setButton(1, null);
				setButton(2, null);
				setButton(4, "Stop");
				playerWaiting = false;
//				System.out.println("setting to null");
			}
			// stop wait
			else if (!location.playerWaiting && !playerWaiting) {
				setButton(1, "Charge!");
				setButton(2, "Wait");
				setButton(4, "Withdraw");
				playerWaiting = true;
//				System.out.println("setting to rest");
			}
		}
		// if friendly player is inside
		else if (location.playerIn && playerIn) {
			//start Wait
			if (location.playerWaiting && playerWaiting) {
				setButton(1, null);
				setButton(4, "Stop");
				playerWaiting = false;
			}
			//stop Wait
			else if (!location.playerWaiting && !playerWaiting) {
				setButton(1, "Rest");
				setButton(4, "Back");
				playerWaiting = true;
			}
		}
//		else if (location.underSiege() && location.getKingdom().getPlayer().isInSiege() && location.getKingdom().getPlayer().getSiege().location == location) {

		if (!location.ruin) {
			String garrStr = party.getHealthySize() + "";

			int totalGarr = 0;
			for (Army a: location.getGarrisoned()) {
				// don't show passive armies in garrison
				if (a.passive) continue;
				//			garrStr += "+" + a.getParty().getHealthySize();
				totalGarr += a.getParty().getHealthySize();
			}
			//		if (totalGarr > 0) garrStr += "+" + totalGarr;
			if (totalGarr > 0) garrStr = totalGarr + location.garrison.getParty().getHealthySize() + " (" + garrStr + "+" + totalGarr + ")";
			garrisonSize.setText(garrStr);

			population.setText((int) location.getPop() + "");
			wealth.setText("" + party.wealth);
		}
		else {
			String garrStr = "";

			int totalGarr = 0;
			for (Army a: location.getGarrisoned()) {
				//			garrStr += "+" + a.getParty().getHealthySize();
				// don't show passive armies in garrison
				if (a.passive) continue;
				totalGarr += a.getParty().getHealthySize();
			}
			//		if (totalGarr > 0) garrStr += "+" + totalGarr;
			if (totalGarr > 0) garrStr = totalGarr + " (" + garrStr + "+" + totalGarr + ")";
			garrisonSize.setText(garrStr);

			population.setText((int) location.getPop() + "");
			wealth.setText("0");
		}
		
		if (location.underSiege())
			faction.setText("Under Siege!");
		else {
			if (location.getKingdom().getPlayer().isAtWar(location)) faction.setText(location.getFactionName() + " (at war)");
			else faction.setText(location.getFactionName());
		}
		
		updateSoldierTable();
		super.act(delta);
	}
	
	public void updateSoldierTable() {
		soldierTable.clear(); // clearing the table is a problem right now. it hides the scroll bar and prevents click-drag scrolling
		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
		
		garrisonC.setAlignment(Align.center);
		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
		soldierTable.add(garrisonC).colspan(2);
		soldierTable.row();
		if (party == null || party.getTotalSize() == 0) {
			emptyC.setAlignment(Align.center);
			soldierTable.add(emptyC).colspan(2).center().width(SidePanel.WIDTH - PAD*2);
			soldierTable.row();
		}
		else {
			PanelParty.updateTableWithTypes(soldierTable, party.getConsolHealthy(), ls);
			PanelParty.updateTableWithTypes(soldierTable, party.getConsolWounded(), lsG);
		}
		
		if (!location.ruin) {
			if (party != null && party.getPrisoners().size > 0)
				prisonersC.setText("Captured");
			else prisonersC.setText("");
			prisonersC.setAlignment(0,0);
			soldierTable.add(prisonersC).colspan(2).width(SidePanel.WIDTH - PAD*2).expandX().fillX().padTop(0);
			soldierTable.row();
			PanelParty.updateTableWithTypes(soldierTable, party.getConsolPrisoners(), ls);
		}
		// For Garrisoned armies
		for (Army a : location.getGarrisoned()) {
			// don't show passive armies in garrison
			if (a.passive) continue;
			Party garrParty = a.getParty();
			
			// TODO fix this memory leak
			Label partyName = new Label(a.getName(), ls);
			partyName.setAlignment(Align.center);
			soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
			soldierTable.add(partyName).colspan(2);
			soldierTable.row();
			
			PanelParty.updateTableWithTypes(soldierTable, garrParty.getConsolHealthy(), ls);
			PanelParty.updateTableWithTypes(soldierTable, garrParty.getConsolWounded(), lsG);
			
			
			// TODO fix this memory leak
			Label garrPrisonersC;
			if (garrParty.getPrisoners().size > 0)
				garrPrisonersC = new Label("Captured", ls);
			else garrPrisonersC = new Label("",ls);
			garrPrisonersC.setAlignment(0,0);
			soldierTable.add(garrPrisonersC).colspan(2).width(SidePanel.WIDTH - PAD*2).expandX().fillX().padTop(0);
			soldierTable.row();
			
			PanelParty.updateTableWithTypes(soldierTable, garrParty.getConsolPrisoners(), ls);
		}
	}
	
	public void setStats(Soldier s) {
		stats.setVisible(true);
		nameS.setText(s.getTypeName() + "");
		levelS.setText(s.level + "");
		expS.setText(s.exp + "");
		nextS.setText(s.next + "");
		if (s.getBonusAtk() >= 0)
			atkS.setText(s.getAtk() + " (" + s.baseAtk + "+" + s.getBonusAtk() + ")");
		else 
			atkS.setText(s.getAtk() + " (" + s.baseAtk + s.getBonusAtk() + ")");
		if (s.getBonusDef() >= 0)
			defS.setText(s.getDef() + " (" + s.baseDef + "+" + s.getBonusDef() + ")");
		else 
			defS.setText(s.getDef() + " (" + s.baseDef + s.getBonusDef() + ")");
		if (s.getBonusSpd() >= 0)
			spdS.setText(s.getSpd() + " (" + s.baseSpd + "+" + s.getBonusSpd() + ")");
		else 
			spdS.setText(s.getSpd() + " (" + s.baseSpd + s.getBonusSpd() + ")");
		weaponS.setText(s.unitType.melee.name);
	}
	
	public void clearStats() {
		stats.setVisible(false);
	}
	
	public void setActiveFaction() {
		panel.setActiveFaction(location.getFaction());
	}
	public void centerCamera() {
		Camera camera = panel.getKingdom().getMapScreen().getCamera();
//		camera.translate(new Vector2(location.getCenterX()-camera.position.x, location.getCenterY()-camera.position.y));
		camera.translate(new Vector3(location.getCenterX()-camera.position.x, location.getCenterY()-camera.position.y, 0));
	}
	
	@Override
	public void resize() { // problem with getting scroll bar to appear...
		Cell cell = text.getCell(soldierPane);
		cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
		soldierPane = new ScrollPane(soldierTable);
		soldierPane.setHeight(panel.getHeight() - DESC_HEIGHT);
		soldierPane.setScrollingDisabled(true, false);
		soldierPane.setFadeScrollBars(false);
		soldierPane.setScrollbarsOnTop(true);
		cell.setWidget(soldierPane);
		super.resize();
	}
	
	@Override
	public void button1() {
		if (this.getButton(1).isVisible()) {
			if (playerIn) {
				location.startWait();
			}
			else if (playerBesieging && !location.playerWaiting) {
				BottomPanel.log("charge!");
				location.getSiege().attack();
				System.out.println("ATTACKING ");
			}
			else { // besiege/raid
				panel.setStay(false);
				if (!location.underSiege()) {
					if (location.isVillage()) {
						panel.getKingdom().getPlayer().raid((Village) location);
					}
					else {
						BottomPanel.log("Besieging " + location.getName());
						panel.getKingdom().getPlayer().besiege(location);
					}
				}
				else {
//					if (location.isVillage()) {
//						panel.getKingdom().getPlayer().raid((Village) location);
//					}
//					else {
						BottomPanel.log("Resuming siege of" + location.getName());
						location.getSiege().add(panel.getKingdom().getPlayer());
//					}
				}
			}
		}
	}
	@Override
	public void button2() {
		if (this.getButton(2).isVisible()) {
			if (playerIn) {
				panel.setActive(this.panelHire);
			}
			else if (playerBesieging) {
				BottomPanel.log("waiting");
				location.startWait();
			}
			else {
				panel.setStay(false);
				panel.setDefault();		
			}
		}
	}
	@Override
	public void button3() {
		
	}
	@Override
	public void button4() {
		if (location.playerWaiting) {
			location.stopWait();
		}
		else if (playerBesieging) {
			location.getKingdom().getPlayer().leaveSiege();
			BottomPanel.log("Withdraw!");
		}
		else {
			panel.setDefault();
		}
	}
	
	@Override
	public TextureRegion getCrest() {
		if (location.getFaction() == null) return null;
		return location.getFaction().crest;
	}
}
