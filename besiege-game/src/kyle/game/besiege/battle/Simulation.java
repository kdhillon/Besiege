package kyle.game.besiege.battle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import kyle.game.besiege.Assets;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.PartyType.Type;
import kyle.game.besiege.party.UnitLoader;

public class Simulation extends Game {
	SpriteBatch batch;
	private MapScreen mapScreen;
	private BattleStage bs;

	@Override
	public void create () {
		Texture.setEnforcePotImages(false);
		
		Assets.load();
		// don't generate a new kingdom
		mapScreen = new MapScreen();

        // NOTE that the test parties may have forced culture types.
        CultureType type1 = UnitLoader.cultureTypes.get("Forest");
        if (Math.random() < 0.5) {
            type1 = UnitLoader.cultureTypes.get("Plains");
        }
        CultureType type2 = UnitLoader.cultureTypes.get("Tundra");
        if (Math.random() < 0.5) {
            type2 = UnitLoader.cultureTypes.get("Desert");
        }
        type1 = UnitLoader.cultureTypes.get("Jungle");
		type2 = UnitLoader.cultureTypes.get("Jungle");

		if (type1 == null || type2 == null) throw new AssertionError();

		bs = new BattleStage(mapScreen, PartyType.generatePartyType(Type.TEST_ALL, type1),
										PartyType.generatePartyType(Type.TEST_ALL, type2), 2);
		
		mapScreen.getSidePanel().initializePanels(bs.allies.parties.get(0));
		
		mapScreen.switchToBattleView(bs);
		
		setScreen(mapScreen); // eventually make mainMenu
	}
}
