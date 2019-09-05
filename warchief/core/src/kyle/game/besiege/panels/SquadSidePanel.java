package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import kyle.game.besiege.Assets;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.party.Squad;

// This guy is basically a mini side panel for each Squad panel. Draws a background for each squad panel, manages button
// presses, etc.
// It's not actually a side panel... only called that because it behaves in a similar way
public class SquadSidePanel extends Group implements PanelHolder {
    public static final int SQUAD_TABLE_HEIGHT = 500;
    private final String textureRegion = "panelBG";
    private TextureRegion region;

    SquadManagementScreen squadManagementScreen;
    SquadPanel squadPanel;
    Squad squad;

    public SquadSidePanel(SquadManagementScreen squadManagementScreen, Squad squad) {
        this.squadManagementScreen = squadManagementScreen;
        this.squad = squad;

        squadPanel = new SquadPanel(squadManagementScreen, this, squad);
        this.addActor(squadPanel);
        this.region = new TextureRegion(Assets.atlas.findRegion(textureRegion));
    }

    public SoldierTable getSoldierTable() {
        return squadPanel.getSoldierTable();
    }

    public Squad getSquad() {
        return squad;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
        super.draw(batch, parentAlpha);
    }

    @Override
    public float getHeight() {
        // TODO see what happens
        return SQUAD_TABLE_HEIGHT;
    }

    @Override
    public MapScreen getMapScreen() {
        return squadManagementScreen.getMapScreen();
    }

    @Override
    public void press(int buttonIndex) {

    }

    @Override
    public void returnToPrevious(boolean force) {

    }
}
