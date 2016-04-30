import hexifence.gui.core.GameRoom;

public class PlayerData {
	private static int PLAYER_COUNT = 0;
	
	public String name;
	public int id;
	public GameRoom curr_game;
	
	public PlayerData(String name) {
		this.name = name;
		this.id = PLAYER_COUNT++;
		this.curr_game = null;
	}
}
