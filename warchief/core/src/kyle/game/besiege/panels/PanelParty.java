/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

public class PanelParty extends Panel { // TODO organize soldier display to consolidate same-type soldiers
	private final float PAD = 10;
	private final float DESC_HEIGHT = 530;
	private final int r = 3;
	private SidePanel panel;
	public Party party;
	public Army army; // may be null

	private TopTable topTable;

    private SoldierTable soldierTable;
    private TopTable mainTopTable;

	private LabelStyle ls;
	private LabelStyle lsG;	// wounded
	private LabelStyle lsY; // upgrade

	public boolean playerTouched;

	private Soldier dragging;

	public PanelParty(SidePanel panel, Party party) {
		this.panel = panel;
		this.party = party;
		this.army = party.army;
		this.addParentPanel(panel);

		LabelStyle lsBig = new LabelStyle();
		lsBig.font = Assets.pixel24;

		LabelStyle lsFaction = new LabelStyle();
		lsFaction.font = Assets.pixel18;

		ls = new LabelStyle();
		ls.font = Assets.pixel16;

		lsG = new LabelStyle();
		lsG.font = Assets.pixel16;
		lsG.fontColor = Color.GRAY;

		lsY = new LabelStyle();
		lsY.font = Assets.pixel16;
		lsY.fontColor = Color.YELLOW;

		String title = party.getName();
		String subTitle = ""; // don't have a subtitle for a non-noble
//		if (party.army != null && party.army.isNoble()) {
//            title = party.getGeneral().getName();
//            subTitle = party.getName();
//        }

		topTable = new TopTable();
		topTable.updateTitle(title, new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				centerCamera();
			}
		});
		String factionName = "default faction";
		if (party.getFaction() != null) {
			factionName = party.getFaction().getOfficialName();
		}
		topTable.addSubtitle("factionname", factionName, new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				setActiveFaction();
			}
		});
		
		if (!party.player && army.isNoble()) {
			topTable.addSubtitle("subtitle", subTitle, null);
		}

		if (army != null)
			topTable.addSubtitle("action", army.getAction(), null);

		topTable.addSmallLabel("Size", "Size:");
		topTable.addSmallLabel("Atk", "Atk:");
		topTable.addSmallLabel("Morale", "Morale:");
		topTable.addSmallLabel("Def", "Def:");
		topTable.addSmallLabel("Wealth", "Wealth:");
		topTable.addSmallLabel("Spd", "Spd:");

//        topTable.add(soldierTable).colspan(4).top().padTop(0).expandY();

		this.addTopTable(topTable);
		this.mainTopTable = topTable;

		soldierTable = new SoldierTable(this, party);
		this.addSoldierTable(soldierTable);
//		text.debug();

		if (this.army != null && this.army == panel.getPlayer()) {
			this.setButton(1, "Upgrades");

			if (Soldier.WEAPON_NEEDED)
				this.setButton(2, "Inventory");
//			this.setButton(3, "Character");
			this.setButton(4, "Save");
		}		
		playerTouched = false;
		
		System.out.println("created new panelparty");

		if (!this.party.player) this.setButton(4, "Back");
	}

	@Override
	public void act(float delta) {
		if (army != null) {
			if (army.playerTouched && !playerTouched) {
				setButton(1, "Attack!");
				setButton(2, "Withdraw");
				setButton(4, null);
				playerTouched = true;
			} else if (!army.playerTouched && playerTouched) {
				setButton(1, null);
				setButton(2, null);
				setButton(4, "Back");
				playerTouched = false;
			}
			if (party.player) {
				mainTopTable.update("factionname", army.getFactionName(), null);
				mainTopTable.updateTitle(army.getName(), null);
			}

			if (!army.player && army.getKingdom().getPlayer().isAtWar(army))
				mainTopTable.update("factionname", army.getFactionName() + " (at war)", null);
			else
				mainTopTable.update("factionname", army.getFactionName(), null);

			mainTopTable.update("action", army.getAction(), null);
            mainTopTable.update("Morale", army.getMoraleString() + "");
			mainTopTable.update("Wealth", "" + army.getParty().wealth);

			// set speed to be current travel speed, not playerPartyPanel speed
			mainTopTable.update("Spd", Panel.format("" + army.getSpeed() * Army.SPEED_DISPLAY_FACTOR, 2));
		}
		mainTopTable.update("Size", getSizeString()); //+"/"+playerPartyPanel.getMaxSize());
//		mainTopTable.update("Captives", party.getHealthySize()+"/"+party.getTotalSize()); //+"/"+playerPartyPanel.getMaxSize());

		mainTopTable.update("Atk", ""+ party.getAtk());
		mainTopTable.update("Def", Panel.format(""+party.getAvgDef(), 2));
		
		//spd.setText(Panel.format(""+playerPartyPanel.getAvgSpd(), 2));

		if (army != null) {
			// don't call this every frame.
			if (party.updated) {
                soldierTable.update();
				if (!army.isInBattle())
					party.updated = false;
			}
		}

		// minor leak is not here?
		super.act(delta);
	}

	public String getSizeString() {
		String s = party.getHealthySize()+"/"+party.getTotalSize();
		if (party.getHealthySize() == party.getTotalSize()) {
			s = party.getHealthySize()+"";
		}
		if (army != null && party.getPrisoners().size > 0) {
			return s + "+" + party.getPrisoners().size;
		}
		return s;
	}

	public void setActiveFaction() {
		panel.setActiveFaction(army.getFaction());
	}

	public static void switchToPanel(Soldier s) {
		MapScreen.sidePanelReference.setActiveUnit(s);
	}

	public void centerCamera() {
		army.getKingdom().getMapScreen().centerOn(army);
	}

	public void notifySelect(Soldier s) {
		// a soldier was just selected
		// Want to replace this toptable with the unit's toptable
		if (topTable == mainTopTable) {
			topTable = PanelUnit.getTopTable(s, null, ls);
			updateTopTable(mainTopTable, topTable);
		}
	}

	public void notifyDeselect() {
		if (topTable != mainTopTable) {
			updateTopTable(topTable, mainTopTable);
			topTable = mainTopTable;
		}
	}

	@Override
	public void resize() {
        super.resize();
	}

	public void deselect() {
		soldierTable.deselect();
	}

	@Override
	public void button1() {
		if (this.getButton(1).isVisible()) {
			if (party == panel.getKingdom().getPlayer().getParty()) {
				panel.upgrades.update();
				panel.setActive(panel.upgrades);
			}
			else {
				panel.setHardStay(false); // allows changing panel to panelbattle (attack)
				panel.getKingdom().getPlayer().attack((Army) panel.getKingdom().getPlayer().getTarget());
			}
		}
	}

	@Override
	public void button2() {
		if (this.getButton(2).isVisible()) {
			if (party == panel.getKingdom().getPlayer().getParty() && Soldier.WEAPON_NEEDED) {
				panel.setPanelInventory();
			}
			else {
				panel.setHardStay(false); // withdraw
				panel.setDefault();
			}
		}
	}
	@Override
	public void button3() {
//		if (this.getButton(3).isVisible()) {
//			if (playerPartyPanel == panel.getKingdom().getPlayer().getParty()) {
////				panel.setActive(panel.character);
//			}
//		}
	}
	@Override
	public void button4() {
		if (party.player) {
			this.panel.getMapScreen().save();
		} else 
			panel.returnToPrevious(true);
	}

	@Override
	public Crest getCrest() {
		if (army == null || army.getFaction() == null) return null;
		return army.getFaction().crest;
	}

	@Override
	public Soldier getSoldierInsteadOfCrest() {
		if (soldierTable.selected != null) {
			return soldierTable.selected;
		}
		return null;
	}
}
