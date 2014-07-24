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
}
