package kyle.game.besiege.battle;

import com.badlogic.gdx.scenes.scene2d.Group;

public class FireContainer extends Group {
	
//	public void draw() {
		
//	}
	public void addFire(Fire fire) {
		this.addActor(fire);
	}
	
	public void updateRotation(float rotation) {
		setRotation(rotation);
	}
}
