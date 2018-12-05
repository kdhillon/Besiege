/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kyle.game.besiege.voronoi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.Gdx;
//import java.awt.Color;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.MapUtils;
import kyle.game.besiege.ext.PerlinNoiseGenerator;
import kyle.game.besiege.geom.PointH;
import kyle.game.besiege.geom.Rectangle;
import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.party.UnitLoader;
import kyle.game.besiege.utils.MyRandom;
import kyle.game.besiege.voronoi.nodename.as3delaunay.LineSegment;
import kyle.game.besiege.voronoi.nodename.as3delaunay.Voronoi;


/**
 * VoronoiGraph.java Function Date Jun 14, 2013
 *
 * @author Connor
 * (modified by Kyle Dhillon)
 */
public class VoronoiGraph {
	private static final double ROUNDNESS = .5; // increase for more centralized (~.5) (.46 is good)
	private static final double WATER_FREQ = .24; // increase for a little more wild (~.2) (.14 is good)
	private static final double LAND_FREQ = .4; // increase for overall land mass (sensitive) (~.4) (.43 is good)
	private static final double TURBULENCE = 4; // (~5)

    public ArrayList<Corner> corners = new ArrayList<Corner>();
    public ArrayList<Center> centers = new ArrayList<Center>();
    public ArrayList<Edge> edges = new ArrayList<Edge>();

    public Rectangle bounds;
    private MyRandom r;
    private PerlinNoiseGenerator perlin;

    // Lower is hotter...
    private float tempTopRight = 0.77f;
    private float tempTopLeft  = 0.87f;
    private float tempBotRight = 0.0f;
    private float tempBotLeft  = 0.0f;

    private float maxElevation = 0;

    // for serialization
    public VoronoiGraph() {
    	// need to reinitialize biome textures.
    }

    public VoronoiGraph(Voronoi v, int numLloydRelaxations, MyRandom r) {
        this.r = r;
        System.out.println("initializing Perlin generator");
	    perlin = new PerlinNoiseGenerator(r.seed);
        bumps = r.nextInt(1, 6);
        startAngle = r.nextDouble(0, 2 * Math.PI);
        dipAngle = r.nextDouble(0, 2 * Math.PI);
        dipWidth = r.nextDouble(0.2, 0.7);
        bounds = v.get_plotBounds();
        
        System.out.println("Performing lloyd relaxations");
        for (int i = 0; i < numLloydRelaxations; i++) {
            ArrayList<PointH> pointHs = v.siteCoords();
            for (PointH p : pointHs) {
                ArrayList<PointH> region = v.region(p);
                float x = 0;
                float y = 0;
                for (PointH c : region) {
                    x += c.x;
                    y += c.y;
                }
                x /= region.size();
                y /= region.size();
                p.x = x;
                p.y = y;
            }
            v = new Voronoi(pointHs, null, v.get_plotBounds());
        }
                
        System.out.println("building graph");
        buildGraph(v);
        System.out.println("improving corners");        
        improveCorners();

        System.out.println("assigning elevations, coast, and more elevations");
        assignCornerElevations();
        assignOceanCoastAndLand();
        redistributeElevations(landCorners());
        assignPolygonElevations();
        assignPolygonTemperatures();

        System.out.println("doing downslopes and moistures and biomes");
        calculateDownslopes();
        //calculateWatersheds();
//        createRivers();
        assignCornerMoisture();
        redistributeMoisture(landCorners());
        assignPolygonMoisture();
        initBiomeTextures();
        assignBiomes();
        assignCultures();

        System.out.println("doing areas");
        calculateAreas();
        
        // added by Kyle
        System.out.println("initializing meshes");
        initMeshes();
    }
    
    public void initMeshes() {
    	for (Center c : centers) {
    		c.initMesh(this);
    	}
    }
    
    private void calculateAreas() {
     Pixmap g = new Pixmap((int) bounds.width, (int) bounds.height, Pixmap.Format.RGB888);
     
//        Graphics2D g = img.createGraphics();
       
        for (Center c : centers) {
            g.setColor(new Color(c.index));

            //only used if Center c is on the edge of the graph. allows for completely filling in the outer polygons
            Corner edgeCorner1 = null;
            Corner edgeCorner2 = null;
            for (Center n : c.neighbors) {
                Edge e = edgeWithCenters(c, n);

                if (e.v0 == null) {
                    //outermost voronoi edges aren't stored in the graph
                    continue;
                }

                //find a corner on the exterior of the graph
                //if this Edge e has one, then it must have two,
                //finding these two corners will give us the missing
                //triangle to render. this special triangle is handled
                //outside this for loop
                Corner cornerWithOneAdjacent = e.v0.border ? e.v0 : e.v1;
                if (cornerWithOneAdjacent.border) {
                    if (edgeCorner1 == null) {
                        edgeCorner1 = cornerWithOneAdjacent;
                    } else {
                        edgeCorner2 = cornerWithOneAdjacent;
                    }
                }

                drawTriangle(g, e.v0, e.v1, c);
                c.area += (c.loc.x * (e.v0.loc.y - e.v1.loc.y) + e.v0.loc.x * (e.v1.loc.y - c.loc.y) + e.v1.loc.x * (c.loc.y - e.v0.loc.y)) / 2;
            }

            //handle the missing triangle
            if (edgeCorner2 != null) {
                //if these two outer corners are NOT on the same exterior edge of the graph,
                //then we actually must render a polygon (w/ 4 points) and take into consideration
                //one of the four corners (either 0,0 or 0,height or width,0 or width,height)
                //note: the 'missing polygon' may have more than just 4 points. this
                //is common when the number of sites are quite low (less than 5), but not a problem
                //with a more useful number of sites. 
                //TODO: find a way to fix this

                if (closeEnough(edgeCorner1.loc.x, edgeCorner2.loc.x, 1)) {
                    drawTriangle(g, edgeCorner1, edgeCorner2, c);
                } else {
                    int[] x = new int[4];
                    int[] y = new int[4];
                    x[0] = (int) c.loc.x;
                    y[0] = (int) c.loc.y;
                    x[1] = (int) edgeCorner1.loc.x;
                    y[1] = (int) edgeCorner1.loc.y;

                    //determine which corner this is
                    x[2] = (int) ((closeEnough(edgeCorner1.loc.x, bounds.x, 1) || closeEnough(edgeCorner2.loc.x, bounds.x, .5)) ? bounds.x : bounds.right);
                    y[2] = (int) ((closeEnough(edgeCorner1.loc.y, bounds.y, 1) || closeEnough(edgeCorner2.loc.y, bounds.y, .5)) ? bounds.y : bounds.bottom);

                    x[3] = (int) edgeCorner2.loc.x;
                    y[3] = (int) edgeCorner2.loc.y;

//                    g.fillPolygon(x, y, 4);
                    g.fillTriangle(x[0], y[0], x[1], y[1], x[3], y[3]);
                }
            }
        }
    }

    private void improveCorners() {
        PointH[] newP = new PointH[corners.size()];
        for (Corner c : corners) {
            if (c.border) {
                newP[c.index] = c.loc;
            } else {
                newP[c.index] = c.loc;

                // Move points to the centroids of created polygons
//                double x = 0;
//                double y = 0;
//                for (Center center : c.touches) {
//                    x += center.loc.x;
//                    y += center.loc.y;
//                }
//                newP[c.index] = new PointH(x / c.touches.size(), y / c.touches.size());
            }
        }
        for (Corner c : corners) {
            c.loc = newP[c.index];
        }
        for (Edge e : edges) {
            if (e.v0 != null && e.v1 != null) {
                e.setVornoi(e.v0, e.v1);
            }
        }
    }

    private Edge edgeWithCenters(Center c1, Center c2) {
        for (Edge e : c1.borders) {
            if (e.d0 == c2 || e.d1 == c2) {
                return e;
            }
        }
        return null;
    }

    private void drawTriangle(Pixmap g, Corner c1, Corner c2, Center center) {
        int[] x = new int[3];
        int[] y = new int[3];
        x[0] = (int) center.loc.x;
        y[0] = (int) center.loc.y;
        x[1] = (int) c1.loc.x;
        y[1] = (int) c1.loc.y;
        x[2] = (int) c2.loc.x;
        y[2] = (int) c2.loc.y;
        g.fillTriangle(x[0], y[0], x[1], y[1], x[2], y[2]);
    }

    private boolean closeEnough(double d1, double d2, double diff) {
        return Math.abs(d1 - d2) <= diff;
    }

    public void paint(Pixmap g) {
        paint(g, true, false, false, false, false, false);
    }

    
    //Kyle TODO: find way to break up into smaller triangles recursively, then draw those bad boys
    // in the proper color to create illusion of small amount of "noise"
    public void paint(Pixmap g, boolean drawBg, boolean drawSites, boolean drawCorners, boolean drawBorderCorners, boolean drawDelaunay, boolean drawVoronoi) {
        final int numSites = centers.size();

        Color[] key = new Color[numSites];
        for (int i = 0; i < key.length; i++) {
            Random r = new Random(i);
            key[i] = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255), 1);
        }

        //draw via triangles
        if (drawBg) {
//            g.setColor(new Color(OCEAN));
////        	System.out.println(bounds.width + " width and height is " + bounds.height);
//        	g.fillRectangle(0, 0, (int) bounds.width, (int)bounds.height);
//        	// first cover bg with ocean (to eliminate black corners and white edges)
            for (Center c : centers) {                
//            	
//            	g.setColor(key[c.index]);
//                if (c.ocean) {
//                    g.setColor(new Color(OCEAN));
//                } else if (c.water) {
//                    g.setColor(new Color(LAKE));
//                } else if (c.coast) {
//                    g.setColor(new Color(BEACH));
//                } else {
//                    Color color = null;
//                    switch (c.biome) {
//                        case TUNDRA:
//                            color = new Color(TUNDRA);
//                            break;
//                        case TROPICAL_SEASONAL_FOREST:
//                            color = new Color(TROPICAL_SEASONAL_FOREST);
//                            break;
//                        case TROPICAL_RAIN_FOREST:
//                            color = new Color(TROPICAL_RAIN_FOREST);
//                            break;
//                        case SUBTROPICAL_DESERT:
//                            color = new Color(SUBTROPICAL_DESERT);
//                            break;
//                        case SNOW:
//                            color = new Color(SNOW);
//                            break;
//                        case SCORCHED:
//                            color = new Color(SCORCHED);
//                            break;
//                        case LAKESHORE:
//                            color = new Color(LAKESHORE);
//                            break;
//                        case ICE:
//                            color = new Color(ICE);
//                            break;
//                        case BEACH:
//                            color = new Color(BEACH);
//                            break;
//                        case COAST:
//                            color = new Color(COAST);
//                            break;
//                        case MOUNTAINS:
//                            color = new Color(MOUNTAINS);
//                            break;
//                        case SHRUBLAND:
//                            color = new Color(SHRUBLAND);
//                            break;
//                        case TAIGA:
//                            color = new Color(TAIGA);
//                            break;
//                        case MARSH:
//                            color = new Color(MARSH);
//                            break;
//                        case SWAMP:
//                        	System.out.println("Painting swamp");
//                            color = new Color(SWAMP);
//                            break;
//                        default:
//                            color = new Color(GRASSLAND);
//                            throw new java.lang.AssertionError();
//                    }
//                    g.setColor(color);
//                }

                //only used if Center c is on the edge of the graph. allows for completely filling in the outer polygons
                Corner edgeCorner1 = null;
                Corner edgeCorner2 = null;
                for (Center n : c.neighbors) {
                    Edge e = edgeWithCenters(c, n);

                    if (e.v0 == null) {
                        //outermost voronoi edges aren't stored in the graph
                        continue;
                    }

                    //find a corner on the exterior of the graph
                    //if this Edge e has one, then it must have two,
                    //finding these two corners will give us the missing
                    //triangle to render. this special triangle is handled
                    //outside this for loop
                    Corner cornerWithOneAdjacent = e.v0.border ? e.v0 : e.v1;
                    if (cornerWithOneAdjacent.border) {
                        if (edgeCorner1 == null) {
                            edgeCorner1 = cornerWithOneAdjacent;
                        } else {
                            edgeCorner2 = cornerWithOneAdjacent;
                        }
                    }

                    drawTriangle(g, e.v0, e.v1, c);
                }

                //handle the missing triangle
                if (edgeCorner2 != null) {
                    //if these two outer corners are NOT on the same exterior edge of the graph,
                    //then we actually must render a polygon (w/ 4 points) and take into consideration
                    //one of the four corners (either 0,0 or 0,height or width,0 or width,height)
                    //note: the 'missing polygon' may have more than just 4 points. this
                    //is common when the number of sites are quite low (less than 5), but not a problem
                    //with a more useful number of sites. 
                    //TODO: find a way to fix this

                    if (closeEnough(edgeCorner1.loc.x, edgeCorner2.loc.x, 1)) {
                        drawTriangle(g, edgeCorner1, edgeCorner2, c);
                    } else {
                        int[] x = new int[4];
                        int[] y = new int[4];
                        x[0] = (int) c.loc.x;
                        y[0] = (int) c.loc.y;
                        x[1] = (int) edgeCorner1.loc.x;
                        y[1] = (int) edgeCorner1.loc.y;

                        //determine which corner this is
                        x[2] = (int) ((closeEnough(edgeCorner1.loc.x, bounds.x, 1) || closeEnough(edgeCorner2.loc.x, bounds.x, .5)) ? bounds.x : bounds.right);
                        y[2] = (int) ((closeEnough(edgeCorner1.loc.y, bounds.y, 1) || closeEnough(edgeCorner2.loc.y, bounds.y, .5)) ? bounds.y : bounds.bottom);

                        x[3] = (int) edgeCorner2.loc.x;
                        y[3] = (int) edgeCorner2.loc.y;

                        g.fillTriangle(x[0], y[0], x[1], y[1], x[3], y[3]);
                    }
                }
            }
        }


//        for (Edge e : edges) {
//            if (drawDelaunay) {
//                g.setStroke(new BasicStroke(1));
//                g.setColor(Color.YELLOW);
//                g.drawLine((int) e.d0.loc.x, (int) e.d0.loc.y, (int) e.d1.loc.x, (int) e.d1.loc.y);
//            }
//            if (e.river > 0) {
//                g.setStroke(new BasicStroke(1 + (int) Math.sqrt(e.river * 2)));
//                g.setColor(new Color(RIVER));
//                g.drawLine((int) e.v0.loc.x, (int) e.v0.loc.y, (int) e.v1.loc.x, (int) e.v1.loc.y);
//            }
//        }
//
//        if (drawSites) {
//            g.setColor(Color.BLACK);
//            for (Center s : centers) {
//                g.fillOval((int) (s.loc.x - 2), (int) (s.loc.y - 2), 4, 4);
//            }
//        }
//
//        if (drawCorners) {
//            g.setColor(Color.WHITE);
//            for (Corner c : corners) {
//                g.fillOval((int) (c.loc.x - 2), (int) (c.loc.y - 2), 4, 4);
//            }
//        }
//        if (drawBorderCorners) {
//        	g.setColor(Color.RED);
//        	for (Corner c : corners) {
//        		if (c.waterBorder)
//        			g.fillOval((int) (c.loc.x - 2), (int) (c.loc.y - 2), 4, 4);
//            }
//        }
//        g.setColor(Color.WHITE);
//        g.drawRectangle((int) bounds.x, (int) bounds.y, (int) bounds.width, (int) bounds.height);
    }

    public void calcRelativeTemp(Corner corner) {
        float x = (float) (corner.loc.x / bounds.right);
        float y = (float) (corner.loc.y / bounds.bottom);

        // bilinear interpolation:
        float topLeft = tempTopLeft * (1-x) * (1-y);
        float topRight = tempTopRight * (x) * (1-y);
        float botLeft = tempBotLeft * (1-x) * (y);
        float botRight = tempBotRight * (x) * (y);

        corner.temperature = topLeft + topRight + botLeft + botRight;
        System.out.println("Temperature: " + corner.temperature);
    }

    public static Color getColor(Center c) {
    	Color color = new Color(OCEAN);
    
    	switch (c.biome) {
    	case OCEAN:
    		color = new Color(OCEAN);
    		break;
    	case LAKE:
    		color = new Color(LAKE);
    		break;
    	case TUNDRA:
    		color = new Color(TUNDRA);
    		break;
    	case TROPICAL_SEASONAL_FOREST:
    		color = new Color(TROPICAL_SEASONAL_FOREST);
    		break;
    	case TROPICAL_RAIN_FOREST:
    		color = new Color(TROPICAL_RAIN_FOREST);
    		break;
    	case TEMPERATE_DECIDUOUS_FOREST:
    		color = new Color(TEMPERATE_DECIDUOUS_FOREST);
    		break;
    	case SUBTROPICAL_DESERT:
    		color = new Color(SUBTROPICAL_DESERT);
    		break;
    	case PLATEAU:
    		color = new Color(TEMPERATE_DESERT);
    		break;
    	case SNOW:
    		color = new Color(SNOW);
    		break;
    	case SCORCHED:
    		color = new Color(SCORCHED);
    		break;
    	case LAKESHORE:
    		color = new Color(LAKESHORE);
    		break;
    	case ICE:
    		color = new Color(SNOW);
    		break;
    	case BEACH:
    		color = new Color(BEACH);
    		break;
    	case MOUNTAINS:
    		color = new Color(BARE);
    		break;
    	case SHRUBLAND:
    		color = new Color(SHRUBLAND);
    		break;
    	case TAIGA:
    		color = new Color(TAIGA);
    		break;
    	case MARSH:
    		color = new Color(MARSH);
    		break;
    	case SWAMP:
    		color = new Color(SWAMP);
    		break;
    	case GRASSLAND:
    		color = new Color(GRASSLAND);
    		break;
    	default:
//    		throw new java.lang.AssertionError("No color for " + c.biome);
    		System.out.println(("No color for " + c.biome));
    		color = new Color(GRASSLAND);
    	}
    	return color;
    }
    
    private void buildGraph(Voronoi v) {
        final HashMap<PointH, Center> pointCenterMap = new HashMap();
        final ArrayList<PointH> pointHs = v.siteCoords();
        for (PointH p : pointHs) {
            Center c = new Center();
            c.loc = p;
            c.index = centers.size();
            centers.add(c);
            pointCenterMap.put(p, c);
        }

        //bug fix
        for (Center c : centers) {
            v.region(c.loc);
        }

        final ArrayList<kyle.game.besiege.voronoi.nodename.as3delaunay.Edge> libedges = v.edges();
        final HashMap<Integer, Corner> pointCornerMap = new HashMap();

        for (kyle.game.besiege.voronoi.nodename.as3delaunay.Edge libedge : libedges) {
            final LineSegment vEdge = libedge.voronoiEdge();
            final LineSegment dEdge = libedge.delaunayLine();

            final Edge edge = new Edge();
            edge.index = edges.size();
            edges.add(edge);

            edge.v0 = makeCorner(pointCornerMap, vEdge.p0);
            if (edge.v0 != null) edge.adjCorner0 = edge.v0.index;
            edge.v1 = makeCorner(pointCornerMap, vEdge.p1);
            if (edge.v1 != null) edge.adjCorner1 = edge.v1.index;
            edge.d0 = pointCenterMap.get(dEdge.p0);
            if (edge.d0 != null) edge.adjCenter0 = edge.d0.index;
            edge.d1 = pointCenterMap.get(dEdge.p1);
            if (edge.d1 != null) edge.adjCenter1 = edge.d1.index;

//            System.out.println(edge.adjCenter0 + " " + edge.adjCenter1 + " " + edge.adjCorner0 + " " + edge.adjCorner1);
            
            // Centers point to edges. Corners point to edges.
            if (edge.d0 != null) {
                edge.d0.borders.add(edge);
                edge.d0.adjEdges.add(edge.index);
            }
            if (edge.d1 != null) {
                edge.d1.borders.add(edge);
                edge.d1.adjEdges.add(edge.index);
            }
            if (edge.v0 != null) {
                edge.v0.protrudes.add(edge);
                edge.v0.adjEdges.add(edge.index);
            }
            if (edge.v1 != null) {
                edge.v1.protrudes.add(edge);
                edge.v1.adjEdges.add(edge.index);
            }

            // Centers point to centers.
            if (edge.d0 != null && edge.d1 != null) {
                addToCenterList(edge.d0.neighbors, edge.d1);
                addToCenterList(edge.d1.neighbors, edge.d0);
            }

            // Corners point to corners
            if (edge.v0 != null && edge.v1 != null) {
                addToCornerList(edge.v0.adjacent, edge.v1);
                addToCornerList(edge.v1.adjacent, edge.v0);
            }

            // Centers point to corners
            if (edge.d0 != null) {
                addToCornerList(edge.d0.corners, edge.v0);
                addToCornerList(edge.d0.corners, edge.v1);
            }
            if (edge.d1 != null) {
                addToCornerList(edge.d1.corners, edge.v0);
                addToCornerList(edge.d1.corners, edge.v1);
            }

            // Corners point to centers
            if (edge.v0 != null) {
                addToCenterList(edge.v0.touches, edge.d0);
                addToCenterList(edge.v0.touches, edge.d1);
            }
            if (edge.v1 != null) {
                addToCenterList(edge.v1.touches, edge.d0);
                addToCenterList(edge.v1.touches, edge.d1);
            }
        }
    }

    // Helper functions for the following for loop; ideally these
    // would be inlined
    private void addToCornerList(ArrayList<Corner> list, Corner c) {
        if (c != null && !list.contains(c)) {
            list.add(c);
        }
    }

    private void addToCenterList(ArrayList<Center> list, Center c) {
        if (c != null && !list.contains(c)) {
            list.add(c);
        }
    }

    //ensures that each corner is represented by only one corner object
    private Corner makeCorner(HashMap<Integer, Corner> pointCornerMap, PointH p) {
        if (p == null) {
            return null;
        }
        int index = (int) ((int) p.x + (int) (p.y) * bounds.width * 2);
        Corner c = pointCornerMap.get(index);
        if (c == null) {
            c = new Corner();
            c.loc = p;
            c.border = bounds.liesOnAxes(p);
            c.index = corners.size();
            corners.add(c);
            pointCornerMap.put(index, c);
        }
        return c;
    }

    private void assignCornerElevations() {
        Array<Corner> queue = new Array<Corner>();
        for (Corner c : corners) {
            c.water = isWater(c.loc);
            if (c.border) {
                c.elevation = 0;
                queue.add(c);
            } else {
                c.elevation = Float.MAX_VALUE;
            }
        }

        while (queue.size > 0) {
            Corner c = queue.pop();
            for (Corner a : c.adjacent) {
            	float newElevation = 0.01f + c.elevation;
                if (!c.water && !a.water) {
                    newElevation += 1;
                }
                if (newElevation < a.elevation) {
                    a.elevation = newElevation;
                    queue.add(a);
                }
            }
        }
        // Calculate temperature from elevation and distance from corners
        for (Corner c : corners) {
            calcRelativeTemp(c);
        }

//        for (Corner c : corners) {
//        	System.out.println("Elevation: " + c.elevation);
//        }
    }
    double[][] noise;
    double ISLAND_FACTOR = 1.07;  // 1.0 means no small islands; 2.0 leads to a lot
    int bumps;
    double startAngle;
    double dipAngle;
    double dipWidth;
    
    //only the radial implementation of amitp's map generation
    //TODO implement more island shapes

    private boolean isWater(PointH p) {
    	
    	/* Attempts at adding Perlin noise made by Kyle Dhillon
    	 * 9/2013
    	 * 
    	 */
    	p = new PointH(2 * (p.x / bounds.width - 0.5), 2 * (p.y / bounds.height - 0.5));
    	//
    	//        double angle = Math.atan2(p.y, p.x);
    	//        double length = 0.5 * (Math.max(Math.abs(p.x), Math.abs(p.y)) + p.length());
    	//
    	//        double r1 = 0.5 + 0.40 * Math.sin(startAngle + bumps * angle + Math.cos((bumps + 3) * angle));
    	//        double r2 = 0.7 - 0.20 * Math.sin(startAngle + bumps * angle - Math.sin((bumps + 2) * angle));
    	//        if (Math.abs(angle - dipAngle) < dipWidth
    	//                || Math.abs(angle - dipAngle + 2 * Math.PI) < dipWidth
    	//                || Math.abs(angle - dipAngle - 2 * Math.PI) < dipWidth) {
    	//            r1 = r2 = 0.2;
    	//        }
    	//        return !(length < r1 || (length > r1 * ISLAND_FACTOR && length < r2));



    	//return false;

//    	if (noise == null) {
//    		noise = new Perlin2d(.125, 8, r.seed).createArray(257, 257);
//    	}
//    	int x = (int) ((p.x + 1) * 128);
//    	int y = (int) ((p.y + 1) * 128);
//    	return noise[x][y] < .3 + .3 * p.l2();

//    	boolean eye1 = new Point(p.x - 0.2, p.y / 2 + 0.2).length() < 0.05;
//    	boolean eye2 = new Point(p.x + 0.2, p.y / 2 + 0.2).length() < 0.05;
//    	boolean body = p.length() < 0.8 - 0.18 * Math.sin(5 * Math.atan2(p.y, p.x));
//    	return !(body && !eye1 && !eye2);

    	// The Perlin-based island combines perlin noise with the radius

//    	System.out.println("x: " + (float) ((p.x+1)));
//    	System.out.println("y: " + (float) ((p.y+1)));
//    	System.out.println("noise: " + perlin.tileableNoise2((int) ((p.x+1)*128), (int) ((p.y+1)*128), 56, 56));
//    	System.out.println("int : " + (int) (perlin.tileableNoise2((int) ((p.x+1)*128), (int) ((p.y+1)*128), 56, 56)));
//    	
    	
//    	System.out.println((perlin.noise2((int) ((p.x+1)*128), (int) ((p.y+1)))) / 255);
    	double c = perlin.noise2((float) ((p.x)*TURBULENCE), (float) ((p.y)*TURBULENCE));
    	c += LAND_FREQ;
//    	System.out.println("C: " + c);

    	double compare = WATER_FREQ+ROUNDNESS*p.length()*p.length()*p.length()*p.length(); // compare increases as distance from center increases, outside will have high compare, outside should be water, high compare should be water
//    	System.out.println("len: " + p.length());
//    	System.out.println("compare: " + compare);
    	return c < (compare);
    };



    private void assignOceanCoastAndLand() {
        Array<Center> queue = new Array<Center>();
        final double waterThreshold = 0.6; // 0.3 originally
        
        for (final Center center : centers) {
            int numWater = 0;
            for (final Corner c : center.corners) {
                if (c.border) {
                    center.border = center.water = center.ocean = true;
                    queue.add(center);
                }
                if (c.water) {
                    numWater++;
                }
            }
            center.water = center.ocean || ((double) numWater / center.corners.size() >= waterThreshold);
        }
        while (queue.size > 0) {
            final Center center = queue.pop();
            for (final Center n : center.neighbors) {
                if (n.water && !n.ocean) {
                    n.ocean = true;
                    queue.add(n);
                }
            }
        }
        for (Center center : centers) {
            boolean oceanNeighbor = false;
            boolean landNeighbor = false;
            for (Center n : center.neighbors) {
                oceanNeighbor |= n.ocean;
                landNeighbor |= !n.water;
            }
            center.coast = oceanNeighbor && landNeighbor;
//            if (center.coast) {
            	
//            }
        }

        for (Corner c : corners) {
            int numOcean = 0;
            int numLand = 0;
            for (Center center : c.touches) {
                numOcean += center.ocean ? 1 : 0;
                numLand += !center.water ? 1 : 0;
            }
            c.ocean = numOcean == c.touches.size();
            c.coast = numOcean > 0 && numLand > 0;
            c.water = c.border || ((numLand != c.touches.size()) && !c.coast);
        }
        
    }

    private ArrayList<Corner> landCorners() {
        final ArrayList<Corner> list = new ArrayList();
        for (Corner c : corners) {
            if (!c.ocean && !c.coast) {
                list.add(c);
            }
        }
        return list;
    }

    private void redistributeElevations(ArrayList<Corner> landCorners) {
        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                if (o1.elevation > o2.elevation) {
                    return 1;
                } else if (o1.elevation < o2.elevation) {
                    return -1;
                }
                return 0;
            }
        });

        final double SCALE_FACTOR = 1.1;
        for (int i = 0; i < landCorners.size(); i++) {
        	float y = (float) i / landCorners.size();
            float x = (float) (Math.sqrt(SCALE_FACTOR) - Math.sqrt(SCALE_FACTOR * (1 - y)));
            x = Math.min(x, 1);
            landCorners.get(i).elevation = x;
        }

        for (Corner c : corners) {
            if (c.ocean || c.coast) {
                c.elevation = 0.0f;
            }
        }
    }

    private void assignPolygonElevations() {
        for (Center center : centers) {
        	float total = 0;
            for (Corner c : center.corners) {
                total += c.elevation;
            }
            center.elevation = total / center.corners.size();
            if (center.elevation > maxElevation) {
                maxElevation = center.elevation;
            }
        }
    }

    private void assignPolygonTemperatures() {
        for (Center center : centers) {
            float total = 0;
            for (Corner c : center.corners) {
                total += c.temperature;
            }
            center.temperature = total / center.corners.size();

            // modify temp based on elevation
            // basically, 20 degrees temperature loss
            float MAX_TEMP_LOSS = 0.2f;
            center.temperature += ((center.elevation / maxElevation)) * MAX_TEMP_LOSS;

            float BASE_LOSS = 0.1f;
            center.temperature += BASE_LOSS;
        }
    }

    private void calculateDownslopes() {
        for (Corner c : corners) {
            Corner down = c;
            //System.out.println("ME: " + c.elevation);
            for (Corner a : c.adjacent) {
                //System.out.println(a.elevation);
                if (a.elevation <= down.elevation) {
                    down = a;
                }
            }
            c.downslope = down;
        }
    }

    private void createRivers() {
        for (int i = 0; i < bounds.width / 2; i++) {
            Corner c = corners.get(r.nextInt(0, corners.size()));
            if (c.ocean || c.elevation < 0.3 || c.elevation > 0.9) {            	
            	continue;
            }
        	// Added by Kyle to reduce num of rivers
            if (Math.random() < .4) continue;
            // Bias rivers to go west: if (q.downslope.x > q.x) continue;
            while (!c.coast) {
                if (c == c.downslope) {
                    break;
                }
                Edge edge = lookupEdgeFromCorner(c, c.downslope);
                if (!edge.v0.water || !edge.v1.water) {
                    edge.river++;
                    c.river++;
                    c.downslope.river++;  // TODO: fix double count
                }
                c = c.downslope;
            }
        }
    }

    private Edge lookupEdgeFromCorner(Corner c, Corner downslope) {
        for (Edge e : c.protrudes) {
            if (e.v0 == downslope || e.v1 == downslope) {
                return e;
            }
        }
        return null;
    }

    private void assignCornerMoisture() {
        Array<Corner> queue = new Array<Corner>();
        for (Corner c : corners) {
            if ((c.water || c.river > 0) && !c.ocean) {
                c.moisture = c.river > 0 ? Math.min(3.0, (0.2 * c.river)) : 1.0;
                queue.add(c); // formerly push
            } else {
                c.moisture = 0.0;
            }
        }

        while (queue.size > 0) {
            Corner c = queue.pop();
            for (Corner a : c.adjacent) {
                double newM = .9 * c.moisture;
                if (newM > a.moisture) {
                    a.moisture = newM;
                    queue.add(a);
                }
            }
        }

        // Salt water
        for (Corner c : corners) {
            if (c.ocean || c.coast) {
                c.moisture = 1.0;
            }
        }
    }

    private void redistributeMoisture(ArrayList<Corner> landCorners) {
        Collections.sort(landCorners, new Comparator<Corner>() {
            @Override
            public int compare(Corner o1, Corner o2) {
                if (o1.moisture > o2.moisture) {
                    return 1;
                } else if (o1.moisture < o2.moisture) {
                    return -1;
                }
                return 0;
            }
        });
        for (int i = 0; i < landCorners.size(); i++) {
            landCorners.get(i).moisture = (double) i / landCorners.size();
        }
    }

    private void assignPolygonMoisture() {
        for (Center center : centers) {
            double total = 0;
            for (Corner c : center.corners) {
                total += c.moisture;
            }
            center.moisture = total / center.corners.size();
        }
    }

    private Biomes getBiomeFromTemp(Center p) {
        if (p.ocean) {
            return Biomes.OCEAN;
        }
        // this could be fucking up pathfinding?
        else if (p.water) {
            if (p.temperature < 0.2) {
                return Biomes.MARSH;
            }
//            if (p.temper > 0.8) {
//                return Biomes.ICE;
//            }
            return Biomes.LAKE;
        } else if (p.coast && p.temperature < 0.6 && (p.elevation / maxElevation) < 0.2) {
//            for (Center adj : p.neighbors) {
//                if (!adj.water && adj.biome != null && adj.biome != Biomes.BEACH && Math.random() < 0.5) return adj.biome;
//            }
            return Biomes.BEACH;
        }
        else if (p.elevation / maxElevation > 0.8) {
            if (p.temperature > 0.75) {
                return Biomes.MOUNTAINS;
            } else if (p.temperature > 0.3) {
                return Biomes.PLATEAU;
            } else {
                return Biomes.SCORCHED;
            }
        }

        else if (p.temperature > 0.83) {
            if (p.moisture > 0.60) {
                return Biomes.SNOW;
            } else
                return Biomes.TUNDRA;
        } else if (p.temperature > 0.75) {
            if (p.moisture > 0.5) {
                return Biomes.TAIGA;
            } else {
                return Biomes.SHRUBLAND;
            }
        } else if (p.temperature > 0.5) {
            // TODO figure out how to properly distribute decidous forest, maybe group different moisture regions more closely.
             if (p.moisture > 0.65) {
                return Biomes.TEMPERATE_DECIDUOUS_FOREST;
            } else if (p.moisture > 0) {
                return Biomes.GRASSLAND;
            } else {
                return Biomes.SUBTROPICAL_DESERT;
            }
        } else {
            if (p.moisture > 0.80) {
                return Biomes.SWAMP;
            } else if (p.moisture > 0.45) {
                return Biomes.TROPICAL_RAIN_FOREST;
            } else {
                return Biomes.SUBTROPICAL_DESERT;
            }
        }
    }

//    private Biomes getBiome(Center p) {
//        if (p.ocean) {
//            return Biomes.OCEAN;
//        }
//        // this could be fucking up pathfinding?
//        else if (p.water) {
//            if (p.elevation < 0.2) {
//                return Biomes.MARSH;
//            }
////            if (p.elevation > 0.8) {
////                return Biomes.ICE;
////            }
//            return Biomes.LAKE;
//        } else if (p.coast) {
//        	for (Center adj : p.neighbors) {
//        		if (!adj.water && adj.biome != null && Math.random() < 0.7) return adj.biome;
//        	}
//            return Biomes.BEACH;
//        }
//        // Kyle modified these values
//        // originally 0.8
//        else if (p.elevation > 0.55) {
//            if (p.moisture > 0.60) {
//                return Biomes.SNOW;
//            } else if (p.moisture > 0.43) {
//                return Biomes.TUNDRA;
//            } else if (p.moisture > 0.26) {
//                return Biomes.MOUNTAINS;
//            } else {
//                return Biomes.SCORCHED;
//            }
//        } else if (p.elevation > 0.6) {
//            if (p.moisture > 0.66) {
//                return Biomes.TAIGA;
//            } else if (p.moisture > 0.33) {
//                return Biomes.SHRUBLAND;
//            } else {
//                return Biomes.PLATEAU;
//            }
//        } else if (p.elevation > 0.3) {
//            if (p.moisture > 0.83) {
//                return Biomes.TROPICAL_RAIN_FOREST;
//            } else if (p.moisture > 0.50) {
//                return Biomes.TEMPERATE_DECIDUOUS_FOREST;
//            } else if (p.moisture > 0.16) {
//                return Biomes.GRASSLAND;
//            } else {
//                return Biomes.PLATEAU;
//            }
//        } else {
//            if (p.moisture > 0.73) {
//                return Biomes.SWAMP;
//            } else if (p.moisture > 0.33) {
//                return Biomes.TROPICAL_SEASONAL_FOREST;
//            } else if (p.moisture > 0.16) {
//                return Biomes.GRASSLAND;
//            } else {
//                return Biomes.SUBTROPICAL_DESERT;
//            }
//        }
//    }

//    int textureIndex;

    public transient HashMap<Biomes, Texture> biomeMap;
//    public HashMap<Texture, Integer> textureMap;
    
    // test
    transient Texture texture8;
    transient Texture texture16;
    transient Texture texture8trees;
    transient Texture texture8treeslight;
    transient Texture texture12trees;
    transient Texture texture12treeslight;
    transient Texture texture16trees;
    transient Texture texture16treeslight;
    transient Texture texture32trees;
    transient Texture texture32treeslight;

    transient Texture texture64;
    transient Texture texture64inv;
//    Texture texture8;

    private void initBiomeTextures() {
//    	textureMap = new HashMap<Texture, Integer>();
    	texture8 = addTexture("8.png");
    	texture16 = addTexture("16.png");
    	texture16trees = addTexture("16trees.png");
    	texture16treeslight = addTexture("16treeslight.png");
    	texture12trees = addTexture("12trees.png");
    	texture12treeslight = addTexture("12treeslight.png");
    	texture8trees = addTexture("8trees.png");
    	texture8treeslight = addTexture("8treeslight.png");

        texture32trees = addTexture("32trees.png");
        texture32treeslight = addTexture("32treeslight.png");
    	
    	texture64 = addTexture("64.png");
    	texture64inv = addTexture("64inv.png");
		Texture grass = addTexture("grass.png");
		Texture sand = addTexture("sand.png");
		Texture sandlight = addTexture("sandlight.png");
		Texture flowers = addTexture("flowers.png");
		Texture darkgrass = addTexture("darkgrass.png");
		Texture dirt = addTexture("dirt.png");
		Texture mud = addTexture("mud.png");
		Texture snow = addTexture("snow.png");
		Texture swamp = addTexture("swamp.png");
		Texture swampdark = addTexture("swamp2.png");
		Texture water = addTexture("water.png");
		Texture lightgrass = addTexture("lightgrass.png");
		Texture rock = addTexture("rock.png");

    	biomeMap = new HashMap<Biomes, Texture>();
//    	mapBiome(Biomes.OCEAN, water);
//    	mapBiome(Biomes.LAKE, water);
//    	mapBiome(Biomes.TUNDRA, dirt);
//    	mapBiome(Biomes.TROPICAL_SEASONAL_FOREST, flowers);
//    	mapBiome(Biomes.TROPICAL_RAIN_FOREST, darkgrass);
//    	mapBiome(Biomes.TEMPERATE_DECIDUOUS_FOREST, grass);
//    	mapBiome(Biomes.SUBTROPICAL_DESERT, sand);
//    	mapBiome(Biomes.TEMPERATE_DESERT, sand);
//    	mapBiome(Biomes.SNOW, snow);
//    	mapBiome(Biomes.SCORCHED, mud);
//    	mapBiome(Biomes.LAKESHORE, sandlight);
//    	mapBiome(Biomes.BEACH, sandlight);
//    	mapBiome(Biomes.COAST, sandlight);
//    	mapBiome(Biomes.ICE, snow);
//    	mapBiome(Biomes.MOUNTAINS, rock);
//    	mapBiome(Biomes.SHRUBLAND, lightgrass);
//    	mapBiome(Biomes.TAIGA, water);
//    	mapBiome(Biomes.MARSH, swampdark);
//    	mapBiome(Biomes.SWAMP, swamp);
//    	mapBiome(Biomes.GRASSLAND, lightgrass);


        // TODO for maximum realness, draw a layer of trees AFTER the player has been drawn
    	mapBiome(Biomes.OCEAN, texture8);
    	mapBiome(Biomes.LAKE, texture8);
    	mapBiome(Biomes.TUNDRA, texture16);
    	mapBiome(Biomes.TROPICAL_SEASONAL_FOREST, texture32treeslight);
    	mapBiome(Biomes.TROPICAL_RAIN_FOREST, texture32trees);
    	mapBiome(Biomes.TEMPERATE_DECIDUOUS_FOREST, texture32trees);
    	mapBiome(Biomes.SUBTROPICAL_DESERT, texture16);
    	mapBiome(Biomes.PLATEAU, texture16);
    	mapBiome(Biomes.SNOW, texture64);
    	mapBiome(Biomes.SCORCHED, texture64);
    	mapBiome(Biomes.LAKESHORE, texture16);
    	mapBiome(Biomes.BEACH, texture64);
    	mapBiome(Biomes.ICE, texture64);
    	mapBiome(Biomes.MOUNTAINS, texture16);
    	mapBiome(Biomes.SHRUBLAND, texture32treeslight);
    	mapBiome(Biomes.TAIGA, texture16);
    	mapBiome(Biomes.MARSH, texture8);
    	mapBiome(Biomes.SWAMP, texture16);
    	mapBiome(Biomes.GRASSLAND, texture64);
    }
    
    public Texture addTexture(String name) {
    	Texture ok = new Texture(Gdx.files.internal("ground/" + name));
    	
//    	textureMap.put(ok, textureIndex++);
    	return ok;
    }
    
    public void mapBiome(Biomes b, Texture t) {
    	biomeMap.put(b, t);

//    	biomeMap.put(b, t);
    }
    

    private void assignBiomes() {
        for (Center center : centers) {
//            System.out.println(getBiomeFromTemp(center).name());
        	center.setBiome(getBiomeFromTemp(center));
        	center.setBiomeTexture(biomeMap.get(getBiomeFromTemp(center)));
        }
    }

    private void assignCultures() {
        // Literally repeat this until they're all full.
        boolean atLeastOneIsNull = true;
        System.out.println("Assigning culture");
        int totalIters = 0;
        while (atLeastOneIsNull && totalIters < 15) {
            totalIters++;
            System.out.println("at least one null");
            atLeastOneIsNull = false;
            for (Center center : centers) {
                if (center.water) continue;
//                continue;

                System.out.println("doing center");

                if (center.biome == null) throw new AssertionError();
                CultureType type = UnitLoader.biomeCultures.get(center.biome);

                if (type == null) {
                    System.out.println("culture type is null");
                    HashMap<CultureType, Integer> counts = new HashMap<>();
                    for (Center adj : center.neighbors) {
                        if (adj.cultureType != null) {
                            if (counts.containsKey(adj.cultureType)) {
                                counts.put(adj.cultureType, counts.get(adj.cultureType) + 1);
                            } else {
                                counts.put(adj.cultureType, 1);
                            }
                        }
                    }

                    int max = 0;
                    for (CultureType key : counts.keySet()) {
                        if (counts.get(key) > max) {
                            max = counts.get(key);
                            type = key;
                        }
                    }
                }

                if (type != null) {
                    System.out.println("setting culture type for " + center.biome.name() + " " + type.name);
                    center.setCultureType(type);
                } else {
                    System.out.println("culture type is null2");
                    atLeastOneIsNull = true;
                }
            }
        }
    }
    
    public static int OCEAN = 0x44447aff;
//    public static int LAKESHORE = 0xff5588ff;
    public static int LAKESHORE = 0x225588ff;
    public static int LAKE = 0x336699ff;
//    public static int RIVER = 0x225588ff;
    public static int MARSH = 0x2f6666ff;
    public static int SWAMP = 0x309066ff;
    public static int ICE = 0x99ffffff;
    public static int BEACH = 0xa09077ff;
    public static int ROAD1 = 0x442211ff;
    public static int ROAD2 = 0x553322ff;
    public static int ROAD3 = 0x664433ff;
//    public static int vBRIDGE = 0x686860ff;
    public static int LAVA = 0xcc3333ff;
    // Terrain
    public static int SNOW = 0xffffffff;
//    public static int TUNDRA = 0xbbbbaaff;
    public static int TUNDRA = 0x99bb88ff;
    public static int BARE = 0x888888ff;
//    public static int SCORCHED = 0x555555ff;
    public static int SCORCHED = 0x774444ff;
    public static int TAIGA = 0x99aa77ff;
    public static int SHRUBLAND = 0x889977ff;
    public static int TEMPERATE_DESERT = 0xc9d29bff;
    public static int TEMPERATE_RAIN_FOREST = 0x448855ff;
    public static int TEMPERATE_DECIDUOUS_FOREST = 0x679459ff;
    public static int GRASSLAND = 0x88aa55ff;
    public static int SUBTROPICAL_DESERT = 0xd2b98bff;
    public static int TROPICAL_RAIN_FOREST = 0x337755ff;
    public static int TROPICAL_SEASONAL_FOREST = 0x559944ff;
//    public static HashMap<Integer, String> colorBiomeMap = new HashMap();
//    public static HashMap<String, Integer> biomeColorMap = new HashMap();

    static {
//        addColor("OCEAN", 0x44447aff);
//        addColor("COAST", 0x33335aff);
//        addColor("LAKESHORE", 0x225588ff);
//        addColor("LAKE", 0x336699ff);
//        addColor("RIVER", 0x225588ff);
//        addColor("MARSH", 0x2f6688ff);
//        addColor("SWAMP", 0x2f6666ff);
//        addColor("ICE", 0x99ffffff);
//        addColor("BEACH", 0xa09077ff);
//        addColor("SNOW", 0xffffffff);
//        addColor("TUNDRA", 0xbbbbaaff);
//        addColor("MOUNTAINS", 0x888888ff);
//        addColor("SCORCHED", 0x555555ff);
//        addColor("TAIGA", 0x99aa77ff);
//        addColor("SHRUBLAND", 0x889977ff);
//        addColor("TEMPERATE_DESERT", 0xc9d29bff);
////        addColor("TEMPERATE_RAIN_FOREST", 0x448855ff);
//        addColor("TEMPERATE_DECIDUOUS_FOREST", 0x679459ff);
//        addColor("GRASSLAND", 0x88aa55ff);
//        addColor("SUBTROPICAL_DESERT", 0xd2b98bff);
//        addColor("TROPICAL_RAIN_FOREST", 0x337755);
//        addColor("TROPICAL_SEASONAL_FOREST", 0x559944);
    }

//    private static void addColor(String name, int color) {
//        colorBiomeMap.put(color, name);
//        biomeColorMap.put(name, color);
//    }
    
    
    // call this after restoring the map to bridge the graph
    public void restore() {
        initBiomeTextures();

    	for (Edge edge : edges) {
    		edge.restoreFromVoronoi(this);
    	}
//    	System.out.println("edges restored");
    	for (Corner corner : corners) {
    		corner.restoreFromVoronoi(this);
    	}
//    	System.out.println("corners restored");
    	for (Center center : centers) {
    		center.restoreFromVoronoi(this);
    	}
//    	System.out.println("centers restored");
    }
}
