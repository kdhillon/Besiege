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
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 530;
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private SidePanel panel;
	public Party party;
	public Army army; // may be null

	private TopTable topTable;
	
//	private Label size;
//	private Label morale;
//	private Label money;
//	private Label atk;
//	private Label def;
//	private Label spd;

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

		topTable = new TopTable(3);
		topTable.updateTitle(party.getName(), new InputListener() {
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
			topTable.updateSubtitle2(party.getGeneral().getName(), null);
		}
		
		topTable.addSmallLabel("Size", "Size:");
		topTable.addSmallLabel("Atk", "Atk:");
		topTable.addSmallLabel("Morale", "Morale:");
		topTable.addSmallLabel("Def", "Def:");
		topTable.addSmallLabel("Wealth", "Wealth:");
		topTable.addSmallLabel("Spd", "Spd:");

		soldierTable = new Table();
		//soldierTable.debug();
		soldierTable.defaults().padTop(NEG);
		soldierTable.top();
		soldierTable.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));
		topTable.row();
		topTable.add().colspan(4).padBottom(PAD);
		topTable.row();

		soldierPane = new ScrollPane(soldierTable);
		soldierPane.setScrollbarsOnTop(true);
		soldierPane.setFadeScrollBars(false);
		topTable.add(soldierPane).colspan(4).top().padTop(0);

		topTable.row();
		
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

		topTable.add(stats).colspan(4).padTop(PAD);

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

		updateSoldierTable();

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

			// for testing targetOf
			// if (army.targetOf.size > 0) {
			// title.setText(army.getName() + "<-" + army.targetOf.random().getName());
			// }
			// else {
			// title.setText(army.getName() + " " + army.isGarrisoned());
			// }

			if (army.getKingdom().getPlayer().isAtWar(army))
				topTable.updateSubtitle(army.getFactionName() + " (at war)", null);
			else
				topTable.updateSubtitle(army.getFactionName(), null);

			topTable.updateSubtitle3(army.getAction(), null);
			topTable.update("Morale", army.getMoraleString() + "");
			topTable.update("Wealth", "" + army.getParty().wealth);

			// set speed to be current travel speed, not party speed
			topTable.update("Spd", Panel.format("" + army.getSpeed() * Army.SPEED_DISPLAY_FACTOR, 2));
		}
		topTable.update("Size", party.getHealthySize()+"/"+party.getTotalSize()); //+"/"+party.getMaxSize());		
		topTable.update("Atk", ""+ party.getAtk());
		topTable.update("Def", Panel.format(""+party.getAvgDef(), 2));
		
		//spd.setText(Panel.format(""+party.getAvgSpd(), 2));

		if (army != null) {
			// don't call this every frame.
			if (party.updated) {
				updateSoldierTable();
				if (!army.isInBattle())
					party.updated = false;
			}
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
		
//		soldierTable.debug();
		// This method is very leaky. Should only call when update to party occurs necessary.
		//		updateTableWithTypes(soldierTable, party.getConsolHealthy(), ls);
		//		updateTableWithTypes(soldierTable, party.getConsolWounded(), lsG);
		updateTableWithParty(soldierTable, party, ls, lsG);
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


	public static class TypeLabel extends Label {
		StrictArray<Soldier> type;
		Table expand;
		boolean expanded = false;

		public TypeLabel(String name, LabelStyle ls) {
			super(name, ls);
			expand = new Table();
			this.addListener(new ClickListener() {
				public boolean touchDown(InputEvent event, float x,
						float y, int pointer, int button) {
					return true;
				}
				public void touchUp(InputEvent event, float x, float y,
						int pointer, int button) {
					if (expanded) {
						clearExpand();
						expanded = false;
					}
					else {
						createExpand();
						expanded = true;
					}
				}
			});
		}
		public void createExpand() {
			for (Soldier s : type) {
				SoldierLabel soldierName = new SoldierLabel(s.getName(), this.getStyle(), s);
				soldierName.setColor(Color.GRAY);
				expand.add(soldierName).left().padBottom(PanelUnit.NEG).expandX();

				soldierName.addListener(new ClickListener() {
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						return true;
					}
					public void touchUp(InputEvent event, float x, float y,
							int pointer, int button) {
						switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
					}
				});
				expand.row();
			}
		}
		public void clearExpand() {
			expand.clear();
		}
	}

	// sometimes this runs a ton of times...
	public static void updateTableWithParty(Table table, Party party, LabelStyle style, LabelStyle wounded) {
		System.out.println("starting panelparty update: " + party.getName());
		for (Subparty s : party.sub) {
			SoldierLabel general;
			if (s.general != null) {
//				System.out.println(s.general.getName());
				general = new SoldierLabel(s.general.getRank() + " " + s.general.getLastName(), style, s.general);
			}
			else {
				general = new SoldierLabel("No general!", style, s.general);
			}

			general.addListener(new ClickListener() {
				public boolean touchDown(InputEvent event, float x,
						float y, int pointer, int button) {
					return true;
				}
				public void touchUp(InputEvent event, float x, float y,
						int pointer, int button) {
					switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
				}
			});
			table.add(general).left().expandX();
			general.setColor(Color.CYAN);
			
			Label generalCount = new Label(s.getHealthySize() + "", style);
			table.add(generalCount).right();
			generalCount.setColor(Color.CYAN);
			table.row();


			updateTableWithTypes(table, s.getConsolHealthy(), style);
			updateTableWithTypes(table, s.getConsolWounded(), wounded);
		}
	}

	// this method is evil don't call this frequently
	public static void updateTableWithTypes(Table table, StrictArray<StrictArray<Soldier>> types, LabelStyle style) {
		//		table.debug();
		for (StrictArray<Soldier> type : types) {
			TypeLabel name = new TypeLabel(type.first().getTypeName(), style);
			name.type = type;
			name.setColor(type.first().unitType.unitClass.color);
			table.add(name).left();
			Label count = new Label(type.size + "", style);
			table.add(count).right();
			table.row();

			float indent = 15;

			table.add(name.expand).expandX().left().padLeft(indent);
			table.row();
			name.expand.padBottom(-PanelUnit.NEG);
		}
	}

	public static void updateTableWithSoldiers(Table table, StrictArray<Soldier> soldiers, LabelStyle style) {
		for (Soldier s : soldiers) {
			SoldierLabel name = new SoldierLabel(s.getTypeName(), style, s);
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
	}

	public void clearStats() {
		stats.setVisible(false);
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
//			if (party == panel.getKingdom().getPlayer().getParty()) {
////				panel.setActive(panel.character);
//			}
//		}
	}
	@Override
	public void button4() {
		if (party.player) {
			this.panel.getMapScreen().save();
		} else 
			panel.returnToPrevious();
	}

	@Override
	public Crest getCrest() {
		if (army == null) return null;
		return army.getFaction().crest;
	}
}
