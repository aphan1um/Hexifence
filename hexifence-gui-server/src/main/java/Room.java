import org.eclipse.jetty.websocket.api.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
	private static final int START_ID = 0;
	private static int next_id = START_ID;

	public List<Session> users = new CopyOnWriteArrayList<Session>();
	public int id;
	public int curr_index_player = 0;
	
	public ServerBoard sev_board;
	
	public Room(Session player, int dim) {
		this.id = next_id++;
		users.add(player);
		
		sev_board = new ServerBoard(dim);
	}
	
	public Session getHost() {
		return users.get(0);
	}
}
