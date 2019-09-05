package kyle.game.besiege.panels;

import com.badlogic.gdx.scenes.scene2d.Group;
import kyle.game.besiege.MapScreen;

public class MiniSidePanel extends Group implements PanelHolder {
    @Override
    public MapScreen getMapScreen() {
        return null;
    }

    @Override
    public void press(int buttonIndex) {

    }

    @Override
    public void returnToPrevious(boolean force) {

    }
}
