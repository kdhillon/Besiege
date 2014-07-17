/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

import kyle.game.besiege.army.Army;

// Actor containing both black fog and gray fog to show where player has los and has visited before.
// Can be toggled on and off in MapScreen. Uses an algorithm that checks larger squares of the map first, 
// so as to avoid drawing a few million small squares every frame. Algorithm can possibly be improved if
// too slow on android devices (can be made recursive).
public class Fog extends Actor {
	private final String fogTexture = "fog";
	private final String lightFogTexture = "lightFog";
	private final TextureRegion fog;
	private final TextureRegion lightFog;
	private final int squareWidth = 512;
	private final int squareHeight = 512;
	private final int minWidth = 4;
	private final int minHeight = 4;
	
	private MapScreen mapScreen;
	private boolean[][] fogArray;
	private boolean[][] lightFogArray;
	private Point test;
	
	public Fog(MapScreen mapScreen) {
		this.mapScreen = mapScreen;
		this.fog = Assets.atlas.findRegion(fogTexture);
		this.lightFog = Assets.atlas.findRegion(lightFogTexture);
		fogArray = new boolean[Map.WIDTH/minWidth][Map.HEIGHT/minHeight];
		lightFogArray = new boolean[Map.WIDTH/minWidth][Map.HEIGHT/minHeight];
		test = new Point(0, 0);
	
	}
	public void updateFog() {
		updateFog((int) mapScreen.getKingdom().getPlayer().getCenterX(), (int) mapScreen.getKingdom().getPlayer().getCenterY(), (int) mapScreen.getKingdom().getPlayer().getLineOfSight());
	}
	// works in a circle around the player, only dealing with points in the square containing the circle
	public void updateFog(int centerX, int centerY, int LOS) {
		// workaround for glitchy "false" setting above which leaves some squares behind. this sets all false.
		for (int i = 0; i < lightFogArray.length; i++) {
			for (int j = 0; j < lightFogArray.length; j++) {
				lightFogArray[i][j] = false;
			}
		}
		for (int i = ((centerX - LOS)/minWidth); i < ((centerX + LOS)/minWidth); i++) {
			for (int j = ((centerY - LOS)/minHeight); j < ((centerY + LOS)/minHeight); j++) {
				if (distToCenter(i, j, mapScreen.getKingdom().getPlayer()) <= LOS) {
					fogArray[i][j] = true;
					lightFogArray[i][j] = true;
				}
				else lightFogArray[i][j] = false; // breaks on certain squares for some reason...
			}
		}
	}
	private double distToCenter(int i, int j, Army a) {
		test.setPos(i*minWidth + minWidth/2, j*minHeight + minHeight/2);
		return Kingdom.distBetween(a, test);
	}
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		if (mapScreen.fogOn) {
			
			
			for (int i = 0; i < Map.WIDTH; i+= squareWidth) { // cycles through big squares
				for (int j = 0; j < Map.HEIGHT; j+= squareHeight) {
					//cycles through fogarray to check if any in big square are true
					boolean squareGray = true;
					for (int k = i; k < i + squareWidth; k += minWidth) {
						for (int l = j; l < j + squareHeight; l += minHeight) {
							if (lightFogArray[k/minWidth][l/minHeight]) squareGray = false;
						}
					}
					if (squareGray) {
						batch.draw(lightFog, i, j, squareWidth, squareHeight);
					}
					else {
						for (int k = i; k < i + squareWidth; k += minWidth) {
							for (int l = j; l < j + squareHeight; l += minHeight) {
								if (!lightFogArray[k/minWidth][l/minHeight]) batch.draw(lightFog, k, l, minWidth, minHeight);
							}
						}
					}
					
					boolean squareBlack = true;
					for (int k = i; k < i + squareWidth; k += minWidth) {
						for (int l = j; l < j + squareHeight; l += minHeight) {
							if (fogArray[k/minWidth][l/minHeight]) squareBlack = false;
						}
					}
					if (squareBlack) {
						batch.draw(fog, i, j, squareWidth, squareHeight);
					}
					else {
						for (int k = i; k < i + squareWidth; k += minWidth) {
							for (int l = j; l < j + squareHeight; l += minHeight) {
								if (!fogArray[k/minWidth][l/minHeight]) batch.draw(fog, k, l, minWidth, minHeight);
							}
						}
					}
					
				}
			}
		}
//		
//		
//		
//		
//		if (mapScreen.fogOn) {
//			super.draw(batch, parentAlpha);
//			for (int i = 0; i < fogArray.length; i++) {
//				for (int j = 0; j < fogArray.length; j++) {
//					if (!fogArray[i][j])
//						batch.draw(fog, i*minWidth, j*minHeight); // change to different width/heights
//				}
//			}
//		}
	}
	
}
