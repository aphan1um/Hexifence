package hexifence.gui.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import hexifence.gui.client.net.PacketHandler;

public class FrameBoard extends JFrame {
	public static final Color[] USER_COLOURS = new Color[] { Color.RED, Color.CYAN, Color.ORANGE };
	public static final String[] USER_COLOURS_STR = new String[] { "R", "C", "O" };
	int user_colours_count = 0;
	
	/** Area of text, to show information about the game/server */
	private JTextArea txt_info = new JTextArea(10, 30);
	/** Players in the game, mapping from player ID from server to player name */
	private HashMap<Integer, String> players = new HashMap<Integer, String>();
	/** Button to start game */
	private JButton btn_start;
	/** Contains the panel to displaying the game */
	private GUIBoard board;
	
	public HashMap<Integer, Integer> players_c;
	
	public static int CURR_ID_TURN = -1;
	public static boolean IS_LOCKED = false;
	
	public FrameBoard(int r, int offset, int dim, String roomName) {
		// get centre location of this board, which is to be drawn on the frame
		double x_cen = r * (2*dim - 1) * Math.cos(Math.PI/6) + offset;
		double y_cen = r * Math.sin(Math.PI/6) * Math.floor((2*dim -1)/2)  + r*Math.ceil((2*dim - 1)/2.0) + offset;
		
		IS_LOCKED = false;
		
		setTitle("Hexifence - " + roomName);
		
		initUI(x_cen, y_cen, offset, dim, r);
		
		pack();
		setVisible(true);
	}

	private void initUI(double x_cen, double y_cen, int offset, int dim, int r) {
		// add the game board onto window
		board = new GUIBoard(dim, new Point2D.Double(x_cen, y_cen), r, this);
		board.getBoardPanel().setPreferredSize(new Dimension((int)x_cen * 2, (int)y_cen * 2 + offset));
		add(board.getBoardPanel());
		
		// add the main player into 'players' (no need supplied)
		players.put(PacketHandler.USER_ID, null);
		
		// info textbox
		JPanel bottom_panel = new JPanel();
		add(bottom_panel, BorderLayout.SOUTH);
		bottom_panel.setLayout(new BoxLayout(bottom_panel, BoxLayout.Y_AXIS));
		
		txt_info.setBorder(BorderFactory.createEtchedBorder());
		// txt_info.setPreferredSize(new Dimension((int)x_cen * 2, 200));
		txt_info.setEditable(false);
		txt_info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		// bottom_panel.add(txt_info);
		
		// scroll pane for textbox
		JScrollPane scroll = new JScrollPane (txt_info);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		bottom_panel.add(scroll);
		
		txt_info.append("Welcome to Hexifence!");
		
		// when the board is closed, indicate player left
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Driver.leaveRoom();
			}
		});
		
		// start button
		btn_start = new JButton("Start Game");
		btn_start.setSize(new Dimension((int)x_cen * 2, 20));
		btn_start.setAlignmentX(Component.CENTER_ALIGNMENT);
		bottom_panel.add(btn_start);
		
		// button to start game event
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Driver.beginGame();
			}
		});
	}
	
	public void addPlayer(String name, int player_id) {
		players.put(player_id, name);
		txt_info.append("\n" + name + " has entered the room.");
	}
	
	public void removePlayer(int player_id) {
		txt_info.append("\n" + players.get(player_id) + " has left the room.");
		players.remove(player_id);
	}
	
	public void startGame(int player_id) {
		btn_start.setVisible(false);
		btn_start.getParent().remove(btn_start);
		txt_info.append("\nGame has started!");
		players_c = new HashMap<Integer, Integer>();
		
		nextTurn(player_id);
	}
	
	/** Give the next player a turn to select an open edge to occupy.
	 * @param player_id Player ID to go next, given by the server.
	 */
	public void nextTurn(int player_id) {
		CURR_ID_TURN = player_id;
		
		if (isMyTurn()) {
			txt_info.append("\n" + "It is your turn.");
			IS_LOCKED = false;
		}
	}

	public static boolean isMyTurn() {
		return CURR_ID_TURN == PacketHandler.USER_ID;
	}

	public void confirmMove(int x, int y, int next_id) {
		// if current player has not been given a colour
		if (CURR_ID_TURN >= 0 && players_c.get(CURR_ID_TURN) == null) {
			players_c.put(CURR_ID_TURN, user_colours_count++);
		}
		
		// game has ended
		if (next_id == -1) {
			txt_info.append("\n" + "Game has ended.");
		}
		
		System.out.println("GOOGO " + CURR_ID_TURN + " " + next_id);
		// 'use' the cell (ie. make it unselectable, and tell cells with
		// this edge that edge is no longer open
		board.getEdges()[x][y].useCell(USER_COLOURS[players_c.get(CURR_ID_TURN)], CURR_ID_TURN);
		nextTurn(next_id);
	}
}