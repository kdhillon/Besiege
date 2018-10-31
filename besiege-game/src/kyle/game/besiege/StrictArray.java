package kyle.game.besiege;

import com.badlogic.gdx.utils.Array;

public class StrictArray<Object> extends Array<Object> {
	
	public StrictArray() {
		super();
	}
	
	public StrictArray(StrictArray<Object> s) {
		super(s);
	}
	
	@Override
	public void add(Object s) {
		if (this.contains(s, true)) return;

		super.add(s);
	}
}
