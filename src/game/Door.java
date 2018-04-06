package game;

import java.awt.Point;

import ui.Square;

public class Door extends Square{

	public static final int NOT_A_DOOR = 0;
	public static final int VERTICAL = 1;
	public static final int HORIZONTAL = 2;

	private int direction = 0;

	/**
	 * Create a new door object that is also a square
	 * NOT_A_DOOR = 0;
	 * VERTICAL = 1;
	 * HORIZONTAL = 2;
	 * @param x position x
	 * @param y position y
	 * @param direction direction fo the door
	 */
	public Door(int x, int y, int direction) {
		super(x, y);
		this.direction = direction;
	}

	/**
	 * Create a new door object that is also a square
	 * NOT_A_DOOR = 0;
	 * VERTICAL = 1;
	 * HORIZONTAL = 2;
	 * @param x position x
	 * @param y position y
	 * @param direction direction of the door
	 */
	public Door(int x, int y) {
		super(x, y);
	}

	/**
	 * Returns an integer determining the direction of the door
	 * @return NOT_A_DOOR || VERTICAL || HORIZONTAL
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * Assigns the direction of the door
	 * @param direction
	 */
	public void setDirection(int direction){

		// Check for valid direction
		if( direction != HORIZONTAL && direction != NOT_A_DOOR && direction != VERTICAL ){
			throw new RuntimeException(direction + " is an invalid direction");
		}

		// Assign new direction
		this.direction = direction;
	}

	/**
	 * Determines whether or not the door can be entered determining by the position
	 * @param point Position entering the door from
	 * @return Vertical indicates if the door can be entered above or below the door
	 */
	public boolean canUseDoor(Point point){
		return canUseDoor(point.x, point.y);
	}

	/**
	 * Determines whether or not the door can be entered determining by the position
	 * @param point Position entering the door from
	 * @return Vertical indicates if the door can be entered above or below the door
	 */
	public boolean canUseDoor(int x, int y){
		if( direction == VERTICAL ){
			return x == position.x;
		}
		else if( direction == HORIZONTAL ){
			return y == position.y;
		}

		// Invalid position to enter the door from
		return false;
	}

	public String toString(){
		if( room != null ){
			return "Door to " + room.getName() + "(" + position.x + "," + position.y + ")";
		}

		return "Door(" + position.x + "," + position.y + ")";
	}
}
