/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kyle.game.besiege.voronoi;


import kyle.game.besiege.Map;
import kyle.game.besiege.geom.PointH;

/**
 * Edge.java Function Date Jun 5, 2013
 *
 * @author Connor
 */
public class Edge {

	public int index;
	transient public Center d0, d1;  // Delaunay edge
	transient public Corner v0, v1;  // Voronoi edge

	public int adjCenter0 = -1, adjCenter1 = -1;
	public int adjCorner0 = -1, adjCorner1 = -1;

	public PointH midpoint;  // halfway between v0,v1
	public boolean impassable;

	public int river;
	public boolean road; // is there a road across this delaunay edge?

	public static final int subDivisions = 8;
	public static final double noiseFactor = .3;
	// this contains the list of randomly generated sub-edges, which can be drawn to add extra noise to edges
	// Subedges go from v0 to v1.
	public PointH[] subEdges;

	public void setVornoi(Corner v0, Corner v1) {
		this.v0 = v0;  this.adjCorner0 = v0.index;
		this.v1 = v1;  this.adjCorner1 = v1.index;
		midpoint = new PointH((v0.loc.x + v1.loc.x) / 2, (v0.loc.y + v1.loc.y) / 2);

		calculateSubedges();
	}

	public void calculateSubedges() {
		subEdges = new PointH[subDivisions - 1];

		recursiveSubdivide(-1, subDivisions-1, subEdges);	
	}

	// take midpoint of p1, p2. "push" in random direction by random value between 0 and factor*distance(p1, p2).
	// recursively do this on this midpoint and start and end points.
	public void recursiveSubdivide(int left, int right, PointH[] currentPoints) {
		if (right-left <= 1) return;
		System.out.println(left + " " + right);

		int midpoint = (right - left) / 2 + left;

		PointH leftPoint, rightPoint;
		if (left >= 0)
			leftPoint = currentPoints[left];
		else 
			leftPoint = v0.getLoc();
		if (right < currentPoints.length)
			rightPoint = currentPoints[right];
		else 
			rightPoint = v1.getLoc();

		double dist = PointH.distance(leftPoint, rightPoint);
		dist *= noiseFactor;

		double randomPush = dist * Math.random();
		double randomAngle = Math.random() * 360;

		// should be negative or positive
		float push_x = (float) (randomPush * Math.cos(randomAngle));
		float push_y = (float) (randomPush * Math.sin(randomAngle));

		currentPoints[midpoint] = leftPoint.midpoint(rightPoint);

		// push midpoint out appropriately    	
		currentPoints[midpoint].x += push_x;
		currentPoints[midpoint].y += push_y;

		recursiveSubdivide(left, midpoint, currentPoints);
		recursiveSubdivide(midpoint, right, currentPoints);
	}

	public boolean isImpassable() {
		return ((d0.water || d1.water));// && !(d0.water && d1.water));
	}
	public boolean isBorder() {
		return ((d0.water || d1.water) && !(d0.water && d1.water));
	}
	public void restoreFromVoronoi(VoronoiGraph vg) {
		if (adjCorner0 != -1) 
			v0 = vg.corners.get(adjCorner0);
		if (adjCorner1 != -1) 
			v1 = vg.corners.get(adjCorner1);
		if (adjCenter0 != -1) 
			d0 = vg.centers.get(adjCenter0);
		if (adjCenter1 != -1) 
			d1 = vg.centers.get(adjCenter1);
		
		if (!d0.water && !d1.water && river < Map.RIVER_THRESHOLD) {
			this.road = Math.random() < 0.5;
		}
	}
	// returns true if factions on either side are at war
	public boolean war(Map map) {
		if (map.getCenter(this.adjCenter0).faction != null && map.getCenter(this.adjCenter1).faction != null && map.getCenter(this.adjCenter0).faction.atWar(map.getCenter(this.adjCenter1).faction)) return true;
		return false;
	}
}
