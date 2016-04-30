import java.awt.EventQueue;
import java.net.URI;
import java.net.URISyntaxException;

import hexifence.gui.core.GameRoom;


public class Driver {
	public static final String SERVER_ADDRESS = "http://localhost:5000";
	public static final String WEBSOCKET_ADDRESS = "ws://localhost:5000/players";
	
	private static int dim;
	private static ChatClientEndpoint client;
	
	static {
		try {
			client = new ChatClientEndpoint(new URI(WEBSOCKET_ADDRESS));
			client.addMessageHandler(new PacketHandler());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Input arguments: first board size, second path of player1 and third path of player2
	 */
	public static void main(String[] args)
	{
		dim = Integer.valueOf(args[0]);
		
		EventQueue.invokeLater(new Runnable() {
            public void run() {
                
                // Driver.initUI(dim);
                PacketHandler.MENU_WINDOW = new MainMenu();
                PacketHandler.MENU_WINDOW.setLocationRelativeTo(null);
            }
        });
	}

	
	public static void createRoom(String name, int dim) {
		client.sendMessage("CRT " + name + " " + dim);
	}

	public static void joinRoom(GameRoom room) {
		client.sendMessage("JON " + room.id);
	}
	
	public static void beginGame() {
		client.sendMessage("BGN");
	}
	
	public static void sendMove(int x, int y) {
		client.sendMessage("MVE " + x + " " + y);
	}
	
	public static void createPlayer(String name) {
		client.sendMessage("NEW " + name);
	}
	
	public static void leaveRoom() {
		client.sendMessage("LEV");
		
		PacketHandler.MENU_WINDOW.setVisible(true);
	}
}
