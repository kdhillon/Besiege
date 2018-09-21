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

    private Label.LabelStyle ls;
    private Label.LabelStyle lsG;	// wounded
    private Label.LabelStyle lsY; // upgrade

    public boolean playerTouched;

    public PanelBattle2(SidePanel panel, Battle battle) {
        this.panel = panel;
        this.battle = battle;

        this.addParentPanel(panel);

        Label.LabelStyle lsBig = new Label.LabelStyle();
        lsBig.font = Assets.pixel24;

        Label.LabelStyle lsFaction = new Label.LabelStyle();
        lsFaction.font = Assets.pixel18;

        ls = new Label.LabelStyle();
        ls.font = Assets.pixel16;

        lsG = new Label.LabelStyle();
        lsG.font = Assets.pixel16;
        lsG.fontColor = Color.GRAY;

        lsY = new Label.LabelStyle();
        lsY.font = Assets.pixel16;
        lsY.fontColor = Color.YELLOW;

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
        topTableAttackers.addBigLabel("size", "Size:");

        soldierTableAttackers = new SoldierTable(attackingParties.first(), true);
        attackingParties.first().updated = true;
        topTableAttackers.add(soldierTableAttackers).colspan(4).top().padTop(0).expandY();

        topTableAttackers.row();
        topTableAttackers.add().colspan(4).padBottom(PAD);
        topTableAttackers.row();

        topTableAttackers.row();

        this.addTopTable(topTableAttackers);

        topTableDefenders = new TopTable();
        StrictArray<Party> defendingParties = battle.getDefendingParties();

        String defendingFactionName = "Independent";
        if (attackingParties.first().getFaction() != null)
            defendingFactionName = defendingParties.first().getFaction().name;
//        topTableDefenders.addSubtitle("attackers", "Attackers");
        topTableDefenders.addSubtitle("partyname", defendingParties.first().getName());
        topTableDefenders.addSubtitle("factionname", defendingFactionName);
        topTableDefenders.addBigLabel("size", "Size:");

        soldierTableDefenders = new SoldierTable(defendingParties.first(), true);
        attackingParties.first().updated = true;
        topTableDefenders.add(soldierTableDefenders).colspan(4).top().padTop(0).expandY();

        topTableDefenders.row();
        topTableDefenders.add().colspan(4).padBottom(PAD);
        topTableDefenders.row();

        topTableDefenders.row();

        this.addTopTable2(topTableDefenders);
    }

    @Override
    public void act(float delta) {
        // is either side fighting with allies?
        boolean aAllies = false;
        boolean dAllies = false;
        StrictArray<Party> attackingParties = battle.getAttackingParties();
        StrictArray<Party> defendingParties = battle.getDefendingParties();

        if (attackingParties != null) {
            for (Party a : attackingParties)
                if (a.getFaction() != battle.getAttackingFactionOrNull()) aAllies = true;
            for (Party d : defendingParties)
                if (d.getFaction() != battle.getDefendingFactionOrNull()) dAllies = true;
        }
//        if (attackingParties.size >= 1) {
////            if (battle.getAttackingFactionOrNull() != null) {
////                if (aAllies)
////                    attackers.setText(battle.getAttackingFactionOrNull().name + " and allies");
////                else attackers.setText(battle.getAttackingFactionOrNull().name);
////            }
//
//            String trpsStrA = attackingParties.first().getHealthySize() + "";
//            for (Party p: attackingParties) {
//                if (p != attackingParties.first())
//                    trpsStrA += "+" + p.getHealthySize();
//            }
////            trpsA.setText(trpsStrA);
//
//            String defStrA = Panel.format(""+ attackingParties.first().getAvgDef(), 2);
//            for (Party p: attackingParties) {
//                if (p != attackingParties.first())
//                    defStrA = Panel.format(""+ p.getAvgDef(), 2);
//            }
////            defA.setText(defStrA);
//        }

//        if (army != null) {
//            if (army.playerTouched && !playerTouched) {
//                setButton(1, "Attack!");
//                setButton(2, "Withdraw");
//                setButton(4, null);
//                playerTouched = true;
//            } else if (!army.playerTouched && playerTouched) {
//                setButton(1, null);
//                setButton(2, null);
//                setButton(4, "Back");
//                playerTouched = false;
//            }
//            if (party.player) {
//                topTableAttackers.update("factionname", army.getFactionName(), null);
//                topTableAttackers.updateTitle(army.getName(), null);
//            }
//
//            if (army.getKingdom().getPlayer().isAtWar(army))
//                topTableAttackers.update("factionname", army.getFactionName() + " (at war)", null);
//            else
//                topTableAttackers.update("factionname", army.getFactionName(), null);
//
//            topTableAttackers.update("action", army.getAction(), null);
//            topTableAttackers.update("Morale", army.getMoraleString() + "");
//            topTableAttackers.update("Wealth", "" + army.getParty().wealth);
//
//            // set speed to be current travel speed, not playerPartyPanel speed
//            topTableAttackers.update("Spd", Panel.format("" + army.getSpeed() * Army.SPEED_DISPLAY_FACTOR, 2));
//        }
//        topTableAttackers.update("Size", party.getHealthySize()+"/"+party.getTotalSize()); //+"/"+playerPartyPanel.getMaxSize());
//        topTableAttackers.update("Atk", ""+ party.getAtk());
//        topTableAttackers.update("Def", Panel.format(""+party.getAvgDef(), 2));

        //spd.setText(Panel.format(""+playerPartyPanel.getAvgSpd(), 2));
            // don't call this every frame.
        if (attackingParties.first().updated) {
            topTableAttackers.update("size", attackingParties.first().getHealthySize()+"");
            soldierTableAttackers.update();
            attackingParties.first().updated = false;
        }
        if (defendingParties.first().updated) {
            topTableDefenders.update("size", defendingParties.first().getHealthySize()+"");
            soldierTableDefenders.update();
            defendingParties.first().updated = false;
        }

        if (battleStage != null && !battleStage.placementPhase) {
            this.setButton(2, "Charge!");

            // TODO remove comment
//			if (battleStage.allies.stance == Stance.AGGRESSIVE)
//				this.getButton(2).setDisabled(true);
            //			else this.setButton(2, null);
        }

        // minor leak is not here?
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
        soldierTableAttackers = new SoldierTable(battle.getAttackingParties().first(), true);
        soldierTableAttackers.update();
        soldierTableAttackers.setHeight(panel.getHeight() - DESC_HEIGHT);
        cell.setWidget(soldierTableAttackers);

        Cell cell2 = topTableDefenders.getCell(soldierTableDefenders);
        cell2.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
        soldierTableDefenders = new SoldierTable(battle.getDefendingParties().first(), true);
        soldierTableDefenders.update();
        soldierTableDefenders.setHeight(panel.getHeight() - DESC_HEIGHT);
        cell2.setWidget(soldierTableDefenders);
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
