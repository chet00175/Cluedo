package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.ImageIcon;

public class Piece extends Item {

	//private Color color = null;
	private ImageIcon image;

	public Piece(String n, Point p, ImageIcon image) {
		name = n;
		this.position = p;
		this.image = image;
	}

	public void draw(Graphics g, int x, int y, int w, int h){
		g.drawImage(image.getImage(), x, y, w, h, null);
	}

	public ImageIcon getImage() {
		return image;
	}

	public void setImage(ImageIcon image) {
		this.image = image;
	}

	@Override
	public Piece clone() throws CloneNotSupportedException {
		return (Piece) super.clone();
	}

	/**
	 * Checks if the piece is in a room
	 * @return true if the piece is in a room, but not in a corridoor
	 */
	public boolean isInRoom() {
		return room != null && !room.getName().equals(Room.CORRIDOR);
	}

}
