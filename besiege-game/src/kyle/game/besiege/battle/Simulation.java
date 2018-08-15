package kyle.game.besiege.battle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kyle.game.besiege.Assets;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.PartyType.Type;
import kyle.game.besiege.party.UnitLoader;

public class Simulation extends Game {
	SpriteBatch batch;
	Texture img;
	Stage mainstage;
	MapScreen mapScreen;
	BattleStage bs;

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
        type1 = UnitLoader.cultureTypes.get("Plains");
        if (type1 == null || type2 == null) throw new AssertionError();

        bs = new BattleStage(mapScreen, PartyType.getPartyType(Type.TEST_1, type1),
										PartyType.getPartyType(Type.SCOUT, type2));
		
		mapScreen.getSidePanel().initializePanels(bs.allies.parties.get(0));
		
		mapScreen.switchToBattleView(bs);
		
		setScreen(mapScreen); // eventually make mainMenu
	}

//	@Override
//	public void render () {
//		Gdx.gl.glClearColor(0, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//		mainstage.act();
//		mainstage.draw();
//	}
}
