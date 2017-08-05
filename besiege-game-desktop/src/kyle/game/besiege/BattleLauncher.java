package kyle.game.besiege;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class BattleLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Besiege Battle Simulator";
		LwjglApplication app = new LwjglApplication(new kyle.game.besiege.battle.Simulation(), config);
		
		config.width = 1280;
		config.height = 800;
	}
}
