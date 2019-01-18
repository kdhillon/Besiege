package kyle.game.besiege.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import kyle.game.besiege.BesiegeMain;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = BesiegeMain.GAME_NAME;
		cfg.useGL30 = false;
		cfg.vSyncEnabled = false;
		
		// prevents lag from swapbuffers
//		cfg.fullscreen = true;
//		cfg.width = 900;
//		cfg.height = 650;
		
		cfg.addIcon("textures/icon-temple.png", FileType.Internal);
		cfg.width = 1280;
		cfg.height = 800;
		
//		cfg.width = 1920;
//		cfg.height = 1080;
		new LwjglApplication(new BesiegeMain(), cfg);
	}
}
