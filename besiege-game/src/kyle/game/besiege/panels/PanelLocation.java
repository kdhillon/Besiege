/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
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

import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.party.Party;

public class PanelLocation extends Panel { // TODO organize soldier display to consolidate same-type soldiers
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 300;
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private SidePanel panel;
	public Location location;
	
	private TopTable topTable;
	
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
	
	private Label garrisonC;
	private Label emptyC;
	private Label nullC;
	private Label prisonersC;
	
	public PanelLocation(SidePanel panel, Location location) {
		this.panel = panel;
		this.location = location;
		
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
		
		topTable = new TopTable(2);
		topTable.updateTitle(location.getName(), new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				centerCamera();
			}
		});
		topTable.updateSubtitle(location.getFactionName(), new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				setActiveFaction();
			}
		});
		topTable.updateSubtitle2(location.getTypeStr(),  null);
		
		topTable.addBigLabel("Garrison", "Garrison:");
		topTable.addSmallLabel("Pop", "Pop:");
		topTable.addSmallLabel("Wealth", "Pop:");

		garrisonC = new Label("Garrison", ls);
		emptyC = new Label("No troops garrisoned!",ls);
		nullC = new Label("Garrison is null!",ls);
		prisonersC = new Label("Captured", ls);

		soldierTable = new Table();

		soldierTable.defaults().padTop(NEG);
		soldierTable.top();
		soldierTable.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));
	
		soldierPane = new ScrollPane(soldierTable);
		soldierPane.setScrollbarsOnTop(true);
		soldierPane.setFadeScrollBars(false);
		topTable.add(soldierPane).colspan(4).top().padTop(0);
		
		topTable.row();
		
		//stats.debug();
		topTable.padLeft(MINI_PAD);
		this.addTopTable(topTable);

		playerIn = false;
//		this.hireMode = false;
		this.panelHire = new PanelHire(panel, location);
				
		location.needsUpdate = true;
		System.out.println("just created new panellocation");
		
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
			String garrStr = getParty().getHealthySize() + "";

			int totalGarr = 0;
			for (Army a: location.getGarrisoned()) {
				// don't show passive armies in garrison
				if (a.passive) continue;
				//			garrStr += "+" + a.getParty().getHealthySize();
				totalGarr += a.getParty().getHealthySize();
			}
			//		if (totalGarr > 0) garrStr += "+" + totalGarr;
			if (totalGarr > 0) garrStr = totalGarr + location.garrison.getParty().getHealthySize() + " (" + garrStr + "+" + totalGarr + ")";
			topTable.update("Garrison", garrStr);

			topTable.update("Pop", (int) location.getPop() + "");
			topTable.update("Wealth", ""+ location.getWealth());
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
			topTable.update("Garrison", garrStr);

			topTable.update("Pop", (int) location.getPop() + "");
			topTable.update("Wealth", "0");
		}
		
		if (location.underSiege())
			topTable.updateSubtitle("Under Siege!", null);
		else {
			if (location.getKingdom().getPlayer().isAtWar(location)) topTable.updateSubtitle(location.getFactionName() + " (at war)", null);
			else topTable.updateSubtitle(location.getFactionName(), null);
		}
		
		if (location.needsUpdate)
			updateSoldierTable();
		super.act(delta);
	}
	
	public void updateSoldierTable() {
		location.needsUpdate = false;
		soldierTable.clear(); // clearing the table is a problem right now. it hides the scroll bar and prevents click-drag scrolling
		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
		
		garrisonC.setAlignment(Align.center);
		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
		soldierTable.add(garrisonC).colspan(2);
		soldierTable.row();
		if (getParty() == null) {
			emptyC.setAlignment(Align.center);
			soldierTable.add(nullC).colspan(2).center().width(SidePanel.WIDTH - PAD*2);
			soldierTable.row();
		}
		else if (getParty().getTotalSize() == 0) {
			emptyC.setAlignment(Align.center);
			soldierTable.add(emptyC).colspan(2).center().width(SidePanel.WIDTH - PAD*2);
			soldierTable.row();
		}
		else {
			
			PanelParty.updateTableWithParty(soldierTable, getParty(), ls, lsG);
//			PanelParty.updateTableWithTypes(soldierTable, party.getConsolHealthy(), ls);
//			PanelParty.updateTableWithTypes(soldierTable, party.getConsolWounded(), lsG);
		}
		
		if (!location.ruin) {
			if (getParty() != null && getParty().getPrisoners().size > 0)
				prisonersC.setText("Captured");
			else prisonersC.setText("");
			prisonersC.setAlignment(0,0);
			soldierTable.add(prisonersC).colspan(2).width(SidePanel.WIDTH - PAD*2).expandX().fillX().padTop(0);
			soldierTable.row();
			PanelParty.updateTableWithTypes(soldierTable, getParty().getConsolPrisoners(), ls);
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
			
			PanelParty.updateTableWithParty(soldierTable, garrParty, ls, lsG);

//			PanelParty.updateTableWithTypes(soldierTable, garrParty.getConsolHealthy(), ls);
//			PanelParty.updateTableWithTypes(soldierTable, garrParty.getConsolWounded(), lsG);
			
			
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
	
	public Party getParty() {
		return this.location.getParty();
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
		Cell cell = topTable.getCell(soldierPane);
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
				
				// TODO temp fix
				if (location.getSiege() == null) {
					playerBesieging = false;
					return; 
				}
				location.getSiege().attack();
				System.out.println("ATTACKING ");
			}
			else { // besiege/raid
				panel.setHardStay(false);
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
				panel.setHardStay(false);
				panel.setDefault(true);		
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
			panel.setDefault(true);
		}
	}
	
	@Override
	public Crest getCrest() {
		if (location.getFaction() == null) return null;
		return location.getFaction().crest;
	}
}
