package game;

public class Portal implements Cloneable {

	private Room targetRoom;

	public Portal(Room r) {
		targetRoom = r;
	}

	public Room getTargetRoom(){
		return targetRoom;
	}

}
