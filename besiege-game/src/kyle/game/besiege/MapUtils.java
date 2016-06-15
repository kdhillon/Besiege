package kyle.game.besiege;

import com.badlogic.gdx.math.Polygon;

import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;
import kyle.game.besiege.voronoi.Edge;

public class MapUtils {
	/** reorganizes array disconnectedCenters into separate arrays of 
	 *  connected centers
	 * 
	 * @param disconnectedCenters
	 * @return
	 */
	public static StrictArray<StrictArray<Center>> calcConnectedCenters(StrictArray<Center> original) {
		StrictArray<Center> disconnectedCenters = new StrictArray<Center>();
		for (Center c : original) {
			disconnectedCenters.add(c);
		}
		StrictArray<StrictArray<Center>> aaCenters = new StrictArray<StrictArray<Center>>();
		// start with random one, calc connected, remove all of those from disconnected, continue until disconnected is empty
		
		while (disconnectedCenters.size > 0) {
			StrictArray<Center> connectedToStart = new StrictArray<Center>();
			Center start = disconnectedCenters.random();
			calcConnectedContained(start, connectedToStart, original);
//			System.out.println("connected " + connectedToStart.size);
			aaCenters.add(connectedToStart);
			disconnectedCenters.removeAll(connectedToStart, true);
		}
//		System.out.println("number of separate polygons: " + aaCenters.size);
		return aaCenters;
	}
	
	// calc connected components, recursively
	private static void calcConnectedContained(Center center, StrictArray<Center> connected, StrictArray<Center> container) {
		connected.add(center);
		for (Center neighbor : center.neighbors) {
			if (container.contains(neighbor, true) && !connected.contains(neighbor, true)) {
				calcConnectedContained(neighbor, connected, container);
			}
		}
	}
	/**
	 * Converts array of centers to the largest polygon that can hold
	 * all of them. returns that polygon.
	 * 
	 * Problem when there is a different polygon in the middle of all of them.
	 *  
	 * @param polygonCenters
	 */
	public static Polygon centersToPolygon(StrictArray<Center> polygonCenters) {
		return edgesToPolygon(getBordersOfCenters(polygonCenters));
	}
	
	/** Doesn't work! problem is when there is only one shared edge 
	 * and it doesn't know which corner to choose. Workaround: use
	 * all edges of all centers, but don't use ones that are shared!
	 * Won't yield 'Polygon' result but I'm sure there's a way to 
	 * convert it... pretty easy way actually
	 * How to paint a polygon? Paint it's triangles. Should I make a
	 * method for painting triangles of a center or a polygon? Yes.
	 * 
	 * @param corner
	 * @param outsideCorners
	 * @param used
	 * @param vertices
	 * @param index
	 */
	// recursively find and add adjacent vertex to vertices
//	private static void getNextVertex(Corner corner, StrictArray<Corner> outsideCorners, StrictArray<Corner> used, float[] vertices, int index) {
//		for (Corner next : corner.adjacent) {
//			if (outsideCorners.contains(next, true) && !used.contains(next, true)) {
//				used.add(next);
//				vertices[index] = (float) next.loc.x;
//				index++;
//				vertices[index] = (float) (Map.HEIGHT-next.loc.y);
//				index++;
//				getNextVertex(next, outsideCorners, used, vertices, index);
//				return;
//			}
//		}
//	}
	
	private static StrictArray<Edge> getBordersOfCenters(StrictArray<Center> centers) {
		StrictArray<Edge> usedMoreThanOnce = new StrictArray<Edge>();
		StrictArray<Edge> usedOnce = new StrictArray<Edge>();
		for (Center center : centers) {
			for (Edge edge : center.borders) {
				if (usedOnce.contains(edge, true)) {
					usedOnce.removeValue(edge, true);
					usedMoreThanOnce.add(edge);
				}
				else if (!usedMoreThanOnce.contains(edge, true))
					usedOnce.add(edge);
			}
		}
		return usedOnce;
	}
	
	/** Converts Array of connected edges into a Libgdx polygon
	 * 
	 */
	private static Polygon edgesToPolygon(StrictArray<Edge> edges) {
		StrictArray<Edge> used = new StrictArray<Edge>();
		int index = 0;
		float[] vertices = new float[edges.size*2];
		
		vertices[index] = (float) edges.first().v0.loc.x;
		index++;
		vertices[index] = (float) (Map.HEIGHT- edges.first().v0.loc.y);
		index++;
		adjEdge(edges.first().v0, edges.first(), edges, used, index, vertices);
		return new Polygon(vertices);
	}
	
	/** Recursively finds adjacent edges in a polygon adding their corners
	 *  to vertices.
	 * 
	 * @param startC
	 * @param startE
	 * @param allEdges
	 * @param used
	 * @param index
	 * @param vertices
	 */
	private static void adjEdge(Corner startC, Edge startE, StrictArray<Edge> allEdges, StrictArray<Edge> used, int index, float[] vertices) {
		used.add(startE);
		for (Edge adj : startC.protrudes) {
			if (allEdges.contains(adj, true) && !used.contains(adj, true)) {
				if (adj.v0 != startC) {
					vertices[index] = (float) adj.v0.loc.x;
					index++;
					vertices[index] = (float) (Map.HEIGHT - adj.v0.loc.y);
					index++;
					adjEdge(adj.v0, adj, allEdges, used, index, vertices);
					return;
				}
				else if (adj.v1 != startC) {
					vertices[index] = (float) adj.v1.loc.x;
					index++;
					vertices[index] = (float) (Map.HEIGHT - adj.v1.loc.y);
					index++;
					adjEdge(adj.v1, adj, allEdges, used, index, vertices);
					return;
				}
				else System.out.println("adjEdge not following proper path");
			}
		}
	}
}
