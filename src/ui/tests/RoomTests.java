package ui.tests;

import static org.junit.Assert.*;
import game.Door;
import game.InvalidMoveException;
import game.Piece;
import game.Player;
import game.Room;

import java.awt.Point;

import org.junit.Test;

import control.AMove_Node;
import ui.Board;
import ui.Square;

public class RoomTests {

	private static final Player pMustard = new Player("Player 1", "Colonal Mustard");
	private static final Player pPlum = new Player("Player 2", "Professor Plum");
	private static final Player pWhite = new Player("Player 3", "Mrs. White");
	private static Player[] players = { pMustard, pPlum, pWhite };

	@Test
	public void IsRoomCorridoor() {

		Square sVoid = new Square(1, 0);
		Square sCORRIDOR = new Square(0, 0);
		Square sPANCAKE = new Square(0, 1);

		Room rCORRIDOR = new Room(Room.CORRIDOR);
		Room rPANCAKE = new Room("Pancake Room");

		sCORRIDOR.setRoom(rCORRIDOR);
		sPANCAKE.setRoom(rPANCAKE);

		// Corridor
		assertTrue(sCORRIDOR.isCorridoor());
		assertFalse(sCORRIDOR.isRoom());

		// Room
		assertTrue(sPANCAKE.isRoom());
		assertFalse(sPANCAKE.isCorridoor());

		// Null Room
		assertFalse(sVoid.isRoom());
		assertFalse(sVoid.isCorridoor());

		Square door = new Door(1,1,Door.VERTICAL);
		door.setRoom(rPANCAKE);
		assertTrue(door.isRoom());
		assertFalse(door.isCorridoor());
	}

}
