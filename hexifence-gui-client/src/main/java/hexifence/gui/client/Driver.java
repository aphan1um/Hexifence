package hexifence.gui.client;
import java.awt.EventQueue;
import java.net.URI;
import java.net.URISyntaxException;

import hexifence.gui.client.net.ChatClientEndpoint;
import hexifence.gui.client.net.PacketHandler;
import hexifence.gui.core.GameRoom;


public class Driver {
	/** Web address which hosts the server application */
	public static final String SERVER_ADDRESS = "http://localhost:5000";
	/** Websocket address */
	public static final String WEBSOCKET_ADDRESS = "ws://localhost:5000/players";

	/** Class which allows client to send messages to server */
	private static ChatClientEndpoint client;
	
	static {
		// initialise a connect to web socket
		try {
			client = new ChatClientEndpoint(new URI(WEBSOCKET_ADDRESS));
			client.addMessageHandler(new PacketHandler());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
            public void run() {
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
