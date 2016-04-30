import java.awt.Frame;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class PacketHandler implements MessageHandler {
	public static Window CURR_WINDOW = null;
	public static MainMenu MENU_WINDOW = null;
	
	public static int USER_ID = -1;
	
	public void handleMessage(String message) {
		String[] tokens = message.split(" ");
		
		System.out.println(message);
		
		switch (tokens[0]) {
		case "RED":
			// TODO: Add wait...
			while (MENU_WINDOW == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			Driver.createPlayer(MENU_WINDOW.txt_name.getText());
			break;
		
		case "RDY":
			USER_ID = Integer.valueOf(tokens[1]);
			System.out.println("USER_ID = " + USER_ID);
			MENU_WINDOW.enableWindow();
			break;
			
		case "REJ":
			JOptionPane.showMessageDialog(null,
				    "The room name you have chosen to create has been taken.",
				    "Room name occupied",
				    JOptionPane.WARNING_MESSAGE);
			
			
			CURR_WINDOW.setEnabled(true);
			CURR_WINDOW.toFront();
			
			break;
		case "ACT":
			if (CURR_WINDOW != null) {
				CURR_WINDOW.dispose();
				CURR_WINDOW = null;
			}
			
			// token[2] = dim
			initBoard(Integer.valueOf(tokens[2]), tokens[3]);

			break;
			
		case "FAL":
			JOptionPane.showMessageDialog(null,
				    "The room you have specified could not be found\nIt might have been closed before you entered.",
				    "Room not found",
				    JOptionPane.ERROR_MESSAGE);
			
		case "QUE":
			if (!(CURR_WINDOW instanceof BoardFrame)) {
				break;
			}
			
			// 0 for invite, 1 for leave
			System.out.println(tokens[1]);
			if (Integer.valueOf(tokens[1]) == 0) {

				((BoardFrame)CURR_WINDOW).addPlayer(tokens[3], Integer.valueOf(tokens[2]));
			} else {
				((BoardFrame)CURR_WINDOW).removePlayer(Integer.valueOf(tokens[2]));
			}
			
			break;
		
		// start game packet
		case "SRT":
			if (!(CURR_WINDOW instanceof BoardFrame)) {
				break;
			}
			
			((BoardFrame)CURR_WINDOW).startGame(Integer.valueOf(tokens[1]));
			break;
		
		// confirmed move packet
		case "CON":
			if (!(CURR_WINDOW instanceof BoardFrame)) {
				break;
			}
			
			((BoardFrame)CURR_WINDOW).confirmMove(Integer.valueOf(tokens[1]), Integer.valueOf(tokens[2]), Integer.valueOf(tokens[3]));
			break;
		}
		
		
	}

	
	private void initBoard(int dim, String boardName) {
		int r = 40;
		int offset = 20;

		CURR_WINDOW = new BoardFrame(r, offset, dim, boardName);
		CURR_WINDOW.toFront();
		CURR_WINDOW.setLocationRelativeTo(null);
		
		MENU_WINDOW.setVisible(false);
	}
}
