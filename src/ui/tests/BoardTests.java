package ui.tests;

import static org.junit.Assert.*;
import game.Door;
import game.InvalidMoveException;
import game.Piece;
import game.Player;
import game.Room;

import java.awt.Point;

import org.junit.Before;
import org.junit.Test;

import ui.Board;
import ui.Square;
import control.AMove_Node;

public class BoardTests {

	private Player pMustard = new Player("Player 1", "Colonel Mustard");
	private Player pPlum = new Player("Player 2", "Professor Plum");
	private Player pWhite = new Player("Player 3", "Mrs. White");
	private Player[] players = { pMustard, pPlum, pWhite };

	@Before
	public void initialize(){
		pMustard = new Player("Player 1", "Colonel Mustard");
		pPlum = new Player("Player 2", "Professor Plum");
		pWhite = new Player("Player 3", "Mrs. White");
		players[0] = pMustard;
		players[1] = pPlum;
		players[2] = pWhite;
	}

	@Test
	public void creation(){
		Board board = new Board();
		board.setUp(players);
	}

	@Test
	public void setUp(){

		Board board = new Board();
		board.setUp(players);

		Player player = players[0];
		Piece piece = players[0].getPiece();

		try {
			// Move to a new square
			board.movePiece(new AMove_Node(new Point(1,17), 0, 0, null), piece);

		} catch (InvalidMoveException e) {
			fail();
		}

		// Check current Position
		assertTrue(piece.getPosition().equals(new Point(1,17)));

		// Reset the board up so the players are in the same rooms
		board.setUp(players);

		// Check piece has moved
		assertTrue(piece.getPosition().equals(new Point(0,17)));
	}
}












