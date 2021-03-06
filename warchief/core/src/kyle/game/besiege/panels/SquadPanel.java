package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Squad;

public class SquadPanel extends Panel {

    // Represents a table for a single squad.
    // Drawn with fixed width and rounded corner background (same color as SidePanel on right?)
    //
    // Top part:
    // squad name ("Etoo's Militia", "Natahual Archers", "X's bodyguard")
    // stats (healthy/wounded out of total)
    // melee strength, range, etc (including bonuses from general)
    //
    // Selectable SoldierTable:
    // general at top
    // soldier list, name on left, troop type on right
    //
    // Note: buttons for removing troops, upgrading, promoting, etc are in the side panel as usual.
    //

    static final int WIDTH = SidePanel.WIDTH - 5; // sidepanel width is 190
    private SquadManagementScreen squadManagementScreen;

    private TopTable topTable;
    private Squad squad;
    private SquadSoldierTable soldierTable;

    public SquadPanel(SquadManagementScreen squadManagementScreen, SquadSidePanel squadSidePanel, Squad squad) {
        super(false);

        this.squadManagementScreen = squadManagementScreen;
        this.squad = squad;
        this.addParentPanel(squadSidePanel);
        this.resize();

        BackgroundColor backgroundColor = new BackgroundColor("whitepixel.png");
        int gray = 45;
        backgroundColor.setColor(gray,gray,gray,255);
//        this.setBackground(backgroundColor);
        this.setWidth(WIDTH);
//        this.setHeight(WarchiefGame.HEIGHT/2);

        topTable = new TopTable();
//        Label generalName = new Label(squad.getGeneral().getOfficialName(), SquadManagementScreen.mediumLs);
        this.addTopTable(topTable);
        topTable.updateTitle(squad.getGeneral().getOfficialName(), null, Color.WHITE);
        topTable.addSubtitle("class", "");
        topTable.addSmallLabel("size", "Size:");
        topTable.addSmallLabel("remaining", "Remaining:");

        soldierTable = new SquadSoldierTable(squadManagementScreen, this, squad);
        this.addSoldierTable(soldierTable);
//        this.add(soldierTable).top().expandY();
        this.update();
    }

    // Update this panel with any new information
    public void update() {
        soldierTable.update();
        topTable.update("size", squad.getHealthySize() + "/" + squad.getTotalSize());
        topTable.update("remaining", (squad.getGeneral().getMaxSquadSize() - squad.getTotalSize()) + "");
        topTable.update("class", squad.getMaxClass().name);
    }

    @Override
    protected void notifyDragStart(Soldier soldier) {
        // Handled in subclasses
        squadManagementScreen.notifyDragStart(soldier);
    }

    // This goes up the chain
    @Override
    protected void notifyDragRelease(Soldier soldier) {
        squadManagementScreen.notifyDragRelease(soldier);
    }

    public SquadSoldierTable getSquadSoldierTable() {
        return soldierTable;
    }

    @Override
    protected boolean shouldAddTimeAndPaused() {
        return false;
    }

    @Override
    boolean leaveSpaceForMinimap() {
        return false;
    }
//
//    public void resize() {
////        this.setHeight(WarchiefGame.HEIGHT/2);
//        super.resize();
//    }
    public SquadSoldierTable getSoldierTable() {
        return soldierTable;
    }
}
