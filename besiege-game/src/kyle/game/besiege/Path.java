/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

//import hoten.geom.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;
//import java.util.Map;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.geom.PointH;
import kyle.game.besiege.voronoi.Corner;
import kyle.game.besiege.voronoi.CornerNode;
import kyle.game.besiege.voronoi.Edge;

import com.badlogic.gdx.math.Vector2;

public class Path {
	private static final int A_STAR_WAIT = 100;
	private static final int GOAL_DIST = 8;
	public Map map;
	private Destination start;
	public Destination finalGoal;
//	private Stack<Destination> dStack; // maybe a queue, whatever will be easier.
	//public for debugging
	public Stack<Destination> dStack; // maybe a queue, whatever will be easier.
	public Destination nextGoal;
	private Vector2 toTarget;
	
	// how many iterations ago a star failed
	int lastAStarFail = 0;
	
	// Should I do it with SearchNodes?
	private class SearchNode implements Comparable<SearchNode> {
		private Corner curr; // represents a total map (with previous)
		private SearchNode prev; // prev search node
		private double H; // distance to goal
		private double G; // distance traveled to get here (can be relaxed?)

		public int compareTo(SearchNode that) {
			return (int) ((this.H + this.G) - (that.H + that.G));
		}
	}	
	
	// for Kyro
	public Path() {
		
	}

	public Path(Destination d, Kingdom k) {
		this.start = d;
		this.map = k.getMap();
		dStack = new Stack<Destination>();
		toTarget = new Vector2();
	}
	
//	public Path(Army army) {
//		this.start = army;
////		this.map = army.getKingdom().getMap();
//		dStack = new Stack<Destination>();
//		toTarget = new Vector2();
//	}

	public void calcStraightPathTo(Destination endDest) {
		dStack.clear();
		dStack.add(endDest);
	}
	
	/** attempts a* to new destination
	 *  returns true if succeeds, false if doesn't
	 * @param endDest
	 * @return
	 */
	public boolean calcPathTo(Destination endDest) {
		if (lastAStarFail > 0) {
			calcStraightPathTo(endDest);
			return true;
		}
		
		finalGoal = endDest;
//		System.out.println(army.getName() + " getting new Path"); not the problem for freezing
//		if (army.getParty().player) System.out.println("player getting new path");
		// TODO remove news?
		dStack.clear();
		Corner startCorner = new Corner();
		startCorner.loc = new PointH(start.getCenterX(), Map.HEIGHT-start.getCenterY());
		startCorner.init();
		Corner endCorner = new Corner();
		endCorner.loc = new PointH(endDest.getCenterX(), Map.HEIGHT-endDest.getCenterY());
		endCorner.init();
		
		Edge edgeBlock = map.openPath(startCorner, endCorner);
//		if (army.getParty().player) {
//			if (map.isInWater(endDest))
//				System.out.println("destination in water!!!");
//		}
		
		// allow armies off island to travel directly to destination on island
		// TODO Make armies off island head towards closest land
		if (edgeBlock == null || map.isInWater(start)) {
//			if (army.getParty().player) {
//				map.testIndex = 1;
//				//				System.out.println("open path");
//			}
			dStack.add(endDest);
			return true;
		}
		else if (pathExists(endDest.getCenterX(), endDest.getCenterY())) {
			map.addCorner(endCorner);
//			map.addCorner(startCorner);
			map.calcVisible(startCorner);
//			map.calcVisible(endCorner);
			Stack<Destination> aStarStack = aStar(startCorner, endCorner, endDest);
			if (aStarStack != null) {
				this.dStack = aStarStack;
//				System.out.println("a star completed");
			}
			else {
				// this is really broken TODO fix
				System.out.println("a star failed");
				map.removeCorner(endCorner);
				map.removeCorner(startCorner);
				this.lastAStarFail = A_STAR_WAIT;
				return false;
			}

//			if (army.getParty().player) {
//				map.testIndex = map.impassable.indexOf(edgeBlock, true);
//			}
			
			map.removeCorner(endCorner);
			map.removeCorner(startCorner);
			return true;
		}
		else {
			System.out.println(start.getName() + " can't access");
			return false;
		}
	}

//	public Stack<Destination> BFS(Corner start, Corner goal) {
//		ArrayList<Corner> visited = new ArrayList<Corner>(); 	// should only visit each corner once
//		//ArrayList<Corner> notVisited = new ArrayList<Corner>(); // should visit all corners 
//		PriorityQueue<SearchNode> pq = new PriorityQueue<SearchNode>();
//
//		// can iterate if fails
//		//for (Corner c : map.borderCorners)
//		//	notVisited.add(c);
//
//		SearchNode first = new SearchNode();
//
//		first.curr = start;
//		first.prev = null;
//		first.H = 0; // hamming/manhattan distance corresponds to H
//		first.G = 0; // moves corresponds to G, we're trying to minimize G
//
//		pq = new PriorityQueue<SearchNode>();
//		pq.add(first);
//
//		SearchNode min; 
//
//		while (pq.peek().curr != goal) { // || notVisited.isEmpty()) {
//			min = pq.remove();
//		//	notVisited.remove(min.curr);
//			visited.add(min.curr);
//
//			for (Corner corner : min.curr.visibleCorners) {
//				if (!visited.contains(corner)) {
//					SearchNode s = new SearchNode();
//					s.curr = corner;
//					s.prev = min;
//					s.G = min.G + heuristicDist(s.curr, min.curr);
//					s.H = 0; // don't worry about heuristic
//					pq.add(s);
//				}
//			}
//		}
//		
//		//if (notVisited.isEmpty()) {
//		//	System.out.println("can't get to goal");
//		//	return null;
//		//}
//
//		min = pq.remove();
//		double minG = min.G;
//		System.out.println("dist is " + minG);
//		
//		Stack<Destination> path = new Stack<Destination>();
//		SearchNode tracker = min;
//		while (tracker != null) {
//			path.push(cornerToPoint(tracker.curr));
//			tracker = tracker.prev;
//		}
//		return path;
//	}

	
	// serious lag occurs when army calls this repeatedly.
	// TODO remove news
	public Stack<Destination> aStar(Corner start, Corner goal, Destination endDest) {
//		if (army.getParty().player) System.out.println("player in a*");

		PriorityQueue<SearchNode> pq;
		ArrayList<Corner> notVisited = new ArrayList<Corner>(); // should visit all corners 
		ArrayList<Corner> visited = new ArrayList<Corner>();

		for (Corner c : map.borderCorners)
			notVisited.add(c);
		
		SearchNode first = new SearchNode();
		first.curr = start;
		first.prev = null;
		first.H = heuristicDist(start, goal);   // hamming/manhattan distance corresponds to H
		first.G = 0;                    // moves corresponds to G, we're trying to minimize G

		pq = new PriorityQueue<SearchNode>();
		pq.add(first);

		SearchNode min; 
		
		int loops = 0;
		
//		System.out.println(army.getName() + " A* started to " + endDest.getName());
		
		while (!pq.isEmpty() && pq.peek().curr != goal && loops < 1000) {// && !notVisited.isEmpty()) {
			min = pq.remove();
			notVisited.remove(min.curr);
			visited.add(min.curr);
//			System.out.println("starting loop");
			loops++;

			for (Corner corner : min.curr.visibleCorners) {
				// skip check, may be specific to board problem
				if (min.prev == null || (corner != min.curr && corner != min.prev.curr)) {
					if (!visited.contains(corner)) {
						SearchNode s = new SearchNode();
						s.curr = corner;
						s.prev = min;
						s.G = min.G + heuristicDist(s.curr, min.curr);
						s.H = heuristicDist(s.curr, goal);
						//						System.out.println("adding new " + loops);
						pq.add(s);
					}
				}
			}   
		}
		if (loops >= 1000) {
			System.out.println("too long");
			return null;
		}
		
		if (notVisited.isEmpty()) {
			System.out.println("can't get to goal");
			return null;
		}
		if (pq.isEmpty()) return null;
		min = pq.remove();
		double minG = min.G;
//		if (army.getParty().player) System.out.println(minG);

		Stack<Destination> path = new Stack<Destination>();
		path.push(endDest);
		SearchNode tracker = min.prev;
		while (tracker != null) {
			path.push(cornerToPoint(tracker.curr));
			tracker = tracker.prev;
		}
		return path;
	}
	
	private boolean pathExists(double px, double py) {
		return map.pathExists(start, px, py);
	}

	private Point cornerToPoint(Corner corner) {
//		return new Point(corner.getLoc().x, Map.HEIGHT-corner.getLoc().y);
		return new Point(corner.getLoc().x, Map.HEIGHT-corner.getLoc().y);
	}

	public static double heuristicDist(Corner c1, Corner c2) {
		return Math.sqrt((c2.getLoc().x - c1.getLoc().x)*(c2.getLoc().x - c1.getLoc().x) + 
				(c2.getLoc().y-c1.getLoc().y)*(c2.getLoc().y-c1.getLoc().y));
//		return Math.sqrt((c2.getLoc().x - c1.getLoc().x)*(c2.getLoc().x - c1.getLoc().x) + 
//				(c2.getLoc().y-c1.getLoc().y)*(c2.getLoc().y-c1.getLoc().y));
	}

	public void travel() {
		lastAStarFail--;
		Army army = (Army) start;
		// make sure only doing detectCollision when close to goal
		if (nextGoal != null) {
			army.setRotation(calcRotation());
			updatePosition();
			army.updatePolygon();

			// only do collision detect when close to enemy
			
			
			if (dStack.isEmpty() && army.hasTarget()) {
//				if (army.getParty().player) System.out.println("player detecting collision");
				// if detects collision, set nextGoal to null
				if (army.detectCollision()) nextGoal = null;
//				if (army.runTo != null) army.runTo = null;
			}
			else {
				this.detectPointCollision();
			}
		}
		// try this to see if fixes garrison bug - nope
		//else dStack.clear();
	}

	public float calcRotation() {
		toTarget.x = nextGoal.getCenterX()-start.getCenterX();
		toTarget.y = nextGoal.getCenterY()-start.getCenterY();
		return toTarget.angle();
	}
	public void updatePosition() {
		Army army = (Army) start;
//		if (army.getParty().player)
//			System.out.println("rotation before translate " + army.getRotation());
		toTarget.nor();
		army.translate(toTarget.x*army.getSpeed(), toTarget.y*army.getSpeed());
//		if (army.getParty().player)
//			System.out.println("rotation after translate " + army.getRotation());
	}
	public void detectPointCollision() {
//		if (dStack.size() == 0)
		// probably target is being set to something very close to the army, this is being called, next is called
		if (Kingdom.distBetween(start, nextGoal) < GOAL_DIST) {
			next();
//			System.out.println(army.getName() + " detecting collision, close to " + nextGoal.getName());
		}
	}
	public void next() {
//		if (army.getParty().player) System.out.println("player's path.next");
//		if (dStack.isEmpty()) System.out.println("problem");
		
		// cleans out shit destinations, maybe fixes bug
		while (dStack.size() > 1 && Kingdom.distBetween(start, dStack.peek()) < GOAL_DIST) {
//			System.out.println("cleaning!");
			dStack.pop();
		}
		if (dStack.isEmpty()) {
			nextGoal = null;
		}
		else {
			nextGoal = dStack.pop();
			travel();
		}
	}
	
	/**returns true if there is no remaining destination in this path
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		if (nextGoal == null && dStack.isEmpty()) return true;
		return false;
	}
	
	
	public double getRemainingDistance() {
		if (dStack.isEmpty()) return Double.POSITIVE_INFINITY;
		
		double total = 0;
		Destination current = null;
		Destination next = start;
		for (Destination d : dStack) {
			current = next;
			next = d;
			total += Kingdom.distBetween(current, next);
		}
		return total;
	}
}
