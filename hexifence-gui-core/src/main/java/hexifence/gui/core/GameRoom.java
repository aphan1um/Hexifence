package hexifence.gui.core;

public class GameRoom implements java.io.Serializable {
	/** Serial version UID. */
	private static final long serialVersionUID = 4418396005311045882L;
	public String room_name;
	public int id;
	public int dim;
	public boolean started;

	public GameRoom(String room_name, int dim, int id) {
		this.id = id;
		this.dim = dim;
		this.room_name = room_name;
		// by default, game has not started
		this.started = false;
	}
	
	@Override
	public String toString() {
		return room_name;
	}
}
