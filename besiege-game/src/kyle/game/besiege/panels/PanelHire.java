/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;

import kyle.game.besiege.Assets;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.SoldierLabel;

public class PanelHire extends Panel { // TODO incorporate "list.java" into this and improve layout (consolidate)
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 600;
	private final float OFFSET = 1;
	private final double COST_FACTOR = 2.85; // this*level = cost of soldier
	private final String upTexture = "grey-med9";
	private final String downTexture = "grey-dm9";
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private SidePanel panel;
	private Location location;
//	private Party toHire;
	
	private Table text;
	private Label title;
	private Label cityName;
	private Label playerParty;
	private Label playerWealth;

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
	private SoldierLabel hireLabel;
	private Button hireButton;
	
	private Soldier selected;
	
	private Table soldierTable;
	private ScrollPane soldierPane;
	
	private LabelStyle ls;
	
	public PanelHire(SidePanel panel, Location location) {
		this.panel = panel;
		this.location = location;
		this.addParentPanel(panel);
		
		LabelStyle lsBig = new LabelStyle();
		lsBig.font = Assets.pixel24;
		
		LabelStyle lsMB = new LabelStyle();
		lsMB.font = Assets.pixel22;
		
		LabelStyle lsMed = new LabelStyle();
		lsMed.font = Assets.pixel18;
		
		LabelStyle lsSmall = new LabelStyle();
		lsSmall.font = Assets.pixel14;
		
		LabelStyle lsMicro = new LabelStyle();
		lsMicro.font = Assets.pixel14;
		
		ls = new LabelStyle();
		ls.font = Assets.pixel16;
		
		ButtonStyle bs = new ButtonStyle();
		bs = new ButtonStyle();
		bs.up = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(upTexture), r,r,r,r));
		bs.down = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(downTexture), r,r,r,r));
		bs.pressedOffsetX = OFFSET;
		bs.pressedOffsetY = -OFFSET;	
		
		Label playerPartyC = new Label("Party: ", ls);
		Label playerWealthC = new Label("Wealth: ", ls);

		title = new Label("For Hire", lsBig);
		title.setAlignment(0,0);
		title.setWrap(true);
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
		cityName = new Label("", lsMed);
		cityName.setAlignment(0,0);
		cityName.setText(location.getName());
		playerParty = new Label("", ls);
		playerWealth = new Label("",ls);
		
		// Create text
		text = new Table();
		//text.debug();
		text.defaults().padTop(NEG).left();
		
		text.add(title).colspan(4).fillX().expandX().padBottom(0);
		text.row();
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.row();
		text.add(cityName).colspan(4).padBottom(MINI_PAD).fillX().expandX();
		text.row();
		text.add(playerWealthC).padLeft(MINI_PAD);
		text.add(playerWealth);
		text.add(playerPartyC).padLeft(PAD);
		text.add(playerParty);
		text.row();
		
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
		hireLabel = new SoldierLabel("", ls, null);
		hireLabel.setTouchable(Touchable.disabled);
		hireButton = new Button(bs);
		hireButton.add(hireLabel).padLeft(MINI_PAD).padRight(MINI_PAD);
		hireButton.addListener(new ClickListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				hireSelected();
			}
		});

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
		stats.row(); 
		stats.add(hireButton).colspan(4).center().padTop(PAD);

		//stats.debug();

		text.add(stats).colspan(4).padTop(PAD);

		this.addTopTable(text);
		this.setButton(2, "Hire All");
		this.setButton(4, "Back");
		
		updateSoldierTable();
	}

	@Override
	public void act(float delta) {
		playerWealth.setText("" + panel.getKingdom().getPlayer().getParty().wealth);
		playerParty.setText("" + panel.getKingdom().getPlayer().getPartyInfo());

		if (location.getToHire() != null && location.getToHire().getHealthySize() == 0) getButton(2).setDisabled(true);
		else getButton(2).setDisabled(false);
		
		super.act(delta);
	}
	
	public void updateSoldierTable() {
		soldierTable.clear();
		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
		soldierTable.add().colspan(2).width(SidePanel.WIDTH - PAD*2).padTop(0);
		soldierTable.row();
//		System.out.println(location.getName());
		if (location.getToHire() == null) return;
		for (Soldier s : location.getToHire().getHealthy()) {
			SoldierLabel name = new SoldierLabel(s.getTypeName(), ls, s);
			if (selected == s) name.setColor(Color.YELLOW);
			name.addListener(new ClickListener() {
				@Override
				public boolean touchDown(InputEvent event, float x,
						float y, int pointer, int button) {
					return true;
				}
				public void touchUp(InputEvent event, float x, float y,
						int pointer, int button) {
					select(((SoldierLabel) event.getTarget()).soldier);
//					((SoldierLabel) event.getTarget()).setColor(Color.YELLOW);
				}
			});
			soldierTable.add(name).left();
			Label count = new Label(s.level +"", ls);
//			soldierTable.add(count).right();
			soldierTable.add().right();
			soldierTable.row();
		}
		if (location.getToHire().getHealthySize() == 0) {
			Label none = new Label("No troops available for hire", ls);
			none.setWrap(true);
			none.setAlignment(0,0);
			soldierTable.add(none).colspan(2).width(SidePanel.WIDTH - PAD*6).padTop(PAD*4);

			soldierTable.row();
		}
	}
	
	public void select(Soldier s) {
		this.selected = s;

		stats.setVisible(true);
		nameS.setText(s.getTypeName() + "");
		levelS.setText(s.level + "");
		expS.setText(s.exp + "");
		nextS.setText(s.next + "");
//		if (s.getBonusAtk() >= 0)
//			atkS.setText(s.getAtk() + " (" + s.baseAtk + "+" + s.getBonusAtk() + ")");
//		else 
//			atkS.setText(s.getAtk() + " (" + s.baseAtk + s.getBonusAtk() + ")");
//		if (s.getBonusDef() >= 0)
//			defS.setText(s.getDef() + " (" + s.baseDef + "+" + s.getBonusDef() + ")");
//		else 
//			defS.setText(s.getDef() + " (" + s.baseDef + s.getBonusDef() + ")");
//		if (s.getBonusSpd() >= 0)
//			spdS.setText(s.getSpd() + " (" + s.baseSpd + "+" + s.getBonusSpd() + ")");
//		else 
//			spdS.setText(s.getSpd() + " (" + s.baseSpd + s.getBonusSpd() + ")");
		weaponS.setText(s.unitType.melee.name);
		equipmentS.setText(s.unitType.armor.name);

		hireLabel.soldier = s;
		int cost = s.getBuyCost();
//		int cost = (int) (s.level*COST_FACTOR);
		hireLabel.setText("Hire " + s.getTypeName() + " (" + cost + ")");
		
		updateSoldierTable();
	}

	public void deselect() {
		this.selected = null;
		stats.setVisible(false);
		updateSoldierTable();
	} 
	
	public void hireSelected() {
		if (location.hire(panel.getKingdom().getPlayer().getParty(), selected)) { // only if successfully hires
			String name = selected.getTypeName();
			BottomPanel.log("Hired " + name);
			deselect();
		}
		else BottomPanel.log("Can't afford " + selected.getTypeName());
		updateSoldierTable();
	}
	
	public void hireAll() { //Fixed iterator problem
		location.getToHire().getHealthy().shrink();
		Array<Soldier> soldiers = location.getToHire().getHealthyCopy();
		soldiers.reverse(); // cheapest first!
		for (Soldier s : soldiers) {
			if (location.hire(panel.getKingdom().getPlayer().getParty(), s)) { // only if successfully hires
				String name = s.getTypeName();
				BottomPanel.log("Hired " + name);
			}
			else {
				BottomPanel.log("Can't afford " + s.getTypeName());
				break;
			}
		}
		updateSoldierTable();
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
		if (stats.isVisible())
			hireSelected();
	}
	@Override
	public void button2() {
		hireAll();
	}
	
	@Override
	public void button4() {
		panel.returnToPrevious();
	}
	@Override
	public TextureRegion getCrest() {
		if (selected == null)
			return location.getFaction().crest;
		else return null;
	}
	@Override
	public Soldier getSoldierInsteadOfCrest() {
		if (selected != null) return selected;
		return null;
	}
}
