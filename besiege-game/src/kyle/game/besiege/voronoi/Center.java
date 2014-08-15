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
    public ArrayList<Corner> corners = new ArrayList<Corner>();//good
    transient public ArrayList<Center> neighbors = new ArrayList<Center>();//good
    transient public ArrayList<Edge> borders = new ArrayList<Edge>();
    public Array<Army> armies; // armies within this polygon
    public Polygon polygon;
    public Array<float[]> triangles; // x0, y0, x1, y1, x2, y2 (already converted)
    
    public boolean border, ocean, water, coast;
    public double elevation;
    public double moisture;
    public Biomes biome;
    public double area;
    public Faction faction; // controlling faction
    
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
}
