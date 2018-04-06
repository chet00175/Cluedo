package game;

import java.awt.Graphics;
import java.awt.Point;

public abstract class Item implements Cloneable {

	protected String name;
	protected Point position;
	protected Room room;

	public Point getPosition() {
		return position;
	}

	public void setPosition(int x, int y) {
		this.position = new Point(x,y);
	}

	public String getName() {
		return name;
	}

	public Room getRoom(){
		return room;
	}

	public void setRoom(Room room){
		this.room = room;
	}

	public void setName(String n) {
		name = n;
	}

	public abstract void draw(Graphics g, int x, int y, int w, int h);

	@Override
	public Item clone() throws CloneNotSupportedException {
		return (Item)super.clone();
	}

	@Override
	public String toString(){
		return name;
	}
}
