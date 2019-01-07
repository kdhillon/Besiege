package kyle.game.besiege.battle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import kyle.game.besiege.Assets;

public class QuickBattleGame extends Game {
    @Override
    public void create() {
        Texture.setEnforcePotImages(false);

        Assets.load();

        Simulation simulation = new Simulation();

        setScreen(simulation.getMapScreen()); // eventually make mainMenu
    }
}
