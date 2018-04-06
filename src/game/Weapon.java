package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import javax.swing.ImageIcon;

import ui.Square;

public class Weapon extends Item {

	private Square square;
	private ImageIcon image;

	public Weapon(String name, Room room, ImageIcon image) {
		this.name = name;
		this.room = room;
		this.image = image;

		if( room != null ){
			room.addWeapon(this);
		}
	}

	public ImageIcon getImage() {
		return image;
	}

	public void setImage(ImageIcon image) {
		this.image = image;
	}

	public void draw(Graphics g, int x, int y, int w, int h){
		g.drawImage(image.getImage(), x, y, w, h, null);
	}

	@Override
	public Weapon clone() throws CloneNotSupportedException {
		return (Weapon) super.clone();
	}
}
