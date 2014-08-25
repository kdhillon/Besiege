/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kyle.game.besiege.voronoi;


import java.util.ArrayList;

import kyle.game.besiege.Map;
import kyle.game.besiege.geom.PointH;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Corner.java Function Date Jun 6, 2013
 *
 * @author Connor
 * modified by Kyle
 */
public class Corner {
	private final float PUSH_DIST = 5; //distance corners will be pushed away from convex corners
	
	transient public ArrayList<Center> touches = new ArrayList<Center>(); // can be easily reconstructed from edge
	transient public ArrayList<Corner> adjacent = new ArrayList<Corner>(); // can be easily reconstructed from edge
	transient public ArrayList<Edge> protrudes = new ArrayList<Edge>();
	transient public ArrayList<Corner> visibleCorners; // can be recalculated if necessary...
	
	// used for serialization
	public ArrayList<Integer> adjEdges = new ArrayList<Integer>();
	public ArrayList<Integer> visibleCornersIndices = new ArrayList<Integer>();
	// distance to bordercorner at index, if non-null/0 then it's visible.
	//    public ArrayList<Double> visibleDistance;
	public PointH loc;
	public PointH locNew;// Map.Height - y
	private PointH locPushed; // for use in visibility
	public int index;
	public boolean border;
	public boolean waterBorder;
	public int waterTouches; // num water centers it touches
	//    public boolean blocked;

	public double elevation;
	public boolean water, ocean, coast;
	transient public Corner downslope;
	public int river;
	public double moisture; 
	
	public Color lerpColor; // linear interpolation of land colors
	
	public void init() {
		locPushed = new PointH(this.loc.x, this.loc.y);
		locNew = new PointH(this.loc.x, Map.HEIGHT-this.loc.y);
		
		ArrayList<Center> nonWaterCenters = new ArrayList<Center>();
		for (Center center : touches) {
			if (!center.water) nonWaterCenters.add(center);
		}
		
		if (nonWaterCenters.size() == 1) lerpColor = VoronoiGraph.getColor(nonWaterCenters.get(0));
		if (nonWaterCenters.size() == 2) {
			lerpColor = VoronoiGraph.getColor(nonWaterCenters.get(0));
			lerpColor.lerp(VoronoiGraph.getColor(nonWaterCenters.get(1)), .5f);
		}
		if (nonWaterCenters.size() == 3) {
			lerpColor = VoronoiGraph.getColor(nonWaterCenters.get(0));
			lerpColor.lerp(VoronoiGraph.getColor(nonWaterCenters.get(1)), .5f);
			lerpColor.lerp(VoronoiGraph.getColor(nonWaterCenters.get(2)), .333f);
		}
		if (nonWaterCenters.size() == 4) {
			lerpColor = VoronoiGraph.getColor(nonWaterCenters.get(0));
			lerpColor.lerp(VoronoiGraph.getColor(nonWaterCenters.get(1)), .5f);
			lerpColor.lerp(VoronoiGraph.getColor(nonWaterCenters.get(2)), .333f);
			lerpColor.lerp(VoronoiGraph.getColor(nonWaterCenters.get(3)), .25f);
		}
		
		if (lerpColor == null) lerpColor = new Color(VoronoiGraph.OCEAN);
	}
	
	public void addVisible(Corner corner) {
		this.visibleCorners.add(corner);
		this.visibleCornersIndices.add(corner.index);
	}

	public void calcWaterTouches() {
		init();
		waterTouches = 0;
		for (Center center: touches)
			if (center.water) waterTouches++;
		pushOut();
	}
	public void pushOut() {
		if (waterTouches == 1 && touches.size() == 3) {
			for (Center center : touches) {
				if (center.water) {
					//System.out.println("PUSHING OUT");
					// create vector pointing towards center of water
					Vector2 vector = new Vector2((float)(center.loc.x-loc.x), (float)(center.loc.y-loc.y));// don't worry about "upside down map"
					vector.scl(1/vector.len()); // unitize
					vector.rotate(180); // rotate 180
					vector.scl(PUSH_DIST);
					this.locPushed.x += vector.x;
					this.locPushed.y += vector.y;
					break;
				}
			}
		}
		if (waterTouches == 2 && touches.size() == 3) {
			for (Center center : touches) {
				if (!center.water) {
					// create vector pointing towards center of land
					Vector2 vector = new Vector2((float)(center.loc.x-loc.x), (float)(center.loc.y-loc.y));// don't worry about "upside down map"
					vector.scl(1/vector.len()); // unitize
//					vector.rotate(180); // rotate 180
					vector.scl(PUSH_DIST);
					this.locPushed.x += vector.x;
					this.locPushed.y += vector.y;
					break;
				}
			}
		}
	}
	
	public PointH getLoc() {
		if (locPushed != null) return locPushed;
		else return loc;
	}
	
	public void restoreFromVoronoi(VoronoiGraph vg) {
		this.protrudes = new ArrayList<Edge>();
		this.adjacent = new ArrayList<Corner>();
		this.touches = new ArrayList<Center>();
		this.visibleCorners = new ArrayList<Corner>();

		// first restore adjacent edges
		for (int index: this.adjEdges) {
			if (index != -1)
				this.protrudes.add(vg.edges.get(index));
		}
		// then restore adjacent faces and corners
		for (Edge edge : protrudes) {
			// add corners if they're not this one
			if (edge.v0 != null && edge.v0 != this && !this.adjacent.contains(edge.v0))
				this.adjacent.add(edge.v0);
			if (edge.v1 != null && edge.v0 != this && !this.adjacent.contains(edge.v1))
				this.adjacent.add(edge.v1);
			
			if (edge.d0 != null && !this.adjacent.contains(edge.d0))
				this.touches.add(edge.d0);
			if (edge.d1 != null && !this.adjacent.contains(edge.d1))
				this.touches.add(edge.d1);
		}
		
		// also restore visible corners
		for (int index: this.visibleCornersIndices) {
			if (index != -1)
				this.visibleCorners.add(vg.corners.get(index));
		}
	}
}
