package kyle.game.besiege.panels;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import kyle.game.besiege.Assets;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Squad;

/**
 * A soldier table specifically for SquadManagementScreen, allowing player to manage squads.
 */
public class SquadSoldierTable extends SoldierTable {

    SquadManagementScreen squadManagementScreen;

    private boolean entered;
    private Squad squad;

    public SquadSoldierTable(SquadManagementScreen squadManagementScreen, SquadPanel panel, Squad squad) {
        super(panel, null, false, null, null, null, squad);
        this.squad = squad;
        this.squadManagementScreen = squadManagementScreen;
        this.setLockSoldierExpand(true);
        this.squadSoldierTable = true;
        this.addListener();
        this.update();
    }

    @Override
    public void select(Soldier soldier) {
        super.select(soldier);
        squadManagementScreen.notifySelect(soldier, this);
    }

    @Override
    public void deselect() {
        super.deselect();
        squadManagementScreen.notifyDeselect();
    }

    // Add listener for entering/exiting the soldierTable
    private void addListener() {
        this.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                SquadSoldierTable.this.enter();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                SquadSoldierTable.this.exit();
            }
        });
    }

    // Notify this panel that a soldier is being released.
    // If this panel is currently being dragged over, add the soldier to this party and release it from the other party.
    // returns true if released
    public boolean  notifySoldierReleased(Soldier soldier) {
        if (entered) {
            if (!this.squad.isFull()) {
                if (soldier.squad != null) soldier.squad.removeSoldier(soldier);
                this.squad.addSoldier(soldier);
                exit();
                this.update();
                // TODO update squads on the battle map if they've already been placed.
                return true;
            }
        }
        return false;
    }

    private void enter() {
        if (squadManagementScreen.getSoldierBeingDragged() != null && squadManagementScreen.getSoldierBeingDragged().squad != squad) {
            this.entered = true;
            soldierTable.setBackground(Assets.ninepatchBackgroundLightGray);
        }
    }
    private void exit() {
        this.entered = false;
        soldierTable.setBackground(Assets.ninepatchBackgroundDarkGray);
    }
    public boolean entered() {
        return entered;
    }
}
