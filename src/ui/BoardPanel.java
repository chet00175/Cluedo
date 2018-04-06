package ui;

import game.InvalidMoveException;
import game.Portal;
import game.Room;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import control.AMove_Node;
import control.AStar;
import control.Controller;

public class BoardPanel extends JPanel implements MouseListener, MouseMotionListener{

	private static final int COORDINATEBAR_THICKNESS = 25;
	private static final long serialVersionUID = -3863986693958886842L;
	private Board board;
	private Point movePoint = new Point(0,0);
	private AMove_Node path = null;

	private Controller control = null;


	/**\
	 * Creates a basic BoardPanel with the listeners. But does not contain a board or controller.
	 */
	public BoardPanel(){

		// Listeners
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	/**
	 * Creates a new BoardPanel that will be associating with the given board and panel
	 * @param board Board we want to draw
	 * @param control Controller that we want to update once the mouse has done something
	 */
	public BoardPanel(Board board, Controller control){
		this();

		this.board = board;
		this.control = control;

		// Assign the size of the background according to the panel
		this.setBackground(Color.black);
		this.setSize(new Dimension(board.size().width + COORDINATEBAR_THICKNESS, board.size().height + COORDINATEBAR_THICKNESS));
	}

	@Override
	public void paintComponent(Graphics g){
		if( board == null || control == null) return;
		super.paintComponents(g);

		// Draw Background
		g.setColor(getBackground());
		g.fillRect(0, 0, getSize().width, getSize().height);

		// Draw the Board bars that indicate the coodinates of the squares
		drawCoordinateBars(g);

		// Draw the board with an offset away from the bars
		g.translate(COORDINATEBAR_THICKNESS, 0);
		board.draw(g);

		// Draw the path from the characters Piece to the mouse
		if( control.getStatus() == Controller.Status.SELECTING_MOVE ){

			Square highlightedSquare = board.getSquare(movePoint);

			// Draw path if the mouse is on a square
			if( highlightedSquare.isPortal() && control.getCurrentPlayer().getPiece().getRoom().equals(highlightedSquare.getRoom()) ){

				// Draw path from the portal to the room it's targetting
				drawLineFromPortal(g, highlightedSquare);
			}
			else{

				// Draw path from the character to the mouse
				drawPath(g);
			}
		}
	}

	/**
	 * Draws the coordinate bars on the left column and bottom row to indicate which squares are which
	 * @param g Graphics object to draw on
	 */
	public void drawCoordinateBars(Graphics g){

		g.setColor(Color.white);

		// Left Column
		for( int y = 0; y < Board.BOARD_HEIGHT; y++ ){

			// Center the coordinates to the panel
			int x = (COORDINATEBAR_THICKNESS/2) - (g.getFontMetrics().stringWidth(String.valueOf(y))/2);
			g.drawString(String.valueOf(y), x, (y+1)*Board.TILE_HEIGHT-Board.TILE_HEIGHT/4);
		}

		// Bottom Row
		for( int coordinate = 0; coordinate < Board.BOARD_WIDTH; coordinate++ ){

			// Get character of coordinate
			String c = String.valueOf((char)('A' + coordinate));

			// Center the coordinates to the panel
			int x = g.getFontMetrics().stringWidth(String.valueOf(coordinate));
			g.drawString(c, COORDINATEBAR_THICKNESS + 8 + (coordinate)*Board.TILE_WIDTH, board.size().height-Board.TILE_HEIGHT/2);
		}
	}

	/**
	 * Draws a lien from the portal to the square it's targeting
	 * @param g Graphics object to draw the details on
	 * @param highlightedSquare The square that contains the portal
	 */
	public void drawLineFromPortal(Graphics g, Square highlightedSquare){
		if( highlightedSquare == null ) return;

		Portal portal = highlightedSquare.getPortal();
		Room targetRoom = portal.getTargetRoom();
		if( targetRoom == null ){
			throw new RuntimeException(highlightedSquare + " does not contain a portal!");
		}

		// Get points from the middle of the portal to the middle of the targetRoom
		Point startPoint = new Point(highlightedSquare.getPosition().x, highlightedSquare.getPosition().y);
		Point endPoint = new Point((targetRoom.getBounds().x * Board.TILE_WIDTH) + (targetRoom.getBounds().width/2) * Board.TILE_WIDTH,
								   (targetRoom.getBounds().y * Board.TILE_HEIGHT) + (targetRoom.getBounds().height/2) * Board.TILE_HEIGHT);

		// Draw line form the portal to the center of the room
		g.setColor(Color.black);
		g.drawLine(startPoint.x * Board.TILE_WIDTH + Board.TILE_WIDTH/2, startPoint.y * Board.TILE_HEIGHT  + Board.TILE_HEIGHT/2,
				   endPoint.x, endPoint.y);

		// Create a path so we can travel to the room
		path = new AMove_Node(board.getSquare(movePoint).getPortal().getTargetRoom().getRandomSquare().getPosition(), 0, 0, null);
	}


	/**
	 * Draws the shortest path from the characters Piece to the location on the mouse
	 * @param g Graphics object to draw the path on
	 */
	public void drawPath(Graphics g ){
		if( movePoint == null || board == null || !board.isSetUp() ) return;
		g.setColor(new Color(255,255,255,128));

		// Positions for the path
		Square startPosition = board.getSquare(control.getCurrentPlayer().getPiece().getPosition());
		Square endPosition = board.getSquare(movePoint);

		// Can not move around in the same room
		if( startPosition.isRoom() && endPosition.isRoom() && startPosition.getRoom().equals(endPosition.getRoom()) ){
			return;
		}

		// If we are in a room, find the closest door to the character
		if( startPosition.isRoom() ){

			// Find closest door to the mouse
			Square closestDoor = startPosition;
			for(Square door : startPosition.getRoom().getDoors() ){
				if( closestDoor == null ||
						door.getPosition().distance(movePoint) < closestDoor.getPosition().distance(movePoint)){
					closestDoor = door;
				}
			}

			// Start position will now be the closest Door to the character
			startPosition = closestDoor;
		}

		// If we are hovering over a room, only display the door
		if( endPosition.isRoom() ){

			// Find closest door
			Square closestDoor = null;
			for(Square door : endPosition.getRoom().getDoors() ){
				if( closestDoor == null ||
						door.getPosition().distance(startPosition.getPosition()) < closestDoor.getPosition().distance(startPosition.getPosition())){
					closestDoor = door;
				}
			}

			// End position will now be the closest Door to the mouse
			endPosition = closestDoor;
		}



		// Get the path from the mouse to the desired location
		path = AStar.getPath(startPosition.getPosition(), endPosition.getPosition(), control.getCurrentPlayer().getPiece(), board);

		// Draw the path if we have a path
		if( path != null ){

			// Transparent White for the path
			g.setColor(new Color(255,255,255,128));

			// Draw path from the character to the mouse
			AMove_Node lastNode = path;
			int movesRequired = 0;
			while( lastNode != null ){

				// Draw Each tile but not the last
				g.fillRect(lastNode.getPoint().x*Board.TILE_WIDTH, lastNode.getPoint().y*Board.TILE_HEIGHT, Board.TILE_WIDTH, Board.TILE_HEIGHT);

				// Get next node and increase moves required
				lastNode = lastNode.getNext();
				movesRequired++;

				// Can't access this square via the path
				if( movesRequired >= control.getMovesRemaining() ){
					// Set Color to red
					g.setColor(new Color(255,0,0,128));
				}
			}
		}

		// Draw the coordinates of the mouse
		g.setColor(Color.black);
		((JFrame) SwingUtilities.getWindowAncestor(this)).setTitle("Coordinates: " + getCoordinateString());
	}


	@Override
	public void mousePressed(MouseEvent e) {


		try {

			// Move the character to the position according to the path
			if( control.getMovesRemaining() > 0 ){

				// Move to the path if we aren't clicking on a portal
				control.attemptPlayerMove(path);
			}
		} catch (InvalidMoveException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if( board == null || !board.isSetUp() ) return;

		// Get position according to the board
		int x = (e.getX()-COORDINATEBAR_THICKNESS)/Board.TILE_WIDTH;
		int y = (e.getY())/Board.TILE_HEIGHT;

		// Stay on the bounds
		if( x >= Board.BOARD_WIDTH || y >= Board.BOARD_HEIGHT ){
			movePoint = null;
			return;
		}

		// If we don't have a move point, then assign one
		if( movePoint == null ){
			movePoint = new Point(x,y);
			repaint();
		}
		// We already have a point, make sure they aren't the same
		else if( movePoint.x != x || movePoint.y != y ){

			// Save point if it's different to the currently saved
			movePoint.setLocation(x,y);
			repaint();
		}
	}

	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mouseDragged(MouseEvent e) {}

	/**
	 * Retrieves the point that the mouse is currently on representing the board
	 * (2, 1) represents the square (2, 1)
	 * @return Point of which the mouse it on
	 */
	public Point getMousePoint(){
		return (Point) movePoint.clone();
	}


	/**
	 * @return the board
	 */
	public Board getBoard() {
		return board;
	}

	/**
	 * @param board the board to set
	 */
	public void setBoard(Board board) {
		this.board = board;

		// Assign the size of the background according to the panel
		this.setBackground(Color.black);
		this.setSize(board.size());
	}

	/**
	 * @return the control
	 */
	public Controller getController() {
		return control;
	}

	/**
	 * @param control the control to set
	 */
	public void setController(Controller control) {
		this.control = control;
	}

	public String getCoordinateString(){
		String x = String.valueOf((char)('A'+movePoint.x));
		String y = String.valueOf(movePoint.y);
		return "(" + x + "," + y  + " )";
	}
}
