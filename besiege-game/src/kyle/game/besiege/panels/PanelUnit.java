/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import java.text.DecimalFormat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.battle.BattleStage;
import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

public class PanelUnit extends Panel { 
	protected static final float PAD = 10;
	protected static final float MINI_PAD = 5;
	public static final float NEG = -5;
	private final float DESC_HEIGHT = 530;
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private final String redPatch = "red9";
	private final String greenPatch = "green9";
	private BattleStage battleStage;
	private Battle battle;
	private SidePanel panel;
	private Unit unit;
	protected Soldier soldier;
	private float max_hp;
	private Party party;

	protected Table text;
	protected Label title;
	protected Label subTitle;
	private Label armyName;
	private Label partyName;
	private Label type;

	private Table health;
	private Table red;
	private Table green;

	//	private Table stats;
	//	private Label nameS;
	protected Label atkSC;
	protected Label defSC;
	protected Label levelSC;
	protected Label hpSC;
	protected Label moraleSC;
	protected Label spdSC;

	protected Table generalStats;

	protected Label levelS;
	protected Label hpS;
	protected Label moraleS;
	protected Label atkS;
	protected Label defS;
	protected Label spdS;
	private Label weaponS;
	private Label equipmentS;
	private Label actionS;

	//	private Table soldierTable;
	//	private ScrollPane soldierPane;

	protected LabelStyle ls;
	private LabelStyle lsMed;
	private LabelStyle lsG;

	protected DecimalFormat df = new DecimalFormat("#.00"); 

	// can be used for soldier or unit
	public PanelUnit(SidePanel panel, Unit unit, Soldier soldier) {
		this.panel = panel;

		this.unit = unit;
		//		unit = null;
		//		this.unit = null;

		this.party = soldier.party;

		if (unit != null) {
			this.battleStage = unit.stage;
			this.battle = this.battleStage.battle;
			this.soldier = unit.soldier;
			soldier = unit.soldier;
			this.max_hp = soldier.getHp();
		}
		else {
			this.soldier = soldier;
		}

		this.addParentPanel(panel);

		LabelStyle lsBig = new LabelStyle();
		lsBig.font = Assets.pixel22;
		lsMed = new LabelStyle();
		lsMed.font = Assets.pixel18;
		ls = new LabelStyle();
		ls.font = Assets.pixel16;
		lsG = new LabelStyle();
		lsG.font = Assets.pixel16;
		lsG.fontColor = Color.GRAY;

		levelSC = 	new Label("Level:", ls);
		hpSC = 		new Label("HP:",ls);
		moraleSC =		new Label("Morale:",ls);

		atkSC =		new Label("Atk:", ls);
		defSC = 	new Label("Def:", ls);
		spdSC = 		new Label("Spd:", ls); 

		generalStats = new Table();

		Label weaponSC = 	new Label("Weapon: ", ls);
		Label equipmentSC = new Label("Armor: ", ls);

		title = new Label(soldier.getTypeName(), lsBig);
		title.setColor(soldier.unitType.unitClass.color);
		title.setAlignment(0,0);
		//		title.setWrap(true); // wrapping messes up click listeners... WTF?
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);

		String name = soldier.getName();
		if (soldier.female) name += " (f)";
		else name += " (m)";
		subTitle = new Label(name, ls);
		subTitle.setColor(Color.WHITE);
		subTitle.setAlignment(0, 0);
		subTitle.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);

		//		title.addListener(new InputListener() {
		//			public boolean touchDown(InputEvent event, float x,
		//					float y, int pointer, int button) {
		//				return true;
		//			}
		//			public void touchUp(InputEvent event, float x, float y,
		//					int pointer, int button) {
		//				centerCamera();
		//			}
		//		});

		armyName = new Label("", ls);
		if (party.army != null)
			armyName.setText(party.army.getName());
		armyName.setAlignment(0,0);

		levelS = new Label("" + soldier.level, ls);
		hpS = new Label("", ls);
		moraleS = new Label("" + soldier.next, ls);
		atkS = new Label("" + df.format(soldier.getAtk()), ls);
		defS = new Label("" + df.format(soldier.getDef()), ls);
		spdS = new Label("" + df.format(soldier.getSpd()), ls);
		weaponS = new Label("" + soldier.unitType.melee.name, ls);
		if (unit != null && unit.isRanged()) weaponS.setText(soldier.unitType.ranged.name + "(" + unit.quiver + ")");
		weaponS.setAlignment(0,0);

		equipmentS = new Label("", ls);
		if (unit != null) {
			actionS = new Label("" + unit.getStatus(), ls);
			actionS.setAlignment(0,0);
			health = new Table();
			red = new Table();
			red.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(redPatch), r,r,r,r)));
			green = new Table();
			green.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(greenPatch), r,r,r,r)));
		}

		// Create text
		text = new Table();
		//text.debug();
		text.defaults().padTop(NEG).left();

		//		title.addListener(new InputListener() {
		//			public boolean touchDown(InputEvent event, float x,
		//					float y, int pointer, int button) {
		//				return true;
		//			}
		//			public void touchUp(InputEvent event, float x, float y,
		//					int pointer, int button) {
		//				centerCamera();
		//			}
		//		});
		text.add(title).colspan(4).fillX().expandX().padBottom(0);
		text.row();
		text.add(subTitle).colspan(4).fillX().expandX().padBottom(0);
		text.row();
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.row();
		text.add(armyName).colspan(4).fillX().expandX();
		text.row();
		if (unit != null) {
			text.add(health).colspan(4).padTop(MINI_PAD).padBottom(MINI_PAD);
			text.row();
			text.add(actionS).colspan(4).fillX().expandX();
			text.row();
		}
		text.add(weaponS).colspan(4).fillX().expandX();
		text.row();
		text.add(generalStats).colspan(4).fillX().expandX().padTop(MINI_PAD).padBottom(MINI_PAD);
		text.row();

		text.add(levelSC).padLeft(PAD);
		text.add(levelS);
		text.add(atkSC).padLeft(MINI_PAD);
		text.add(atkS);
		text.row();
		text.add(hpSC).padLeft(PAD);
		text.add(hpS);
		text.add(defSC).padLeft(MINI_PAD);
		text.add(defS);
		text.row();
		text.add(moraleSC).padLeft(PAD);
		text.add(moraleS);
		text.add(spdSC).padLeft(MINI_PAD);
		text.add(spdS);

		text.row();

		text.padLeft(MINI_PAD);
		this.addTopTable(text);

		if (battleStage == null)
			this.setButton(4, "Back");
	}

	// this is probably pretty slow if these strings are being constructed every frame
	// TODO: an update system.
	@Override
	public void act(float delta) {	
		//		if (!unit.isGeneral()) {
		levelS.setText("" + soldier.level);

		hpS.setText(df.format(soldier.hp.getValue()));
		if (unit != null) {
			moraleS.setText(unit.bsp.getCurrentMoraleString());
			moraleS.setColor(unit.bsp.moraleColor);
		}
		
//		expS.setText("" + (int) (unit.getFloorHeight()*10) / 10.0);

		if (unit != null && unit.bowOut()) {
			atkSC.setText("Pow:");
			defSC.setText("Rng:");
			atkS.setText("" + df.format(unit.getRangeDmg()));
			defS.setText("" + df.format(unit.getBaseRange()));
		}
		else {
			atkSC.setText("Atk:");
			defSC.setText("Def:");
			atkS.setText("" + df.format(soldier.getAtk()));
			defS.setText("" + df.format(soldier.getDef()));
		}
		spdS.setText(df.format(soldier.getSpd()));
		//		}

		if (soldier.getRanged() != null && (unit == null || (unit.attacking == null && unit.quiver > 0))) {
			String toPut = soldier.unitType.ranged.name;
			if (unit != null) toPut += " (" + unit.quiver + ")";
			weaponS.setText(toPut);
		}
		else weaponS.setText(soldier.unitType.melee.name);

		weaponS.setText(weaponS.getText() + ", " + soldier.unitType.armor.name);

		String mounted = "";
		if (unit != null) {
			if (unit.isMounted()) mounted = " (Mounted)";

			actionS.setText(unit.getStatus() + mounted);

			health.clear();
			float totalWidth = SidePanel.WIDTH - PAD;

			health.add(green).width((float) (totalWidth*(unit.hp*1.0/max_hp)));
			if (totalWidth*(1-(unit.hp*1.0/max_hp)) > 0)
				health.add(red).width((float) (totalWidth*(1-(unit.hp*1.0/max_hp))));
		}

		if (battleStage != null) {
			if (battle.playerInA || battle.playerInD) {
				if (!battleStage.placementPhase) {
					if (battleStage.retreatTimerPlayer <= 0 && !battleStage.allies.retreating) {
						this.setButton(1, "Retreat!");
						this.getButton(1).setDisabled(false);
					}
					else if (!(battleStage.retreatTimerPlayer <= 0)) {
						this.setButton(1, "Retreat (" + String.format("%.0f", battleStage.retreatTimerPlayer) + ")");
						this.getButton(1).setDisabled(true);
					}
					else if (battleStage.allies.retreating) {
						this.setButton(1, "Retreat!");
						this.getButton(1).setDisabled(true);
						this.getButton(1).setVisible(true);
						this.getButton(2).setDisabled(true);
					}
				}
				else {
					this.setButton(1, unit.bsp.formation.name);
					this.setButton(2, unit.bsp.stance.name());
				}
			}
		}

		if (battleStage != null && !battleStage.placementPhase) {
			this.setButton(2, "Charge!");
			
			if (unit.bsp.stance == Stance.AGGRESSIVE)
				this.getButton(2).setDisabled(true);
//			else this.setButton(2, null);
		}


		super.act(delta);
	}

	//	public void setStats(Soldier s) {
	//		stats.setVisible(true);
	//		nameS.setText(s.name + "");
	//		levelS.setText(s.level + "");
	//		expS.setText(s.exp + "");
	//		nextS.setText(s.next + "");
	//		if (s.bonusAtk >= 0)
	//			atkS.setText(s.getAtk() + " (" + s.baseAtk + "+" + s.bonusAtk + ")");
	//		else 
	//			atkS.setText(s.getAtk() + " (" + s.baseAtk + s.bonusAtk + ")");
	//		if (s.bonusDef >= 0)
	//			defS.setText(s.getDef() + " (" + s.baseDef + "+" + s.bonusDef + ")");
	//		else 
	//			defS.setText(s.getDef() + " (" + s.baseDef + s.bonusDef + ")");
	//		if (s.bonusSpd >= 0)
	//			spdS.setText(s.getSpd() + " (" + s.baseSpd + "+" + s.bonusSpd + ")");
	//		else 
	//			spdS.setText(s.getSpd() + " (" + s.baseSpd + s.bonusSpd + ")");
	//		weaponS.setText(s.weapon.name);
	//	}

	//	public void clearStats() {
	//		stats.setVisible(false);
	//	}
	//	
	//	public void setActiveFaction() {
	//		panel.setActiveFaction(location.getFaction());
	//	}
	//	public void centerCamera() {
	//		OrthographicCamera camera = panel.getKingdom().getMapScreen().getCamera();
	//		camera.translate(new Vector2(location.getCenterX()-camera.position.x, location.getCenterY()-camera.position.y));
	//	}


	@Override
	public void button1() {
		//retreat button
		if (getButton(1).isVisible()) {

			if (battleStage == null) {
//				unit.bsp.retreat(sidePanel.getKingdom().getPlayer());
				return;
			}
			else {
				if (battleStage.placementPhase) {
					this.unit.bsp.toNextFormation();
				}
				else {
					battleStage.retreatAll(true);
					getButton(1).setDisabled(true);
				}
			}

		}
	}
	@Override
	public void button2() {

		if (getButton(2).isVisible()) {

			if (battleStage == null) BottomPanel.log("no battle stage to retreat!!");
			else {
				// toggle stance
				if (battleStage.placementPhase) {
					this.unit.bsp.toggleStance();
				}
				// charge all (move "Begin!" to button 3)
				else {
					unit.bsp.charge();
//					getButton(2).setVisible(false);
					getButton(2).setDisabled(true);
				}
			}
		}
		//		BottomPanel.log("b2");
	}
	@Override
	public void button3() {

	}
	@Override
	public void button4() {
		if (getButton(4).isVisible()) {
			if (battleStage == null) {
				//				battle.retreat(sidePanel.getKingdom().getPlayer());
				sidePanel.returnToPrevious();
				return;
			}
		}
	}

	@Override
	public Crest getCrest() {
		//		if (party.army != null)
		//			return party.army.getFaction().crest;
		//		return null;
		return null;
	}

	@Override
	public Soldier getSoldierInsteadOfCrest() {
		return this.soldier;
	}
}
