package kyle.game.besiege.battle;

import com.badlogic.gdx.Game;
import kyle.game.besiege.Assets;

public class QuickBattleGame extends Game {
    @Override
    public void create() {
        Assets.load();

        // TODO make this real.
        BattleOptions options = new BattleOptions();

        Simulation simulation = new Simulation(options, null);

        setScreen(simulation.getMapScreen()); // eventually make mainMenu
    }
}
