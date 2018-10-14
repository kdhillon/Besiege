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

    public BattleStage battleStage;

    private TopTable topTableAttackers;
    private TopTable topTableDefenders;

    private StrictArray<SoldierTable> attackerSoldierTables;
    private StrictArray<SoldierTable> defenderSoldierTables;

    public PanelBattle2(SidePanel panel, Battle battle) {
        this.panel = panel;
        this.battle = battle;

        this.addParentPanel(panel);

        Label.LabelStyle lsBig = new Label.LabelStyle();
        lsBig.font = Assets.pixel24;

        Label.LabelStyle lsFaction = new Label.LabelStyle();
        lsFaction.font = Assets.pixel18;

        String title = "Battle!";

        topTableAttackers = new TopTable();
        topTableAttackers.updateTitle(title, null);

        StrictArray<Party> attackingParties = battle.getAttackingParties();
        String attackingFactionName = "Independent";
        if (attackingParties.first().getFaction() != null)
            attackingFactionName = attackingParties.first().getFaction().getName();
//        topTableAttackers.addSubtitle("attackers", "Attackers");
        topTableAttackers.addSubtitle("partyname", attackingParties.first().getName());
        topTableAttackers.addSubtitle("factionname", attackingFactionName);
        topTableAttackers.addSmallLabel("size", "Size:");
        topTableAttackers.addSmallLabel("empty1", "");

        attackerSoldierTables = new StrictArray<>();
        for (Party p : attackingParties) {
            SoldierTable attacker = new SoldierTable(p, true, battleStage);
            attackerSoldierTables.add(attacker);
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

        // Put the balancebar in the middle
        topTableDefenders.addGreenBar();

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
        topTableDefenders.addSmallLabel("size", "Size:");
        topTableDefenders.addSmallLabel("empty1", "");

        defenderSoldierTables = new StrictArray<>();
        for (Party p : defendingParties) {
            SoldierTable defender = new SoldierTable(p, true, battleStage);
            defenderSoldierTables.add(defender);
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
    }

    @Override
    public void act(float delta) {
        // is either side fighting with allies?
        StrictArray<Party> attackingParties = battle.getAttackingParties();
        StrictArray<Party> defendingParties = battle.getDefendingParties();

        String sizeAtk = attackingParties.first().getHealthySize() + "";
        for (int i = 0; i < attackerSoldierTables.size; i++) {
            if (attackingParties.get(i).updated) {
                attackerSoldierTables.get(i).update();
                attackingParties.get(i).updated = false;
                topTableDefenders.updateGreenBar(1 - battle
                        .getBalanceDefenders());
            }
            if (i > 0)
                sizeAtk += " + " + attackingParties.get(i).getHealthySize();
            // Recall that the bar is always on the defender table, I think.

        }
        topTableAttackers.update("size", sizeAtk);

        String sizeDef = defendingParties.first().getHealthySize() + "";
        for (int i = 0; i < defenderSoldierTables.size; i++) {
            if (defendingParties.get(i).updated) {
                defenderSoldierTables.get(i).update();
                defendingParties.get(i).updated = false;
                topTableDefenders.updateGreenBar(1 - battle
                        .getBalanceDefenders());
            }
            if (i > 0)
                sizeDef += " + " + defendingParties.get(i).getHealthySize();

        }
        topTableDefenders.update("size", sizeDef);

        // Green bar is on the defenders table

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
        for (int i = 0; i < attackerSoldierTables.size; i++) {
            SoldierTable soldierTable = attackerSoldierTables.get(i);
            attackerSoldierTables.removeValue(soldierTable, true);
            Cell cell = topTableAttackers.getCell(soldierTable);
            cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
            soldierTable = new SoldierTable(battle.getAttackingParties().get(i), true, battleStage);
            attackerSoldierTables.add(soldierTable);
            soldierTable.update();
            soldierTable.setHeight(panel.getHeight() - DESC_HEIGHT);
            cell.setWidget(soldierTable);
        }

        for (int i = 0; i < defenderSoldierTables.size; i++) {
            SoldierTable soldierTable = defenderSoldierTables.get(i);
            defenderSoldierTables.removeValue(soldierTable, true);
            Cell cell = topTableDefenders.getCell(soldierTable);
            cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
            soldierTable = new SoldierTable(battle.getDefendingParties().get(i), true, battleStage);
            defenderSoldierTables.add(soldierTable);
            soldierTable.update();
            soldierTable.setHeight(panel.getHeight() - DESC_HEIGHT);
            cell.setWidget(soldierTable);
        }

        battle.getAttackingParties().first().updated = true;
        battle.getDefendingParties().first().updated = true;

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
