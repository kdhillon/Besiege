package kyle.game.besiege.battle;

import kyle.game.besiege.Assets;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.title.MainMenuScreen;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;

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
		mapScreen = new MapScreen(true);
		
//		mapScreen.resize(width, height);
		// create fake battlestage
		bs = new BattleStage(mapScreen, null, null, false, null); 
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
