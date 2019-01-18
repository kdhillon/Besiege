package kyle.game.besiege.battle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

import kyle.game.besiege.Assets;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.PartyType.Type;
import kyle.game.besiege.party.UnitLoader;
import kyle.game.besiege.title.MainMenuScreen;
import kyle.game.besiege.title.QuickBattleScreen;

import static kyle.game.besiege.battle.BattleMap.MapType.BEACH;

// TODO don't make this a game, but rather, a screen.
// that way, battle simulation can be a part of the main menu.
public class Simulation {
	Batch batch;
	private MapScreen mapScreen;
	private BattleStage bs;

	public Simulation(BattleOptions options, MainMenuScreen originalScreen) {
		// don't generate a new kingdom
		mapScreen = new MapScreen();

		bs = new BattleStage(mapScreen, options, originalScreen);

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
