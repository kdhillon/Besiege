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
import kyle.game.besiege.party.RangedWeapon;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.SoldierLabel;
import kyle.game.besiege.party.Weapon;
import kyle.game.besiege.party.WeaponButton;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;

public class PanelUpgrades extends Panel { // TODO incorporate "list.java" into this and improve layout (consolidate)
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 600;
	private final float OFFSET = 1;
	private final String upTexture = "grey-med9";
	private final String downTexture = "grey-dm9";
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private SidePanel panel;
	private Army army;
	private Party party;

	private Table text;
	private Label title;
	private Label faction;
	private Label size;
	private Label morale;
	private Label money;
	private Label atk;
	private Label def;
	private Label spd;

	private Table upgrades;
	private Label nameS;
	private Label upgradeS;
	private Label naturalS;
	private Label equippedS;
	private Label equippedStatsS;
	private WeaponButton up1B;
	private WeaponButton up2B;
	private WeaponButton up3B;
	private Label upgrade1S;
	private Label upgrade2S;
	private Label upgrade3S;
	private Label upgrade1StatsS;
	private Label upgrade2StatsS;
	private Label upgrade3StatsS;

	private Soldier selected;

	private Table soldierTable;
	private ScrollPane soldierPane;

	private LabelStyle ls;

	private Array<Soldier> upgradable;

	public PanelUpgrades(SidePanel panel, Army army) {
		this.panel = panel;
		this.army = army;
		this.party = army.getParty();
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

		Label sizeC = new Label("Size:", ls);
		Label moraleC = new Label("Morale:",ls);
		Label moneyC = new Label("Money:",ls);
		Label atkC = new Label("Atk:", ls);
		Label defC = new Label("Def:", ls);
		Label spdC = new Label("Spd:", ls); 

		title = new Label("Upgrades", lsBig);
		title.setAlignment(0,0);
		title.setWrap(true);
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
		faction = new Label("", lsMed);
		faction.setAlignment(0,0);
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

		text.add(title).colspan(4).fillX().expandX().padBottom(MINI_PAD);
		text.row();
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.row();
		text.add(faction).colspan(4).padBottom(MINI_PAD).fillX().expandX();
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

		upgradable = new Array<Soldier>(party.getUpgradable()); // use copy constructor to avoid problems later

		// Soldier's stats
		upgrades = new Table();
		upgrades.setVisible(false);
		upgrades.padLeft(MINI_PAD);

		Label equippedSC = new Label("Equipped with:", lsMed);
		equippedSC.setAlignment(0,0);
		Label upgradeSC = new Label("Upgrade with:",lsMed);
		upgradeSC.setAlignment(0,0);

		nameS = new Label("", lsMB);
		nameS.setAlignment(0,0);
		upgradeS = new Label("", ls);
		upgradeS.setAlignment(0,0);
		naturalS = new Label("", ls);
		naturalS.setAlignment(0,0);
		equippedS = new Label("", lsMed);
		equippedStatsS = new Label("", ls);
		upgrade1S = new Label("", lsMicro);
		upgrade2S = new Label("" ,lsMicro);
		upgrade3S = new Label("", lsMicro);
		upgrade1S.setTouchable(Touchable.disabled);
		upgrade2S.setTouchable(Touchable.disabled);
		upgrade3S.setTouchable(Touchable.disabled);
		upgrade1StatsS = new Label("", lsSmall);
		upgrade2StatsS = new Label("", lsSmall);
		upgrade3StatsS = new Label("", lsSmall);
		upgrade1StatsS.setWrap(true);
		upgrade2StatsS.setWrap(true);
		upgrade3StatsS.setWrap(true);
		//		upgrade1StatsS.setAlignment(Align.right,Align.right);
		//		upgrade2StatsS.setAlignment(Align.right,Align.right);
		//		upgrade3StatsS.setAlignment(Align.right,Align.right);

		ButtonStyle bs = new ButtonStyle();
		bs = new ButtonStyle();
		bs.up = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(upTexture), r,r,r,r));
		bs.down = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(downTexture), r,r,r,r));
		bs.pressedOffsetX = OFFSET;
		bs.pressedOffsetY = -OFFSET;

		up1B = new WeaponButton(upgrade1S, bs, null);
		up1B.addListener(new ClickListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				upgradeCurrent(((WeaponButton) event.getTarget()).weapon);
			}
		});
		up2B = new WeaponButton(upgrade2S, bs, null);
		up2B.addListener(new ClickListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				upgradeCurrent(((WeaponButton) event.getTarget()).weapon);
			}
		});
		up3B = new WeaponButton(upgrade3S, bs, null);
		up3B.addListener(new ClickListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				upgradeCurrent(((WeaponButton) event.getTarget()).weapon);
			}
		});
		up1B.setVisible(false);
		up2B.setVisible(false);
		up3B.setVisible(false);

		upgrades.defaults().left();
		upgrades.add(nameS).colspan(2).width(SidePanel.WIDTH-PAD*2).fillX().expandX();
		//		upgrades.row();
		//		upgrades.add(equippedSC).fillX().expandX();
		//		upgrades.row();
		//		upgrades.add(equippedS).left();
		//		upgrades.row();
		//		upgrades.add(equippedStatsS).padTop(NEG);
		upgrades.row();
		upgrades.add(upgradeS).padBottom(NEG).expandX().fillX().colspan(2);
		upgrades.row();
		upgrades.add(naturalS).padBottom(MINI_PAD).expandX().fillX().colspan(2);
		upgrades.row();
		upgrades.add(upgradeSC).fillX().expandX().padBottom(MINI_PAD).colspan(2);
		upgrades.row();
		upgrades.add(up1B).padRight(MINI_PAD).fillX().padBottom(MINI_PAD).padTop(MINI_PAD);
		upgrades.add(upgrade1StatsS).fillX().expandX();
		upgrades.row();
		upgrades.add(up2B).padRight(MINI_PAD).fillX().padTop(MINI_PAD).padBottom(MINI_PAD);
		upgrades.add(upgrade2StatsS).fillX().expandX();
		upgrades.row();
		upgrades.add(up3B).padRight(MINI_PAD).fillX().padTop(MINI_PAD).padTop(MINI_PAD);
		upgrades.add(upgrade3StatsS).fillX().expandX();

		//upgrades.debug();

		text.add(upgrades).colspan(4).padTop(PAD);

		if (upgradable.size > 0)
			select(upgradable.first());
		
		this.addTopTable(text);

		//		this.addButton(null);
		//		this.addButton(null);
		//
		//		this.addButton(null);
		this.setButton(4, "Upgrade All");
	}

	// use this for desktop
	//	@Override
	//	public void act(float delta) {
	//		morale.setText(army.getMorale() + "");
	//		size.setText(party.getHealthySize()+"/"+party.getTotalSize());
	//		money.setText("" + army.getParty().wealth);
	//		atk.setText(""+ party.getAtk());
	//		def.setText(String.format("%.2f", party.getAvgDef()));
	//		spd.setText(String.format("%.2f", party.getAvgSpd()));
	//		updateSoldierTable();
	//		super.act(delta);
	//	}

	// use this for web
	@Override
	public void act(float delta) {
		morale.setText(army.getMorale() + "");
		size.setText(party.getHealthySize()+"/"+party.getTotalSize());
		money.setText("" + army.getParty().wealth);
		atk.setText(""+ party.getAtk());
		def.setText(Panel.format(""+ party.getAvgDef(),2));
		spd.setText(Panel.format(""+ party.getAvgSpd(),2));
		updateSoldierTable();
		super.act(delta);
	}

	public void updateSoldierTable() {
		soldierTable.clear();
		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
		//		Label healthyC = new Label("Upgradable", ls);
		//		healthyC.setAlignment(0,0);
		soldierTable.add().colspan(2).width(SidePanel.WIDTH - PAD*2).padTop(0);
		soldierTable.row();
		for (Soldier s : upgradable) {
			SoldierLabel name = new SoldierLabel(s.getName(), ls, s);

			if (selected == s) name.setColor(Color.YELLOW);
			//else name.setColor(Color.DARK_GRAY);

			name.addListener(new ClickListener() {
				@Override
				public boolean touchDown(InputEvent event, float x,
						float y, int pointer, int button) {
					return true;
				}
				public void touchUp(InputEvent event, float x, float y,
						int pointer, int button) {
					select(((SoldierLabel) event.getTarget()).soldier);
				}
			});
			soldierTable.add(name).left();
			Label count = new Label(s.level +"", ls);
			soldierTable.add(count).right();
			soldierTable.row();
		}
		if (upgradable.size == 0) {
			Label none = new Label("No troops are ready to upgrade!", ls);
			//			healthyC.setText("");
			none.setWrap(true);
			none.setAlignment(0,0);
			soldierTable.add(none).colspan(2).width(SidePanel.WIDTH - PAD*6).padTop(PAD*4);
			this.setButton(4, "Back");
			soldierTable.row();
		}
		
		if (!upgradableRemaining()) this.setButton(4, "Back");
		
		// may be memory infeficienta
//		if (upgradable.size == 0 || !upgradableRemaining()) {
//			this.setButton(4, null);
//		}
//		else this.setButton(4, "Upgrade All");
	}

	public void select(Soldier s) {
		this.selected = s;

		nameS.setText(s.getName());

		upgradeS.setText("Tier " + ((s.getTier())/2) + "  ->  Tier " + ((s.getTier() + 1)/2));
		String naturalUp = "";
		if (Soldier.ATK_TIER[s.getTier()+1])
			naturalUp += "+1 atk";
		if (Soldier.DEF_TIER[s.getTier()+1])
			naturalUp += "+1 def";
		if (Soldier.SPD_TIER[s.getTier()+1])
			naturalUp += "+1 spd";
		naturalS.setText(naturalUp);
		equippedS.setText(s.weapon.name);
		equippedStatsS.setText(s.weapon.atkMod + " atk, " + s.weapon.defMod + " def, " + s.weapon.spdMod + " spd");

		Array<Weapon> upgradeArr = Weapon.upgrade(s.weapon);
		if (upgradeArr.size >= 1) {
			Weapon up1 = upgradeArr.get(0);
			up1B.weapon = up1;
			up1B.setVisible(true);
			if (s.rangedWeapon == null) {
				upgrade1S.setText(" " + up1.name + " ");
				upgrade1StatsS.setText(s.getUpgradeCost() + "");
				//upgrade1StatsS.setText(statDif(s.weapon, up1));
			}
			else {
				upgrade1S.setText(" " + Weapon.getRanged(up1).name + " ");
				upgrade1StatsS.setText(s.getUpgradeCost() + "");
				//upgrade1StatsS.setText(statDif(s.rangedWeapon, Weapon.getRanged(up1)));
			}
		}
		if (upgradeArr.size >= 2) {
			Weapon up2 = upgradeArr.get(1);
			up2B.weapon = up2;
			up2B.setVisible(true);
			if (s.rangedWeapon == null) {
				upgrade2S.setText(" " + up2.name + " ");
				upgrade2StatsS.setText(s.getUpgradeCost() + "");
				//upgrade2StatsS.setText(statDif(s.weapon, up2));
			}
			else {
				upgrade2S.setText(" " + Weapon.getRanged(up2).name + " ");
				upgrade2StatsS.setText(s.getUpgradeCost() + "");
				//upgrade2StatsS.setText(statDif(s.rangedWeapon, Weapon.getRanged(up2)));
			}
		}
		else {
			upgrade2S.setText("");
			up2B.setVisible(false);
			upgrade2StatsS.setText("");
		}
		if (upgradeArr.size >= 3) {
			Weapon up3 = upgradeArr.get(2);
			up3B.weapon = up3;
			up3B.setVisible(true);
			if (s.rangedWeapon == null) {
				upgrade3S.setText(" " + up3.name + " ");
				upgrade3StatsS.setText(s.getUpgradeCost() + "");
				//upgrade3StatsS.setText(statDif(s.weapon, up3));
			}
			else {
				upgrade3S.setText(" " + Weapon.getRanged(up3).name + " ");
				upgrade3StatsS.setText(s.getUpgradeCost() + "");
				//upgrade3StatsS.setText(statDif(s.rangedWeapon, Weapon.getRanged(up3)));
			}
		}
		else {
			upgrade3S.setText("");
			up3B.setVisible(false);
			upgrade3StatsS.setText("");
		}
		upgrades.setVisible(true);
	}
	public void deselect() {
		this.selected = null;
		upgrades.setVisible(false);
		up1B.setVisible(false);
		up2B.setVisible(false);
		up3B.setVisible(false);
	}

	// select next unit after currently selected one
	public void selectNext() {
		if (selected == null) {
			System.out.println("selected is null");
			deselect();
		}
		//boolean select_was_prev = false;
		if (upgradable.size > 1) {
			int prev_index = getIndexOfSelected();
			//System.out.println(prev_index);
			int count = 0;
			for (Soldier s : upgradable) {
				if (prev_index + 1 == count) {
					select(s);
					break;
				}
				count++;
				if (count == upgradable.size) deselect();
			}
			//			for (Soldier s : upgradable) {
			//				if (select_was_prev && upgradable.contains(s, true)) {
			//					System.out.println("selecting " + s.name);
			//					select(s);
			//					break;
			//				}
			//					//if (s == selected) select_was_prev = true;
			//				System.out.println(s.name);
			//			}
			//			//upgradable.shrink();
			//			//select(upgradable.random());
		}
		else {
			System.out.println("none in upgradable");
			deselect();
		}
	}

	private int getIndexOfSelected() {
		int index = 0;
		for (Soldier s : upgradable) {
			if (s == selected) return index;
			index++;
		}
		return index;
	}

	private void upgradeCurrent(Weapon weapon) {
		//int index = party.getUpgradable().indexOf(selected, true);
		String first = selected.getName();
		if (selected.upgrade(weapon)) { // only if successfully upgrades
			String next = selected.getName();
			BottomPanel.log(first + " upgraded to " + next);
			Soldier prev_selected = selected;
			selectNext();
			upgradable.removeValue(prev_selected, true);
		}
		//		if (index > 0)
		//			selected = party.getUpgradable().get(index-1);
		//		else if (party.getUpgradable().size > 0)
		//			selected = party.getUpgradable().get(index);
		//else
	}

	
	private boolean upgradableRemaining() {
		Array<Soldier> possibleToUpgrade = new Array<Soldier>(upgradable);

		for (Soldier s : possibleToUpgrade) {
			if (Weapon.upgrade(s.weapon).size == 1) {
				return true;
			}
		}
		return false;
	}

	private void upgradeAll() {
		// can't remove them as you loop through... so start with first, keep selecting next
		Array<Soldier> possibleToUpgrade = new Array<Soldier>(upgradable);

		for (Soldier s : possibleToUpgrade) {
			if (Weapon.upgrade(s.weapon).size == 1) {
				select(s);
				upgradeCurrent(Weapon.upgrade(s.weapon).first());
			}
		}
	}

	private String statDif(Weapon curr, Weapon up) {
		String stats = "";
		int atkDif, defDif, spdDif;
		atkDif = up.atkMod - curr.atkMod;
		defDif = up.defMod - curr.defMod;
		spdDif = up.spdMod - curr.spdMod;
		if (atkDif != 0) {
			if (atkDif > 0) stats +=  "+" + atkDif + "a";
			else stats += atkDif + "a";
		}
		if (defDif != 0) {
			if (atkDif != 0) stats += ", ";
			if (defDif > 0) stats +=  "+" + defDif + "d";
			else stats += defDif + "d";
		}
		if (spdDif != 0) {
			if (atkDif != 0 || defDif != 0) stats += ", ";
			if (spdDif > 0) stats +=  "+" + spdDif + "s";
			else stats += spdDif + "s";
		}
		if (atkDif == 0 && defDif == 0 && spdDif == 0)
			stats += "no change";
		return stats;
	}

	private String statDif(RangedWeapon curr, RangedWeapon up) {
		String stats = "";
		int atkDif, rangeDif, accuracyDif;
		atkDif = up.atkMod - curr.atkMod;
		rangeDif = up.range - curr.range;
		accuracyDif = up.accuracy - curr.accuracy;
		if (atkDif != 0) {
			if (atkDif > 0) stats +=  "+" + atkDif + "at";
			else stats += atkDif + "at";
		}
		if (rangeDif != 0) {
			if (atkDif != 0) stats += ", ";
			if (rangeDif > 0) stats +=  "+" + rangeDif + "rg";
			else stats += rangeDif + "rg";
		}
		if (accuracyDif != 0) {
			if (atkDif != 0 || rangeDif != 0) stats += ", ";
			if (accuracyDif > 0) stats +=  "+" + accuracyDif + "ac";
			else stats += accuracyDif + "ac";
		}
		if (atkDif == 0 && rangeDif == 0 && accuracyDif == 0)
			stats += "no change";
		return stats;
	}

	//	public void setStats(Soldier s) {
	//		nameS.setText(s.name + "");
	//		levelS.setText(s.level + "");
	//		expS.setText(s.exp + "");
	//		nextS.setText(s.next + "");
	//		if (s.bonusAtk >= 0)
	//			atkS.setText(s.atk + " (" + s.baseAtk + "+" + s.bonusAtk + ")");
	//		else 
	//			atkS.setText(s.atk + " (" + s.baseAtk + s.bonusAtk + ")");
	//		if (s.bonusDef >= 0)
	//			defS.setText(s.def + " (" + s.baseDef + "+" + s.bonusDef + ")");
	//		else 
	//			defS.setText(s.def + " (" + s.baseDef + s.bonusDef + ")");
	//		if (s.bonusSpd >= 0)
	//			spdS.setText(s.spd + " (" + s.baseSpd + "+" + s.bonusSpd + ")");
	//		else 
	//			spdS.setText(s.spd + " (" + s.baseSpd + s.bonusSpd + ")");
	//		weaponS.setText(s.weapon.name);
	//		equipmentS.setText(s.equipmentList());
	//	}

	public void clearStats() {
		upgrades.setVisible(false);
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
		if (up1B.isVisible())
			upgradeCurrent(up1B.weapon);
	}
	@Override
	public void button2() {
		if (up2B.isVisible()) {
			upgradeCurrent(up2B.weapon);
		}
	}
	@Override
	public void button3() {
		if (up3B.isVisible()) {
			upgradeCurrent(up3B.weapon);
		}
	}
	@Override
	public void button4() {
		if (upgradableRemaining())
			upgradeAll();
		else panel.returnToPrevious();
		//panel.returnToPrevious();
	}
}
