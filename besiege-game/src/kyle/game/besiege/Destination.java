/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

public interface Destination {
	public enum DestType {
		POINT, LOCATION, ARMY, BATTLE
	}
	
	public float getX();
	public float getY();
	public float getCenterX();
	public float getCenterY();
	public String getName();
	public Faction getFaction(); // (see faction list in Kingdom)
	public DestType getType(); // type of destination (0 = point, 1 = city, 2 = army, 4 = battle)
//	public double distTo(Destination d);
//	public double distToCenter(Destination d);
	public float getOriginX();
	public float getOriginY();
}

