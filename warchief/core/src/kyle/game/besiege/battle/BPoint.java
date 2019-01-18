package kyle.game.besiege.battle;

import kyle.game.besiege.battle.Unit.Orientation;


public class BPoint {
	public int pos_x;
	public int pos_y;
	public Orientation orientation;
	
	public BPoint(int pos_x, int pos_y) {
		this.pos_x = pos_x;
		this.pos_y = pos_y;
	}
	public BPoint(Unit u) {
		this.pos_x = u.pos_x;
		this.pos_y = u.pos_y;
	}
	public double distanceTo(BPoint that) {
		if (that == null) return Double.POSITIVE_INFINITY;
		return Math.sqrt((that.pos_x-this.pos_x)*(that.pos_x-this.pos_x) + (that.pos_y-this.pos_y)*(that.pos_y-this.pos_y));
	}

	@Override
	public String toString() {
		return pos_x + ", " + pos_y;
	}

	@Override
	public boolean equals(Object o) {
		if (pos_x == ((BPoint) o).pos_x && pos_y == ((BPoint) o).pos_y) return true;
		return false;
	}
}
