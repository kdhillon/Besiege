/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kyle.game.besiege.voronoi;

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

    public void setVornoi(Corner v0, Corner v1) {
        this.v0 = v0;  this.adjCorner0 = v0.index;
        this.v1 = v1;  this.adjCorner1 = v1.index;
        midpoint = new PointH((v0.loc.x + v1.loc.x) / 2, (v0.loc.y + v1.loc.y) / 2);
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
    }
}
