package game;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class Player implements Cloneable {

	private String name;
	private String pieceName;
	private Piece piece = null;
	private List<String> cards = new ArrayList<String>();

	public Player(String name, String pieceName) {
		this.name = name;
		this.pieceName = pieceName;
	}

	public Player(String name, Piece piece) {
		this.name = name;
		this.piece = piece;
	}

	/**
	 * Returns the name of the player
	 * @return String relating to the playters name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the piece that the player is using to move aroudn the board
	 * @return Piece associating with the Player
	 */
	public Piece getPiece() {
		return piece;
	}

	/**
	 * Assigns a piece to the player for them to move aroudn the board
	 * @param piece new Piece for the player to use
	 */
	public void setPiece(Piece piece) {
		this.piece = piece;
	}

	/**
	 * Returns a list of cards that the player has in their deck
	 * @return List of card in the players hands
	 */
	public List<String> getCards() {
		return cards;
	}

	/**
	 * Checks if this player has the card provided
	 * @param card Card to check for in their deck
	 * @return True if the given card is in their deck
	 */
	public boolean hasCard(String cardName){
		return cards.contains(cardName);
	}

	/**
	 * Adds a card to the players deck
	 * @param card Card to add ot the players deck
	 */
	public void addCard(String card) {
		cards.add(card);
	}

	/**
	 * Prints the name of the Player
	 */
	@Override
	public String toString(){
		return name;
	}

	@Override
	public boolean equals(Object object){
		if( object == null )
			return false;
		if( object instanceof Player )
			return false;
		Player other = (Player)object;

		// Compare names of ther players
		return name.equals(other.getName());
	}


	/**
	 * Returns the name of the piece that the player is using to move around the board.
	 * @return name of the piece the player is using
	 */
	public String getPieceName() {
		return pieceName;
	}

	/**
	 * Assigns the name of the piece that the player is using to move around the board.
	 * @param pieceName Name of the piece the player is moving aroudn the board with.
	 */
	public void setPieceName(String pieceName) {
		this.pieceName = pieceName;
	}
}
