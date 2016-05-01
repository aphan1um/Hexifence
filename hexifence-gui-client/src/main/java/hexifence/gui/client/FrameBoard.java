package hexifence.gui.client;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import hexifence.gui.client.net.PacketHandler;
import hexifence.gui.core.Board;

public class FrameBoard extends JFrame {
	/** Area of text, to show information about the game/server */
	private JTextArea txt_info = new JTextArea();
	/** Players in the game, mapping from player ID from server to player name */
	private HashMap<Integer, String> players = new HashMap<Integer, String>();
	/** Button to start game */
	private JButton btn_start;
	/** Contains the panel to displaying the game */
	private GUIBoard board;
	
	/** If it is the player's turn to make a move */
	public static boolean my_turn = false;

	
	public FrameBoard(int r, int offset, int dim, String roomName) {
		// get centre location of this board, which is to be drawn on the frame
		double x_cen = r * (2*dim - 1) * Math.cos(Math.PI/6) + offset;
		double y_cen = r * Math.sin(Math.PI/6) * Math.floor((2*dim -1)/2)  + r*Math.ceil((2*dim - 1)/2.0) + offset;
		
		setTitle("Hexifence - " + roomName);
		
		initUI(x_cen, y_cen, offset, dim, r);
		
		pack();
		setVisible(true);
	}

	private void initUI(double x_cen, double y_cen, int offset, int dim, int r) {
		// add the game board onto window
		board = new GUIBoard(dim, new Point2D.Double(x_cen, y_cen), r);
		board.getBoardPanel().setPreferredSize(new Dimension((int)x_cen * 2, (int)y_cen * 2 + offset));
		add(board.getBoardPanel());
		
		// add the main player into 'players' (no need supplied)
		players.put(PacketHandler.USER_ID, null);
		
		// info textbox
		JPanel bottom_panel = new JPanel();
		add(bottom_panel, BorderLayout.SOUTH);
		bottom_panel.setLayout(new BoxLayout(bottom_panel, BoxLayout.Y_AXIS));
		
		txt_info.setBorder(BorderFactory.createEtchedBorder());
		txt_info.setPreferredSize(new Dimension((int)x_cen * 2, 100));
		txt_info.setEditable(false);
		txt_info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		bottom_panel.add(txt_info);
		
		txt_info.append("Welcome to Hexifence (waiting for players)!");
		
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
		System.out.println("HIHIHI");
		players.put(player_id, name);
		txt_info.append("\n" + name + " has entered the room.");
	}
	
	public void removePlayer(int player_id) {
		txt_info.append("\n" + players.get(player_id) + " has left the room.");
		players.remove(player_id);
	}
	
	/** Give the next player a turn to select an open edge to occupy.
	 * @param player_id Player ID to go next, given by the server.
	 */
	public void nextTurn(int player_id) {
		if (player_id == PacketHandler.USER_ID) {
			my_turn = true;
			txt_info.append("\n" + "It is your turn.");
		}
	}

	public void confirmMove(int x, int y, int next_id) {
		// 'use' the cell (ie. make it unselectable, and tell cells with
		// this edge that edge is no longer open
		board.getEdges()[x][y].useCell(Color.ORANGE);
		nextTurn(next_id);
	}
}