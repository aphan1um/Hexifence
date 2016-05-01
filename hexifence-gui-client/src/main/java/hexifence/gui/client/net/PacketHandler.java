package hexifence.gui.client.net;

import java.awt.Window;

import javax.swing.JOptionPane;

import hexifence.gui.client.FrameBoard;
import hexifence.gui.client.Driver;
import hexifence.gui.client.MainMenu;
import hexifence.gui.core.ClientPacket;

public class PacketHandler implements MessageHandler {
	public static Window CURR_WINDOW = null;
	public static MainMenu MENU_WINDOW = null;
	
	public static int USER_ID = -1;
	
	public void handleMessage(String message) {
		System.out.println("Packet received.");
		
		String[] tokens = message.split(" ");
		System.out.println(message);
		
		ClientPacket packet = ClientPacket.fromString(tokens[0]);
		
		switch (packet) {
		case CONNECTING:
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
		
		case READY:
			USER_ID = Integer.valueOf(tokens[1]);
			MENU_WINDOW.enableWindow();
			break;
			
		case ROOM_NAME_REJECTED:
			JOptionPane.showMessageDialog(null,
				    "The room name you have chosen to create has been taken.",
				    "Room name occupied",
				    JOptionPane.WARNING_MESSAGE);
			
			
			CURR_WINDOW.setEnabled(true);
			CURR_WINDOW.toFront();
			
			break;
		case ROOM_ACCEPTED:
			if (CURR_WINDOW != null) {
				CURR_WINDOW.dispose();
				CURR_WINDOW = null;
			}
			
			System.out.println("Got here");
			// token[2] = dim
			initBoard(Integer.valueOf(tokens[2]), tokens[3]);

			break;
			
		case ROOM_NOT_FOUND:
			JOptionPane.showMessageDialog(null,
				    "The room you have specified could not be found\nIt might have been closed before you entered.",
				    "Room not found",
				    JOptionPane.ERROR_MESSAGE);
			
		case ROOM_STATUS_NEW:
			if (!(CURR_WINDOW instanceof FrameBoard)) {
				break;
			}
			
			// 0 for invite, 1 for leave
			System.out.println(tokens[1]);
			if (Integer.valueOf(tokens[1]) == 0) {

				((FrameBoard)CURR_WINDOW).addPlayer(tokens[3], Integer.valueOf(tokens[2]));
			} else {
				((FrameBoard)CURR_WINDOW).removePlayer(Integer.valueOf(tokens[2]));
			}
			
			break;
		
		// start game packet
		case ROOM_START:
			if (!(CURR_WINDOW instanceof FrameBoard)) {
				break;
			}
			
			((FrameBoard)CURR_WINDOW).startGame(Integer.valueOf(tokens[1]));
			break;
		
		// confirmed move packet
		case MOVE_CONFIRMED:
			if (!(CURR_WINDOW instanceof FrameBoard)) {
				break;
			}

			((FrameBoard)CURR_WINDOW).confirmMove(Integer.valueOf(tokens[1]), Integer.valueOf(tokens[2]), Integer.valueOf(tokens[3]));
			break;
			
		// last move, so game ends
		case GAME_FINISHED:
			if (!(CURR_WINDOW instanceof FrameBoard)) {
				break;
			}
			
			((FrameBoard)CURR_WINDOW).confirmMove(Integer.valueOf(tokens[1]), Integer.valueOf(tokens[2]), -1);
			break;
		}
		
	}

	
	private void initBoard(int dim, String boardName) {
		int r = 40;
		int offset = 20;

		CURR_WINDOW = new FrameBoard(r, offset, dim, boardName);
		CURR_WINDOW.toFront();
		CURR_WINDOW.setLocationRelativeTo(null);
		
		MENU_WINDOW.setVisible(false);
	}
}
