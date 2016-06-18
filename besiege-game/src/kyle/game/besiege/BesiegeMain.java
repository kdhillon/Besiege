/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
// Entry point (starter class)

package kyle.game.besiege;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import kyle.game.besiege.title.MainMenuScreen;
	
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
		mainMenuScreen = new MainMenuScreen(this);
		
//		mapTest = new MapTest();
		
//		setScreen(mapScreen); // eventually make mainMenu
		setScreen(mainMenuScreen);
		
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
	
	public void loadMapScreen() {
		mapScreen = new MapScreen(false, "Default");
	}
	
	
	public void createMapScreen(String name) {
		mapScreen = new MapScreen(true, name);
//		mapScreen.getCharacter().name = name;
//		mapScreen.getKingdom().getPlayer().setName(name);
	}
	
//	public void setPlayerName(String text) {
//		mapScreen.getCharacter().name = text;
//	}

	@Override
	public void dispose() {
		mapScreen.dispose();
		mainMenuScreen.dispose();
		Assets.dispose();
	}
		
}
