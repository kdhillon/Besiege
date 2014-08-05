/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
// Entry point (starter class)

package kyle.game.besiege;

import kyle.game.besiege.title.MainMenuScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
	
public class BesiegeMain extends Game {
	public static int WIDTH = 400;
	public static int HEIGHT = 512;
	public static int appType; // 0: desktop, 1: android, 2: Html5, 3: iOS
	public MapScreen mapScreen;
	public MainMenuScreen mainMenuScreen;
		
	@Override
	public void create() {
		Texture.setEnforcePotImages(false);
		
		Assets.load();
		mapScreen = new MapScreen();
		mainMenuScreen = new MainMenuScreen(this);
		
//		mapTest = new MapTest();
		
		setScreen(mapScreen); // eventually make mainMenu
//		setScreen(mainMenuScreen);
		
		// doesn't work!pppppppppp
		switch (Gdx.app.getType()) {
			case Desktop:
				appType = 0; // CHANGE BACK
				break;
			case Android:
				appType = 1;
				break;
			case WebGL:
				appType = 2;
				break;
			case iOS:
				appType = 3;
			default:
				break;
		}	
	}
	
	public void setPlayerName(String text) {
		mapScreen.getCharacter().name = text;
		mapScreen.getKingdom().getPlayer().setName(text);
	}

	@Override
	public void dispose() {
		mapScreen.dispose();
		mainMenuScreen.dispose();
		Assets.dispose();
	}
		
}
