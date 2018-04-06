package ui;

import game.Door;
import game.InvalidMoveException;
import game.Item;
import game.Piece;
import game.Player;
import game.Portal;
import game.Room;
import game.Weapon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.management.RuntimeErrorException;
import javax.swing.ImageIcon;

import control.AMove_Node;

/**
 * Board class of which should be unique for all classes of which contain a board object
 *
 */
public class Board implements Cloneable{

	public static final int BOARD_WIDTH = 24;
	public static final int BOARD_HEIGHT = 25;
	public static final int TILE_WIDTH = 25;
	public static final int TILE_HEIGHT = 25;
	public static final int WALL_THICKNESS = 4;

	// roomSet of which contains a 2D array of the rooms on the map
	private Square[][] layout = new Square[BOARD_WIDTH][BOARD_HEIGHT]; // [x][y]

	// Maps from room name to the room itself
	private ArrayList<Room> roomList = new ArrayList<Room>();

	// Maps from room name to the room itself
	private HashMap<String,Room> roomMap = new HashMap<String,Room>();

	// Statics positions where the pieces will always start
	private static HashMap<String, Point> pieceStartingPosition = new HashMap<String, Point>(){{
		put("Mrs. White",new Point(9,0));
		put("The Reverand Green",new Point(14,0));
		put("Mrs. Peacock",new Point(7,24));
		put("Colonel Mustard",new Point(0,17));
		put("Miss Scarlett",new Point(23,6));
		put("Professor Plum",new Point(16,24));
	}};


	// List of all Piece in the game
	private ArrayList<Piece> pieceList = new ArrayList<Piece>();

	// Mapping from PieceName to the Piece Object
	private HashMap<String, Piece> pieceMap = new HashMap<String, Piece>();

	// List of all weapons in the game
	private ArrayList<Weapon> weaponList = new ArrayList<Weapon>();

	// List of all weapons in the game
	private HashMap<String, Weapon> weaponMap = new HashMap<String, Weapon>();

	// Boolean to indicate if the board has been setUp or not.
	private boolean hasBeenSetUp = false;

	/**
	 * Creates a new board object.
	 * Sets up the board layout consisting of each square being a blank square where they are not a room, Piece or weapon.
	 */
	public Board(){

		// Sets up the game with all the rooms, pieces and weapons
		setUpRooms();
		setUpPieces();
		setUpWeapons();
	}

	/**
	 * Performs the given move on the board by moving it from the items location in the move object, by the moveBy x,y containing in the
	 * Throws an exception if the item is not at the given position specified in the move object.
	 * Throws exception if the given move is attempting a move to a position that already contains a piece on the board
	 * @param move
	 * @throws InvalidMoveException
	 */
	public void movePiece(AMove_Node move, Item item) throws InvalidMoveException{

		// Can not move null pieces
		if( move == null ){
			throw new InvalidMoveException("Can not perform a NULL move");
		}

		// Get the position of the next move
		int newX = move.getPoint().x;
		int newY = move.getPoint().y;

		// Make sure we can move to that location
		if( !layout[newX][newY].canContainPiece() ){
			throw new InvalidMoveException(newX + "," + newY + " already contains a piece. ( Can't jump over other pieces ) ");
		}

		// Remove item from board
		layout[item.getPosition().x][item.getPosition().y].removeItem(item);

		/** Perform the move */
		// Assign the item to be at that position on the board
		if( item instanceof Weapon ){

			// Weapons move to the location
			layout[newX][newY].setWeapon((Weapon)item);
			item.setPosition(newX,newY);

		}else if( item instanceof Piece ){
			Piece piece = (Piece)item;

			// Check if then next position is a room
			if( layout[newX][newY].isRoom() ){

				// Get a randomSquare that can contain the Piece
				Square randomSquare = null;
				while( randomSquare == null || !randomSquare.canContainPiece() ){
					randomSquare = layout[newX][newY].getRoom().getRandomSquare();
				}

				// Set the piece to the random Square
				layout[randomSquare.getPosition().x][randomSquare.getPosition().y].setPiece(piece);
				piece.setRoom(layout[randomSquare.getPosition().x][randomSquare.getPosition().y].getRoom());
				piece.setPosition(randomSquare.getPosition().x,randomSquare.getPosition().y);
			}
			else if( layout[newX][newY].isCorridoor() || piece.isInRoom()){

				// Moving around in the corridoors
				layout[newX][newY].setPiece((Piece)item);
				piece.setRoom(layout[newX][newY].getRoom());
				piece.setPosition(newX,newY);
			}
			else{
				// Unhandled Exception, should never get here unless the room is null
				throw new RuntimeException("Piece moved to an area that is not a room or a corridoor Room(" + layout[newX][newY].isRoom() + ")" );
			}
		}
	}

	/**
	 * Moves the given piece and weapons associated with their names to the suppleid room
	 * @param pieceName Name of the piece to move to the room
	 * @param weaponName Name of the weapon to move to the room
	 * @param roomName Name of the Room we want to move everything to.
	 */
	public void moveToRoom(String pieceName, String weaponName, String roomName){

		if( pieceName == null || weaponName == null || roomName == null ) return;

		Piece piece = pieceMap.get(pieceName);
		Weapon weapon = weaponMap.get(weaponName);
		Room room = roomMap.get(roomName);

		try {

			// Move Piece to a random position in the room
			Square randomSquare = null;
			for( randomSquare = room.getRandomSquare(); !randomSquare.canContainPiece(); randomSquare = room.getRandomSquare()){}
			movePiece(new AMove_Node(randomSquare.getPosition(), 0, 0, null), piece);

			// Move Weapon to that position
			for( randomSquare = room.getRandomSquare(); !randomSquare.canContainWeapon(); randomSquare = room.getRandomSquare()){}
			movePiece(new AMove_Node(randomSquare.getPosition(), 0, 0, null), weapon);

		} catch (InvalidMoveException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Board clone(){
		Board clone = null;
		try {
			clone = (Board)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		// Clone all the items in layout
		clone.layout = new Square[BOARD_WIDTH][BOARD_HEIGHT];
		for( int x = 0; x < BOARD_WIDTH; x++ ){
			for( int y = 0; y < BOARD_HEIGHT; y++ ){
				if( layout[x][y] != null ){
					clone.layout[x][y] = layout[x][y].clone();
				}
			}
		}


		return clone;
	}

	/**
	 * Draws the board on the graphics pane provided
	 * @param g
	 */
	public void draw(Graphics g){

		// We should never have a null, if it's null then we haven't set the board up yet.
		if( layout[0][0] == null){return;}

		// Draw background
		for( int x = 0; x < BOARD_WIDTH; x++ ){
			for( int y = 0; y < BOARD_HEIGHT; y++ ){

				// Draw Square base colour
				layout[x][y].draw(g, x*TILE_WIDTH, y*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);

				// Draw walls of rooms
				if( layout[x][y].isRoom() ){

					g.setColor(new Color(58,19,50));

					//		N
					//	E		W
					//		S

					// East Wall
					if( x-1 >= 0 && !layout[x-1][y].isRoom() &&  ( !(layout[x][y] instanceof Door) || ((Door)layout[x][y]).getDirection() == Door.VERTICAL ) ){
						g.fillRect( x*TILE_WIDTH, y*TILE_HEIGHT, WALL_THICKNESS, TILE_HEIGHT+1);
					}

					// WEST Wall
					if( x+1 < BOARD_WIDTH && !layout[x+1][y].isRoom() &&  ( !(layout[x][y] instanceof Door) || ((Door)layout[x][y]).getDirection() == Door.VERTICAL ) ){
						g.fillRect( x*TILE_WIDTH+TILE_WIDTH-WALL_THICKNESS+1, y*TILE_HEIGHT, WALL_THICKNESS, TILE_HEIGHT+1);
					}

					// North Wall
					if( y-1 >= 0 && !layout[x][y-1].isRoom() &&  ( !(layout[x][y] instanceof Door) || ((Door)layout[x][y]).getDirection() == Door.HORIZONTAL ) ){
						g.fillRect( x*TILE_WIDTH, y*TILE_HEIGHT, TILE_WIDTH+1, WALL_THICKNESS);
					}

					// South Wall
					if( y+1 < BOARD_HEIGHT && !layout[x][y+1].isRoom() &&  ( !(layout[x][y] instanceof Door) || ((Door)layout[x][y]).getDirection() == Door.HORIZONTAL ) ){
						g.fillRect( x*TILE_WIDTH, y*TILE_HEIGHT+TILE_HEIGHT-WALL_THICKNESS/2-1, TILE_WIDTH+1, WALL_THICKNESS);
					}
				}
			}
		}

		// Draw Name of room in the center of the room
		g.setColor(Color.black);
		for(Room room : roomList){

			// Do not draw name for corridors
			if( room.getName().equals(Room.CORRIDOR) ) continue;

			Rectangle bounds = room.getBounds();
			int x = (TILE_WIDTH *bounds.x) +  (TILE_WIDTH * bounds.width/2) - g.getFontMetrics().stringWidth(room.getName())/2;
			int y = (TILE_HEIGHT *bounds.y) + (TILE_HEIGHT * bounds.height/2);
			g.drawString(room.getName(), x, y);
		}

	}

	/**
	 * Sets up all the weapons and weapon mapping from weaponName to weapon object
	 */
	private void setUpWeapons(){

		// List of all weapons in the game
		weaponList.add(new Weapon("Lead Pipe",null, new ImageIcon("src/resources/lead_pipe.png")));
		weaponList.add(new Weapon("Rope",null, new ImageIcon("src/resources/rope.png")));
		weaponList.add(new Weapon("Wrench",null, new ImageIcon("src/resources/wrench.png")));
		weaponList.add(new Weapon("Revolver",null, new ImageIcon("src/resources/revolver.png")));
		weaponList.add(new Weapon("Dagger",null, new ImageIcon("src/resources/dagger.png")));
		weaponList.add(new Weapon("Candlestick",null, new ImageIcon("src/resources/candlestick.png")));

		// List of all weapons in the game
		weaponMap.put("Lead Pipe",weaponList.get(0));
		weaponMap.put("Rope",weaponList.get(1));
		weaponMap.put("Wrench",weaponList.get(2));
		weaponMap.put("Revolver",weaponList.get(3));
		weaponMap.put("Dagger",weaponList.get(4));
		weaponMap.put("Candlestick",weaponList.get(5));
	}

	/**
	 * Sets up the rooms and room mapping from room name to room object
	 */
	private void setUpRooms(){

		// Set up all the rooms
		roomList.add( new Room("Conservatory") );
		roomList.add( new Room("Kitchen") );
		roomList.add( new Room("Hall") );
		roomList.add( new Room("Dining Room") );
		roomList.add( new Room("Lounge") );
		roomList.add( new Room("Billiard Room") );
		roomList.add( new Room("Library") );
		roomList.add( new Room("Study") );
		roomList.add( new Room("Ball Room") );
		roomList.add( new Room(Room.CORRIDOR) );

		// Set up mapping for the rooms
		roomMap.put( "Conservatory", roomList.get(0) );
		roomMap.put( "Kitchen",roomList.get(1) );
		roomMap.put( "Hall",roomList.get(2) );
		roomMap.put( "Dining Room",roomList.get(3) );
		roomMap.put( "Lounge",roomList.get(4) );
		roomMap.put( "Billiard Room",roomList.get(5) );
		roomMap.put( "Library",roomList.get(6) );
		roomMap.put( "Study",roomList.get(7) );
		roomMap.put( "Ball Room",roomList.get(8) );
		roomMap.put( Room.CORRIDOR,roomList.get(9) );
	}

	/**
	 * Sets up new pieces for the game as well as the mapping
	 */
	private void setUpPieces(){

		// List of all Piece in the game
		pieceList.add(new Piece("Mrs. White",null, new ImageIcon("src/resources/piece_white.png")));
		pieceList.add(new Piece("The Reverand Green",null,new ImageIcon("src/resources/piece_green.png")));
		pieceList.add(new Piece("Mrs. Peacock",null,new ImageIcon("src/resources/piece_blue.png")));
		pieceList.add(new Piece("Colonel Mustard",null,new ImageIcon("src/resources/piece_yellow.png")));
		pieceList.add(new Piece("Miss Scarlett",null,new ImageIcon("src/resources/piece_red.png")));
		pieceList.add(new Piece("Professor Plum",null,new ImageIcon("src/resources/piece_purple.png")));

		// Mapping from PieceName to the Piece Object
		pieceMap.put("Mrs. White",pieceList.get(0));
		pieceMap.put("The Reverand Green",pieceList.get(1));
		pieceMap.put("Mrs. Peacock",pieceList.get(2));
		pieceMap.put("Colonel Mustard",pieceList.get(3));
		pieceMap.put("Miss Scarlett",pieceList.get(4));
		pieceMap.put("Professor Plum",pieceList.get(5));
	}

	/**
	 * Sets up the board assigning all squares to the appropriate rooms
	 * @param players The players of which will all be attending the game. Must be at length 6
	 */
	public void setUp(Player[] players){

		// Check players
		if( players == null || players.length < 3 ){
			throw new RuntimeErrorException(null, "Player count should range from 3 - 6");
		}

		Scanner scan = null;
		try {
			scan = new Scanner(new File("src/rooms.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}


		// Doors saved to look at later
		ArrayList<Point> doors = new ArrayList<Point>();
		LinkedHashMap<Point,Character> portals = new LinkedHashMap<Point,Character>();

		for(int y = 0; scan.hasNext(); y++ ){

			String line = scan.next();
			for(int x = 0; x < line.length(); x++ ){
				char c = line.charAt(x);

				Square square = new Square(x,y);
				square.setRoom(this.getRoom(c));

				if( c == 'D' ){ square = new Door(x,y); doors.add(new Point(x,y)); }
				if( c == 'P' ){ portals.put(new Point(x,y), line.charAt(x+1)); }


				// Record the square in the rooms list
				if( square.getRoom() != null ){
					square.getRoom().addSquare(square);
				}


				if( c == 'P' ){
					// If we are making a portal
					// push x over 1 as the square to the right of the portal is the identication of where this room should head to
					layout[x++][y] = square;
				}
				else{
					layout[x][y] = square;
				}
			}
		}

		// Assign Doors
		for(Point p : doors){

			// Assign the door a room according to it's closest neighbouring rooms
			for( int x = -1; x < 2; x++){
				for( int y = -1; y < 2; y++){

					// Check bounds
					if( p.x+x >= 0 && p.x+x < BOARD_WIDTH &&
							p.y+y >= 0 && p.y+y < BOARD_HEIGHT ){

						// Get a room that isn't a corridoor as all rooms are seperated by a corridoor at least
						if( layout[p.x+x][p.y+y].isRoom() ){

							// Found a room, assign it to the square
							layout[p.x][p.y].setRoom(layout[p.x+x][p.y+y].getRoom());

							// Record the square in the rooms list
							if( layout[p.x][p.y].getRoom() != null ){
								layout[p.x][p.y].getRoom().addSquare(layout[p.x][p.y]);
								layout[p.x][p.y].getRoom().addDoor(layout[p.x][p.y]);
							}

							break;
						}
					}
				}
			}


			// Assign which way the door is going (VERTICAL,HORIZONTAL)

			// Check if there is a room on either side of the square
			// If there is a room on either side (Vertically) then the door is opening Horizontally
			// If there is a room on either side (Horizontally) then the door is opening Vertically
			// else, guess?

			if( !layout[p.x+1][p.y].isCorridoor() && !layout[p.x-1][p.y].isCorridoor() ){

				// There's a room on the left side and right sides of the door
				((Door)layout[p.x][p.y]).setDirection(Door.VERTICAL);
			}
			else if( !layout[p.x][p.y+1].isCorridoor() && !layout[p.x][p.y-1].isCorridoor() ){

				// There's a room above and below the door
				((Door)layout[p.x][p.y]).setDirection(Door.HORIZONTAL);
			}
			else{
				// Corner doors?
				((Door)layout[p.x][p.y]).setDirection(Door.VERTICAL);
			}

		}

		// Handle portals
		for(Point point : portals.keySet()){
			Square portal = layout[point.x][point.y];

			// Assign a portal to the square directing to the room it should take us to
			portal.setPortal(new Portal(getRoom(portals.get(point))));

			Square square = null;
			if( 	 point.x-1 >= 0 &&           layout[point.x-1][point.y] != null && layout[point.x-1][point.y].isRoom() ){ square = layout[point.x-1][point.y]; }
			else if( point.x+1 < BOARD_WIDTH &&  layout[point.x+1][point.y] != null && layout[point.x+1][point.y].isRoom() ){ square = layout[point.x+1][point.y]; }
			else if( point.y-1 >= 0 &&           layout[point.x][point.y-1] != null && layout[point.x][point.y-1].isRoom() ){ square = layout[point.x][point.y-1]; }
			else if( point.y+1 < BOARD_HEIGHT && layout[point.x][point.y+1] != null && layout[point.x][point.y+1].isRoom() ){ square = layout[point.x][point.y+1]; }
			else{ throw new RuntimeException("Can not find adjacent wall to Portal " + point); }


			// Assign a room to the portal
			portal.setRoom(square.getRoom());
			square.getRoom().setPortal(portal.getPortal());

			// Deal with following character of the portal
			Square nextSquare = new Square(point.x+1,point.y);
			layout[point.x+1][point.y] = nextSquare;

			// Assign the rooms
			nextSquare.setRoom(square.getRoom());
			square.getRoom().addSquare(nextSquare);
		}

		// Assign players positions and pieces
		for( int i = 0; i < players.length; i++){

			Player player = players[i];

			// Create a new Piece object
			Piece piece = pieceMap.get(player.getPieceName());
			piece.setRoom(roomMap.get(Room.CORRIDOR));

			// Assign the piece to the player and the board
			player.setPiece(piece);
		}

		// Put the pieces on the board
		for( Piece piece : pieceList){

			// Reset the piece
			if( piece.getRoom() != null && piece.getRoom() != roomMap.get(Room.CORRIDOR)){
				try {
					getSquare(piece.getPosition()).removeItem(piece);
				} catch (InvalidMoveException e) {
					e.printStackTrace();
				}
			}

			// Put the piece in their default location
			piece.setPosition(pieceStartingPosition.get(piece.getName()).x, pieceStartingPosition.get(piece.getName()).y);

			//
			layout[piece.getPosition().x][piece.getPosition().y].setPiece(piece);
		}

		// List of all rooms that we have added weapons to
		Set<Room> randomRoomlist = new HashSet<Room>();
		randomRoomlist.add(roomMap.get(Room.CORRIDOR));

		// Put weapons into rooms
		for( Weapon weapon : weaponList ){

			// Remove weapon from room
			if( weapon.getRoom() != null ){
				weapon.getRoom().removeWeapon(weapon);
			}

			// Choose a random room to put the weapon in
			Room randomRoom = roomList.get(new Random().nextInt(roomList.size()));
			while( randomRoomlist.contains(randomRoom) ){
				randomRoom = roomList.get(new Random().nextInt(roomList.size()));
			}
			randomRoomlist.add(randomRoom);

			// Put the weapon in a random square of the room
			Square randomSquare = randomRoom.getRandomSquare();
			while( !randomSquare.canContainWeapon() ){
				randomSquare = randomRoom.getRandomSquare();
			}
			randomSquare.setWeapon(weapon);

			Room room = randomSquare.getRoom();
			room.addWeapon(weapon);
			weapon.setRoom(room);
			weapon.setPosition(randomSquare.getPosition().x, randomSquare.getPosition().y);
		}


		// Indicate that the board ahs been set up
		hasBeenSetUp = true;
	}

	/**
	 * Returns the room object where
	 * @param roomCharacter Character representing the room that's imported from rooms.txt
	 * @return Room reference related to the character
	 */
	public Room getRoom(char roomCharacter){
		if( roomCharacter == 'K' ){ return roomMap.get("Kitchen"); }
		if( roomCharacter == 'A' ){ return roomMap.get("Ball Room"); }
		if( roomCharacter == 'C' ){ return roomMap.get("Conservatory"); }
		if( roomCharacter == 'N' ){ return roomMap.get("Dining Room"); }
		if( roomCharacter == 'B' ){ return roomMap.get("Billiard Room"); }
		if( roomCharacter == 'I' ){ return roomMap.get("Library"); }
		if( roomCharacter == 'L' ){ return roomMap.get("Lounge"); }
		if( roomCharacter == 'H' ){ return roomMap.get("Hall"); }
		if( roomCharacter == 'S' ){ return roomMap.get("Study"); }
		if( roomCharacter == '-' ){ return roomMap.get(Room.CORRIDOR); }
		return null;
	}

	/**
	 * Checks if the board has been setup using the setUp command yet
	 * @return True if the board has already been setup
	 */
	public boolean isSetUp(){
		return hasBeenSetUp;
	}

	/**
	 * Retrieves the square at the given x,y coordinates of which will be divided by TILE_HEIGHT and TILE_WIDTH
	 * @param x
	 * @param y
	 * @return
	 */
	public Square getSquare(int x, int y){
		return layout[x/TILE_WIDTH][y/TILE_HEIGHT];
	}

	public ArrayList<Square> getAdjacentSquares(int x, int y, Square exception){
		ArrayList<Square> list = new ArrayList<Square>();

		if( x-1 >= 0 &&           validMove(layout[x][y], layout[x-1][y], exception) ){ list.add(layout[x-1][y] ); }
		if( x+1 < BOARD_WIDTH &&  validMove(layout[x][y], layout[x+1][y], exception) ){ list.add(layout[x+1][y] ); }
		if( y-1 >= 0 &&           validMove(layout[x][y], layout[x][y-1], exception) ){ list.add(layout[x][y-1] ); }
		if( y+1 < BOARD_HEIGHT && validMove(layout[x][y], layout[x][y+1], exception) ){ list.add(layout[x][y+1] ); }


		return list;
	}

	/**
	 * Checks if it's a valid move to move between the two squares
	 * The exception parameter is meant to contain a square that contains the room we want to move to if any.
	 * This is due to the player not being able to enter a room that they do not want.
	 * @param from Square we are moving from
	 * @param to Square we want to move to
	 * @param exception Square containing a room exception.
	 * @return True if a player can move from the square "from" to the square "to".
	 */
	public boolean validMove(Square from, Square to, Square exception){

		// If we are coming from a null room to heading to a null room
		if( from.getRoom() == null || to.getRoom() == null ) return false;

		// Going to a corridor from a room
		if( to.isCorridoor() && from.isRoom() ){

			// Need to be coming from a door
			if( !(from instanceof Door) ){
				return false;
			}

			// We should be able to use the door coming from that direction
			Door door = (Door)from;
			if( !door.canUseDoor(to.getPosition()) ){
				return false;
			}
		}
		else if( from.isCorridoor() && to.isRoom() ){

			// Going to a door from a room

			// Room we are heading to must be the one we want to move into
			if( exception != null && !exception.getRoom().equals(to.getRoom()) ){
				return false;
			}

			// Must be entering a door
			if( !(to instanceof Door) ){
				return false;
			}

			// Must be entering from the correct position
			Door door = (Door)to;
			if( !door.canUseDoor(from.getPosition()) ){
				return false;
			}
		}

		// Can move to this square
		return true;
	}

	/**
	 * Returns the square at the given coordintes
	 * @param point Position referencing the square
	 * @return Square at the position
	 */
	public Square getSquare(Point point){
		return layout[point.x][point.y];
	}

	/**
	 * Returns the width and height of the board
	 * @return Dimension object containing the width and height
	 */
	public Dimension size() {
		return new Dimension((BOARD_WIDTH)*TILE_WIDTH,(BOARD_HEIGHT+1)*TILE_HEIGHT);
	}
}
