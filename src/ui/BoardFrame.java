package ui;

import game.Player;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.JobAttributes;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import control.AlreadySuggestedException;
import control.Controller;
import control.TurnNotFinishedException;
import control.Controller.Status;
import control.PlayerEliminatedException;

public class BoardFrame extends JFrame implements WindowListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Controller controller;
	private List<Player> players = new ArrayList<Player>();

	// These fields are used for entering the player's input.
	JTextField name1;
	JTextField name2;
	JTextField name3;
	JTextField name4;
	JTextField name5;
	JTextField name6;
	JComboBox list1;
	JComboBox list2;
	JComboBox list3;
	JComboBox list4;
	JComboBox list5;
	JComboBox list6;

	// These fields are used in the control panel to help the user play the
	// game.
	JMenuBar menuBar;
	JMenu file;
	JMenu options;
	JMenuItem menuItem;
	JLabel playerName;
	JButton rollDice;
	JTextField numberDice;
	JButton suggest;
	JButton accuse;
	JButton finishTurn;
	JComboBox cardList;

	public BoardFrame() {
		addWindowListener(this);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		showDialog(); // This displays the input dialog.
	}

	/**
	 * This sets up the code for the board and boardpanel and passes the players
	 * to the board.
	 */
	private void setupBoard() {
		// Set the panel to the controller and deal the cards to the players.
		BoardPanel panel = new BoardPanel(controller.getBoard(), controller);
		controller.setPanel(panel);
		controller.dealCards();

		super.setLayout(new BorderLayout());

		// Set up the menu bar.
		menuBar = new JMenuBar();
		file = new JMenu("File");
		options = new JMenu("Options");

		menuItem = new JMenuItem(new AbstractAction("About") {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(BoardFrame.this,
						"Made by Venkata and Jimmy (August, 2014)");
			}
		});
		file.add(menuItem);
		menuItem = new JMenuItem(new AbstractAction("Restart") {

			// This restarts the game with the same players.
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.reset();
				rollDice.setEnabled(true);
			}
		});
		file.add(menuItem);
		menuItem = new JMenuItem(new AbstractAction("Quit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		file.add(menuItem);

		menuItem = new JMenuItem(new AbstractAction("Accuse") {

			@Override
			public void actionPerformed(ActionEvent e) {
				accuse();
			}
		});
		options.add(menuItem);
		menuItem = new JMenuItem(new AbstractAction("Suggest") {

			@Override
			public void actionPerformed(ActionEvent e) {
				suggest();
			}
		});
		options.add(menuItem);

		menuBar.add(file);
		menuBar.add(options);

		super.setJMenuBar(menuBar);

		// The panel playerInfo represents information about the player like his
		// name and piece.
		JPanel playerInfo = new JPanel();
		playerInfo.setLayout(new FlowLayout());

		playerName = new JLabel();
		playerName.setText("Current Player:   "
				+ controller.getCurrentPlayer().getName());
		playerName.setIcon(controller.getCurrentPlayer().getPiece().getImage());

		playerInfo.add(playerName);

		// This sets up the control panels which have buttons and other
		// components that the player can
		// use to play the game.
		JPanel control1 = new JPanel();
		control1.setLayout(new FlowLayout());
		rollDice = new JButton("Roll Dice");
		numberDice = new JTextField(2);
		numberDice.setEditable(false);
		suggest = new JButton("Suggest");
		accuse = new JButton("Accuse");

		control1.add(rollDice);
		control1.add(numberDice);
		control1.add(suggest);
		control1.add(accuse);
		rollDice.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int roll = new Random().nextInt(12) + 1;
				numberDice.setText(Integer.toString(roll));
				controller.setDiceRoll(roll);
				rollDice.setEnabled(false); // A player cannot roll the dice
											// twice in his turn.
				controller.setStatus(Status.SELECTING_MOVE);
			}
		});

		// ActionListener for the suggest button.

		suggest.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				suggest();
			}
		});

		// ActionListener for the accuse button.

		accuse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				accuse();
			}
		});

		JPanel control2 = new JPanel();
		control2.setLayout(new FlowLayout());
		finishTurn = new JButton("Finish Turn");

		cardList = new JComboBox(playerCards());
		cardList.addItem("Player's Cards");
		cardList.setSelectedIndex(playerCards().length);

		// The actionlistener for the cardList makes sure that the player's
		// cards are always hidden from view after he is finished looking at
		// them.
		cardList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cardList.setSelectedIndex(playerCards().length);
			}
		});

		control2.add(cardList);
		control2.add(finishTurn);
		finishTurn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					controller.finishTurn();

					// Setting up things in the GUI for the next player's turn.
					rollDice.setEnabled(true);
					numberDice.setText("");
					playerName.setText("Current Player: "
							+ controller.getCurrentPlayer().getName());
					playerName.setIcon(controller.getCurrentPlayer().getPiece()
							.getImage());
					String[] playerCards = new String[playerCards().length + 1];
					for (int i = 0; i < playerCards().length; i++)
						playerCards[i] = playerCards()[i];
					playerCards[playerCards.length - 1] = "Player's Cards";
					DefaultComboBoxModel model = new DefaultComboBoxModel(
							playerCards);
					cardList.setModel(model);
					cardList.setSelectedIndex(playerCards.length - 1);
				} catch (TurnNotFinishedException ex) {
					JOptionPane
							.showMessageDialog(BoardFrame.this,
									"You must roll the dice and move before you can finish your turn!");
				}
			}
		});

		// Add all the control panels to a single panel and setting the layout.
		JPanel controls = new JPanel();
		controls.setLayout(new BorderLayout());
		controls.add(playerInfo, BorderLayout.NORTH);
		controls.add(control1, BorderLayout.CENTER);
		controls.add(control2, BorderLayout.SOUTH);

		// Add the board panel and the controls to the frame.
		super.add(panel, BorderLayout.CENTER);
		super.add(controls, BorderLayout.SOUTH);

		super.setSize(655, 850);
		super.setLocationRelativeTo(null);
		super.setResizable(false);
		super.setVisible(true);
	}

	/**
	 * This method displays the dialog where the players have to enter their
	 * names and their desired pieces. The game requires at least 3 players and
	 * a maximum or 6. After the input is entered, the game is started.
	 */
	private void showDialog() {
		String[] pieces = { "None", "Miss Scarlett", "Colonel Mustard",
				"Mrs. White", "The Reverand Green", "Mrs. Peacock",
				"Professor Plum" };
		final JDialog d = new JDialog(this, "Player Details", true);
		d.setSize(400, 350);

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(1, 1));

		JLabel playersLabel = new JLabel("Players");
		playersLabel.setFont(playersLabel.getFont().deriveFont(23.0f));
		JLabel tokensLabel = new JLabel("Tokens");
		tokensLabel.setFont(tokensLabel.getFont().deriveFont(23.0f));

		inputPanel.add(playersLabel);
		inputPanel.add(tokensLabel);
		d.getContentPane().add(inputPanel, BorderLayout.NORTH);

		// This code sets up the panel which has the components which can be
		// used to input the player's details.
		JPanel details = new JPanel();
		details.setLayout(new GridLayout(6, 1));
		name1 = new JTextField();
		name2 = new JTextField();
		name3 = new JTextField();
		name4 = new JTextField();
		name5 = new JTextField();
		name6 = new JTextField();
		list1 = new JComboBox(pieces);
		list1.setSelectedIndex(1);
		list2 = new JComboBox(pieces);
		list2.setSelectedIndex(2);
		list3 = new JComboBox(pieces);
		list3.setSelectedIndex(3);
		list4 = new JComboBox(pieces);
		list5 = new JComboBox(pieces);
		list6 = new JComboBox(pieces);
		details.add(name1);
		details.add(list1);
		details.add(name2);
		details.add(list2);
		details.add(name3);
		details.add(list3);
		details.add(name4);
		details.add(list4);
		details.add(name5);
		details.add(list5);
		details.add(name6);
		details.add(list6);
		d.getContentPane().add(details, BorderLayout.CENTER);

		JButton b = new JButton("Start");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {

				// First check for wrong user input.
				boolean isWrongInput = false; // flag to check if user entered
												// wrong input.
				if ((!list1.getSelectedItem().equals("None") && name1.getText()
						.equals(""))
						|| (!list2.getSelectedItem().equals("None") && name2
								.getText().equals(""))
						|| (!list3.getSelectedItem().equals("None") && name3
								.getText().equals(""))
						|| (!list4.getSelectedItem().equals("None") && name4
								.getText().equals(""))
						|| (!list5.getSelectedItem().equals("None") && name5
								.getText().equals(""))
						|| (!list6.getSelectedItem().equals("None") && name6
								.getText().equals(""))) {
					JOptionPane.showMessageDialog(d,
							"Every player must have a name!");
					isWrongInput = true;
				} else if ((list1.getSelectedItem().equals("None") && !name1
						.getText().equals(""))
						|| (list2.getSelectedItem().equals("None") && !name2
								.getText().equals(""))
						|| (list3.getSelectedItem().equals("None") && !name3
								.getText().equals(""))
						|| (list4.getSelectedItem().equals("None") && !name4
								.getText().equals(""))
						|| (list5.getSelectedItem().equals("None") && !name5
								.getText().equals(""))
						|| (list6.getSelectedItem().equals("None") && !name6
								.getText().equals(""))) {
					JOptionPane.showMessageDialog(d,
							"Every player must have a piece!");
					isWrongInput = true;
				} else if (name1.getText().equals(name2.getText())
						|| name1.getText().equals(name3.getText())
						|| name1.getText().equals(name4.getText())
						|| name1.getText().equals(name5.getText())
						|| name1.getText().equals(name6.getText())) {
					if (!name1.getText().equals("")) {
						JOptionPane.showMessageDialog(d,
								"Every player must have a unique name!");
						isWrongInput = true;
					}
				} else if (name2.getText().equals(name3.getText())
						|| name2.getText().equals(name4.getText())
						|| name2.getText().equals(name5.getText())
						|| name2.getText().equals(name6.getText())) {
					if (!name2.getText().equals("")) {
						JOptionPane.showMessageDialog(d,
								"Every player must have a unique name!");
						isWrongInput = true;
					}
				} else if (name3.getText().equals(name4.getText())
						|| name3.getText().equals(name5.getText())
						|| name3.getText().equals(name6.getText())) {
					if (!name3.getText().equals("")) {
						JOptionPane.showMessageDialog(d,
								"Every player must have a unique name!");
						isWrongInput = true;
					}
				} else if (name4.getText().equals(name5.getText())
						|| name4.getText().equals(name6.getText())) {
					if (!name4.getText().equals("")) {
						JOptionPane.showMessageDialog(d,
								"Every player must have a unique name!");
						isWrongInput = true;
					}
				} else if (name5.getText().equals(name6.getText())) {
					if (!name5.getText().equals("")) {
						JOptionPane.showMessageDialog(d,
								"Every player must have a unique name!");
						isWrongInput = true;
					}
				}
				if (list1.getSelectedItem().equals(list2.getSelectedItem())
						|| list1.getSelectedItem().equals(
								list3.getSelectedItem())
						|| list1.getSelectedItem().equals(
								list4.getSelectedItem())
						|| list1.getSelectedItem().equals(
								list5.getSelectedItem())
						|| list1.getSelectedItem().equals(
								list6.getSelectedItem())) {
					if (!list1.getSelectedItem().equals("None")) {
						JOptionPane.showMessageDialog(d,
								"Every player must have a unique piece!");
						isWrongInput = true;
					}
				} else if (list2.getSelectedItem().equals(
						list3.getSelectedItem())
						|| list2.getSelectedItem().equals(
								list4.getSelectedItem())
						|| list2.getSelectedItem().equals(
								list5.getSelectedItem())
						|| list2.getSelectedItem().equals(
								list6.getSelectedItem())) {
					if (!list2.getSelectedItem().equals("None")) {
						JOptionPane.showMessageDialog(d,
								"Every player must have a unique piece!");
						isWrongInput = true;
					}
				} else if (list3.getSelectedItem().equals(
						list4.getSelectedItem())
						|| list3.getSelectedItem().equals(
								list5.getSelectedItem())
						|| list3.getSelectedItem().equals(
								list6.getSelectedItem())) {
					if (!list3.getSelectedItem().equals("None")) {
						JOptionPane.showMessageDialog(d,
								"Every player must have a unique piece!");
						isWrongInput = true;
					}
				} else if (list4.getSelectedItem().equals(
						list5.getSelectedItem())
						|| list4.getSelectedItem().equals(
								list6.getSelectedItem())) {
					if (!list4.getSelectedItem().equals("None")) {
						JOptionPane.showMessageDialog(d,
								"Every player must have a unique piece!");
						isWrongInput = true;
					}
				} else if (list5.getSelectedItem().equals(
						list6.getSelectedItem())) {
					if (!list5.getSelectedItem().equals("None")) {
						JOptionPane.showMessageDialog(d,
								"Every player must have a unique piece!");
						isWrongInput = true;
					}
				}

				// If everything is okay with the user's input, the players'
				// profiles are confirmed and added to the controllers.
				if (!isWrongInput) {
					if (!name1.getText().equals(""))
						players.add(new Player(name1.getText(), (String) list1
								.getSelectedItem()));
					if (!name2.getText().equals(""))
						players.add(new Player(name2.getText(), (String) list2
								.getSelectedItem()));
					if (!name3.getText().equals(""))
						players.add(new Player(name3.getText(), (String) list3
								.getSelectedItem()));
					if (!name4.getText().equals(""))
						players.add(new Player(name4.getText(), (String) list4
								.getSelectedItem()));
					if (!name5.getText().equals(""))
						players.add(new Player(name5.getText(), (String) list5
								.getSelectedItem()));
					if (!name6.getText().equals(""))
						players.add(new Player(name6.getText(), (String) list6
								.getSelectedItem()));

					if (players.size() >= 3) {
						Player[] gamePlayers = new Player[players.size()];
						for (int i = 0; i < players.size(); i++)
							gamePlayers[i] = players.get(i);
						Board board = new Board();
						board.setUp(gamePlayers);
						controller = new Controller(board, null); // Creates
																	// the
						// controller
						// for the
						// game
						// passing
						// the
						// players
						// for
						// the
						// board.
						controller.setPlayers(gamePlayers);
						setupBoard();
						d.dispose();
						d.setVisible(false);
					} else {
						// If the user does not enter in 3 or more players.
						players.clear();
						JOptionPane
								.showMessageDialog(d,
										"You must have at least 3 players to play this game!");
					}
				}
			}
		});
		JPanel p = new JPanel();
		p.add(b);
		d.getContentPane().add(p, BorderLayout.SOUTH);

		d.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	/**
	 * Returns the controller for the game.
	 * 
	 * @return
	 */
	public Controller getController() {
		return controller;
	}

	/**
	 * This method returns all the current player's cards in an an array.
	 * 
	 * @return
	 */
	public String[] playerCards() {
		String[] cardNames = new String[controller.getCurrentPlayer()
				.getCards().size()];
		controller.getCurrentPlayer().getCards().toArray(cardNames);
		return cardNames;
	}

	/**
	 * This method returns all the rotating player's cards in an an array for
	 * when a suggestion is made.
	 * 
	 * @return
	 */
	public String[] rotatingPlayerCards() {
		String[] playerCards = new String[controller.getRotatingPlayer()
				.getCards().size()];
		controller.getCurrentPlayer().getCards().toArray(playerCards);
		return playerCards;
	}

	/**
	 * A player can make a suggestion about whom he/she thinks the murderer
	 * cards are based on the room they are in. A separate dialog is opened
	 * which allows the other players to reveal a card of theirs to disprove the
	 * current player's suggestion or simply skip the turn.
	 */
	public void suggest() {

		if (!controller.getCurrentPlayer().getPiece().isInRoom()) {
			// You cannot make a suggestion if you are not in a room.
			JOptionPane.showMessageDialog(BoardFrame.this,
					"You are not in a room!");
		} else {

			final JDialog d = new JDialog(BoardFrame.this, "Suggest", true);
			d.setSize(400, 400);

			JLabel selectLabel = new JLabel("Select Murderer Cards:");
			selectLabel.setFont(selectLabel.getFont().deriveFont(20.0f));
			d.getContentPane().add(selectLabel, BorderLayout.NORTH);

			// Sets up the radio buttons for selecting a character.
			JPanel cards1 = new JPanel();
			cards1.setLayout(new FlowLayout());
			cards1.add(new JLabel("Characters: "));
			JRadioButton white = new JRadioButton("Mrs. White");
			white.setActionCommand(white.getText());
			JRadioButton reverand = new JRadioButton("The Reverand Green");
			reverand.setActionCommand(reverand.getText());
			JRadioButton peacock = new JRadioButton("Mrs. Peacock");
			peacock.setActionCommand(peacock.getText());
			JRadioButton mustard = new JRadioButton("Colonel Mustard");
			mustard.setActionCommand(mustard.getText());
			JRadioButton scarlett = new JRadioButton("Miss Scarlett");
			scarlett.setActionCommand(scarlett.getText());
			JRadioButton plum = new JRadioButton("Professor Plum");
			plum.setActionCommand(plum.getText());
			cards1.add(white);
			cards1.add(reverand);
			cards1.add(peacock);
			cards1.add(mustard);
			cards1.add(scarlett);
			cards1.add(plum);
			white.setSelected(true);

			// Adds the radio buttons to a button group.
			final ButtonGroup characters = new ButtonGroup();
			characters.add(white);
			characters.add(reverand);
			characters.add(peacock);
			characters.add(mustard);
			characters.add(scarlett);
			characters.add(plum);

			// Sets up the radio buttons for selecting a character.
			JPanel cards2 = new JPanel();
			cards2.setLayout(new FlowLayout());
			cards2.add(new JLabel("Weapons: "));
			JRadioButton dagger = new JRadioButton("Dagger");
			dagger.setActionCommand(dagger.getText());
			JRadioButton piping = new JRadioButton("Lead Pipe");
			piping.setActionCommand(piping.getText());
			JRadioButton revolver = new JRadioButton("Revolver");
			revolver.setActionCommand(revolver.getText());
			JRadioButton rope = new JRadioButton("Rope");
			rope.setActionCommand(rope.getText());
			JRadioButton candleStick = new JRadioButton("Candlestick");
			candleStick.setActionCommand(candleStick.getText());
			JRadioButton wrench = new JRadioButton("Wrench");
			wrench.setActionCommand(wrench.getText());
			cards2.add(dagger);
			cards2.add(piping);
			cards2.add(revolver);
			cards2.add(rope);
			cards2.add(candleStick);
			cards2.add(wrench);
			dagger.setSelected(true);

			// Adds the radio buttons to a button group.
			final ButtonGroup weapons = new ButtonGroup();
			weapons.add(dagger);
			weapons.add(piping);
			weapons.add(revolver);
			weapons.add(rope);
			weapons.add(candleStick);
			weapons.add(wrench);

			// Sets up the radio buttons for the rooms.
			JPanel cards3 = new JPanel();
			cards3.setLayout(new FlowLayout());
			cards3.add(new JLabel("Rooms: "));
			JRadioButton conservatory = new JRadioButton("Conservatory");
			conservatory.setActionCommand(conservatory.getText());
			JRadioButton kitchen = new JRadioButton("Kitchen");
			kitchen.setActionCommand(kitchen.getText());
			JRadioButton hall = new JRadioButton("Hall");
			hall.setActionCommand(hall.getText());
			JRadioButton diningRoom = new JRadioButton("Dining Room");
			diningRoom.setActionCommand(diningRoom.getText());
			JRadioButton lounge = new JRadioButton("Lounge");
			lounge.setActionCommand(lounge.getText());
			JRadioButton library = new JRadioButton("Library");
			library.setActionCommand(library.getText());
			JRadioButton billiardRoom = new JRadioButton("Billiard Room");
			billiardRoom.setActionCommand(billiardRoom.getText());
			JRadioButton study = new JRadioButton("Study");
			study.setActionCommand(study.getText());
			JRadioButton ballRoom = new JRadioButton("Ball Room");
			ballRoom.setActionCommand(ballRoom.getText());
			cards3.add(conservatory);
			cards3.add(kitchen);
			cards3.add(hall);
			cards3.add(diningRoom);
			cards3.add(lounge);
			cards3.add(library);
			cards3.add(billiardRoom);
			cards3.add(study);
			cards3.add(ballRoom);

			// The room which the player is in is automatically selected when
			// making a suggestion.
			if (conservatory.getActionCommand().equals(
					controller.getCurrentPlayer().getPiece().getRoom()
							.getName())) {
				conservatory.setSelected(true);
			} else if (kitchen.getActionCommand().equals(
					controller.getCurrentPlayer().getPiece().getRoom()
							.getName())) {
				kitchen.setSelected(true);
			} else if (hall.getActionCommand().equals(
					controller.getCurrentPlayer().getPiece().getRoom()
							.getName())) {
				hall.setSelected(true);
			} else if (diningRoom.getActionCommand().equals(
					controller.getCurrentPlayer().getPiece().getRoom()
							.getName())) {
				diningRoom.setSelected(true);
			} else if (lounge.getActionCommand().equals(
					controller.getCurrentPlayer().getPiece().getRoom()
							.getName())) {
				lounge.setSelected(true);
			} else if (library.getActionCommand().equals(
					controller.getCurrentPlayer().getPiece().getRoom()
							.getName())) {
				library.setSelected(true);
			} else if (billiardRoom.getActionCommand().equals(
					controller.getCurrentPlayer().getPiece().getRoom()
							.getName())) {
				billiardRoom.setSelected(true);
			} else if (study.getActionCommand().equals(
					controller.getCurrentPlayer().getPiece().getRoom()
							.getName())) {
				study.setSelected(true);
			} else if (ballRoom.getActionCommand().equals(
					controller.getCurrentPlayer().getPiece().getRoom()
							.getName())) {
				ballRoom.setSelected(true);
			}

			conservatory.setEnabled(false);
			kitchen.setEnabled(false);
			hall.setEnabled(false);
			diningRoom.setEnabled(false);
			lounge.setEnabled(false);
			library.setEnabled(false);
			billiardRoom.setEnabled(false);
			study.setEnabled(false);
			ballRoom.setEnabled(false);

			final ButtonGroup rooms = new ButtonGroup();
			rooms.add(conservatory);
			rooms.add(kitchen);
			rooms.add(hall);
			rooms.add(diningRoom);
			rooms.add(lounge);
			rooms.add(library);
			rooms.add(billiardRoom);
			rooms.add(study);
			rooms.add(ballRoom);

			JPanel cardsPanel = new JPanel();
			cardsPanel.setLayout(new GridLayout(3, 1));
			cardsPanel.add(cards1);
			cardsPanel.add(cards2);
			cardsPanel.add(cards3);
			d.getContentPane().add(cardsPanel, BorderLayout.CENTER);

			JPanel confirmPanel = new JPanel();
			confirmPanel.setLayout(new FlowLayout());
			JButton confirm = new JButton("Confirm");
			JButton cancel = new JButton("Cancel");
			confirmPanel.add(confirm);
			confirmPanel.add(cancel);
			d.getContentPane().add(confirmPanel, BorderLayout.SOUTH);

			confirm.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					final String accusedChar = characters.getSelection()
							.getActionCommand();
					final String accusedWeapon = weapons.getSelection()
							.getActionCommand();
					final String accusedRoom = rooms.getSelection()
							.getActionCommand();
					try {
						controller.suggestMurderer(accusedChar, accusedWeapon,
								accusedRoom);
					} catch (Exception ex) {
						if (ex instanceof AlreadySuggestedException) {
							JOptionPane.showMessageDialog(BoardFrame.this,
									"You have already suggested in this turn!");
						} else if (ex instanceof PlayerEliminatedException) {
							JOptionPane.showMessageDialog(BoardFrame.this,
									"You have been eliminated from the game!");
						}
						d.dispose();
						return;
					}
					d.dispose();

					// This is the disprove dialog which opens up so
					// that
					// other players can reveal

					final JDialog disprove = new JDialog(BoardFrame.this,
							"Disprove ("
									+ controller.getRotatingPlayer().getName()
									+ ")", true);
					disprove.setSize(400, 200);

					JPanel mainPanel = new JPanel();
					mainPanel.setLayout(new BorderLayout());

					JPanel suggestedPanel = new JPanel();
					suggestedPanel.setLayout(new BorderLayout());

					final JLabel myLabel = new JLabel("Suggested Character: "
							+ accusedChar);
					suggestedPanel.add(myLabel, BorderLayout.NORTH);
					suggestedPanel.add(new JLabel("Suggested Weapon: "
							+ accusedWeapon), BorderLayout.CENTER);
					suggestedPanel.add(new JLabel("Suggested Room: "
							+ accusedRoom), BorderLayout.SOUTH);

					JPanel disprovePanel = new JPanel();
					disprovePanel.setLayout(new FlowLayout());
					final JComboBox toDisproveList = new JComboBox(
							rotatingPlayerCards());
					toDisproveList.addItem("Player's Cards");
					toDisproveList
							.setSelectedIndex(rotatingPlayerCards().length);
					disprovePanel.add(toDisproveList);

					JPanel buttons = new JPanel();
					buttons.setLayout(new FlowLayout());
					JButton reveal = new JButton("Reveal");
					JButton skip = new JButton("Skip");
					buttons.add(reveal);
					buttons.add(skip);

					reveal.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							String revealCard = (String) toDisproveList
									.getSelectedItem();

							try {
								controller.setRevealingCard(revealCard);

								JOptionPane.showMessageDialog(BoardFrame.this,
										"Revealed card: " + revealCard);

								disprove.dispose();
								// Revealed correctly

							} catch (RuntimeException error) {
								// Tried revealing the wrong card
								JOptionPane.showMessageDialog(BoardFrame.this,
										"Invalid Card!");
							}
						}
					});

					skip.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							controller.skipRotatingPlayer();
							if (controller.getRotatingPlayer() != controller
									.getCurrentPlayer()) {
								disprove.setTitle("Disprove ("
										+ controller.getRotatingPlayer()
												.getName() + ")");
								myLabel.setText("Suggested Character: "
										+ accusedChar);

								String[] playerCards = new String[playerCards().length + 1];
								for (int i = 0; i < rotatingPlayerCards().length; i++)
									playerCards[i] = rotatingPlayerCards()[i];
								playerCards[playerCards.length - 1] = "Player's Cards";
								DefaultComboBoxModel model = new DefaultComboBoxModel(
										playerCards);
								toDisproveList.setModel(model);
								toDisproveList
										.setSelectedIndex(playerCards.length - 1);
							} else {
								disprove.dispose();
							}
						}
					});

					mainPanel.add(suggestedPanel, BorderLayout.NORTH);
					mainPanel.add(disprovePanel, BorderLayout.CENTER);
					mainPanel.add(buttons, BorderLayout.SOUTH);
					disprove.add(mainPanel);

					disprove.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
					disprove.setLocationRelativeTo(null);
					disprove.setVisible(true);
				}
			});

			cancel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					d.dispose();
				}
			});

			d.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			d.setLocationRelativeTo(null);
			d.setVisible(true);

		}

	}

	/**
	 * This method opens a dialog which allows a player to make an accusation
	 * about the murderer, the room in which it happened and the weapon used for
	 * the murder. This results in either the player winning the game if he/she
	 * gets it right or being eliminated if it was wrong. The game ends if a
	 * player wins or all players are eliminated.
	 */
	public void accuse() {

		final JDialog d = new JDialog(BoardFrame.this, "Accuse", true);
		d.setSize(400, 400);

		JLabel selectLabel = new JLabel("Select Murderer Cards:");
		selectLabel.setFont(selectLabel.getFont().deriveFont(20.0f));
		d.getContentPane().add(selectLabel, BorderLayout.NORTH);

		// Sets up the radio buttons for selecting one of the characters.
		JPanel cards1 = new JPanel();
		cards1.setLayout(new FlowLayout());
		cards1.add(new JLabel("Characters: "));
		JRadioButton white = new JRadioButton("Mrs. White");
		white.setActionCommand(white.getText());
		JRadioButton reverand = new JRadioButton("The Reverand Green");
		reverand.setActionCommand(reverand.getText());
		JRadioButton peacock = new JRadioButton("Mrs. Peacock");
		peacock.setActionCommand(peacock.getText());
		JRadioButton mustard = new JRadioButton("Colonel Mustard");
		mustard.setActionCommand(mustard.getText());
		JRadioButton scarlett = new JRadioButton("Miss Scarlett");
		scarlett.setActionCommand(scarlett.getText());
		JRadioButton plum = new JRadioButton("Professor Plum");
		plum.setActionCommand(plum.getText());
		cards1.add(white);
		cards1.add(reverand);
		cards1.add(peacock);
		cards1.add(mustard);
		cards1.add(scarlett);
		cards1.add(plum);
		white.setSelected(true);

		// Adds the radio buttons to a button group.
		final ButtonGroup characters = new ButtonGroup();
		characters.add(white);
		characters.add(reverand);
		characters.add(peacock);
		characters.add(mustard);
		characters.add(scarlett);
		characters.add(plum);

		// Sets up the radio buttons for selecting one of the weapons.
		JPanel cards2 = new JPanel();
		cards2.setLayout(new FlowLayout());
		cards2.add(new JLabel("Weapons: "));
		JRadioButton dagger = new JRadioButton("Dagger");
		dagger.setActionCommand(dagger.getText());
		JRadioButton piping = new JRadioButton("Lead Pipe");
		piping.setActionCommand(piping.getText());
		JRadioButton revolver = new JRadioButton("Revolver");
		revolver.setActionCommand(revolver.getText());
		JRadioButton rope = new JRadioButton("Rope");
		rope.setActionCommand(rope.getText());
		JRadioButton candleStick = new JRadioButton("Candlestick");
		candleStick.setActionCommand(candleStick.getText());
		JRadioButton wrench = new JRadioButton("Wrench");
		wrench.setActionCommand(wrench.getText());
		cards2.add(dagger);
		cards2.add(piping);
		cards2.add(revolver);
		cards2.add(rope);
		cards2.add(candleStick);
		cards2.add(wrench);
		dagger.setSelected(true);

		// Adds the radio buttons to a button group.
		final ButtonGroup weapons = new ButtonGroup();
		weapons.add(dagger);
		weapons.add(piping);
		weapons.add(revolver);
		weapons.add(rope);
		weapons.add(candleStick);
		weapons.add(wrench);

		// Sets up the radio buttons for selecting one of the rooms.
		JPanel cards3 = new JPanel();
		cards3.setLayout(new FlowLayout());
		cards3.add(new JLabel("Rooms: "));
		JRadioButton conservatory = new JRadioButton("Conservatory");
		conservatory.setActionCommand(conservatory.getText());
		JRadioButton kitchen = new JRadioButton("Kitchen");
		kitchen.setActionCommand(kitchen.getText());
		JRadioButton hall = new JRadioButton("Hall");
		hall.setActionCommand(hall.getText());
		JRadioButton diningRoom = new JRadioButton("Dining Room");
		diningRoom.setActionCommand(diningRoom.getText());
		JRadioButton lounge = new JRadioButton("Lounge");
		lounge.setActionCommand(lounge.getText());
		JRadioButton library = new JRadioButton("Library");
		library.setActionCommand(library.getText());
		JRadioButton billiardRoom = new JRadioButton("Billiard Room");
		billiardRoom.setActionCommand(billiardRoom.getText());
		JRadioButton study = new JRadioButton("Study");
		study.setActionCommand(study.getText());
		JRadioButton ballRoom = new JRadioButton("Ball Room");
		ballRoom.setActionCommand(ballRoom.getText());
		cards3.add(conservatory);
		cards3.add(kitchen);
		cards3.add(hall);
		cards3.add(diningRoom);
		cards3.add(lounge);
		cards3.add(library);
		cards3.add(billiardRoom);
		cards3.add(study);
		cards3.add(ballRoom);
		conservatory.setSelected(true);

		// Adds the radio buttons to a button group.
		final ButtonGroup rooms = new ButtonGroup();
		rooms.add(conservatory);
		rooms.add(kitchen);
		rooms.add(hall);
		rooms.add(diningRoom);
		rooms.add(lounge);
		rooms.add(library);
		rooms.add(billiardRoom);
		rooms.add(study);
		rooms.add(ballRoom);

		JPanel cardsPanel = new JPanel();
		cardsPanel.setLayout(new GridLayout(3, 1));
		cardsPanel.add(cards1);
		cardsPanel.add(cards2);
		cardsPanel.add(cards3);
		d.getContentPane().add(cardsPanel, BorderLayout.CENTER);

		JPanel confirmPanel = new JPanel();
		confirmPanel.setLayout(new FlowLayout());
		JButton confirm = new JButton("Confirm");
		JButton cancel = new JButton("Cancel");
		confirmPanel.add(confirm);
		confirmPanel.add(cancel);
		d.getContentPane().add(confirmPanel, BorderLayout.SOUTH);

		confirm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String accusedChar = characters.getSelection()
						.getActionCommand();
				String accusedWeapon = weapons.getSelection()
						.getActionCommand();
				String accusedRoom = rooms.getSelection().getActionCommand();
				try {
					controller.accuseMurderer(accusedChar, accusedWeapon,
							accusedRoom);
				} catch (Exception ex) {
					// The player gets eliminated if he/she makes a false
					// accusation and cannot make an accusation again.
					if (ex instanceof PlayerEliminatedException) {
						JOptionPane.showMessageDialog(BoardFrame.this,
								"You have been eliminated from the game!");
					}
					d.dispose();
					return;
				}

				if (controller.getStatus() != Status.GAME_OVER) {
					JOptionPane.showMessageDialog(BoardFrame.this,
							"You have been eliminated from the game!");
				}

				if (controller.getStatus() == Status.GAME_OVER) {
					// This brings up the dialog displaying the winner and the
					// murderer cards.
					final JDialog endDialog = new JDialog(BoardFrame.this,
							"Game Over", true);
					endDialog.setSize(400, 400);

					Player winner = controller.getCurrentPlayer();
					JPanel panel = new JPanel();
					panel.setLayout(new BorderLayout());

					JPanel winnerPanel = new JPanel();
					winnerPanel.setLayout(new FlowLayout());

					if (winner != null) {
						winnerPanel.add(new JLabel("The winner is "
								+ winner.getName() + "!"));
					} else {
						winnerPanel
								.add(new JLabel(
										"The murderer escapes! There's no justice in this world!"));
					}

					JPanel murderPanel = new JPanel();
					murderPanel.setLayout(new GridLayout(3, 1));

					murderPanel.add(new JLabel("Murderer: "
							+ controller.getMurdererCharacter()));
					murderPanel.add(new JLabel("Room: "
							+ controller.getMurdererRoom()));
					murderPanel.add(new JLabel("Weapon: "
							+ controller.getMurdererWeapon()));

					JPanel quitPanel = new JPanel();
					quitPanel.setLayout(new FlowLayout());
					JButton quit = new JButton("Quit");
					quitPanel.add(quit);
					quit.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							System.exit(0);
						}
					});

					panel.add(winnerPanel, BorderLayout.NORTH);
					panel.add(murderPanel, BorderLayout.CENTER);
					panel.add(quitPanel, BorderLayout.SOUTH);
					endDialog.add(panel);

					endDialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
					endDialog.setLocationRelativeTo(null);
					endDialog.setVisible(true);
				}

				d.dispose();
			}
		});

		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				d.dispose();
			}
		});

		d.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		d.setLocationRelativeTo(null);
		d.setVisible(true);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		int confirmed = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to quit the game?", "",
				JOptionPane.YES_NO_OPTION);
		if (confirmed == JOptionPane.YES_OPTION) {
			dispose();
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

}