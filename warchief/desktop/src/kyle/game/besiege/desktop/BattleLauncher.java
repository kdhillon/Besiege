package kyle.game.besiege.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import kyle.game.besiege.WarchiefGame;
import kyle.game.besiege.battle.QuickBattleGame;

public class BattleLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.useGL30 = false;
		config.title = WarchiefGame.GAME_NAME + " Battle Simulator";
		LwjglApplication app = new LwjglApplication(new QuickBattleGame(), config);
        config.addIcon("textures/icon-temple.png", Files.FileType.Internal);

        config.width = 1280;
		config.height = 800;
	}
}
