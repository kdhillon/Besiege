/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kyle.game.besiege.voronoi;


import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.Faction;
import kyle.game.besiege.Map;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.geom.PointH;

/**
 * Center.java Function Date Jun 6, 2013
 *
 * @author Connor
 */
public class Center {

	private final float ELEVATION_FACTOR = 700f;
//	private final float BASE_WEALTH_MAX = 100f;
//	private final float BASE_WEALTH_MIN = 50;

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
	private Color ogColor;
	
	// used for serialization
	public ArrayList<Integer> adjEdges = new ArrayList<Integer>();

	public boolean border, ocean, water, coast;

	// used to be double but I changed it
	public float elevation;
	public double moisture;
	public Biomes biome;
	public Texture biomeTexture;
	public int textureIndex;

	public double area;
	public Faction faction; // controlling faction - can be recalculated
	
	public float wealth;

	float minX = 999999, minY = 999999, maxX = -999999, maxY = -99999, absMax = -999;

	// For kryo
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
		System.out.println("center max: " + maxX + ", " + maxY);
	}

	public Center(PointH loc) {
		this.loc = loc;
	}
	
	public void setBiome(Biomes biome, Texture biomeTexture) {
		this.biome = biome;
		//       center.textureIndex = getTexture(center);
		this.biomeTexture = biomeTexture;
		if (biomeTexture == null) throw new java.lang.AssertionError("Cannot find biome texture for: " + biome);
		this.wealth = calculateInitWealth();
	}
	
	public float calculateInitWealth() {
		// wealth should be between 0 and 100, basically gaussian?
		float randomMax = 0.2f;
		
		float base = 1 - randomMax;
		
		float bonus = (float) Math.random() * randomMax - (randomMax/2);
		
		float toRet = biome.getWealthFactor() * base + bonus;
		if (toRet < 0) toRet = 0;
		if (toRet > 1) toRet = 1;
		
		return toRet;
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

	public int getSubVertices() {
		int total = 0; 
		for (Edge e : borders) {
			if (e.subEdges == null) continue;
			total += e.subEdges.length + 2;
		}
		if (total == 0) throw new java.lang.AssertionError();
		return total;
	}

	void initMesh(VoronoiGraph vg) {

		// just hijacking this method 
		int numVerts = getSubVertices() * 3 * 9;
		this.verts = new float[numVerts];
		//		System.out.println("borders are " + this.borders.size());
		// need one per triangle

		VertexAttribute position = new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position");
		VertexAttribute colorAttribute = new VertexAttribute(VertexAttributes.Usage.Color, 4, "a_color");
		VertexAttribute textureCoordinates = new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0");

		// This depends on the size of Edge.subEdges
		float estTriangles = 200;
		// I dont' know why 12 is the magic number for triangles, maybe 6 each side? duplicate triangles?
		mesh = new Mesh(true, true, 200 * 3, 6 * 3 * 9, new VertexAttributes(position, colorAttribute, textureCoordinates));
		// somehow meshes can have up to 13 triangles. make this 15 just to be safe?

		Color color = VoronoiGraph.getColor(this);


		// The following makes each center a bit discolored to account for neighbors, provides some differentition between centers
		float strength = 0.6f;
		float mixedR = 0;
		float mixedG = 0;
		float mixedB = 0;

		int colorCount = 0;

		if (!this.water) {
			// Average with neighbor colors
			for (Center c : this.neighbors) {
				if (c.water) continue;
				Color nColor = VoronoiGraph.getColor(c);
				mixedR += nColor.r;
				mixedG += nColor.g;
				mixedB += nColor.b;
				colorCount++;
			}
			float divFactor = (1 - strength) / colorCount;

			color.r = mixedR * divFactor + color.r * strength;
			color.g = mixedG * divFactor + color.g * strength;
			color.b = mixedB * divFactor + color.b * strength;
			
			ogColor = new Color(color);
		}

		// do some color blending for each polygon...
		// for now, 0.7 * color  + 0.3 * adjacent color, weighted average. 
		float x0 = this.loc.x;
		float y0 = (float) vg.bounds.height-this.loc.y-1;
		float z0 = this.elevation * ELEVATION_FACTOR;
		z0 = 0; // if you wanna draw 3D you gotta switch to a perspective camera from orthographic.

		// draw all triangles
		int idx = 0;


		calcAbsoluteMinMax(vg);

		for (int edgeIndex : this.adjEdges) {		
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

			Center adjacent = vg.centers.get(e.adjCenter0);
			if (adjacent == this) adjacent = vg.centers.get(e.adjCenter1);

			//			Color adjColor = VoronoiGraph.getColor(adjacent);
			//			Color current = new Color(color.r * 0.7f + adjColor.r * 0.3f, color.g * 0.7f + adjColor.g * 0.3f, color.b * 0.7f + adjColor.b * 0.3f, 1);
			Color current = color;

			// draw all 4 or 8 sub-triangles
			for (int i = -1; i < e.subEdges.length; i++) {
				float x1, y1, z1, x2, y2, z2;
				// case with first vertex and second edge
				if (i < 0) {
					x1 = e.v0.loc.x;
					y1 = (float) vg.bounds.height-e.v0.loc.y-1;
				}
				else {
					//					if (e.subEdges[i] == null) return;
					x1 = e.subEdges[i].x;
					y1 = (float) vg.bounds.height-e.subEdges[i].y;
				}
				//				z1 = e.v0.elevation * ELEVATION_FACTOR;
				z1 = 0;

				// case where last vertex is corner
				if (i >= e.subEdges.length - 1) {
					x2 = e.v1.loc.x;
					y2 = (float) vg.bounds.height-e.v1.loc.y-1;	
				}
				else {
					//					if (e.subEdges[i + 1] == null) return;
					x2 = e.subEdges[i + 1].x;
					y2 = (float) vg.bounds.height - e.subEdges[i + 1].y;
				}
				z2 = e.v1.elevation * ELEVATION_FACTOR;
				z2 = 0;

				//			System.out.println(x1 + " " + y1 + " " + z1);
				//now we push the vertex data into our array
				//we are assuming (0, 0) is lower left, and Y is up

				//bottom left vertex 
				verts[idx++] = x0; 			//Position(x, y) 
				verts[idx++] = y0;
				verts[idx++] = z0;
				//				verts[idx++] = z0 * 0.05f - 100;
				verts[idx++] = current.r; 	//Color(r, g, b, a)
				verts[idx++] = current.g;
				verts[idx++] = current.b;
				verts[idx++] = current.a;


				verts[idx++] = getTextureX(x0); 	//Texture coordinates (r, g, b, a)
				verts[idx++] = getTextureY(y0); 	//Texture coordinates (r, g, b, a)

				//top left vertex
				verts[idx++] = x1; 			//Position(x, y) 
				verts[idx++] = y1;
				verts[idx++] = z1;
				//				verts[idx++] = z1 * 0.05f - mod0;
				verts[idx++] = current.r; 	//Color(r, g, b, a)
				verts[idx++] = current.g;
				verts[idx++] = current.b;
				verts[idx++] = current.a;

				// TODO I don't really need to pass in texture coordinates
				// I should be able to calculate where to sample from 
				// if I am smart about using x1, y1 values 

				verts[idx++] = getTextureX(x1); 	//Texture coordinates (r, g, b, a)
				verts[idx++] = getTextureY(y1); 	//Texture coordinates (r, g, b, a)

				//bottom right vertex
				verts[idx++] = x2;	 //Position(x, y) 
				verts[idx++] = y2;
				//				verts[idx++] = z2 * 0.05f - mod0;
				verts[idx++] = z2;
				verts[idx++] = current.r;		 //Color(r, g, b, a)
				verts[idx++] = current.g;
				verts[idx++] = current.b;
				verts[idx++] = current.a; // gotta do this every frame to multiply this by the batch color.

				verts[idx++] = getTextureX(x2); 	//Texture coordinates (r, g, b, a)
				verts[idx++] = getTextureY(y2);
			}
		}
		this.mesh.setVertices(verts);
	}
	
	public void changeColor(VoronoiGraph vg) {
		Color newColor = new Color();
		
		if (Map.drawWealth) {
			newColor.r = 0;
			newColor.g = wealth;
			newColor.b = 0;
		}
		else {
			newColor = ogColor;
		}
		
		int idx = 0;
		for (int edgeIndex : this.adjEdges) {		
			Edge e = vg.edges.get(edgeIndex);
			if (e == null) {
				continue;
			}
			if (e.v0 == null) {
				continue;
			}
			if (e.v1 == null) {
				continue;
			}
			for (int i = -1; i < e.subEdges.length; i++) {
				for (int j = 0; j < 3; j++) {
					idx+=3;
					verts[idx++] = newColor.r; 	//Color(r, g, b, a)
					verts[idx++] = newColor.g;
					verts[idx++] = newColor.b;
					verts[idx++] = newColor.a;
					idx+=2;
				}
			}
		}
		this.mesh.setVertices(verts);
	}

	public void calcAbsoluteMinMax(VoronoiGraph vg) {
		// get absolute min and max of entire center, to be used for drawing texture
		float x0 = this.loc.x;
		float y0 = (float) vg.bounds.height-this.loc.y-1;

		for (int edgeIndex : this.adjEdges) {
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
			for (int i = -1; i < e.subEdges.length; i++) {
				float x1, y1, x2, y2;
				// case with first vertex and second edge
				if (i < 0) {
					x1 = e.v0.loc.x;
					y1 = (float) vg.bounds.height-e.v0.loc.y-1;
				}
				else {
					//					if (e.subEdges[i] == null) return;
					x1 = e.subEdges[i].x;
					y1 = (float) vg.bounds.height-e.subEdges[i].y;
				}
				// case where last vertex is corner
				if (i >= e.subEdges.length - 1) {
					x2 = e.v1.loc.x;
					y2 = (float) vg.bounds.height-e.v1.loc.y-1;	
				}
				else {
					//					if (e.subEdges[i + 1] == null) return;
					x2 = e.subEdges[i + 1].x;
					y2 = (float) vg.bounds.height - e.subEdges[i + 1].y;
				}
				minX = Math.min(minX, Math.min(x0, Math.min(x1, x2)));
				minY = Math.min(minY, Math.min(y0, Math.min(y1, y2)));
				maxX = Math.max(maxX, Math.max(x0, Math.max(x1, x2)));
				maxY = Math.max(maxY, Math.max(y0, Math.max(y1, y2)));	
				
				absMax = Math.max(maxX - minX, maxY - minY);
				
			}
		}
	}

	public float getTextureX(float x) {
		float scale = 2;// *  (maxY - minY) / (maxX - minX);

		float texX = (x - minX) / (absMax);
		//		float texX = (x/Map.WIDTH * scale) % 1;
		//		if (texX < 0) texX = 1- texX;
		System.out.println("x: " + texX);
		return texX * scale;
	}

	public float getTextureY(float y) {
		float scale = 2;
		//		System.out.println("y: " + y);

		float texY = (y - minY) / (absMax);

		//		float texY = (y/Map.HEIGHT * scale) % 1;
		//		if (texY < 0) texY = 1- texY;

		System.out.println("y: " + texY);
		return texY * scale;
	}
	
	public int getAvgElevation() {
		return (int) (elevation * ELEVATION_FACTOR);
	}
	//	this.mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});
	// draw all triangles
	//	idx = 0;
	//	for (int edgeIndex : this.adjEdges) {		
	//		Edge e = vg.edges.get(edgeIndex);
	//		if (e == null) {
	//			//						System.out.println("e is null");
	//			continue;
	//		}
	//		if (e.v0 == null) {
	//			//						System.out.println("e v0 is null");
	//			continue;
	//		}
	//		if (e.v1 == null) {
	//			//						System.out.println("e v1 is null");
	//			continue;
	//		}
	//
	//		Center adjacent = vg.centers.get(e.adjCenter0);
	//		if (adjacent == this) adjacent = vg.centers.get(e.adjCenter1);
	//
	//		//					Color adjColor = VoronoiGraph.getColor(adjacent);
	//		//					Color current = new Color(color.r * 0.7f + adjColor.r * 0.3f, color.g * 0.7f + adjColor.g * 0.3f, color.b * 0.7f + adjColor.b * 0.3f, 1);
	//		Color current = color;
	//
	//		// draw all 4 or 8 sub-triangles
	//		for (int i = -1; i < e.subEdges.length; i++) {
	//			float x1, y1, z1, x2, y2, z2;
	//			// case with first vertex and second edge
	//			if (i < 0) {
	//				x1 = e.v0.loc.x;
	//				y1 = (float) vg.bounds.height-e.v0.loc.y-1;
	//			}
	//			else {
	//				//							if (e.subEdges[i] == null) return;
	//				x1 = e.subEdges[i].x;
	//				y1 = (float) vg.bounds.height-e.subEdges[i].y;
	//			}
	//			z1 = e.v0.elevation * ELEVATION_FACTOR;
	//			//						z1 = 0;
	//
	//			// case where last vertex is corner
	//			if (i >= e.subEdges.length - 1) {
	//				x2 = e.v1.loc.x;
	//				y2 = (float) vg.bounds.height-e.v1.loc.y-1;	
	//			}
	//			else {
	//				//							if (e.subEdges[i + 1] == null) return;
	//				x2 = e.subEdges[i + 1].x;
	//				y2 = (float) vg.bounds.height - e.subEdges[i + 1].y;
	//			}
	//			z2 = e.v1.elevation * ELEVATION_FACTOR;
	//			//						z2 = 0;
	//
	//			//			System.out.println(x1 + " " + y1 + " " + z1);
	//			//now we push the vertex data into our array
	//			//we are assuming (0, 0) is lower left, and Y is up
	//
	//			//bottom right vertex
	//			verts[idx++] = x2;	 //Position(x, y) 
	//			verts[idx++] = y2;
	//			verts[idx++] = z2 * 0.0001f;
	//			verts[idx++] = current.r;		 //Color(r, g, b, a)
	//			verts[idx++] = current.g;
	//			verts[idx++] = current.b;
	//			verts[idx++] = current.a; // gotta do this every frame to multiply this by the batch color.
	//
	//			//top left vertex
	//			verts[idx++] = x1; 			//Position(x, y) 
	//			verts[idx++] = y1;
	//			verts[idx++] = z1 * 0.0001f;
	//			verts[idx++] = current.r; 	//Color(r, g, b, a)
	//			verts[idx++] = current.g;
	//			verts[idx++] = current.b;
	//			verts[idx++] = current.a;
	//
	//					//bottom left vertex
	//			verts[idx++] = x0; 			//Position(x, y) 
	//			verts[idx++] = y0;
	//			verts[idx++] = z0 * 0.0001f;
	//			verts[idx++] = current.r; 	//Color(r, g, b, a)
	//			verts[idx++] = current.g;
	//			verts[idx++] = current.b;
	//			verts[idx++] = current.a;
	//		}
	//	}
	//	this.mesh.setVertices(verts);


	//
	//	void initMesh(VoronoiGraph vg) {
	//		// just hijacking this method 
	//		int numVerts = this.borders.size() * 3 * 7;
	//		this.verts = new float[numVerts];
	////		System.out.println("borders are " + this.borders.size());
	//		// need one per triangle
	//		
	//		VertexAttribute position = new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position");
	//		VertexAttribute colorAttribute = new VertexAttribute(VertexAttributes.Usage.Color, 4, "a_color");
	//
	//		// I dont' know why 12 is the magic number for triangles, maybe 6 each side? duplicate triangles?
	//		mesh = new Mesh(true, true, 15 * 3, 6 * 3 * 7, new VertexAttributes(position, colorAttribute));
	//		// somehow meshes can have up to 13 triangles. make this 15 just to be safe?
	//		
	//		Color color = VoronoiGraph.getColor(this);
	//		
	//		float x0 = this.loc.x;
	//		float y0 = (float) vg.bounds.height-this.loc.y-1;
	//		float z0 = this.elevation * ELEVATION_FACTOR;
	//		z0 = 0;
	//		
	//		// draw all triangles
	//		int idx = 0;
	//		for (int edgeIndex : this.adjEdges) {		
	//			Edge e = vg.edges.get(edgeIndex);
	//			if (e == null) {
	////				System.out.println("e is null");
	//				continue;
	//			}
	//			if (e.v0 == null) {
	////				System.out.println("e v0 is null");
	//				continue;
	//			}
	//			if (e.v1 == null) {
	////				System.out.println("e v1 is null");
	//				continue;
	//			}
	//			
	//			float x1 = e.v0.loc.x;
	//			float y1 = (float) vg.bounds.height-e.v0.loc.y-1;
	//			float z1 = e.v0.elevation * ELEVATION_FACTOR;
	//			z1 = 0;
	//			
	//			float x2 = e.v1.loc.x;
	//			float y2 = (float) vg.bounds.height-e.v1.loc.y-1;
	//			float z2 = e.v1.elevation * ELEVATION_FACTOR;
	//			z2 = 0;
	//			
	////			System.out.println(x1 + " " + y1 + " " + z1);
	//			//now we push the vertex data into our array
	//			//we are assuming (0, 0) is lower left, and Y is up
	//			
	//			//bottom left vertex
	//			verts[idx++] = x0; 			//Position(x, y) 
	//			verts[idx++] = y0;
	//			verts[idx++] = z0;
	//			verts[idx++] = color.r; 	//Color(r, g, b, a)
	//			verts[idx++] = color.g;
	//			verts[idx++] = color.b;
	//			verts[idx++] = color.a;
	//			
	//			//top left vertex
	//			verts[idx++] = x1; 			//Position(x, y) 
	//			verts[idx++] = y1;
	//			verts[idx++] = z1;
	//			verts[idx++] = color.r; 	//Color(r, g, b, a)
	//			verts[idx++] = color.g;
	//			verts[idx++] = color.b;
	//			verts[idx++] = color.a;
	//	 
	//			//bottom right vertex
	//			verts[idx++] = x2;	 //Position(x, y) 
	//			verts[idx++] = y2;
	//			verts[idx++] = z2;
	//			verts[idx++] = color.r;		 //Color(r, g, b, a)
	//			verts[idx++] = color.g;
	//			verts[idx++] = color.b;
	//			verts[idx++] = color.a; // gotta do this every frame to multiply this by the batch color.
	//		}
	//		this.mesh.setVertices(verts);
	//	}

	public void draw(Matrix4 projTrans, ShaderProgram shader, float[] batchColor) {
		if (verts.length % 3 != 0) {
			throw new java.lang.RuntimeException();
		}

		// not sure if I have to do this every frame?
		int vertexCount = verts.length / 3;

		shader.setUniformMatrix("u_projTrans", projTrans);
		// set the batch color
		shader.setUniform3fv("u_batchColor", batchColor, 0, 3);
		shader.setUniformi("u_texture", 0);
		
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
		return "Fakename";
	}
}
