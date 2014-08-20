/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

public class Point implements Destination {
	private float x;
	private float y;
	
	public Point() {
		this.x = 0;
		this.y = 0;	
	}
	
	public Point(float x, float y) {
		this.x = x;
		this.y = y;	
	}
	
	public Point(double x, double y) {
		this.x = (float) x;
		this.y = (float) y;
	}
	
	@Override
	public float getX() {
		return x;
	}
	@Override
	public float getY() {
		return y;
	}
	@Override
	public float getCenterX() {
		return x;
	}
	@Override 
	public float getCenterY() {
		return y;
	}
	
	public void setPos(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String getName() {
		return x + ", " + y;
	}

	@Override
	public Faction getFaction() {
		return null;
	}

	@Override
	public DestType getType() {
		return Destination.DestType.POINT;
	}

//	@Override
//	public double distTo(Destination d) {
//		return Math.sqrt((d.getX())-getX())*(d.getX()-getX()+(d.getY()-getY())*(d.getY()-getY()));
//	}
//	@Override
//	public double distToCenter(Destination d) {
//		return Math.sqrt((d.getX()-getX())*(d.getX()-getX())+(d.getY()-getY())*(d.getY()-getY()));
//	}

	@Override
	public float getOriginX() {
		return 0;
	}

	@Override
	public float getOriginY() {
		return 0;
	}

	@Override
	public void setMouseOver(boolean b) {
		// do nothing
	}
}
