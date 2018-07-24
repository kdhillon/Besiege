/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.voronoi.Center;

public class Fog extends Actor {
    private Kingdom kingdom;
    public Fog(Kingdom kingdom) {
        this.kingdom = kingdom;
    }
	
	public void playerIn(Center center) {
        center.discover();
        for (Center adj : center.neighbors) {
            adj.discover();
		}
        updateAllLocations();
    }

	// Updates any locations that are within this center or only border discovered corners as discovered
	private void updateAllLocations() {
        StrictArray<Location> locations = kingdom.getAllLocationsCopy();
        for (Location location : locations) {
            if (location.isDiscovered()) {
                continue;
            }

            // Check if the location is within a discovered center
            if (location.getCenter() != null) {
                if (location.getCenter().discovered) {
                    location.setDiscovered();
                }
            } else if (location.getCorner() != null) {
                boolean visible = true;
                // Check if the location is surrounded by discovered corners (or water)
                for (Center touchesCorner : location.getCorner().touches) {
                    if (!touchesCorner.water && !touchesCorner.discovered) {
                        visible = false;
                    }
                }
                if (visible) location.setDiscovered();
            }
        }
    }

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
//		if (mapScreen.fogOn) {
//			for (Center c : undiscovered) {
//				
//			}
//		}
	}
}
