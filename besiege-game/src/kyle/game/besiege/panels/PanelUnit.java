/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import java.text.DecimalFormat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
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
	private BattleStage battleStage;
	private SidePanel panel;
	private Unit unit;
	protected Soldier soldier;
	private float max_hp;
	private Party party;

	protected TopTable topTable;

//	protected Table text;
//	protected Label title;
//	protected Label subTitle;
	private Label armyName;
	private Label partyName;
	private Label type;

	//	private Table stats;
	//	private Label nameS;
//	protected Label atkSC;
//	protected Label defSC;
//	protected Label levelSC;
//	protected Label hpSC;
//	protected Label moraleSC;
//	protected Label spdSC;

	protected Table generalStats;

//	protected Label levelS;
//	protected Label hpS;
//	protected Label moraleS;
//	protected Label atkS;
//	protected Label defS;
//	protected Label spdS;
//	private Label weaponS;
//	private Label armorS;
//	private Label equipmentS;
//	private Label actionS;

	//	private Table soldierTable;
	//	private ScrollPane soldierPane;

	protected LabelStyle ls;
	private LabelStyle lsMed;
	private LabelStyle lsG;

	protected DecimalFormat df = new DecimalFormat("0.00");

	// can be used for soldier or unit
	public PanelUnit(SidePanel panel, Unit unit, Soldier soldier) {
        this.panel = panel;

        this.unit = unit;

        this.party = soldier.party;

		if (unit != null) {
            this.battleStage = unit.stage;
            this.soldier = unit.soldier;
            soldier = unit.soldier;
            this.max_hp = soldier.getHp();
        } else {
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

        generalStats = new Table();

		topTable = new TopTable();
		topTable.updateTitle(soldier.getTypeName(), null, soldier.unitType.cultureType.colorLite);

		String name = soldier.getName();
		if (soldier.female) name += " (f)";
		else name += " (m)";

		topTable.addSubtitle("name", name, ls, null);

		// TODO should we have a party name?
		armyName = new Label("", ls);
		if (party.army != null)
			armyName.setText(party.army.getName());
		armyName.setAlignment(0, 0);
//		topTable.addSubtitle("party", armyName, ls, null);

		topTable.addTable(generalStats);

		if (unit != null) {
			topTable.addGreenBar();
			topTable.addSubtitle("action", unit.getStatus(), ls, null);
		}

		// TODO add subpanels for weapons and armor detailing their stats
		topTable.addSubtitle("weapon", soldier.unitType.melee.name, ls, null);

		if (soldier.unitType.ranged != null)
			topTable.addSubtitle("ranged", soldier.unitType.ranged.name, ls, null);

		if (soldier.unitType.shieldType != null)
			topTable.addSubtitle("shield", soldier.unitType.shieldType.name, ls, null);

		String armor = soldier.unitType.armor.name;
		if (armor.equals("None")) armor = "Naked";

		topTable.addSubtitle("armor", armor, ls, null);

		topTable.addSmallLabel("level", "Level:");
		topTable.addSmallLabel("attack", "Atk:");
		topTable.addSmallLabel("hp", "Max HP:");
		topTable.update("hp", ""+ df.format(soldier.getHp()));
		topTable.addSmallLabel("defense", "Def:");
		topTable.addSmallLabel("morale", "Morale:");
		topTable.addSmallLabel("speed", "Spd:");

		topTable.row();
		topTable.add().colspan(4).padBottom(PAD);
		topTable.row();

		topTable.row();

		this.addTopTable(topTable);

		if (soldier.availableForHire()) {
		    this.setButton(3, "Hire");
        }

//		if (battleStage == null)
			this.setButton(4, "Back");
	}

	// this is probably pretty slow if these strings are being constructed every frame
	// TODO: an update system.
	@Override
	public void act(float delta) {	
		//		if (!unit.isGeneral()) {

//		levelS.setText("" + soldier.level);
		topTable.update("level", ""+soldier.level);

//		hpS.setText(df.format(soldier.hp.getValue()));
		if (unit != null) {
//			moraleS.setText(unit.bsp.getCurrentMoraleString());
//			moraleS.setColor(unit.bsp.moraleColor);
			if (unit.isDying) {
				topTable.update("morale","Fallen", null, Color.RED);
			} else {
				topTable.update("morale", unit.bsp.getCurrentMoraleString(), null, unit.bsp.moraleColor);
			}
		}
		
//		expS.setText("" + (int) (unit.getFloorHeight()*10) / 10.0);

		if (unit != null && unit.rangedWeaponOut()) {
//			atkSC.setText("Pow:");
//			defSC.setText("Rng:");
//			atkS.setText("" + df.format(unit.getRangeDmg()));
//			defS.setText("" + df.format(unit.getBaseRange()));

			// TODO allow updating labels in addition to label values.
//			topTable.update("power", );
			topTable.updateLabel("attack", "Pow:");
			topTable.updateLabel("defense", "Rng:");

			topTable.update("attack",  df.format(unit.getRangeDmg()));
			topTable.update("defense",  df.format(unit.getBaseRange()));
		}
		else {
//			atkSC.setText("Atk:");
//			defSC.setText("Def:");
//			atkS.setText("" + df.format(soldier.getAtk()));
//			defS.setText("" + df.format(soldier.getDef()));

			topTable.update("attack",  df.format(soldier.getAtk()));
			topTable.update("defense",  df.format(soldier.getDef()));
		}
//		spdS.setText(df.format(soldier.getSpd()));
		topTable.update("speed", df.format(soldier.getSpd()));
		//		}

//		if (rangedWeaponS != null) {
//			String toPut = soldier.unitType.ranged.name;
//			if (unit != null) toPut += " (" + unit.quiver + ")"; // + soldier.unitType.ammoType.name;
//
//			rangedWeaponS.setText(toPut);
//
//		}
		if (soldier.unitType.ranged != null) {
			String toPut = soldier.unitType.ranged.name;
			if (unit != null) toPut += " (" + unit.quiver + ")"; // + soldier.unitType.ammoType.name;

			topTable.update("ranged", toPut);
		}

//		weaponS.setText(soldier.unitType.melee.name);

		if (unit != null && unit.isShieldBroken()) {
//            shieldS.setText(unit.shield.name + " (Broken)");
            topTable.update("shield", unit.shield.name + " (Broken)");
        }

		String mounted = "";
		if (unit != null) {
			if (unit.isMounted()) mounted = " (Mounted)";

			topTable.update("action",unit.getStatus() + mounted);
			topTable.updateGreenBar(unit.hp*1.0/max_hp);
		}

		if (battleStage != null) {
			if (battleStage.playerAttacking() || battleStage.playerDefending()) {
				if (!battleStage.placementPhase) {
					if (battleStage.retreatTimerPlayer <= 0 && !battleStage.allies.retreating) {
						if (this.getButton(1) == null) {
							this.setButton(1, "Retreat!");
							this.getButton(1).setDisabled(false);
						}
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
			if (unit.bsp.stance == Stance.AGGRESSIVE)
				this.getButton(2).setDisabled(true);
			else if (this.getButton(2) == null) {
				this.setButton(2, "Charge!");
			}
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
					battleStage.tryToRetreatAll(true);
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
	    if (getButton(3).isVisible()) {
            if (soldier.availableForHire()) {
                // hire
                if (sidePanel.previousPanel.getClass() == PanelHire.class) {
                    System.out.println("prev panel was panel hire");
                } else {
                    System.out.println("prev panel was: " + sidePanel.previousPanel.getClass().getName());
                }
            }
        }
	}
	@Override
	public void button4() {
		if (getButton(4).isVisible()) {
//			if (battleStage == null) {
				//				battle.retreat(sidePanel.getKingdom().getPlayer());
				sidePanel.returnToPrevious(true);
				if (battleStage != null) {
					battleStage.unselectUnit();
				}
				return;
//			}
		}
	}

	@Override
    public PanelUnit getPanelUnit() {
	    return this;
    }

    public Unit getUnit() {
	    return unit;
    }

	@Override
	public Crest getCrest() {
		//		if (playerPartyPanel.army != null)
		//			return playerPartyPanel.army.getFaction().crest;
		//		return null;
		return null;
	}

	@Override
	public Soldier getSoldierInsteadOfCrest() {
		return this.soldier;
	}
}
