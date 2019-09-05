package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import kyle.game.besiege.WarchiefGame;
import kyle.game.besiege.party.Squad;

public class SquadTable extends Panel {

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
    // special buttons for removing troops
    //

    static final int WIDTH = SidePanel.WIDTH - 5; // sidepanel width is 190
    private SquadManagementScreen squadManagementScreen;

    private TopTable topTable;
    private Squad squad;
    private SquadSoldierTable soldierTable;

    public SquadTable(SquadManagementScreen squadManagementScreen, Squad squad) {
        this.squadManagementScreen = squadManagementScreen;
        this.squad = squad;
        BackgroundColor backgroundColor = new BackgroundColor("whitepixel.png");
        int gray = 45;
        backgroundColor.setColor(gray,gray,gray,255);
//        this.setBackground(backgroundColor);
        this.setWidth(WIDTH);
        this.setHeight(WarchiefGame.HEIGHT/2);

        topTable = new TopTable();
//        Label generalName = new Label(squad.getGeneral().getOfficialName(), SquadManagementScreen.mediumLs);
        this.addTopTable(topTable);
        topTable.updateTitle(squad.getGeneral().getOfficialName(), null, Color.WHITE);

        soldierTable = new SquadSoldierTable(squadManagementScreen, squad);
        this.addSoldierTable(soldierTable);
//        this.add(soldierTable).top().expandY();
    }

    public void resize() {
        this.setHeight(WarchiefGame.HEIGHT/2);
    }
    public SquadSoldierTable getSoldierTable() {
        return soldierTable;
    }
}
