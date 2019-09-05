package kyle.game.besiege.panels;

import kyle.game.besiege.MapScreen;

public interface PanelHolder {

    MapScreen getMapScreen(); // maybe just make getKingdom

    float getHeight();

    void press(int buttonIndex);

    // TODO we probably don't need this for SMS
    void returnToPrevious(boolean force);
}
