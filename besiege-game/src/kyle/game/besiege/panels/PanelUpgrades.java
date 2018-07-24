/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

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

import kyle.game.besiege.Assets;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.RangedWeaponType;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.SoldierLabel;
import kyle.game.besiege.party.UnitType;
import kyle.game.besiege.party.UpgradeButton;
import kyle.game.besiege.party.WeaponType;

public class PanelUpgrades extends Panel { // TODO incorporate "list.java" into this and improve layout (consolidate)
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 600;
	private final float OFFSET = 1;
	private final String upTexture = "grey-med9";
	private final String downTexture = "grey-dm9";
	private final String disabledTexture = "grey-mmd9";
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
	private Label weaponS;
	private Label rangedS;
	private Label weaponStatsS;
	private Label rangedStatsS;
	private Label upgradeS;
	private Label naturalS;
	private Label equippedS;
	private Label equippedStatsS;
	private UpgradeButton up1B;
	private UpgradeButton up2B;
	private UpgradeButton up3B;
	private Label upgrade1S;
	private Label upgrade2S;
	private Label upgrade3S;
	private Label upgrade1StatsS;
	private Label upgrade2StatsS;
	private Label upgrade3StatsS;
	
	private boolean justPressed3;

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
		Label upgradeSC = new Label("Upgrade to:",lsMed);
		upgradeSC.setAlignment(0,0);

		nameS = new Label("", lsMB);
		nameS.setAlignment(0,0);
		weaponS = new Label("", lsMed);
		weaponS.setAlignment(0,0);
		weaponStatsS = new Label("", lsMed);
		weaponStatsS.setAlignment(0,0);
		rangedS = new Label("", lsMed);
		rangedS.setAlignment(0,0);
		rangedStatsS = new Label("", lsMed);
		rangedStatsS.setAlignment(0,0);
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
		bs.disabled = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(disabledTexture), r,r,r,r));
		bs.pressedOffsetX = OFFSET;
		bs.pressedOffsetY = -OFFSET;

		up1B = new UpgradeButton(upgrade1S, bs, null);
		up1B.addListener(new ClickListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				upgradeCurrent(((UpgradeButton) event.getTarget()).upgrade);
			}
		});
		up2B = new UpgradeButton(upgrade2S, bs, null);
		up2B.addListener(new ClickListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				upgradeCurrent(((UpgradeButton) event.getTarget()).upgrade);
			}
		});
		up3B = new UpgradeButton(upgrade3S, bs, null);
		up3B.addListener(new ClickListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				upgradeCurrent(((UpgradeButton) event.getTarget()).upgrade);
			}
		});
		up1B.setVisible(false);
		up2B.setVisible(false);
		up3B.setVisible(false);

		upgrades.defaults().left();
		upgrades.add(nameS).colspan(2).width(SidePanel.WIDTH-PAD*2).fillX().expandX();
		upgrades.row();
		upgrades.add(weaponS).colspan(2).fillX().expandX();
		upgrades.row();
		upgrades.add(weaponStatsS).colspan(2).fillX().expandX();
		upgrades.row();
		upgrades.add(rangedS).colspan(2).fillX().expandX();
		upgrades.row();
		upgrades.add(rangedStatsS).colspan(2).fillX().expandX();

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
		if (upgradableRemaining()) this.setButton(3, "Upgrade All");
		setButton(4, "Back");
		updateSoldierTable();
	}

	// use this for desktop
	//	@Override
	//	public void act(float delta) {
	//		morale.setText(army.getMorale() + "");
	//		size.setText(playerPartyPanel.getHealthySize()+"/"+playerPartyPanel.getTotalSize());
	//		money.setText("" + army.getParty().wealth);
	//		atk.setText(""+ playerPartyPanel.getAtk());
	//		def.setText(String.format("%.2f", playerPartyPanel.getAvgDef()));
	//		spd.setText(String.format("%.2f", playerPartyPanel.getAvgSpd()));
	//		updateSoldierTable();
	//		super.act(delta);
	//	}
	
	public void update() {
		this.upgradable = party.getUpgradable();
	}

	// use this for web
	@Override
	public void act(float delta) {
		justPressed3 = false;
		morale.setText(army.getMorale() + "");
		size.setText(party.getHealthySize()+"/"+party.getTotalSize());
		money.setText("" + army.getParty().wealth);
		atk.setText(""+ party.getAtk());
		def.setText(Panel.format(""+ party.getAvgDef(),2));
		spd.setText(Panel.format(""+ party.getAvgSpd(),2));
//		updateSoldierTable();
		super.act(delta);
	}

	// don't do this every frame.
	public void updateSoldierTable() {
		soldierTable.clear();
		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
		//		Label healthyC = new Label("Upgradable", ls);
		//		healthyC.setAlignment(0,0);
		soldierTable.add().colspan(2).width(SidePanel.WIDTH - PAD*2).padTop(0);
		soldierTable.row();
		
//		System.out.println("Upgradable: " + upgradable.size);
		
		for (Soldier s : upgradable) {
			SoldierLabel name = new SoldierLabel(s.getTypeName(), ls, s);

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
			this.setButton(3, null);
			soldierTable.row();
		}
		else {
			setButton(3, "Upgrade All");
		}
		
		
		// may be memory infeficienta
//		if (upgradable.size == 0 || !upgradableRemaining()) {
//			this.setButton(4, null);
//		}
//		else this.setButton(4, "Upgrade All");
	}

	public void select(Soldier s) {
		this.selected = s;

		nameS.setText(s.getTypeName());

		weaponS.setText(s.unitType.melee.name);
		
		if (s.unitType.ranged != null) 
			rangedS.setText(s.unitType.ranged.name);
	
		upgradeS.setText("Tier " + ((s.getTier())/2) + "  ->  Tier " + ((s.getTier() + 1)/2));
		String naturalUp = "";
		if (Soldier.ATK_TIER[s.getTier()+1])
			naturalUp += "+1 atk";
		if (Soldier.DEF_TIER[s.getTier()+1])
			naturalUp += "+1 def";
		if (Soldier.SPD_TIER[s.getTier()+1])
			naturalUp += "+1 spd";
		naturalS.setText(naturalUp);
		equippedS.setText(s.getTypeName());
		equippedStatsS.setText(s.getWeapon().atkMod + " atk, " + s.getWeapon().defMod + " def, " + s.getWeapon().spdMod + " spd");

		UnitType[] upgradeArr = s.getUpgrades();
		if (upgradeArr.length >= 1) {
			UnitType up1 = upgradeArr[0];
			up1B.upgrade = up1;
			up1B.setVisible(true);
			if (up1.ranged == null) {
				upgrade1S.setText(" " + up1.name + " ");
				upgrade1StatsS.setText(s.getUpgradeCost() + "");
				//upgrade1StatsS.setText(statDif(s.weapon, up1));
			}
			else {
				upgrade1S.setText(" " + up1.name + " ");
				upgrade1StatsS.setText(s.getUpgradeCost() + "");
				//upgrade1StatsS.setText(statDif(s.rangedWeapon, Weapon.getRanged(up1)));
			}
			if (!this.panel.getMapScreen().getCharacter().canEquip(up1)) {
				up1B.setTouchable(Touchable.disabled);
				up1B.setDisabled(true);
			}
			else {
				up1B.setTouchable(Touchable.enabled);
				up1B.setDisabled(false);
			}
		}
		else {
			upgrade1S.setText("");
			up1B.setVisible(false);
			upgrade1StatsS.setText("");
		}
		if (upgradeArr.length >= 2) {
			UnitType up2 = upgradeArr[1];
			up2B.upgrade = up2;
			up2B.setVisible(true);
			if (up2.ranged == null) {
				upgrade2S.setText(" " + up2.name + " ");
				upgrade2StatsS.setText(s.getUpgradeCost() + "");
				//upgrade2StatsS.setText(statDif(s.weapon, up2));
			}
			else {
				upgrade2S.setText(" " + up2.name + " ");
				upgrade2StatsS.setText(s.getUpgradeCost() + "");
				//upgrade2StatsS.setText(statDif(s.rangedWeapon, Weapon.getRanged(up2)));
			}
			if (!this.panel.getMapScreen().getCharacter().canEquip(up2)) {
				up2B.setTouchable(Touchable.disabled);
				up2B.setDisabled(true);
			}
			else {
				up2B.setTouchable(Touchable.enabled);
				up2B.setDisabled(false);
			}
		}
		else {
			upgrade2S.setText("");
			up2B.setVisible(false);
			upgrade2StatsS.setText("");
		}
		if (upgradeArr.length >= 3) {
			UnitType up3 = upgradeArr[2];
			up3B.upgrade = up3;
			up3B.setVisible(true);
			if (up3.ranged == null) {
				upgrade3S.setText(" " + up3.name + " ");
				upgrade3StatsS.setText(s.getUpgradeCost() + "");
				//upgrade3StatsS.setText(statDif(s.weapon, up3));
			}
			else {
				upgrade3S.setText(" " + up3.name + " ");
				upgrade3StatsS.setText(s.getUpgradeCost() + "");
				//upgrade3StatsS.setText(statDif(s.rangedWeapon, Weapon.getRanged(up3)));
			}
			if (!this.panel.getMapScreen().getCharacter().canEquip(up3)) {
				up3B.setTouchable(Touchable.disabled);
				up3B.setDisabled(true);
			}
			else {
				up3B.setTouchable(Touchable.enabled);
				up3B.setDisabled(false);
			}
		}
		else {
			upgrade3S.setText("");
			up3B.setVisible(false);
			upgrade3StatsS.setText("");
		}
		upgrades.setVisible(true);
		updateSoldierTable();
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

	private void upgradeCurrent(UnitType unitType) {
		//int index = playerPartyPanel.getUpgradable().indexOf(selected, true);
		String first = selected.getTypeName();
		 // only if successfully upgrades
		if (selected.upgrade(unitType, panel.getMapScreen().getCharacter().inventory)) {
			String next = selected.getTypeName();
			BottomPanel.log(first + " upgraded to " + next);
			Soldier prev_selected = selected;
			selectNext();
			upgradable.removeValue(prev_selected, true);
		}
		//		if (index > 0)
		//			selected = playerPartyPanel.getUpgradable().get(index-1);
		//		else if (playerPartyPanel.getUpgradable().size > 0)
		//			selected = playerPartyPanel.getUpgradable().get(index);
		//else
		updateSoldierTable();
	}

	
	private boolean upgradableRemaining() {
		Array<Soldier> possibleToUpgrade = new Array<Soldier>(upgradable);

		for (Soldier s : possibleToUpgrade) {
			if (s.unitType.upgrades.length == 1) {
				return true;
			}
		}
		return false;
	}

	private void upgradeAll() {
		// can't remove them as you loop through... so start with first, keep selecting next
		Array<Soldier> possibleToUpgrade = new Array<Soldier>(upgradable);

		for (Soldier s : possibleToUpgrade) {
			if (s.unitType.upgrades.length == 1) {
				select(s);
				if (this.panel.getMapScreen().getCharacter().canEquip(s.unitType.upgrades[0]))
					upgradeCurrent((s.unitType.upgrades[0]));
			}
			System.out.println("in upgrade loop: " + possibleToUpgrade.size);
		}
	}

	private String statDif(WeaponType curr, WeaponType up) {
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

	private String statDif(RangedWeaponType curr, RangedWeaponType up) {
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
		if (up1B.isVisible()) {
//			upgradeCurrent(up1B.upgrade);
		}
	}
	@Override
	public void button2() {
		if (up2B.isVisible()) {
//			upgradeCurrent(up2B.upgrade);
		}
	}
	@Override
	public void button3() {
		// fixes bug where upgrade loop would be infinite
		if (!justPressed3) {
			justPressed3 = true;
			if (upgradableRemaining())
				upgradeAll();
			if (up3B.isVisible()) {
				//			upgradeCurrent(up3B.upgrade);
			}
		}
	}
	
	
//	@Override
//	public RandomCrest getCrest() {
////		if (playerPartyPanel.army != null)
////			return playerPartyPanel.army.getFaction().crest;
////		return null;
//		return null;
//	}
	
	@Override
	public Soldier getSoldierInsteadOfCrest() {
		return this.selected;
	}
}
