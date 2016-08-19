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
//		Assets.load();
//		batch = new SpriteBatch();
//
//		bs = new BattleStage(null, null, null); 
//		
//		mainstage = new Stage();
//		OrthographicCamera battleCamera = new OrthographicCamera(BesiegeMain.WIDTH, BesiegeMain.HEIGHT);
//		mainstage.setCamera(battleCamera);
//		battleCamera.zoom = 1;
//		battleCamera.translate(0, 0);
//		
//		mainstage.addActor(bs);
		
		Texture.setEnforcePotImages(false);
		
		Assets.load();
		// don't generate a new kingdom
		mapScreen = new MapScreen();
		
//		mapScreen.resize(width, height);
		// create fake battlestage
		bs = new BattleStage(mapScreen, PartyType.getPartyType(Type.TEST_1, UnitLoader.classTypes.get("Basic")), 
										PartyType.getPartyType(Type.TEST_2, UnitLoader.classTypes.get("Basic"))); 
		mapScreen.switchToBattleView(bs);
		
//		mapTest = new MapTest();
		
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
