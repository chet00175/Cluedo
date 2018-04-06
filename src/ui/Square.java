package ui;

import game.InvalidMoveException;
import game.Item;
import game.Piece;
import game.Portal;
import game.Room;
import game.Weapon;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * Square class used for the board to distinguish what each square on the board represents.
 * The board can consist of both a Piece and a weapon and a room
 *
 */
public class Square implements Cloneable {

	protected final Point position;
	protected Room room = null;
	protected Piece piece = null;
	protected Weapon weapon = null;
	protected Portal portal = null;

	/**
	 * Creates a blank square consisting of no Piecees no weapons and is also not a room
	 */
	public Square(int x, int y) {
		this.position = new Point(x,y);
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Piece getPiece() {
		return piece;
	}

	public void setPiece(Piece Piece) {
		this.piece = Piece;
	}

	public Weapon getWeapon() {
		return weapon;
	}

	public void setWeapon(Weapon weapon) {
		this.weapon = weapon;
	}

	/**
	 * Removes the given item form the square
	 * @param item Item to remove from the square ( Piece or Weapon )
	 * @throws InvalidMoveException Thrown if the item was not contained within the Square
	 */
	public void removeItem(Item item) throws InvalidMoveException{
		if( item instanceof Weapon && weapon == item ){
			weapon = null;
		}
		else if( item instanceof Piece && piece == item ){
			piece = null;
		}
		else{
			throw new InvalidMoveException("Invalid item removal " + item.toString() + " = " + weapon + " , " + piece);
		}

	}

	/**
	 * Draws the square at the given position using the given dimensions
	 * Color of the square is determined whether it's a room, corridor or neither.
	 * @param g
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void draw(Graphics g, int x, int y, int w, int h){

		// Rooms are differently coloured to non-rooms
		if( isCorridoor() ){
			g.setColor(Color.yellow);
		}
		else if( isPortal() ){
			g.setColor( Color.blue );
		}
		else if( isRoom() ){
			g.setColor(new Color(229,235,199));
		}
		else{
			g.setColor( Color.black );
		}

		// Draw the base color
		g.fillRect(x,y,w,h);


		// Griding
		if( isCorridoor() ){
			g.setColor(g.getColor().darker());

			// Vertical
			g.drawLine(x, y, x, y+h-1);

			// Horizontal
			g.drawLine(x, y, x+w-1, y);
		}
		else{

		}

		// Draw Weapon
		if( weapon != null ) weapon.draw(g, x, y, w, h);

		// Draw Piece
		if( piece != null ) piece.draw(g, x, y, w, h);
	}

	/**
	 * See if this square is a portal
	 * @return True if the square contains a portal
	 */
	public boolean isPortal() {
		return portal != null;
	}

	/**
	 * Checks if this square is a room or not
	 * @return true if contains a room object
	 */
	public boolean isRoom() {
		return room != null && !isCorridoor();
	}

	/**
	 * Checks if this square is a room or not
	 * @return true if contains a room object
	 */
	public boolean isCorridoor() {
		return room != null && room.getName().equals(Room.CORRIDOR);
	}

	/**
	 * Checks if the given item can be placed on this square
	 * @param item Weapon or Piece to try and be placed here
	 * @return True if there are no items of that type currently on this square
	 */
	public boolean canContainPiece() {
		return this.piece == null;
	}

	/**
	 * Checks if the given item can be placed on this square
	 * @param item Weapon or Piece to try and be placed here
	 * @return True if there are no items of that type currently on this square
	 */
	public boolean canContainWeapon() {
		return this.weapon == null;
	}

	@Override
	public Square clone(){
		try {
			Square clone = (Square)super.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Point getPosition() {
		return (Point) position.clone();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((piece == null) ? 0 : piece.hashCode());
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((room == null) ? 0 : room.hashCode());
		result = prime * result + ((weapon == null) ? 0 : weapon.hashCode());
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
		if (!(obj instanceof Square))
			return false;
		Square other = (Square) obj;
		if (piece == null) {
			if (other.piece != null)
				return false;
		} else if (!piece.equals(other.piece))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (room == null) {
			if (other.room != null)
				return false;
		} else if (!room.equals(other.room))
			return false;
		if (weapon == null) {
			if (other.weapon != null)
				return false;
		} else if (!weapon.equals(other.weapon))
			return false;
		return true;
	}

	@Override
	public String toString(){
		if( room != null){
			return room.toString() + "(" + position + ")";
		}

		return super.toString();
	}

	/**
	 * @return the portal
	 */
	public Portal getPortal() {
		return portal;
	}

	/**
	 * @param portal the portal to set
	 */
	public void setPortal(Portal portal) {
		this.portal = portal;
	}

}
