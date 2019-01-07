package kyle.game.besiege.battle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import kyle.game.besiege.Assets;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.PartyType.Type;
import kyle.game.besiege.party.UnitLoader;

import static kyle.game.besiege.battle.BattleMap.MapType.BEACH;

// TODO don't make this a game, but rather, a screen.
// that way, battle simulation can be a part of the main menu.
public class Simulation {
	SpriteBatch batch;
	private MapScreen mapScreen;
	private BattleStage bs;

	public Simulation() {
		// don't generate a new kingdom
		mapScreen = new MapScreen();

		ArmyTable alliesTable = new ArmyTable();
		ArmyTable enemiesTable = new ArmyTable();

		// TODO this should be specified by the user in some dropdowns.
		alliesTable.setCultureType("Plains");
		enemiesTable.setCultureType("Forest");
		alliesTable.setPartyCount(1);
		enemiesTable.setPartyCount(1);
		alliesTable.setPartyTypeType(Type.NOBLE);
		enemiesTable.setPartyTypeType(Type.TEST_ALL);

//		pt1.forceUnitType(type1.units.get("Spearman (Vet)4"));
//		pt1.forceUnitType(type1.units.get("Archer3"));
//		pt2.forceUnitType(type1.units.get("Archer3"));

		BattleOptions options = new BattleOptions();
		options.allyOptions = alliesTable.getPartyOptions();
		options.enemyOptions = enemiesTable.getPartyOptions();

		bs = new BattleStage(mapScreen, options);

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
