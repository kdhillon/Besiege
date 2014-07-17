/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import kyle.game.besiege.panels.BottomPanel;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;

public class MapControllerAndroid implements GestureListener {
	private OrthographicCamera camera;
	private MapScreen screen;
	
	public MapControllerAndroid(OrthographicCamera camera, MapScreen screen) {
		this.camera = camera;
		this.screen = screen;
	}
	
	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		screen.click(0);
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		camera.translate(-deltaX, deltaY, 0);
		BottomPanel.log("panning x:" + deltaX + " and delta y: " + deltaY);
		BottomPanel.log("panning x and y: " + x + ", " + y);
		return true;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		screen.zoom((initialDistance - distance)/10000);
		return true;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
//		Vector2 a = initialPointer2.sub(initialPointer1).nor();
//		Vector2 b = pointer2.sub(pointer1).nor();
//		float angle = (float)Math.toDegrees((Math.atan2(a.y, a.x) - Math.atan2(b.y, b.x))/100);		
//		camera.rotate(angle);
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}
}
