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

    private SoldierTable soldierTableAttackers;
    private SoldierTable soldierTableDefenders;

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
            attackingFactionName = attackingParties.first().getFaction().name;
//        topTableAttackers.addSubtitle("attackers", "Attackers");
        topTableAttackers.addSubtitle("partyname", attackingParties.first().getName());
        topTableAttackers.addSubtitle("factionname", attackingFactionName);
        topTableAttackers.addSmallLabel("size", "Size:");
        topTableAttackers.addSmallLabel("empty1", "");

        soldierTableAttackers = new SoldierTable(attackingParties.first(), true, battleStage);
        attackingParties.first().updated = true;
        topTableAttackers.add(soldierTableAttackers).colspan(4).top().padTop(0).expandY();

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
            defendingFactionName = defendingParties.first().getFaction().name;
//        topTableDefenders.addSubtitle("attackers", "Attackers");
        topTableDefenders.addSubtitle("partyname", defendingParties.first().getName());
        topTableDefenders.addSubtitle("factionname", defendingFactionName);
        topTableDefenders.addSmallLabel("size", "Size:");
        topTableDefenders.addSmallLabel("empty1", "");

        soldierTableDefenders = new SoldierTable(defendingParties.first(), true, battleStage);
        topTableDefenders.add(soldierTableDefenders).colspan(4).top().padTop(0).expandY();

        topTableDefenders.row();
        topTableDefenders.add().colspan(4).padBottom(PAD);
        topTableDefenders.row();

        topTableDefenders.row();

        attackingParties.first().updated = true;
        defendingParties.first().updated = true;

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

            // don't call this every frame.
        if (attackingParties.first().updated) {
            topTableAttackers.update("size", attackingParties.first().getHealthySize()+"");
            soldierTableAttackers.update();
            attackingParties.first().updated = false;

            // Green bar is on the defenders table
            topTableDefenders.updateGreenBar(1-battle.getBalanceDefenders());
        }
        if (defendingParties.first().updated) {
            topTableDefenders.update("size", defendingParties.first().getHealthySize()+"");
            soldierTableDefenders.update();
            defendingParties.first().updated = false;

            // Green bar is on the defenders table
            topTableDefenders.updateGreenBar(1-battle.getBalanceDefenders());
        }

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
        Cell cell = topTableAttackers.getCell(soldierTableAttackers);
        cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
        soldierTableAttackers = new SoldierTable(battle.getAttackingParties().first(), true, battleStage);
        soldierTableAttackers.update();
        soldierTableAttackers.setHeight(panel.getHeight() - DESC_HEIGHT);
        cell.setWidget(soldierTableAttackers);

        Cell cell2 = topTableDefenders.getCell(soldierTableDefenders);
        cell2.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
        soldierTableDefenders = new SoldierTable(battle.getDefendingParties().first(), true, battleStage);
        soldierTableDefenders.update();

        soldierTableDefenders.setHeight(panel.getHeight() - DESC_HEIGHT);
        cell2.setWidget(soldierTableDefenders);

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
