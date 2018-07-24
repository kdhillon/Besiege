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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.esotericsoftware.tablelayout.Cell;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.SoldierLabel;
import kyle.game.besiege.party.Subparty;

public class PanelParty extends Panel { // TODO organize soldier display to consolidate same-type soldiers
	private final float PAD = 10;
	private final float DESC_HEIGHT = 530;
	private final int r = 3;
	private SidePanel panel;
	public Party party;
	public Army army; // may be null

	private TopTable topTable;

    private SoldierTable soldierTabl;

	private LabelStyle ls;
	private LabelStyle lsG;	// wounded
	private LabelStyle lsY; // upgrade

	public boolean playerTouched;

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
		String subTitle = party.getGeneral().getName();
		if (party.army != null && party.army.isNoble()) {
            title = party.getGeneral().getName();
            subTitle = party.getName();
        }

		topTable = new TopTable(3);
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
			factionName = party.getFaction().name;
		}
		topTable.updateSubtitle(factionName, new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				setActiveFaction();
			}
		});
		
		if (!party.player) {
			topTable.updateSubtitle2(subTitle, null);
		}

		topTable.addSmallLabel("Size", "Size:");
		topTable.addSmallLabel("Atk", "Atk:");
		topTable.addSmallLabel("Morale", "Morale:");
		topTable.addSmallLabel("Def", "Def:");
		topTable.addSmallLabel("Wealth", "Wealth:");
		topTable.addSmallLabel("Spd", "Spd:");

        soldierTabl = new SoldierTable(party);
        topTable.add(soldierTabl).colspan(4).top().padTop(0).expandY();

        topTable.row();
		topTable.add().colspan(4).padBottom(PAD);
		topTable.row();

		topTable.row();

		this.addTopTable(topTable);
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
				topTable.updateSubtitle(army.getFactionName(), null);
				topTable.updateTitle(army.getName(), null);
			}

			if (army.getKingdom().getPlayer().isAtWar(army))
				topTable.updateSubtitle(army.getFactionName() + " (at war)", null);
			else
				topTable.updateSubtitle(army.getFactionName(), null);

			topTable.updateSubtitle3(army.getAction(), null);
			topTable.update("Morale", army.getMoraleString() + "");
			topTable.update("Wealth", "" + army.getParty().wealth);

			// set speed to be current travel speed, not playerPartyPanel speed
			topTable.update("Spd", Panel.format("" + army.getSpeed() * Army.SPEED_DISPLAY_FACTOR, 2));
		}
		topTable.update("Size", party.getHealthySize()+"/"+party.getTotalSize()); //+"/"+playerPartyPanel.getMaxSize());
		topTable.update("Atk", ""+ party.getAtk());
		topTable.update("Def", Panel.format(""+party.getAvgDef(), 2));
		
		//spd.setText(Panel.format(""+playerPartyPanel.getAvgSpd(), 2));

		if (army != null) {
			// don't call this every frame.
			if (party.updated) {
                soldierTabl.update();
				if (!army.isInBattle())
					party.updated = false;
			}
		}

		// minor leak is not here?
		super.act(delta);
	}

	public void setActiveFaction() {
		panel.setActiveFaction(army.getFaction());
	}

	public static void switchToPanel(Soldier s) {
		MapScreen.sidePanelReference.setActiveUnit(s);
	}

	public void centerCamera() {
		Camera camera = panel.getKingdom().getMapScreen().getCamera();
		//		camera.translate(new Vector2(army.getCenterX()-camera.position.x, army.getCenterY()-camera.position.y));
		camera.translate(new Vector3(army.getCenterX()-camera.position.x, army.getCenterY()-camera.position.y, 0));
	}

	@Override
	public void resize() {
        Cell cell = topTable.getCell(soldierTabl);
        cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
        soldierTabl = new SoldierTable(party);
        party.updated = true;
        soldierTabl.setHeight(panel.getHeight() - DESC_HEIGHT);
        cell.setWidget(soldierTabl);
        super.resize();
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
				panel.setDefault(true);
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
		if (army == null) return null;
		return army.getFaction().crest;
	}
}
