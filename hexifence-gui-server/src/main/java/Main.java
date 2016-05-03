import org.eclipse.jetty.websocket.api.*;

import hexifence.gui.core.GameRoom;

import static spark.Spark.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

public class Main {
	private static int PLAYER_COUNT = 0;

	static ConcurrentHashMap<Session, PlayerData> players = new ConcurrentHashMap<Session, PlayerData>();
	static ConcurrentHashMap<GameRoom, Room> rooms = new ConcurrentHashMap<GameRoom, Room>();
	
	static Timer timer_ping = new Timer(true);
	
	public static void createPlayer(Session session, String name) {
		players.put(session, new PlayerData(name));
	}
	
	public static GameRoom createRoom(Session session, String room_name, int dim) {
		Room new_sev = new Room(session, dim);
		GameRoom new_pub_room = new GameRoom(room_name, dim, new_sev.id);
		
		rooms.put(new_pub_room, new_sev);
		
		return new_pub_room;
	}
	
	// send ping
	public static class RemindTask extends TimerTask {
	    public void run() {
	      for (Session p : players.keySet()) {
	    	  try {
				p.getRemote().sendString("PIN");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      }
	    }
	  }
	
	public static void main(String[] args) {
		// setup timer
		timer_ping.schedule(new Main.RemindTask(), 100, 1000 * 30);
		
		port(Integer.valueOf(System.getenv("PORT")));
		staticFileLocation("/public");
		webSocket("/players", SocketHandler.class);
		init();

		get("/rooms", (request, response) -> {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream zos = new GZIPOutputStream(baos);
	        ObjectOutputStream oos = new ObjectOutputStream( zos );

	        oos.writeObject(rooms.keySet().toArray(new GameRoom[rooms.size()]));
	        oos.close();
	        return baos.toByteArray(); 
			
        });
	}

}
