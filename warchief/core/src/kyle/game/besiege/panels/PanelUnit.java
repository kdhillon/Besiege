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
import kyle.game.besiege.tooltip.TextTooltip;

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

	public static TopTable getTopTable(final Soldier soldier, Unit unit, LabelStyle ls) {
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
		if (unit == null) {
			TextTooltip textTooltip = null;
			if (soldier.unitType.melee != null) textTooltip = new TextTooltip(soldier.unitType.melee.getStatsSummary());
			else if (soldier.unitType.ranged != null) textTooltip = new TextTooltip(soldier.unitType.ranged.getStatsSummary());
			topTable.addSubtitle("weapon", soldier.unitType.getWeaponSummary(), textTooltip);
		}
		else {
			TextTooltip textTooltip = new TextTooltip(soldier.unitType.melee.getStatsSummary());
			topTable.addSubtitle("weapon", soldier.unitType.melee.name, ls, textTooltip);
		}

		// Only do detialed ranged weapon if in a battle.
		if (unit != null) {
			if (soldier.unitType.ranged != null) {
				TextTooltip textTooltip = new TextTooltip(soldier.unitType.ranged.getStatsSummary());
				topTable.addSubtitle("ranged", soldier.unitType.ranged.name, ls, textTooltip);
			}

			if (soldier.unitType.shieldType != null) {
				TextTooltip textTooltip = new TextTooltip(soldier.unitType.shieldType.getStatsSummary());
				topTable.addSubtitle("shield", soldier.unitType.shieldType.name, ls, textTooltip);
			}

			String armor = soldier.unitType.armor.name;
			if (armor.equals("None")) armor = "Naked";

			TextTooltip textTooltip = new TextTooltip(soldier.unitType.armor.getStatsSummary());
			topTable.addSubtitle("armor", armor, ls, textTooltip);
		}

		topTable.addSmallLabel("level", "Level:");
		TextTooltip textTooltipAtk = new TextTooltip(soldier.getAtkMulti().getStringSummary());
		topTable.addSmallLabel("attack", "Atk:", textTooltipAtk);

		TextTooltip textTooltipHp = new TextTooltip(soldier.getHpMulti().getStringSummary());
		topTable.addSmallLabel("hp", "Max HP:", textTooltipHp);
		topTable.update("hp", ""+ df.format(soldier.getHp()));

		TextTooltip textTooltipDef = new TextTooltip(soldier.getDefMulti().getStringSummary());
		topTable.addSmallLabel("defense", "Def:", textTooltipDef);

		TextTooltip textTooltipSpd = new TextTooltip(soldier.getSpdMulti().getStringSummary());
		topTable.addSmallLabel("speed", "Spd:", textTooltipSpd);

		// TODO dispaly a summary string for morale
		topTable.addSmallLabel("morale", "Morale:");

		topTable.row();
		return topTable;
	}

	// this is probably pretty slow if these strings are being constructed every frame
	// TODO: an update system.
	@Override
	public void act(float delta) {
		topTable.update("level", ""+soldier.level);

		if (unit != null) {
			if (unit.isDying) {
				topTable.update("morale","Fallen", null, Color.RED);
			} else {
				topTable.update("morale", unit.bsp.getCurrentMoraleString(), null, unit.bsp.moraleColor);
			}
		}

		// TODO do this efficiently instead of redoing every time.
		if (unit != null && unit.rangedWeaponOut()) {
			// TODO allow update tooltip when unit changes weapons
			topTable.updateLabel("attack", "Rng:");
			topTable.update("attack",  df.format(unit.getCurrentRange()) );
		}
		else {
			topTable.update("attack",  df.format(soldier.getAtk()));
		}
		topTable.update("defense",  df.format(soldier.getDef()));
		topTable.update("speed", df.format(soldier.getSpd()));

		if (unit != null && soldier.unitType.ranged != null) {
			String toPut = soldier.unitType.ranged.name;
			toPut += " (" + unit.quiver + ")"; // + soldier.unitType.ammoType.name;
			topTable.update("ranged", toPut);
		}

		if (unit != null && unit.isShieldBroken()) {
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
