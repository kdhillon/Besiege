/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.battle.BattleStage;
import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.party.General;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

import java.text.DecimalFormat;

public class PanelUnit extends Panel { 
	protected static final float PAD = 10;
	protected static final float MINI_PAD = 5;
	public static final float NEG = -5;
	private final float DESC_HEIGHT = 530;
	private BattleStage battleStage;
	private SidePanel sidePanel;
	private Unit unit;
	protected Soldier soldier;
	private float max_hp;
	private Party party;

	protected TopTable topTable;

	private Label armyName;
	protected Table generalStats;

	protected LabelStyle ls;
	private LabelStyle lsMed;
	private LabelStyle lsG;

	public static DecimalFormat df = new DecimalFormat("0.00");

	// can be used for soldier or unit
	public PanelUnit(SidePanel panel, Unit unit, Soldier soldier) {
        this.sidePanel = panel;

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

		topTable = getTopTable(soldier, unit, ls);

		this.addTopTable(topTable);

		if (soldier.availableForHire()) {
		    this.setButton(3, "Hire");
        }

//		if (battleStage == null)
			this.setButton(4, "Back");
	}

	public static TopTable getTopTable(Soldier soldier, Unit unit, LabelStyle ls) {
		TopTable topTable = new TopTable();
		topTable.updateTitle(soldier.getTypeName(), null, soldier.unitType.cultureType.colorLite);

		String name = soldier.getName();
		if (soldier.female) name += " (f)";
		else name += " (m)";

		topTable.addSubtitle("name", name, null);

//		// TODO should we have a party name?
//		armyName = new Label("", ls);
//		if (party.army != null)
//			armyName.setText(party.army.getName());
//		armyName.setAlignment(0, 0);
//		topTable.addSubtitle("party", armyName, ls, null);

		if (soldier.isGeneral()) {
			topTable.addTable(PanelGeneral.getGeneralStats((General) soldier, ls));
		}

		if (unit != null) {
			topTable.addGreenBar();
			topTable.addSubtitle("action", unit.getStatus(), ls, null);
		}

		// TODO add subpanels for weapons and armor detailing their stats

		// If not in battle, use a special minimized format.
		if (unit == null)
			topTable.addSubtitle("weapon", soldier.unitType.getWeaponSummary(), null);
		else
			topTable.addSubtitle("weapon", soldier.unitType.melee.name, ls, null);

		// Only do detialed ranged weapon if in a battle.
		if (unit != null) {
			if (soldier.unitType.ranged != null)
				topTable.addSubtitle("ranged", soldier.unitType.ranged.name, ls, null);

			if (soldier.unitType.shieldType != null)
				topTable.addSubtitle("shield", soldier.unitType.shieldType.name, ls, null);

			String armor = soldier.unitType.armor.name;
			if (armor.equals("None")) armor = "Naked";

			topTable.addSubtitle("armor", armor, ls, null);
		}

		topTable.addSmallLabel("level", "Level:");
		topTable.addSmallLabel("attack", "Atk:");
		topTable.addSmallLabel("hp", "Max HP:");
		topTable.update("hp", ""+ df.format(soldier.getHp()));
		topTable.addSmallLabel("defense", "Def:");
		topTable.addSmallLabel("morale", "Morale:");
		topTable.addSmallLabel("speed", "Spd:");

		topTable.row();
		return topTable;
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
		if (unit != null && soldier.unitType.ranged != null) {
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
					battleStage.updateRetreatButtons(this, unit);
				}
				else {
					this.setButton(1, unit.bsp.formation.name);
					this.setButton(2, unit.bsp.stance.name());
				}
			}
		}

		if (battleStage != null && !battleStage.placementPhase) {
			if (unit.bsp.stance != Stance.DEFENSIVE) {
				this.getButton(2).setDisabled(true);
			} else {
				this.setButton(2, "Charge!");
			}
			battleStage.updateChargeButton(this);
		}
//			else this.setButton(2, null);

		super.act(delta);
	}

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
					battleStage.retreatButton(this);
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
            if (battleStage == null && soldier.availableForHire()) {
                // hire
                if (sidePanel.previousPanel.getClass() == PanelHire.class) {
                    System.out.println("prev panel was panel hire");
                } else {
                    System.out.println("prev panel was: " + sidePanel.previousPanel.getClass().getName());
                }
            }
            if (battleStage != null) {
				battleStage.chargeAllButton(this);
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
