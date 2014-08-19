/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kyle.game.besiege.voronoi;


import java.util.ArrayList;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Map;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.geom.PointH;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

/**
 * Center.java Function Date Jun 6, 2013
 *
 * @author Connor
 */
public class Center {

    public int index;
    public PointH loc;
    
    transient public ArrayList<Corner> corners = new ArrayList<Corner>(); // can be reconstructed from edges
    transient public ArrayList<Center> neighbors = new ArrayList<Center>(); // can be reconstructed from edges
    transient public ArrayList<Edge> borders = new ArrayList<Edge>();
    transient public Array<Army> armies; // armies within this polygon
    public Polygon polygon;
    public Array<float[]> triangles; // x0, y0, x1, y1, x2, y2 (already converted)
    
    // used for serialization
    public ArrayList<Integer> adjEdges = new ArrayList<Integer>();
    
    public boolean border, ocean, water, coast;
    public double elevation;
    public double moisture;
    public Biomes biome;
    public double area;
    transient public Faction faction; // controlling faction - can be recalculated
    
    public Center() {
    	armies = new Array<Army>();
    }
    
	public void calcTriangles() {
		triangles = new Array<float[]>();
		for (Edge border : borders) {
			if (border.v0 != null && border.v1 != null) {
				float[] vertices = new float[6];
				vertices[0] = (float) border.v0.loc.x;
				vertices[1] = (float) (Map.HEIGHT-border.v0.loc.y);

				vertices[2] = (float) border.v1.loc.x;
				vertices[3] = (float) (Map.HEIGHT-border.v1.loc.y);

				vertices[4] = (float) loc.x; // center
				vertices[5] = (float) (Map.HEIGHT-loc.y);
				triangles.add(vertices);
			}
		}
	}

    public Center(PointH loc) {
        this.loc = loc;
    }
    
    public void restoreFromVoronoi(VoronoiGraph vg) {
    	this.borders = new ArrayList<Edge>();
		this.corners = new ArrayList<Corner>();
		this.neighbors = new ArrayList<Center>();
    	
		// first restore adjacent edges
		for (int index: this.adjEdges) {
			if (index != -1)
				this.borders.add(vg.edges.get(index));
		}
		// then restore adjacent faces and corners
		for (Edge edge : borders) {
			// add centers if they're not this one
			if (edge.d0 != null && !this.neighbors.contains(edge.d0))
				this.neighbors.add(edge.d0);
			if (edge.d1 != null && !this.neighbors.contains(edge.d1))
				this.neighbors.add(edge.d1);
			
			if (edge.v0 != null && !this.corners.contains(edge.v0))
				this.corners.add(edge.v0);
			if (edge.v1 != null && !this.corners.contains(edge.v1))
				this.corners.add(edge.v1);
		}
	}
}
