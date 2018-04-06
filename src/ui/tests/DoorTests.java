package ui.tests;

import static org.junit.Assert.*;
import game.Door;

import org.junit.Test;

public class DoorTests {

	@Test
	public void canUseDoorTests(){

		// VERTICAL
		Door verticalDoor = new Door(5,5,Door.VERTICAL);
		assertTrue(verticalDoor.canUseDoor(5,4));
		assertTrue(verticalDoor.canUseDoor(5,6));
		assertTrue(verticalDoor.canUseDoor(5,5));
		assertFalse(verticalDoor.canUseDoor(4,5));
		assertFalse(verticalDoor.canUseDoor(6,5));

		// HORIZONTAL
		Door horizontalDoor = new Door(5,5,Door.HORIZONTAL);
		assertFalse(horizontalDoor.canUseDoor(5,4));
		assertFalse(horizontalDoor.canUseDoor(5,6));
		assertTrue(horizontalDoor.canUseDoor(5,5));
		assertTrue(horizontalDoor.canUseDoor(4,5));
		assertTrue(horizontalDoor.canUseDoor(6,5));

		// NOT A DOOR
		Door dumbDoor = new Door(5,5,Door.NOT_A_DOOR);
		assertFalse(dumbDoor.canUseDoor(5,4));
		assertFalse(dumbDoor.canUseDoor(5,6));
		assertFalse(dumbDoor.canUseDoor(5,5));
		assertFalse(dumbDoor.canUseDoor(4,5));
		assertFalse(dumbDoor.canUseDoor(6,5));
	}
}
