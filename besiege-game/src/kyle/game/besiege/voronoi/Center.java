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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

/**
 * Center.java Function Date Jun 6, 2013
 *
 * @author Connor
 */
public class Center {

	private final float ELEVATION_FACTOR = 700f;
	// max number of verts is 3 * 6
	public int index;
	public PointH loc;

	transient public ArrayList<Corner> corners = new ArrayList<Corner>(); // can be reconstructed from edges
	transient public ArrayList<Center> neighbors = new ArrayList<Center>(); // can be reconstructed from edges
	transient public ArrayList<Edge> borders = new ArrayList<Edge>();

	// contains the 4 or 6 triangles in this center
	transient public Mesh mesh;
	
	transient public Array<Army> armies; // armies within this polygon
	public Polygon polygon;
	public Array<float[]> triangles; // x0, y0, x1, y1, x2, y2 (already converted)

	private float[] verts;
	// used for serialization
	public ArrayList<Integer> adjEdges = new ArrayList<Integer>();

	public boolean border, ocean, water, coast;
	
	// used to be double but I changed it
	public float elevation;
	public double moisture;
	public Biomes biome;
	public double area;
	transient public Faction faction; // controlling faction - can be recalculated

	public Center() {
		armies = new Array<Army>();
//		initMesh(null);
		// 6 * 3 vertices, 7 attributes
	
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
		initMesh(vg);
	}
	
	void initMesh(VoronoiGraph vg) {
		// just hijacking this method 
		int numVerts = this.borders.size() * 3 * 7;
		this.verts = new float[numVerts];
//		System.out.println("borders are " + this.borders.size());
		// need one per triangle
		
		VertexAttribute position = new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position");
		VertexAttribute colorAttribute = new VertexAttribute(VertexAttributes.Usage.Color, 4, "a_color");

		// I dont' know why 12 is the magic number for triangles, maybe 6 each side? duplicate triangles?
		mesh = new Mesh(true, true, 15 * 3, 6 * 3 * 7, new VertexAttributes(position, colorAttribute));
		// somehow meshes can have up to 13 triangles. make this 15 just to be safe?
		
		Color color = VoronoiGraph.getColor(this);
		
		float x0 = this.loc.x;
		float y0 = (float) vg.bounds.height-this.loc.y-1;
		float z0 = this.elevation * ELEVATION_FACTOR;
		z0 = 0;
		
		// draw all triangles
		int idx = 0;
		// hack just draw one triangle to start
//		boolean runOnce = false;
		for (int edgeIndex : this.adjEdges) {
//			if (runOnce) continue;
//			runOnce = true;
		//	int idx = 0;
			
			Edge e = vg.edges.get(edgeIndex);
			if (e == null) {
//				System.out.println("e is null");
				continue;
			}
			if (e.v0 == null) {
//				System.out.println("e v0 is null");
				continue;
			}
			if (e.v1 == null) {
//				System.out.println("e v1 is null");
				continue;
			}
			
			float x1 = e.v0.loc.x;
			float y1 = (float) vg.bounds.height-e.v0.loc.y-1;
			float z1 = e.v0.elevation * ELEVATION_FACTOR;
			z1 = 0;
			
			float x2 = e.v1.loc.x;
			float y2 = (float) vg.bounds.height-e.v1.loc.y-1;
			float z2 = e.v1.elevation * ELEVATION_FACTOR;
			z2 = 0;
			
//			System.out.println(x1 + " " + y1 + " " + z1);
			//now we push the vertex data into our array
			//we are assuming (0, 0) is lower left, and Y is up
			
			//bottom left vertex
			verts[idx++] = x0; 			//Position(x, y) 
			verts[idx++] = y0;
			verts[idx++] = z0;
			verts[idx++] = color.r; 	//Color(r, g, b, a)
			verts[idx++] = color.g;
			verts[idx++] = color.b;
			verts[idx++] = color.a;
			
			//top left vertex
			verts[idx++] = x1; 			//Position(x, y) 
			verts[idx++] = y1;
			verts[idx++] = z1;
			verts[idx++] = color.r; 	//Color(r, g, b, a)
			verts[idx++] = color.g;
			verts[idx++] = color.b;
			verts[idx++] = color.a;
	 
			//bottom right vertex
			verts[idx++] = x2;	 //Position(x, y) 
			verts[idx++] = y2;
			verts[idx++] = z2;
			verts[idx++] = color.r;		 //Color(r, g, b, a)
			verts[idx++] = color.g;
			verts[idx++] = color.b;
			verts[idx++] = color.a; // gotta do this every frame to multiply this by the batch color.
		}
		this.mesh.setVertices(verts);
	}
	
	public void draw(Matrix4 projTrans, ShaderProgram shader, float[] batchColor) {
		if (verts.length % 3 != 0) {
			throw new java.lang.RuntimeException();
		}
		
		// not sure if I have to do this every frame?
		int vertexCount = verts.length / 3;

		shader.setUniformMatrix("u_projTrans", projTrans);
		// set the batch color
		shader.setUniform3fv("u_batchColor", batchColor, 0, 3);
		mesh.render(shader, GL20.GL_TRIANGLES, 0, vertexCount);
//		shader.end();
	}

	public int getBiomeIndex() {
		Biomes[] biomes = Biomes.values();
		int biomeIndex = -1;
		for (int i = 0; i < biomes.length; i++) {
			if (biomes[i] == this.biome)
				biomeIndex = i;
		}
		return biomeIndex;
	}

	public float[] getBiomeDistribution() {
		Biomes[] biomes = Biomes.values();
		float[] biomeDistribution = new float[biomes.length];
		int thisIndex = this.getBiomeIndex();
		biomeDistribution[thisIndex] += 3; // arbitrary

		for (Center neighbor : this.neighbors) {
			if (!neighbor.water) {
				int biomeIndex = neighbor.getBiomeIndex();
				biomeDistribution[biomeIndex]++;
			}
		}
		// normalize biome distribution
		float total = 0;
		for (int i = 0; i < biomeDistribution.length; i++) {
			total += biomeDistribution[i];
		}
		for (int i = 0; i < biomeDistribution.length; i++) {
			biomeDistribution[i] /= total;
		}
		return biomeDistribution;
	}
	
	public String getName() {
		return "";
	}
}
