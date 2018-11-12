package kyle.game.besiege.panels;

import com.esotericsoftware.tablelayout.Cell;
import kyle.game.besiege.Crest;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.battle.BattleStage;
import kyle.game.besiege.battle.VictoryManager;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

public class PanelPostBattle extends Panel {
    private static final String VICTORY_STRING = "Victorious!";
    private static final String DEFEAT_STRING = "Defeat!";

    private final float PAD = 10;
    private final float DESC_HEIGHT = 530;
    private final int r = 3;
    private SidePanel panel;
    public Battle battle;

    public BattleStage battleStage;

    private TopTable topTableAttackers;
    private TopTable topTableDefenders;

    private StrictArray<SoldierTable> soldierTables = new StrictArray<>();

    public PanelPostBattle(SidePanel panel, Battle battle, BattleStage stage) {
        this.panel = panel;
        this.battle = battle;
        this.battleStage = stage;

        this.addParentPanel(panel);

        if (!battle.isOver()) throw new AssertionError();

        String title;
        if (battle.playerAttacking()) {
            title = battle.didAttackersWin() ? VICTORY_STRING : DEFEAT_STRING;
        } else if (battle.playerDefending()) {
            title = battle.didAttackersWin() ? DEFEAT_STRING : VICTORY_STRING;
        } else {
            title = battle.didAttackersWin() ? "Attacker Victory" : "Defender Victory";
        }

        topTableAttackers = new TopTable();
        topTableAttackers.updateTitle(title, null);

        StrictArray<Party> attackingParties = battle.getAttackingParties();
        StrictArray<Party> defendingParties = battle.getDefendingParties();

        // TODO differentiate rewards between retreated parties and non-retreated.
        attackingParties.addAll(battle.getAttackingPartiesRetreated());
        defendingParties.addAll(battle.getDefendingPartiesRetreated());

        populateTopTable(topTableAttackers, attackingParties, battle.didAttackersWin(), battle);
        topTableDefenders = new TopTable();

        populateTopTable(topTableDefenders, defendingParties, !battle.didAttackersWin(), battle);
//
        attackingParties.first().updated = true;
        defendingParties.first().updated = true;

        // Figure out which one is the victor and put them on top. TODO
        this.addTopTable(topTableAttackers);
        this.addTopTable2(topTableDefenders);

        this.setButton(1, "Continue");
//        this.getButton(1).setDisabled(false);
//        this.getButton(1).setTouchable(Touchable.enabled);
    }
    
    private void populateTopTable(TopTable table, StrictArray<Party> parties, boolean didWin, Battle battle) {
        Party p = parties.first();
        VictoryManager vm = battle.getVictoryManager();

        String faction = "Independent";
        if (parties.first().getFaction() != null)
            faction = p.getFaction().getName();

//        table.addSubtitle("victor", didWin ? "Victor" : "Loser");
        table.addSubtitle("partyname", p.getName());
//        table.addSubtitle("factionname", faction);

//        table.addSubtitle("summary", "Battle Summary");

        table.addSmallLabel("lost", "Losses:");
//        table.addSmallLabel("wounded", "Wounded:");
        table.addSmallLabel("remaining", "Remain:");

        table.update("lost", vm.getKilledSoldiersIn(p).size + vm.getWoundedSoldiersIn(p).size + "");
//        table.update("wounded", vm.getWoundedSoldiersIn(p).size+"");

//        table.addSmallLabel("retreated", "Ret:");
//        table.addSmallLabel("remaining", "Remaining:");
//        table.addSmallLabel("empty", "");

        table.update("remaining", p.getHealthySize() + "");
//        table.update("retreated", "");
//        table.update("empty", "");

        StrictArray<StrictArray<Soldier>> consolidatedKilled = Party.getConsol(vm.getKilledSoldiersIn(p), true);
        StrictArray<StrictArray<Soldier>> consolidatedKWounded = Party.getConsol(vm.getWoundedSoldiersIn(p), true);

        SoldierTable soldierTable = new SoldierTable(null, true, battleStage, consolidatedKWounded, consolidatedKilled);
        parties.first().updated = true;
        table.add(soldierTable).colspan(4).top().padTop(0).expandY();
        soldierTables.add(soldierTable);
        soldierTable.update();

        table.row();
        table.add().colspan(4).padBottom(PAD);
        table.row();

        if (didWin) {
            table.addSubtitle("rewards", "Battle Rewards");

            table.addSmallLabel("morale", "Morale:");
            table.addSmallLabel("fame", "Fame:");

//            table.addSmallLabel("loot", "Items:");
            table.addSmallLabel("wealth", "Wealth:");

            table.addSmallLabel("exp", "Exp:");
//            table.addSmallLabel("prisoners", "Captured:");

            // TODO do this for all parties in the battle, not just the first
            table.update("morale", vm.moraleRewards.get(p) + "");
            table.update("fame", vm.fameRewards.get(p) + "");
//            table.update("loot", "n/a");
            table.update("wealth", vm.wealthRewards.get(p)+"");
            table.update("exp", vm.expRewards.get(p) + "");
//            table.update("prisoners", "n/a");
        }

        table.row();
    }

    @Override
    public void act(float delta) {
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
        for (SoldierTable soldierTable : soldierTables) {
            Cell cell = topTableAttackers.getCell(soldierTable);
            if (cell == null)
                cell = topTableDefenders.getCell(soldierTable);

//            cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
            // TODO replace this.
//            soldierTable = new SoldierTable(soldierTable.party, true, battleStage);
//
//            soldierTable.update();
//            soldierTable.setHeight(panel.getHeight() - DESC_HEIGHT);
//            cell.setWidget(soldierTable);
        }

        battle.getAttackingParties().first().updated = true;
        battle.getDefendingParties().first().updated = true;

        super.resize();
    }

    @Override
    public void button1() {
        System.out.println("Clicking b1 post battle");
        if (getButton(1).isVisible()) {
            battleStage.exitBattle();
        }
    }

    @Override
    public void button2() {
    }
    @Override
    public void button3() {
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
