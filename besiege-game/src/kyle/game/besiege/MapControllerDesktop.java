/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class MapControllerDesktop implements InputProcessor {
	OrthographicCamera camera;
	MapScreen screen;
	private float zoomFactor = (1f/20);

	public MapControllerDesktop(OrthographicCamera camera, MapScreen screen) {
		this.camera = camera;
		this.screen = screen;
//		if (Gdx.app.getType() == ApplicationType.WebGL)
//			zoomFactor = 1f/2000;
	}

	@Override
	public boolean keyDown(int keycode) {
		// this will update camera position on desktop!
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

//	@Override
//	public boolean scrolled(int amount) {
//		// TODO Auto-generated method stub
//		return false;
//	}

	@Override
	public boolean scrolled(int amount) {
		if (Gdx.app.getType() == ApplicationType.Desktop)
			screen.zoom(amount*camera.zoom*zoomFactor);
		return false;
	}

}
