package kyle.game.besiege.voronoi;

import kyle.game.besiege.Path;

/* Node created for each visible
 * corner (child) in every 'parent' corner (~N^2 corners)
 * needed by A* algorithm
 * created by Kyle
 */
public class CornerNode implements Comparable<CornerNode> {
	public Corner corner; // corner this represents
	public int index; // index in parent's visible array
	public double distance; // distance to parent fixed
	public double g = 0;
	public double f = Double.MAX_VALUE;
	public CornerNode prev; // previous node (used for traceback)
	
	public CornerNode(Corner parent, Corner child) {
		this.corner = child;
		this.distance = Path.heuristicDist(parent, child);
		this.index = parent.visibleCorners.size();
	}

	@Override
	public int compareTo(CornerNode that) {
		return (int) (f - that.f);
	}
}
