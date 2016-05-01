package hexifence.gui.core;

public enum ServerPacket {
	SERVER_ADDED_USER("NEW"),
	CREATE_ROOM("CRT"),
	USER_JOINED_ROOM("JON"),
	USER_LEFT_ROOM("LEV"),
	GAME_BEGIN("BGN"),
	GAME_NEXT_MOVE("MVE");
	
	private final String packet_name;

	private ServerPacket(String name) {
		this.packet_name = name;
	}

	public String toString() {
		return packet_name;
	}
	
	public static ServerPacket fromString(String name) {
		for (ServerPacket p : ServerPacket.values()) {
			if (p.toString().equals(name)) {
				return p;
			}
		}
		
		return null;
	}
}
