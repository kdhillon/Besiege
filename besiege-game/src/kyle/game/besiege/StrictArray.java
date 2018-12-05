package kyle.game.besiege;

import com.badlogic.gdx.utils.Array;

/** This class guarantees that it will only have at most 1 of each item (using == comparison) */
public class StrictArray<T> extends Array<T> {
	
	public StrictArray() {
		super();
	}
	
	public StrictArray(StrictArray<T> s) {
		super(s);
	}
	public StrictArray(T[] s) {
		super();
		// Do it manually to preserve strictness
		for (T t : s) {
			add(t);
		}
	}

	@Override
	public void add(T s) {
		if (this.contains(s, true)) return;

		super.add(s);
	}

	public void addAllFromStrictArray(StrictArray<?> array) {
		// In this case it's ok to directly call this because the guarantee of having no duplicates is still valid.
		super.addAll((StrictArray<T>) array);
	}

	@Override
	public void addAll (Array<? extends T> array) {
		throw new AssertionError("Please don't use addAll because you may add duplicates. Use the special addStrictArray method.");
	}

//	@Override
//	public void addAll (Array<? extends T> array, int offset, int length) {
//		throw new AssertionError("Please don't use addAll because you may add duplicates. Use the special addStrictArray method.");
//	}

	@Override
	public void addAll (T[] array) {
		throw new AssertionError("Please don't use addAll because you may add duplicates. Use the special addStrictArray method.");
	}

//	public void addAll (T[] array, int offset, int length) {
//		throw new AssertionError("Please don't use addAll because you may add duplicates. Use the special addStrictArray method.");
//	}

	@Override
	public boolean removeValue(T s, boolean identity) {
		boolean removed = super.removeValue(s, identity);
		if (identity && this.contains(s, true))
			throw new AssertionError("was removed: " + removed);
		return removed;
	}
}
