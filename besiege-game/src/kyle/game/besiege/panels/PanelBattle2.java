package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.esotericsoftware.tablelayout.Cell;
import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.battle.BattleStage;
import kyle.game.besiege.battle.BattleSubParty;
import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

public class PanelBattle2 extends Panel {
    private final float PAD = 10;
    private final float DESC_HEIGHT = 530;
    private final int r = 3;
    private SidePanel panel;
    public Battle battle;

    public final BattleStage battleStage;

    private TopTable topTableAttackers;
    private TopTable topTableDefenders;

    private StrictArray<SoldierTable> attackerSoldierTables;
    private StrictArray<SoldierTable> defenderSoldierTables;

    public PanelBattle2(SidePanel panel, Battle battle) {
        this.panel = panel;
        this.battle = battle;
        if (battle instanceof BattleStage) battleStage = (BattleStage) battle;
        else battleStage = null;

        this.addParentPanel(panel);

        Label.LabelStyle lsBig = new Label.LabelStyle();
        lsBig.font = Assets.pixel24;

        Label.LabelStyle lsFaction = new Label.LabelStyle();
        lsFaction.font = Assets.pixel18;

        String title = "Attackers";

        topTableAttackers = new TopTable();
        topTableAttackers.updateTitle(title, null);

        StrictArray<Party> attackingParties = battle.getAttackingParties();
        String attackingFactionName = "Independent";
        if (attackingParties.first().getFaction() != null)
            attackingFactionName = attackingParties.first().getFaction().getName();
        String attackerName = attackingParties.first().getName();
        if (attackingParties.size > 1) {
            for (int i = 1; i < attackingParties.size; i++) {
                attackerName += ", " + attackingParties.get(i).getName();
            }
        }
        topTableAttackers.addSubtitle("partyname", attackerName);
        topTableAttackers.addSubtitle("factionname", attackingFactionName);
        topTableAttackers.addBigLabel("size", "Size:");

        attackerSoldierTables = new StrictArray<>();
        attackerSoldierTables.size = attackingParties.size;
        for (int i = 0 ; i < attackingParties.size; i++) {
            Party p = attackingParties.get(i);
            SoldierTable attacker = new SoldierTable(p, true, battleStage);
            attackerSoldierTables.set(i, attacker);
            topTableAttackers.add(attacker).colspan(4).top().padTop(0).expandY();
            topTableAttackers.row();
            p.updated = true;
        }

        topTableAttackers.row();
        topTableAttackers.add().colspan(4).padBottom(PAD);
        topTableAttackers.row();

        topTableAttackers.row();

        this.addTopTable(topTableAttackers);

        topTableDefenders = new TopTable();

        topTableDefenders.updateTitle("Defenders", null);

        StrictArray<Party> defendingParties = battle.getDefendingParties();

        String defendingFactionName = "Independent";
        if (defendingParties.first().getFaction() != null)
            defendingFactionName = defendingParties.first().getFaction().getName();
//        topTableDefenders.addSubtitle("attackers", "Attackers");
        String defenderName = defendingParties.first().getName();
        if (defendingParties.size > 1) {
            for (int i = 1; i < defendingParties.size; i++) {
                defenderName += ", " + defendingParties.get(i).getName();
            }
        }
        topTableDefenders.addSubtitle("partyname", defenderName);
        topTableDefenders.addSubtitle("factionname", defendingFactionName);
        topTableDefenders.addBigLabel("size", "Size:");

        defenderSoldierTables = new StrictArray<>();
        for (int i = 0; i < defendingParties.size; i++) {
            Party p = defendingParties.get(i);
            SoldierTable defender = new SoldierTable(p, true, battleStage);
            defenderSoldierTables.set(i, defender);
            topTableDefenders.add(defender).colspan(4).top().padTop(0).expandY();
            topTableDefenders.row();
            p.updated = true;
        }

        topTableDefenders.add().colspan(4).padBottom(PAD);
        topTableDefenders.row();

        topTableDefenders.row();

        this.addTopTable2(topTableDefenders);

        if (getButton(2) == null) {
            if (battleStage != null && !battleStage.placementPhase) {
                boolean charging = true;
                if (battle.playerDefending()) {
                    for (BattleSubParty bsp : battleStage.getDefending().subparties) {
                        if (bsp.stance != Unit.Stance.AGGRESSIVE) {
                            charging = false;
                        }
                    }
                }
                if (battle.playerAttacking()) {
                    for (BattleSubParty bsp : battleStage.getAttacking().subparties) {
                        if (bsp.stance != Unit.Stance.AGGRESSIVE) {
                            charging = false;
                        }
                    }
                }
                if (!charging) {
                    this.setButton(2, "Charge!");
                    getButton(2).setDisabled(true);
                }
            }
        }

        // Put the balancebar in the middle
        topTableDefenders.addGreenBar();
    }

    private void actForPartyList(StrictArray<Party> parties, StrictArray<SoldierTable> soldierTables, TopTable topTable) {
        int totalSize = parties.first().getHealthySize();
        String size = "(" + parties.first().getHealthySize();
        System.out.println("Parties size: " + parties.size);
        for (int i = 0; i < parties.size; i++) {
            Party current = parties.get(i);
            System.out.println("checking " + current.getName());
            if (current.updated) {
                System.out.println("updating " + current.getName());
                soldierTables.get(i).update();
                current.updated = false;
                // Always update bar in defenders.
                topTableDefenders.updateGreenBar(1 - battle
                        .getBalanceDefenders());
            }
            if (i > 0) {
                totalSize += current.getHealthySize();
                size += " + " + current.getHealthySize();
            }
            // Recall that the bar is always on the defender table, I think.

        }
        size += ")";

        String toDisplay = totalSize + "";
        if (soldierTables.size > 1) {
            toDisplay +=  " " + size;
        }
        topTable.update("size", toDisplay);
    }

    @Override
    public void act(float delta) {
        // is either side fighting with allies?
        actForPartyList(battle.getAttackingParties(), attackerSoldierTables, topTableAttackers);
        actForPartyList(battle.getDefendingParties(), defenderSoldierTables, topTableDefenders);

        super.act(delta);
    }

    public static void switchToPanel(Soldier s) {
        MapScreen.sidePanelReference.setActiveUnit(s);
    }

    public void centerCamera() {
//        Camera camera = panel.getKingdom().getMapScreen().getCamera();
//        //		camera.translate(new Vector2(army.getCenterX()-camera.position.x, army.getCenterY()-camera.position.y));
//        camera.translate(new Vector3(army.getCenterX()-camera.position.x, army.getCenterY()-camera.position.y, 0));
    }

    @Override
    public void resize() {
        // TODO when you update these, make sure it preserves which subparties were expanded or not.
        for (int i = 0; i < attackerSoldierTables.size; i++) {
//            Cell cell = topTableAttackers.getCell(soldierTable);
//            cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
            SoldierTable soldierTable = new SoldierTable(battle.getAttackingParties().get(i), true, battleStage);
            defenderSoldierTables.removeIndex(i);
            attackerSoldierTables.set(i, soldierTable);
            soldierTable.update();
            battle.getAttackingParties().get(i).updated = true;
            soldierTable.setHeight((panel.getHeight() - DESC_HEIGHT));
//            cell.setWidget(soldierTable);
        }

        for (int i = 0; i < defenderSoldierTables.size; i++) {
//            Cell cell = topTableDefenders.getCell(soldierTable);
//            cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
            SoldierTable soldierTable = new SoldierTable(battle.getDefendingParties().get(i), true, battleStage);
            defenderSoldierTables.removeIndex(i);
            defenderSoldierTables.set(i, soldierTable);
            soldierTable.update();
            battle.getDefendingParties().get(i).updated = true;
            soldierTable.setHeight((panel.getHeight() - DESC_HEIGHT));
//            cell.setWidget(soldierTable);
        }
//
//        if (attackerSoldierTables.size > 0) throw new AssertionError();
//        if (defenderSoldierTables.size > 0) throw new AssertionError();
//
//        for (int i = 0; i < battle.getDefendingParties().size; i++) {
//
//        }

        super.resize();
    }

    @Override
    public void button1() {
        if (getButton(1).isVisible()) {


            if (battleStage == null) {
                //				battle.retreat(sidePanel.getKingdom().getPlayer());
                return;
            }
            else {
                if (battleStage.placementPhase) {
//					this.battleStage.toNextFormation();
                }
                else {
                    battleStage.placementPhase = false;
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
//					this.battleStage.togglePlayerStance();
                }
                // charge all (move "Begin!" to button 3)
                else {
                    battleStage.placementPhase = false;
                    battleStage.chargeAll(true);
                    //					getButton(2).setVisible(false);
                    getButton(2).setDisabled(true);
                }
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

    }

    @Override
    public Crest getCrest() {
        if (battle != null && battle.getAttackingFactionOrNull() != null)
            return battle.getAttackingFactionOrNull().crest;
        return null;
    }
}
