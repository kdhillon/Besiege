package kyle.game.besiege.battle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kyle.game.besiege.Assets;
import kyle.game.besiege.MapScreen;
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
        bs = new BattleStage(mapScreen, PartyType.getPartyType(Type.SCOUT, UnitLoader.cultureTypes.get("Tundra")),
										PartyType.getPartyType(Type.SCOUT, UnitLoader.cultureTypes.get("Aztec")));
		
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
