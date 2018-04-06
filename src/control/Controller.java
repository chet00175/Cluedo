package control;

import game.InvalidMoveException;
import game.Item;
import game.Piece;
import game.Player;
import game.Room;
import game.Weapon;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import ui.Board;
import ui.BoardPanel;
import ui.Square;

/**
 * Controller is a class that deals with the basic game rules of and movement of
 * the game. Working as a finate-state machine. The Controller determines what
 * needs to be done according to which state its in. Movement on the board is
 * done step-by-step through the tick method which needs to be called via an
 * outside loop.
 *
 *
 */
public class Controller {

	// Static array of all the cards in the game
	private static final ArrayList<String> CHARACTER_CARDS = new ArrayList<String>() {
		{

			// Characters
			add("Colonel Mustard");

			add("Professor Plum");
			add("Mrs. White");
			add("The Reverend Green");
			add("Miss Scarlett");
			add("Mrs. Peacock");
		}
	};

	private static final ArrayList<String> WEAPON_CARDS = new ArrayList<String>() {
		{

			// Weapons
			add("Lead Pipe");
			add("Rope");
			add("Wrench");
			add("Revolver");
			add("Dagger");
			add("CandleStick");
		}
	};

	private static final ArrayList<String> ROOM_CARDS = new ArrayList<String>() {
		{

			// Rooms
			add("Conservatory");
			add("Kitchen");
			add("Hall");
			add("Dining Room");
			add("Lounge");
			add("Billiard Room");
			add("Library");
			add("Study");
			add("Ball Room");
		}
	};

	// Players in the game
	private Player[] players; // The players that are in the game linked to
								// their pieces
	private boolean[] eliminatedPlayers; // Boolean representing if the player
											// is eliminated orn ot

	// Status of the Controller to determine which actions it should perform
	// next
	private Status status;

	public static enum Status {
		ROLLING_DICE, // Waiting for the player to roll the dice
		SELECTING_MOVE, // Waiting for a player to select a position to move to
		ACCUSING, // Rotating around the players looking for cards that match
					// the accusation made
		SUGGESTING, // Rotating around the players looking for cards that match
					// the suggestion made
		REVEALING, // Revealing the picked card to the current player
		MOVING, // Moving a piece around the board
		WAITING, // Waiting for the player to accuse or suggest who the murder
					// is
		GAME_OVER // Game over to indicate someone has won or everyone has lost
	};

	// The move we want to perform on the next tick
	private AMove_Node currentMove;

	private Board board; // Server Board that all players should be see
	private BoardPanel panel;

	// Details about the current player
	private int currentPlayer; // Current player that should be playing
	private int diceRoll; // Current dice roll
	private int movesRemaining; // How many moves the player has until their
								// move is over
	private boolean hasSuggested; // boolean to make sure the player can't
									// suggest then accuse before finishing
									// their turn

	// The Murderer Details
	private String murdererCharacter;
	private String murdererWeapon;
	private String murdererRoom;

	// The Accusation and Suggested Details
	// Difference determined by the status of the controller
	private String suggestedCharacter;
	private String suggestedWeapon;

	private String suggestedRoom;

	// The player that is in rotation when making a suggestion.
	private int rotatingPlayer = 0;

	/**
	 * Sets up the controller new controller without a board or panel
	 */
	public Controller() {
		reset();
	}

	/**
	 * Resets the game back to it's default settings for a new game to start
	 */
	public void reset() {
		eliminatedPlayers = null; // Boolean representing if the player is
									// eliminated or not

		// Status of the Controller to determine which actions it should perform
		// next
		status = Status.ROLLING_DICE;
		currentMove = null; // The move we want to perform on the next tick
		currentPlayer = 0; // Current player that should be playing
		diceRoll = 0; // Current dice roll
		movesRemaining = 0; // How many moves the player has until their move is
							// over
		hasSuggested = false; // boolean to make sure the player can't suggest
								// then accuse before finishing their turn

		// The Murderer Details
		murdererCharacter = null;
		murdererWeapon = null;
		murdererRoom = null;

		// The Accusation and Suggested Details
		// Difference determined by the status of the controller
		suggestedCharacter = null;
		suggestedWeapon = null;
		suggestedRoom = null;
		board.setUp(players);
		panel.repaint();
	}

	/**
	 * Sets up the controller using the given board and panel
	 */
	public Controller(Board board, BoardPanel panel) {
		this.board = board;
		this.panel = panel;
	}

	/**
	 * Deals the cards to the players assigned to the controller Also sets up
	 * the murderer cards to the board
	 */
	public void dealCards() {
		if (players == null || players.length == 0) {
			throw new RuntimeException("Players needs to be set up!");
		}

		// Create a new deck so we can have
		ArrayList<String> newDeck = new ArrayList<String>();

		// Add Character Cards
		for (String card : CHARACTER_CARDS) {
			newDeck.add(card);
		}

		// Add Weapon Cards
		for (String card : WEAPON_CARDS) {
			newDeck.add(card);
		}

		// Add Room Cards
		for (String card : ROOM_CARDS) {
			newDeck.add(card);
		}

		// Take 3 cards out for the murderer
		setMurdererCharacter(CHARACTER_CARDS.get(new Random()
				.nextInt(CHARACTER_CARDS.size())));
		newDeck.remove(getMurdererCharacter());

		setMurdererWeapon(WEAPON_CARDS.get(new Random().nextInt(WEAPON_CARDS
				.size())));
		newDeck.remove(getMurdererWeapon());

		setMurdererRoom(ROOM_CARDS.get(new Random().nextInt(ROOM_CARDS.size())));
		newDeck.remove(getMurdererRoom());

		// Shuffle the deck so they are not in order
		Collections.shuffle(newDeck);

		int playerDealt = 0;

		// Deal cards to the players
		while (!newDeck.isEmpty()) {

			// Only deal to a player that's playing
			if (players[playerDealt] != null) {

				// Remove a random card from the deck and give it to the player
				String card = newDeck.remove(newDeck.size() - 1);
				players[playerDealt].addCard(card);
			}

			playerDealt++;
			if (playerDealt >= players.length)
				playerDealt = 0;
		}

	}

	/**
	 * Tick method deals with the state of the controller according to what the
	 * game should be doing next step-by-step
	 *
	 * ROLLING_DICE, // Waiting for the player to roll the dice SELECTING_MOVE,
	 * // Waiting for a player to select a position to move to ACCUSING, //
	 * Rotating around the players looking for cards that match the accusation
	 * made SUGGESTING, // Rotating around the players looking for cards that
	 * match the suggestion made PICKING_REVEAL_CARD, // The RotatingPlayer has
	 * to pick a card that was suggested by the currentPlayer REVEALING, //
	 * Revealing the picked card to the current player MOVING, // Moving a piece
	 * around the board WAITING, // Waiting for the player to accuse or suggest
	 * who the murder is GAME_OVER // Game over to indicate someone has won or
	 * everyone has lost
	 */
	public void tick() {

		if (status == Status.GAME_OVER) {
			// Do nothing, game is over
		}
		// If we need to move a piece around the board
		else if (status == Status.MOVING) {

			Piece playerPiece = players[currentPlayer].getPiece();

			try {
				// Move the piece if it's a valid move
				board.movePiece(currentMove, playerPiece);
				panel.repaint();
			} catch (InvalidMoveException e) {
				e.printStackTrace();
			}

			// Get next move in our path
			currentMove = currentMove.getNext();

			// One less move we can perform now
			movesRemaining--;

			// Check if we are now in a room or are out of moves
			if (playerPiece.isInRoom() || movesRemaining == 0) {

				// Finished all moves OR the player is currently in a room, now
				// we can make an accusation
				status = Status.WAITING;
			}
			// Check if we have another move to perform
			else if (currentMove == null) {

				// Finished all moves, wait for next turn
				status = Status.SELECTING_MOVE;
			}
		}
	}

	/**
	 * Attempt to perform a move on the board
	 *
	 * @param player
	 *            The player performing the move
	 * @param point
	 *            the position to move the player to
	 */
	public void attemptPlayerMove(AMove_Node move) throws InvalidMoveException {

		// Only tell the player to move when we are selecting the next move
		if (status != Controller.Status.SELECTING_MOVE)
			return;

		// Get current player
		Player player = players[currentPlayer];

		// Check if the move is valid
		// Throws exceptions if it's an invalid move
		checkValidMove(move, player.getPiece());

		/** Valid move */
		status = Status.MOVING;
		currentMove = move;
	}

	/**
	 * The current player makes an accusation towards who the murderer is in the
	 * room supplies and the weapon supplied This will eliminate the player from
	 * the game if they make the wrong accusation. Otherwise the game is over
	 *
	 * @param character
	 *            Person who is the murderer
	 * @param weapon
	 *            Weapon used to commit the murder
	 * @param room
	 *            Room where the murder was commited
	 */
	public void accuseMurderer(String character, String weapon, String room)
			throws Exception {

		// Check if this player has already been eliminated
		if (eliminatedPlayers[currentPlayer]) {
			throw new PlayerEliminatedException(
					"You have been eliminated and can not make an accusation!!");
		}

		// Check if the player has already made a suggestion or accusation
		if (hasSuggested) {
			throw new AlreadySuggestedException(
					"You have already made a suggestion/accusation this turn.");
		}

		// Status changed to ACCUSING in order to differentiate between
		// accusation and suggesting
		this.suggestedCharacter = character;
		this.suggestedWeapon = weapon;
		this.suggestedRoom = room;

		this.status = Status.ACCUSING;
		this.hasSuggested = true;

		// Check we accused the correct people
		if (murdererCharacter.equals(suggestedCharacter)
				&& murdererWeapon.equals(suggestedWeapon)
				&& murdererRoom.equals(suggestedRoom)) {

			// This player has won the game
			status = Status.GAME_OVER;
		} else {

			// Eliminate this player
			eliminatedPlayers[currentPlayer] = true;

			// Check to make sure there is still one player playing
			for (Boolean eliminated : eliminatedPlayers) {

				// Player is not eliminated
				if (!eliminated) {

					// Next Players turn
					return;
				}
			}

			// This murderer got away
			status = Status.GAME_OVER;
			currentPlayer = -1;
		}
	}

	/**
	 * The current player makes a suggestion towards who the murderer is in the
	 * room supplies and the weapon supplied This will rotate between the
	 * players until a player has shown a card to the suggester, otherwise turn
	 * ends once it comes back to the person that made the suggestion.
	 *
	 * @param character
	 *            Person who is the murderer
	 * @param weapon
	 *            Weapon used to commit the murder
	 * @param room
	 *            Room where the murder was commited
	 */
	public void suggestMurderer(String character, String weapon, String room)
			throws Exception {

		// Check if this player has already been eliminated
		if (eliminatedPlayers[currentPlayer]) {
			throw new PlayerEliminatedException(
					"You have been and can not make anymore suggestions!!");
		}

		// Check if the player has already made a suggestion or accusation
		if (hasSuggested) {
			throw new AlreadySuggestedException(
					"You have already made a suggestion/accusation this turn.");
		}

		// Record who made the suggestion
		rotatingPlayer = currentPlayer;
		skipRotatingPlayer();

		// Record their suggestion
		this.suggestedCharacter = character;
		this.suggestedWeapon = weapon;
		this.suggestedRoom = room;

		// Move the character into the room
		board.moveToRoom(character, weapon, room);

		// Set the status of the controller to be suggesting.
		this.status = Status.SUGGESTING;
		this.hasSuggested = true;

		// Redraw
		panel.repaint();
	}

	/**
	 * Moves the given weapon to the position
	 *
	 * @param weapon
	 *            The weapon that needs to be moved
	 * @param room
	 *            The room to move the weapon to
	 */
	public void moveWeaponToRoom(Weapon weapon, Room room)
			throws InvalidMoveException {

		// Put the weapon in a random square of the room
		Square randomSquare = room.getRandomSquare();

		while (!randomSquare.canContainWeapon()) {

			// Get another new square
			randomSquare = room.getRandomSquare();
		}

		// Reset the old room so the old room does not hold the weapon
		weapon.getRoom().removeWeapon(weapon);

		// Reset the old square
		board.getSquare(weapon.getPosition()).setWeapon(null);

		// Square can hold the weapon
		randomSquare.setWeapon(weapon);

		// Room holds weapon
		room.addWeapon(weapon);

		// Weapon is now in the room
		weapon.setRoom(room);
	}

	/**
	 * Finishes the turn for the current player, moving to the next player
	 */
	public void finishTurn() throws TurnNotFinishedException {
		if (status == Status.ROLLING_DICE) {
			throw new TurnNotFinishedException(
					"You must roll the dice before you can end your turn.");
		} else if (status == Status.MOVING || status == Status.SELECTING_MOVE) {
			throw new TurnNotFinishedException(
					"You must move before you can end your turn.");
		}

		// Next player
		currentPlayer++;

		// Stay within bounds
		if (currentPlayer >= players.length) {
			currentPlayer = 0;
		}

		// We want to roll the dice next
		status = Status.ROLLING_DICE;
		hasSuggested = false;
	}

	public void checkValidMove(AMove_Node move, Item item)
			throws InvalidMoveException {

		// No Null moves
		if (move == null) {
			throw new InvalidMoveException("Invalid move " + move);
		}

		// Point that will make sure every move from the initial position is
		// valid
		Point ghostPoint = new Point(move.getPoint().x, move.getPoint().y);

		// Check all positive moves in the object for invalid moves
		for (AMove_Node next = move; next != null; next = next.getNext()) {

			// Move ghost point by the moveBy coords
			ghostPoint.setLocation(next.getPoint().x, next.getPoint().y);

			// Position on the board according to the move
			Square square = board.getSquare(ghostPoint.x, ghostPoint.y);

			// Can the Item we want to move, move to this square?
			if (item instanceof Piece && !square.canContainPiece()) {
				throw new InvalidMoveException("Can not move Piece "
						+ item.toString() + " to " + ghostPoint);
			} else if (item instanceof Piece && !square.canContainWeapon()) {
				throw new InvalidMoveException("Can not move Weapon "
						+ item.toString() + " to " + ghostPoint);
			}

			// If we are coming from a room, make sure it's from a portal
			if (board.getSquare(item.getPosition().x, item.getPosition().y)
					.isRoom()
					&& board.getSquare(move.getPoint().x, move.getPoint().x)
							.isRoom()) {

				// Moving from a room to a room
				if (!board
						.getSquare(item.getPosition().x, item.getPosition().y)
						.getRoom()
						.equals(board
								.getSquare(move.getPoint().x, move.getPoint().x)
								.getPortal().getTargetRoom())) {

					throw new InvalidMoveException(
							"Can not move to this room without a portal: "
									+ item.toString() + " to " + ghostPoint);
				}
			}

		}
	}

	/**
	 * Returns a clone of the public board that this master is currently working
	 * with.
	 *
	 * @return Board object
	 */
	public Board getBoard() {
		return board;
	}

	/**
	 * Sets the board that the controller will be associating with
	 *
	 * @param board
	 *            that contains all objects accociated with the board itself.
	 */
	public void setBoard(Board board) {
		this.board = board;
	}

	/**
	 * Returns which player should be playing according to the server
	 *
	 * @return Player that is playing
	 */
	public Player getCurrentPlayer() {

		// Check if the game is over and the murderer won
		if (currentPlayer == -1) {
			return null;
		}

		// Return the player that is currently playing
		return players[currentPlayer];
	}

	/**
	 * Returns the player in rotation where someone has made a suggestion
	 *
	 * @return Player that is in current rotation during a suggestion.
	 */
	public Player getRotatingPlayer() {
		return players[rotatingPlayer];
	}

	/**
	 * Returns what number the dice has landed on
	 *
	 * @return How many moves the player can perform
	 */
	public int getDiceRoll() {
		return diceRoll;
	}

	/**
	 * Returns what number the dice has landed on
	 *
	 * @return How many moves the player can perform
	 */
	public int getMovesRemaining() {
		return movesRemaining;
	}

	/**
	 * Assign how many moves the current player can perform Resets
	 * movesRemaining to the dice roll as well
	 *
	 * @param diceRoll
	 *            Limit on moves the player can perform
	 */
	public void setDiceRoll(int diceRoll) {
		this.diceRoll = diceRoll;
		this.movesRemaining = diceRoll;
	}

	/**
	 * Retrieves the array of players currently playing in the game
	 *
	 * @return Array of Players in the game all labeled with their character
	 *         names
	 */
	public Player[] getPlayers() {
		return players;
	}

	/**
	 * Sets the players for the controller
	 *
	 * @param players
	 *            Array of players that are currently playing. Contains no
	 *            nulls.
	 */
	public void setPlayers(Player[] players) {
		this.players = players;
		this.eliminatedPlayers = new boolean[players.length];
	}

	/**
	 * Returns the board that this controller is directing
	 *
	 * @return BoardPanel that is drawing the board related to this controller
	 */
	public BoardPanel getPanel() {
		return panel;
	}

	/**
	 * Assigns the panel that this Controller will be telling to draw
	 *
	 * @param panel
	 *            Panel that the controller will be accociating with
	 */
	public void setPanel(BoardPanel panel) {
		this.panel = panel;
	}

	/**
	 * Returns the Status of the Controller
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the new status of the controller to behave differently
	 *
	 * @param status
	 *            Static Status of the controller to behave now
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	public String getMurdererCharacter() {
		return murdererCharacter;
	}

	public void setMurdererCharacter(String card) {
		this.murdererCharacter = card;
	}

	public String getMurdererWeapon() {
		return murdererWeapon;
	}

	public void setMurdererWeapon(String murdererWeapon) {
		this.murdererWeapon = murdererWeapon;
	}

	public String getMurdererRoom() {
		return murdererRoom;
	}

	public void setMurdererRoom(String murdererRoom) {
		this.murdererRoom = murdererRoom;
	}

	/**
	 * Assigns the card to be revealed
	 *
	 * @param card
	 *            Any type of card to reveal
	 */
	public void setRevealingCard(String card) {

		// We should check to make we are revealing an acceptable card
		if (!card.equals(suggestedCharacter) && !card.equals(suggestedWeapon)
				&& !card.equals(suggestedRoom)) {
			throw new RuntimeException(
					"Attempting to reveal a card that was not suggested!\n"
							+ card + "\n" + suggestedCharacter + "\n"
							+ suggestedWeapon + "\n" + suggestedRoom);
		}

		// Reveal to the player that made a suggestion
		status = Status.REVEALING;
	}

	/**
	 * Rotates to the next player to make a suggestion
	 */
	public void skipRotatingPlayer() {

		rotatingPlayer++;
		if (rotatingPlayer >= players.length) {
			rotatingPlayer = 0;
		}
	}
}
