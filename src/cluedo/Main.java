package cluedo;

import ui.BoardFrame;

public class Main {
	/**
	 * Tests the walls to see if they visually look correct
	 * @param args
	 */
	public static void main(String[] args){

		BoardFrame frame = new BoardFrame();

		while( true ){

			frame.getController().tick();

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}