import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import hexifence.gui.core.GameRoom;


@WebSocket
public class SocketHandler {
	    private String sender, msg;

	    @OnWebSocketConnect
	    public void onConnect(Session user) throws Exception {
	    	// indicate that connection has been made; request for its name
	    	user.getRemote().sendString("RED");
	    	System.out.println("A player has joined the websocket");
	    }

	    @OnWebSocketClose
	    public void onClose(Session user, int statusCode, String reason) {
	    	// remove from the room and from player list
	    	try {
	    		if (ServerMain.players.remove(user) != null) {
	    			removeFromRoom(user);
	    		}
				
				System.out.println("A player has left the websocket.");
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }

	    @OnWebSocketMessage
	    public void onMessage(Session user, String message) throws IOException {
	    	String[] token = message.split(" ");
	    	GameRoom match_room = null;
	    	GameRoom game_room;
	    	ServerRoomData sev_data;

	    	switch (token[0]) {
	    	case "NEW":
	    		ServerMain.createPlayer(user, token[1]);
	    		System.out.println("Request for player's name successful.");
	    		// TODO: indicate success of storing name
	    		user.getRemote().sendString("RDY " + ServerMain.players.get(user).id);
	    		break;
	    	
	    	case "CRT":
	    		boolean fail = false;
	    		
	    		for (GameRoom r : ServerMain.rooms.keySet()) {
	    			if (r.toString().equals(token[1])) {
	    				fail = true;
	    				break;
	    			}
	    		}
	    		
	    		if (fail) {
	    			user.getRemote().sendString("REJ");
	    		} else {
	    			GameRoom new_room = ServerMain.createRoom(user, token[1], Integer.valueOf(token[2]));
	    			ServerMain.players.get(user).curr_game = new_room;
	    			
	    			user.getRemote().sendString("ACT " + new_room.id + " " + new_room.dim + " " + new_room.room_name);
	    		}
	    		
	    		break;
	    		
	    	case "JON":
	    		// find the room with matching id
	    		for (GameRoom r : ServerMain.rooms.keySet()) {
	    			// ensure game is still open (not started)
	    			if (r.started == false && r.id == Integer.valueOf(token[1])) {
	    				match_room = r;
	    				break;
	    			}
	    		}
	    		
	    		// if the room could not be found
	    		if (match_room == null) {
	    			user.getRemote().sendString("FAL");
	    		} else {
	    			// if there is a matching room
	    			user.getRemote().sendString("ACT " + match_room.id + " " + match_room.dim + " " + match_room.room_name);
	    			
	    			// inform others in the room that a player has joined
	    			for (Session s : ServerMain.rooms.get(match_room).users) {
	    				PlayerData p_data = ServerMain.players.get(s);
	    				
	    				s.getRemote().sendString("QUE 0 " + p_data.id + " " + p_data.name);
	    			}
	    			
	    			// add player to the room
	    			ServerMain.rooms.get(match_room).users.add(user);
	    			ServerMain.players.get(user).curr_game = match_room;
	    		}
	    		
	    		break;
	    		
	    	
	    	case "LEV":
	    		removeFromRoom(user);
	    		break;
	    		
	    	case "BGN":
	    		// indicate the game room has begun
	    		game_room = ServerMain.players.get(user).curr_game;
	    		sev_data = ServerMain.rooms.get(game_room);
	    		game_room.started = true;
	    		
	    		// generate random index to give to first player
	    		int index_first_player = new Random().nextInt(sev_data.users.size());
	    		sev_data.curr_index_player = index_first_player;
	    		int id_first_player = ServerMain.players.get(sev_data.users.get(index_first_player)).id;
	    		
	    		// inform other players to begin game
				for (Session s : ServerMain.rooms.get(game_room).users) {
					s.getRemote().sendString("SRT " + id_first_player);
				}
	    		
	    		break;
	    		
	    	case "MVE":
	    		game_room = ServerMain.players.get(user).curr_game;
	    		sev_data = ServerMain.rooms.get(game_room);
	    		
	    		// get the next player
	    		sev_data.curr_index_player++;
	    		sev_data.curr_index_player %= sev_data.users.size();
	    		
	    		int id_next = ServerMain.players.get(sev_data.users.get(sev_data.curr_index_player)).id;
	    		
	    		// inform other players the next player, and the confirmed move
				for (Session s : sev_data.users) {
					s.getRemote().sendString("CON " + token[1] + " " + token[2] + " " + id_next);
				}

	    		break;
	    	}
	    	
	    }
	    
	    public void removeFromRoom(Session user) throws IOException {
	    	// do nothing if player is not in the server
    		if (ServerMain.players.get(user) == null) {
    			return;
    		}
	    	
    		GameRoom room = ServerMain.players.get(user).curr_game;
    		
    		// remove leaving player
    		ServerMain.rooms.get(room).users.remove(user);
    		ServerMain.players.get(user).curr_game = null;
    		
    		// inform other players the player left
			for (Session s : ServerMain.rooms.get(room).users) {
				PlayerData p_data = ServerMain.players.get(s);
				
				s.getRemote().sendString("QUE 1 " + p_data.id);
			}
			
			// if room has no more players left
			if (ServerMain.rooms.get(room).users.size() == 0) {
				ServerMain.rooms.remove(room);
				System.out.println("Room has been removed");
			}
	    }
}
