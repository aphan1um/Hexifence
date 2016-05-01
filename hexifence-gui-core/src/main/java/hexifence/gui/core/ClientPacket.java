package hexifence.gui.core;

public enum ClientPacket {
	CONNECTING("RED"),
	READY("RDY"),
	ROOM_NAME_REJECTED("REJ"),
	ROOM_ACCEPTED("ACT"),
	ROOM_NOT_FOUND("FAL"),
	ROOM_STATUS_NEW("QUE"),
	ROOM_START("SRT"),
	MOVE_CONFIRMED("CON"),
	GAME_FINISHED("FIN");
	
	private final String packet_name;

	private ClientPacket(String name) {
		this.packet_name = name;
	}

	public String toString() {
		return packet_name;
	}
	
	public static ClientPacket fromString(String name) {
		for (ClientPacket p : ClientPacket.values()) {
			if (p.toString().equals(name)) {
				return p;
			}
		}
		
		return null;
	}
}
