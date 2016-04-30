
import org.eclipse.jetty.websocket.api.*;

import hexifence.gui.core.GameRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerRoomData {
	private static final int START_ID = 0;
	private static int next_id = START_ID;

	public List<Session> users = new CopyOnWriteArrayList<Session>();
	public int id;
	public int curr_index_player = 0;
	
	public ServerRoomData(Session player) {
		this.id = next_id++;
		users.add(player);
	}
	
	public Session getHost() {
		return users.get(0);
	}
}
