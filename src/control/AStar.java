package control;

import game.Item;
import java.awt.Point;
import java.util.HashSet;
import java.util.PriorityQueue;

import ui.Board;
import ui.Square;

public class AStar {

	/**
	 * An A* algorithm of which iterates through the out neighbours for the given startNavPoint and moves towards the end NavPoint until found.
	 * If the algorithm does not find the end NavPoint, it returns null.
	 * @param startNavPoint
	 * @param endNavPoint
	 * @return A Linked list of the NavPoints from the start point to the end point
	 */
	public static AMove_Node getPath(Point start, Point end, Item item, Board board){
		if (start == null || end == null) return null;

		Point startPoint = end;
		Point endPoint = start;

		Square endSquare = board.getSquare(endPoint);

		// Make sure we can leave this navPoint at least
		if( board.getAdjacentSquares(start.x, start.y, endSquare).isEmpty() ){
			System.out.println("A_STAR: No connections! " + start + " to " + end);
			return null;
		}


		HashSet<Point> visited = new HashSet<Point>();

		// Create the fringe and the first start
		PriorityQueue<AMove_Node> fringe = new PriorityQueue<AMove_Node>();
		fringe.add(new AMove_Node(startPoint,0, estimate(startPoint, endPoint), null));

		AMove_Node workingPoint = null;
		Point point = null;

		while (!fringe.isEmpty()) {

			// Take the highest priority NavPoint off the fringe
			workingPoint = fringe.poll();
			point = workingPoint.getPoint();

			// Not Visited
			if ( !visited.contains(point) ) {

				if ( point.equals(endPoint) ) {

					break;
				} else {

					// Not our target NavPoint
					// Have not visited this NavPoint until now
					visited.add(point);

					// Make sure we can move to that position
					if( board.getSquare(point).canContainPiece() ){

						//System.out.println("## 5 found it!" + point);

						// Iterate through all of our NavLinks
						for (Square square : board.getAdjacentSquares(point.x, point.y, endSquare)) {
							Point neighbour = square.getPosition();

							// Add the neighbour to our fringe if it's not visited yet
							// Length = NavPoint + this length
							if ( !visited.contains(neighbour) ) {
								double heuristic = estimate(neighbour, endPoint);
								double totalCost = (workingPoint.getLength()+ 1);
								fringe.add(new AMove_Node(neighbour,
										totalCost, heuristic, workingPoint));
							}
						}
					}
				}
			}
		}

		// We didn't find the end NavPoint so return null
		if ( !point.equals(endPoint) ) {
			return null;
		}

		return workingPoint.getNext();
	}

	/**
	 * Returns the estimate of the start and end NavPoints provided
	 *
	 * @param startPoint
	 * @param endPoint
	 * @return Distance to end from start
	 */
	private static double estimate(Point startPoint, Point endPoint) {
		return Math.abs(startPoint.distance(endPoint)) + 1;
	}
}
