/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import kyle.game.besiege.Assets;
import kyle.game.besiege.SidePanel;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.SoldierLabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;

public class PanelParty extends Panel { // TODO organize soldier display to consolidate same-type soldiers
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 530;
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private SidePanel panel;
	private Army army;
	private Party party;
	
	private Table text;
	private Label title;
	private Label faction;
	private Label action;
	
	private Label size;
	private Label morale;
	private Label money;
	private Label atk;
	private Label def;
	private Label spd;
	
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
	private LabelStyle lsG;	// wounded
	private LabelStyle lsY; // upgrade
	
	public boolean playerTouched;
	
	public PanelParty(SidePanel panel, Army army) {
		this.panel = panel;
		this.army = army;
		this.party = army.getParty();
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
		
		Label sizeC = new Label("Size:", ls);
		Label moraleC = new Label("Morale:",ls);
		Label moneyC = new Label("Wealth:",ls);
		Label atkC = new Label("Atk:", ls);
		Label defC = new Label("Def:", ls);
		Label spdC = new Label("Spd:", ls); 

		title = new Label("", lsBig);
		title.setAlignment(0,0);
		title.setWrap(true);
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
		faction = new Label("", lsFaction);
		faction.setAlignment(0,0);
		
		// testing
		title.setText(army.getName());

		
		
		faction.setText(army.getFactionName());


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
		action = new Label("",ls);
		action.setWrap(true);
		action.setAlignment(Align.center);
		size = new Label("", ls);
		morale = new Label("", ls);
		money = new Label("", ls);
		atk = new Label("" ,ls);
		def = new Label("", ls);
		spd = new Label("", ls);
		
		// Create text
		text = new Table();
		//text.debug();
		text.defaults().padTop(NEG).left();
		
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
		text.add(title).colspan(4).fillX().expandX().padBottom(0);
		text.row();
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.row();
		text.add(faction).colspan(4).padBottom(MINI_PAD).fillX().expandX();
		text.row();
		text.add(action).colspan(4).padBottom(MINI_PAD).fillX().expandX();
		text.row();
		text.add(sizeC).padLeft(MINI_PAD);
		text.add(size);
		text.add(atkC).padLeft(PAD);
		text.add(atk);
		text.row();
		text.add(moraleC).padLeft(MINI_PAD);
		text.add(morale);
		text.add(defC).padLeft(PAD);
		text.add(def);
		text.row();
		text.add(moneyC).padLeft(MINI_PAD);
		text.add(money);
		text.add(spdC).padLeft(PAD);
		text.add(spd);
		
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

		this.addTopTable(text);
		
		if (this.army == panel.getPlayer()) {
			this.setButton(1, "Upgrades");
			this.setButton(2, "Inventory");
			this.setButton(3, "Character");
		}		
		playerTouched = false;
		
		updateSoldierTable();
		
		if (!this.party.player) this.setButton(4, "Back");
	}
	
	@Override
	public void act(float delta) {
		if (army.playerTouched && !playerTouched) {
			setButton(1, "Attack!");
			setButton(2, "Withdraw");
			setButton(4, null);
			playerTouched = true;
		}
		else if (!army.playerTouched && playerTouched) {
			setButton(1, null);
			setButton(2, null);
			setButton(4, "Back");
			playerTouched = false;
		}
		if (party.player) {
			faction.setText(army.getFactionName()); 
			title.setText(army.getName());
		}
		
		// for testing targetOf
//		if (army.targetOf.size > 0) {
//			title.setText(army.getName() + "<-" + army.targetOf.random().getName());
//		}
//		else {
//			title.setText(army.getName() + " " + army.isGarrisoned());
//		}
		
		if (army.getKingdom().getPlayer().isAtWar(army)) faction.setText(army.getFactionName() + " (at war)");
		else faction.setText(army.getFactionName());
		
		action.setText(army.getAction());
		morale.setText(army.getMoraleString() + "");
		size.setText(party.getHealthySize()+"/"+party.getTotalSize());
		money.setText("" + army.getParty().wealth);
		atk.setText(""+ party.getAtk());
		def.setText(Panel.format(""+party.getAvgDef(), 2));
		
		// set speed to be current travel speed, not party speed
		spd.setText(Panel.format(""+party.army.getSpeed()*Army.SPEED_DISPLAY_FACTOR, 2));
		//spd.setText(Panel.format(""+party.getAvgSpd(), 2));
		
		// don't call this every frame.
		if (party.updated) {
			updateSoldierTable();
			if (!army.isInBattle())
				party.updated = false;
		}
		
		// minor leak is not here?
		super.act(delta);
	}
	
	// DO NOT CALL THIS EVERY FRAME - TODO Fix for other panels
	// I am a god.
	public void updateSoldierTable() {
		soldierTable.clear(); // clearing the table is a problem right now. it hides the scroll bar and prevents click-drag scrolling
		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
		soldierTable.row();
		// This method is very leaky. Should only call when update to party occurs necessary.
		updateTableWithTypes(soldierTable, party.getConsolHealthy(), ls);
		updateTableWithTypes(soldierTable, party.getConsolWounded(), lsG);
		Label prisonersC;
		
		// LEAK IS ABOVE
		
		if (party.getPrisoners().size > 0)
			prisonersC = new Label("Captured", ls);
		else prisonersC = new Label("",ls);
		prisonersC.setAlignment(0,0);
		soldierTable.add(prisonersC).colspan(2).width(SidePanel.WIDTH - PAD*2).expandX().fillX().padTop(0);
		soldierTable.row();
		updateTableWithTypes(soldierTable, party.getConsolPrisoners(), lsG);
	}
	
	// this method is evil don't call this frequently
	public static void updateTableWithTypes(Table table, Array<Array<Soldier>> types, LabelStyle style) {
		for (Array<Soldier> type : types) {
			Label name = new Label(type.first().getName(), style);
			table.add(name).left();
			Label count = new Label(type.size + "", style);
			table.add(count).right();
			table.row();
		}
	}
	
	public static void updateTableWithSoldiers(Table table, Array<Soldier> soldiers, LabelStyle style) {
		for (Soldier s : soldiers) {
			SoldierLabel name = new SoldierLabel(s.getName(), style, s);
			name.addListener(new ClickListener() {
				public void enter(InputEvent event, float x,
						float y, int pointer, Actor fromActor) {
					//setStats(((SoldierLabel) event.getTarget()).soldier);
				}
				public void exit(InputEvent event, float x, float y,
						int pointer, Actor fromActor) {
					//clearStats();
				}
			});
			table.add(name).left();
			Label count = new Label(s.level +"", style);
			table.add(count).right();
			table.row();
		}
	}
	
	public void setStats(Soldier s) {
		stats.setVisible(true);
		nameS.setText(s.getName() + "");
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
		weaponS.setText(s.weapon.name);
	}
	
	public void clearStats() {
		stats.setVisible(false);
	}
	
	public void setActiveFaction() {
		panel.setActiveFaction(army.getFaction());
	}
	
	public void centerCamera() {
		OrthographicCamera camera = panel.getKingdom().getMapScreen().getCamera();
		camera.translate(new Vector2(army.getCenterX()-camera.position.x, army.getCenterY()-camera.position.y));
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
			if (party == panel.getKingdom().getPlayer().getParty()) {
				panel.setActive(panel.upgrades);
			}
			else {
				panel.setStay(false); // allows changing panel to panelbattle (attack)
				panel.getKingdom().getPlayer().attack((Army) panel.getKingdom().getPlayer().getTarget());
			}
		}
	}
	@Override
	public void button2() {
		if (this.getButton(2).isVisible()) {
			if (party == panel.getKingdom().getPlayer().getParty()) {
				BottomPanel.log("Inventory");
			}
			else {
				panel.setStay(false); // withdraw
				panel.setDefault();
			}
		}
	}
	@Override
	public void button3() {
		if (this.getButton(3).isVisible()) {
			if (party == panel.getKingdom().getPlayer().getParty()) {
				panel.setActive(panel.character);
			}
		}
	}
	@Override
	public void button4() {
//		if (party == panel.getKingdom().getPlayer().getParty())
			//panel.setActive(panel.main);
//		else 
//			panel.returnToPrevious();
		if (!this.party.player) 
			panel.returnToPrevious();
	}
	
	@Override
	public TextureRegion getCrest() {
		return army.getFaction().crest;
	}
}
