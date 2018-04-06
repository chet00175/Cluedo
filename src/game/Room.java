package game;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ui.Square;

public class Room implements Cloneable {

	public static final String CORRIDOR = "Corridor";

	private String name;
	private Portal portal;
	private List<Player> players;
	private List<Weapon> weapons;
	private ArrayList<Square> squareList = new ArrayList<Square>();
	private ArrayList<Square> doorList = new ArrayList<Square>();
	private Rectangle bounds;

	public Room(String n) {
		name = n;
		portal = null;
		players = new ArrayList<Player>();
		weapons = new ArrayList<Weapon>();
	}

	public Portal getPortal() {
		return portal;
	}

	public void setPortal(Portal portal) {
		this.portal = portal;
	}

	public void addPlayer(Player p) {
		players.add(p);
	}

	public void addWeapon(Weapon w) {
		weapons.add(w);
	}

	public boolean removePlayer(Player p) {
		return players.remove(p);
	}

	public boolean removeWeapon(Weapon w) {
		return weapons.remove(w);
	}

	public String getName(){
		return name;
	}

	/**
	 * Adds the square to the list of squares related to the room
	 * Does not add duplicates
	 * @param square Related to this room
	 */
	public void addSquare(Square square){
		if( !squareList.contains(square) ){
			squareList.add(square);
			updateBounds(square);
		}
	}

	public Rectangle getBounds(){
		return bounds;
	}

	/**
	 * Updates the bounds of the Room according to the squares that's adding to the square
	 * @param newSquare New square of which it's bounds will be added to the room
	 */
	public void updateBounds(Square newSquare){

		// No reason to update the bounds
		if( newSquare == null || name.equals(CORRIDOR)){
			return;
		}

		Rectangle squareBounds = new Rectangle(newSquare.getPosition().x, newSquare.getPosition().y, 1, 1);
		if( bounds == null ){
			bounds = squareBounds;
		}
		else{
			bounds = bounds.union(squareBounds);
		}
	}

	/**
	 * Adds the square to the list of doors that leave and exit the room
	 * Does not add duplicates
	 * @param door Square in order to enter and exit this room
	 */
	public void addDoor(Square door){
		if( !doorList.contains(door) ){
			doorList.add(door);
			updateBounds(door);
		}
	}

	/**
	 * Returns the list of squares for this room
	 * @return List of squares related to this room
	 */
	public ArrayList<Square> getSquares() {
		return squareList;
	}

	/**
	 * Returns a random square inside this room
	 * @return Square associated with the room
	 */
	public Square getRandomSquare(){
		Square randomSquare = null;

		// Loop until we can find a random square in the Room that is NOT a door
		while( randomSquare == null || (randomSquare instanceof Door) ){
			randomSquare = squareList.get(new Random().nextInt(squareList.size()));
		}

		// Return a random door
		return randomSquare;
	}

	public ArrayList<Square> getDoors() {
		return doorList;
	}



	@Override
	public String toString(){
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Room))
			return false;
		Room other = (Room) obj;
		if (!name.equals(other.name))
			return false;
		return true;
	}
}
