/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.SoldierLabel;

public class PanelHire extends Panel {
    private final float MINI_PAD = 5;
    private final float DESC_HEIGHT = 300;
    private SidePanel panel;
    public Location location;

    private TopTable topTable;

    private HireSoldierTable soldierTable;

    private LabelStyle ls;
    private LabelStyle lsMed;
    private LabelStyle lsG;

    private SoldierLabel hireLabel;
    private Button hireButton;

//    private Soldier selected;

    public PanelHire(SidePanel panel, Location location) {
        this.panel = panel;
        this.location = location;
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

        topTable = new TopTable();
        topTable.updateTitle("Volunteers", new InputListener() {
            public boolean touchDown(InputEvent event, float x,
                                     float y, int pointer, int button) {
                return true;
            }

            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                centerCamera();
            }
        });
        topTable.addSubtitle("locationname", location.getName(), new InputListener() {
            public boolean touchDown(InputEvent event, float x,
                                     float y, int pointer, int button) {
                return true;
            }

            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                centerCamera();
            }
        });

        topTable.addBigLabel("PartySize", "Party Size:");
        topTable.addBigLabel("PartyWealth", "Party Wealth:");

        soldierTable = new HireSoldierTable(location.toHire, this);

        topTable.add(soldierTable).colspan(4).top().padTop(0).expandY();
        updateSoldierTable();

        topTable.row();
//        topTable.debug();
//        stats.debug();
        topTable.padLeft(MINI_PAD);
        this.addTopTable(topTable);

        this.setButton(1, "Hire");
        hideButton(1);

        this.setButton(2, "Hire All");
        getButton(2).setTouchable(Touchable.disabled);
        getButton(2).setVisible(false);

        this.setButton(4, "Back");

//
//		stats.defaults().left().padTop(NEG);
//		stats.add(nameS).colspan(4).width(SidePanel.WIDTH-PAD*2).fillX().expandX().padBottom(MINI_PAD);
//		stats.row();
//		stats.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
//		stats.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
//		stats.row();
//		stats.add(levelSC).padLeft(MINI_PAD);
//		stats.add(levelS);
//		stats.add(atkSC).padLeft(PAD);
//		stats.add(atkS);
//		stats.row();
//		stats.add(expSC).padLeft(MINI_PAD);
//		stats.add(expS);
//		stats.add(defSC).padLeft(PAD);
//		stats.add(defS);
//		stats.row();
//		stats.add(nextSC).padLeft(MINI_PAD);
//		stats.add(nextS);
//		stats.add(spdSC).padLeft(PAD);
//		stats.add(spdS);
//		stats.row();
//		stats.add(weaponSC).colspan(2).padLeft(MINI_PAD).padTop(0);
//		stats.add(weaponS).colspan(2).padTop(0);
//		stats.row();
//		stats.add(equipmentSC).colspan(2).padLeft(MINI_PAD).padTop(0).top();
//		stats.add(equipmentS).colspan(2).padTop(0);
//		stats.row();
//		stats.add(hireButton).colspan(4).center().padTop(PAD);

        //stats.debug();

//		text.add(stats).colspan(4).padTop(PAD);

//		this.addTopTable(text);
//		this.setButton(2, "Hire All");
//		this.setButton(4, "Back");
    }

    private void hideButton(int button) {
        getButton(button).setTouchable(Touchable.disabled);
        getButton(button).setVisible(false);
    }

    private void showButton(int button) {
        getButton(button).setTouchable(Touchable.enabled);
        getButton(button).setVisible(true);
    }

    private void centerCamera() {
        Camera camera = panel.getKingdom().getMapScreen().getCamera();
//		camera.translate(new Vector2(location.getCenterX()-camera.position.x, location.getCenterY()-camera.position.y));
        camera.translate(new Vector3(location.getCenterX() - camera.position.x, location.getCenterY() - camera.position.y, 0));
    }

    @Override
    public void act(float delta) {
        topTable.update("PartyWealth", "" + panel.getKingdom().getPlayer().getParty().wealth);
        topTable.update("PartySize", "" + panel.getKingdom().getPlayer().getPartyInfo());

//		if (location.getToHire() != null && location.getToHire().getHealthySize() == 0) getButton(2).setDisabled(true);
//		else getButton(2).setDisabled(false);

        super.act(delta);
    }

    private void updateSoldierTable() {
        if (location.getToHire() == null) {
            System.out.println("To hire at : " + location.getName() + " is null");
        }
        System.out.println("people in location: " + location.getToHire().getTotalSize());
        soldierTable.update();
    }

    // This soldier was selected by the soldiertable
    public void notifySelect(Soldier s) {
        showButton(1);
    }

//    public void select(Soldier s) {
//        this.selected = s;
//
//        hireLabel.soldier = s;
//        int cost = s.getBuyCost();
////		int cost = (int) (s.level*COST_FACTOR);
//        hireLabel.setText("Hire " + s.getTypeName() + " (" + cost + ")");
//        showButton(1);
//
//        updateSoldierTable();
//    }

//    public void deselect() {
//        this.selected = null;
//        hideButton(1);
//        updateSoldierTable();
//    }

    private void hire(Soldier s) {
        if (location.hire(panel.getKingdom().getPlayer().getParty(), s)) { // only if successfully hires
            String name = s.getTypeName();
            BottomPanel.log("Hired " + name);
            hideButton(1);
            soldierTable.notifySelectedSoldierRemoved();
        } else if (panel.getKingdom().getPlayer().getParty().isFull()) {
            BottomPanel.log("Can't hire " + s.getTypeName() + ". Party is full.");
        } else {
            BottomPanel.log("Can't afford " + s.getTypeName());
        }
    }

    public void hireSelected() {
        if (soldierTable.selected != null) {
            hire(soldierTable.selected);
            updateSoldierTable();
        }
    }

    public void hireAll() { //Fixed iterator problem
        location.getToHire().getHealthy().shrink();
        Array<Soldier> soldiers = location.getToHire().getHealthyCopy();
        soldiers.reverse(); // cheapest first!
        for (Soldier s : soldiers) {
            hire(s);
        }
        updateSoldierTable();
    }

    @Override
    public void resize() { // problem with getting scroll bar to appear...
        Cell cell = topTable.getCell(soldierTable);
        cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
        soldierTable = new HireSoldierTable(location.getToHire(), this);
        location.needsUpdate = true;
        soldierTable.setHeight(panel.getHeight() - DESC_HEIGHT);
        cell.setWidget(soldierTable);
        updateSoldierTable();

        super.resize();
    }

    @Override
    public void button1() {
        if (getButton(1).isVisible())
            hireSelected();
    }

    @Override
    public void button2() {
        if (getButton(2).isVisible())
            hireAll();
    }

    @Override
    public Crest getCrest() {
        if (soldierTable.selected == null)
            return location.getFaction().crest;
        else return null;
    }

    @Override
    public Soldier getSoldierInsteadOfCrest() {
        if (soldierTable.selected != null) return soldierTable.selected;
        return null;
    }
}
