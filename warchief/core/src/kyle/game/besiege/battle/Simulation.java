package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.g2d.Batch;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.title.QuickBattleScreen;

// TODO don't make this a game, but rather, a screen.
// that way, battle simulation can be a part of the main menu.
public class Simulation {
	Batch batch;
	private MapScreen mapScreen;
	private BattleStage bs;

	public Simulation(BattleOptions options, QuickBattleScreen quickBattleScreen) {
		// don't generate a new kingdom
		mapScreen = new MapScreen();

		bs = new BattleStage(mapScreen, options, quickBattleScreen);

		mapScreen.getSidePanel().initializePanels(bs.allies.parties.get(0));

		// Options:
			// Map

		// TODO make this replayable for infinite simulation fun.
		mapScreen.switchToBattleView(bs);
	}

	public MapScreen getMapScreen() {
		return mapScreen;
	}
}
