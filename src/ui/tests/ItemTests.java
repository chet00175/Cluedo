package ui.tests;

import static org.junit.Assert.*;
import game.Piece;
import game.Room;

import java.awt.Point;

import org.junit.Test;

public class ItemTests {

	@Test
	public void isInRoomTests(){
		Piece piece = new Piece("Sir play-a-lot", new Point(0,5), null);

		// Can not enter a null room
		assertFalse( piece.isInRoom() );

		// Can not enter a room that is a corridoor
		piece.setRoom(new Room(Room.CORRIDOR));
		assertFalse( piece.isInRoom() );

		// Acceptable Room
		piece.setRoom(new Room("Wart-House"));
		assertTrue( piece.isInRoom() );
	}
}
