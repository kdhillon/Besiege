package kyle.game.besiege.battle;

import kyle.game.besiege.Assets;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.MapScreen;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;

public class CopyOfSimulation extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	Stage mainstage;
	MapScreen mapScreen;
	BattleStage bs;

	
	@Override
	public void create () {
		Assets.load();
		batch = new SpriteBatch();

		bs = new BattleStage(null, null, null); 
		
		mainstage = new Stage();
		OrthographicCamera battleCamera = new OrthographicCamera(BesiegeMain.WIDTH, BesiegeMain.HEIGHT);
		mainstage.setCamera(battleCamera);
		battleCamera.zoom = 1;
		battleCamera.translate(0, 0);
		
		mainstage.addActor(bs);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		mainstage.act();
		mainstage.draw();
	}
}
