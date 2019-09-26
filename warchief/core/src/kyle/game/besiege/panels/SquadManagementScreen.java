package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import kyle.game.besiege.Assets;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.WarchiefGame;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Squad;

import static kyle.game.besiege.panels.SquadSidePanel.SQUAD_TABLE_HEIGHT;

/**
 * Screen allowing player to manage squads. Can be drawn on top of either tactical or strategic map.
 * Note that in the future this gives us a hacky but effective way of tinting the entire screen (for lightning, etc)
 *
 * Layout looks like:
 *  - up to 5 panels horizontally across
 *  - as many rows as is necessary. Should only need
 */
public class SquadManagementScreen extends Group {
    private static final int PAD_BETWEEN = 15;
    private static final int OUTSIDE_PAD = 35;
    public static final Label.LabelStyle mediumLs = new Label.LabelStyle(Assets.pixel20forCities, Color.WHITE);

    SidePanel sidePanel;
    // Transparent background
    Table main;
    Party party;

    private StrictArray<SquadSidePanel> squadTables;
    private ScrollPane squadScrollPane;
    private Table squadScrollPaneTable;

    private Soldier soldierBeingDragged;

    public SquadManagementScreen(SidePanel sidePanel) {
        this.sidePanel = sidePanel;
        this.party = party;
        main = new Table();
        BackgroundColor backgroundColor = new BackgroundColor("whitepixel.png");
        backgroundColor.setColor(50, 50, 50, 200); // r, g, b, a
        main.setBackground(backgroundColor);
        main.setPosition(0, 0);
        squadTables = new StrictArray<SquadSidePanel>();
        this.setPosition(-getWidth(), BottomPanel.HEIGHT);
        this.addActor(main);
        resize();
        debugAll();
    }

    public void initialize(Party party) {
        this.party = party;
        initializeSquadTables();
    }

    private void initializeSquadTables() {
        squadScrollPaneTable = new Table(); // This should have a fixed height, but expand left/right to fit all the squads.
//        squadTable.setHeight(SQUAD_TABLE_HEIGHT);
        squadScrollPane = new ScrollPane(squadScrollPaneTable);
        squadScrollPane.setScrollBarTouch(false);
        squadScrollPane.setScrollbarsOnTop(true);
        squadScrollPane.setFadeScrollBars(false);
        main.add(squadScrollPane).height(SQUAD_TABLE_HEIGHT).width(this.getWidth() - OUTSIDE_PAD * 2);

        for (Squad s : party.squads) {
            SquadSidePanel squadSidePanel = new SquadSidePanel(this, s);
            squadTables.add(squadSidePanel);
            squadScrollPaneTable.add(squadSidePanel).padRight(PAD_BETWEEN).height(SQUAD_TABLE_HEIGHT).width(SquadPanel.WIDTH);
        }
    }

    public MapScreen getMapScreen() {
        return sidePanel.getMapScreen();
    }

    void notifyDragStart(Soldier soldier) {
        soldierBeingDragged = soldier;
        squadScrollPane.cancel();
    }

    void notifyDragRelease(Soldier soldier) {
        boolean released = false;
//        squadScrollPane.setScrollBarTouch(true);
        Squad originalSquad = soldier.squad;
        for (SquadSidePanel sidePanel : squadTables) {
            sidePanel.squadPanel.update();
            if (sidePanel.squadPanel.getSquadSoldierTable().notifySoldierReleased(soldier))
                 released = true;
        }
        if (released && originalSquad != null) {
            for (SquadSidePanel sidePanel : squadTables) {
                if (sidePanel.getSquad() == originalSquad) {
                    sidePanel.squadPanel.update();
                }
            }
        }
        soldierBeingDragged = null;
    }

    public void notifySelect(Soldier s, SoldierTable current) {
        // TODO deselect from all panels except current
        for (SquadSidePanel st : squadTables) {
            if (st.getSoldierTable() != current)
                st.getSoldierTable().deselect();
        }
        sidePanel.setActiveUnit(s);
    }

    public void notifyDeselect() {
        sidePanel.setDefault();
    }

    public void resize() {
        // Fill in all remaining UI space.
        this.setWidth(WarchiefGame.WIDTH - SidePanel.WIDTH);
        this.setHeight(WarchiefGame.HEIGHT - BottomPanel.HEIGHT);
        this.setPosition(-getWidth(), BottomPanel.HEIGHT);
        main.setSize(getWidth(), getHeight());

        // TODO resize the squadtables.
    }

    public Soldier getSoldierBeingDragged() {
        return soldierBeingDragged;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (soldierBeingDragged != null) {
            squadScrollPane.cancel();
            System.out.println("Dragging: " + soldierBeingDragged.getName());
        }
    }
}
