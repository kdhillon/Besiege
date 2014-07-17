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
	private static final int GOAL_DIST = 5;
	private Map map;
	private Army army;
	public Destination finalGoal;
//	private Stack<Destination> dStack; // maybe a queue, whatever will be easier.
	//public for debugging
	public Stack<Destination> dStack; // maybe a queue, whatever will be easier.
	public Destination nextGoal;
	private Vector2 toTarget;
	
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

	public Path(Army army) {
		this.army = army;
		this.map = army.getKingdom().getMap();
		dStack = new Stack<Destination>();
		toTarget = new Vector2();
	}

	/** attempts a* to new destination
	 *  returns true if succeeds, false if doesn't
	 * @param endDest
	 * @return
	 */
	public boolean calcPathTo(Destination endDest) {
		finalGoal = endDest;
//		System.out.println(army.getName() + " getting new Path"); not the problem for freezing
//		if (army.getParty().player) System.out.println("player getting new path");
		// TODO remove news?
		dStack.clear();
		Corner startCorner = new Corner();
		startCorner.loc = new PointH(army.getCenterX(), Map.HEIGHT-army.getCenterY());
		startCorner.init();
		Corner endCorner = new Corner();
		endCorner.loc = new PointH(endDest.getCenterX(), Map.HEIGHT-endDest.getCenterY());
		endCorner.init();
		
		Edge edgeBlock = map.openPath(startCorner, endCorner);
		if (army.getParty().player) {
			if (map.isInWater(endDest))
				System.out.println("destination in water!!!");
		}
		
		// allow armies off island to travel directly to destination on island
		if (edgeBlock == null || map.isInWater(army)) {
			if (army.getParty().player) {
				map.testIndex = 1;
				//				System.out.println("open path");
			}
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
			else System.out.println("a star failed");

			if (army.getParty().player) {
				map.testIndex = map.impassable.indexOf(edgeBlock, true);
			}
			
			map.removeCorner(endCorner);
			map.removeCorner(startCorner);
			return true;
		}
		else {
			System.out.println(army.getName() + " can't access");
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
		if (army.getParty().player) System.out.println("player in a*");

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
		
		System.out.println(army.getName() + " A* started to " + endDest.getName());
		
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
		if (loops >= 1000) System.out.println("too long");
		
		if (notVisited.isEmpty()) {
			System.out.println("can't get to goal");
			return null;
		}
		if (pq.isEmpty()) return null;
		min = pq.remove();
		double minG = min.G;
		if (army.getParty().player) System.out.println(minG);

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
		return map.pathExists(army, px, py);
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
		// very slow, not the best way to fix this
//		if (finalGoal.getType() == 2) { // if following army (mobile)
//			// if path is blocked
//			if (map.openPath(army.getCenterX(), army.getCenterY(), finalGoal.getCenterX(), finalGoal.getCenterY()) != null) {
//				this.calcPathTo(finalGoal);
//			}
//		}
//		if (army.getParty().player) {
//			System.out.println("player target: " + army.getTarget().getName());
//			System.out.println("player travelling");
//		}

		// make sure only doing detectCollision when close to goal
		if (nextGoal != null) {
//			System.out.println(army.getName() + " has goal and dstack size: " + dStack.size());
			
			
			army.setRotation(calcRotation());
//			rotateToggle = !rotateToggle;
//			if (army.getParty().player) System.out.println("player rotation: " + army.getRotation());

			updatePosition();
			army.updatePolygon();

			// only do collision detect when close to enemy
			if (dStack.isEmpty() && army.hasTarget()) {
//				if (army.getParty().player) System.out.println("player detecting collision");
				// if detects collision, set nextGoal to null
				if (army.detectCollision()) nextGoal = null;
				if (army.runTo != null) army.runTo = null;
			}
			else {
				this.detectPointCollision();
			}
		}
	}

	public float calcRotation() {
		toTarget.x = nextGoal.getCenterX()-army.getCenterX();
		toTarget.y = nextGoal.getCenterY()-army.getCenterY();
		return toTarget.angle();
	}
	public void updatePosition() {
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
		if (Kingdom.distBetween(army, nextGoal) < GOAL_DIST) {
			next();
//			System.out.println(army.getName() + " detecting collision, close to " + nextGoal.getName());
		}
	}
	public void next() {
//		if (army.getParty().player) System.out.println("player's path.next");
//		if (dStack.isEmpty()) System.out.println("problem");
		
		// cleans out shit destinations, maybe fixes bug
		while (Kingdom.distBetween(army, dStack.peek()) < GOAL_DIST && dStack.size() > 1) {
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
}
