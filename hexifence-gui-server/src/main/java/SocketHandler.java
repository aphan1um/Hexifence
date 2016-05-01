import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import hexifence.gui.core.GameRoom;
import hexifence.gui.core.ServerPacket;


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
	    		// remove the player from room (if any) before
	    		// removing from serverlist
	    		removeFromRoom(user);
	    		Main.players.remove(user);
				
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
	    	Room sev_data;

	    	ServerPacket packet = ServerPacket.fromString(token[0]);
	    	System.out.println(message);
	    	
	    	switch (packet) {
	    	case SERVER_ADDED_USER:
	    		Main.createPlayer(user, token[1]);
	    		System.out.println("Request for player's name successful.");

	    		// indicate success of adding user to server
	    		user.getRemote().sendString("RDY " + Main.players.get(user).id);
	    		break;
	    	
	    	case CREATE_ROOM:
	    		boolean fail = false;
	    		
	    		for (GameRoom r : Main.rooms.keySet()) {
	    			if (r.toString().equals(token[1])) {
	    				fail = true;
	    				break;
	    			}
	    		}
	    		
	    		if (fail) {
	    			user.getRemote().sendString("REJ");
	    		} else {
	    			GameRoom new_room = Main.createRoom(user, token[1], Integer.valueOf(token[2]));
	    			Main.players.get(user).curr_game = new_room;
	    			
	    			user.getRemote().sendString("ACT " + new_room.id + " " + new_room.dim + " " + new_room.room_name);
	    			System.out.println("A room has been created.");
	    		}
	    		
	    		break;
	    		
	    	case USER_JOINED_ROOM:
	    		// find the room with matching id
	    		for (GameRoom r : Main.rooms.keySet()) {
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
	    			for (Session s : Main.rooms.get(match_room).users) {
	    				PlayerData p_data = Main.players.get(s);
	    				
	    				s.getRemote().sendString("QUE 0 " + p_data.id + " " + p_data.name);
	    			}
	    			
	    			// add player to the room
	    			Main.rooms.get(match_room).users.add(user);
	    			Main.players.get(user).curr_game = match_room;
	    		}
	    		
	    		break;
	    		
	    	
	    	case USER_LEFT_ROOM:
	    		removeFromRoom(user);
	    		break;
	    		
	    	case GAME_BEGIN:
	    		// indicate the game room has begun
	    		game_room = Main.players.get(user).curr_game;
	    		sev_data = Main.rooms.get(game_room);
	    		game_room.started = true;
	    		
	    		// generate random index to give to first player
	    		int index_first_player = new Random().nextInt(sev_data.users.size());
	    		sev_data.curr_index_player = index_first_player;
	    		int id_first_player = Main.players.get(sev_data.users.get(index_first_player)).id;
	    		
	    		// inform other players to begin game
				for (Session s : Main.rooms.get(game_room).users) {
					s.getRemote().sendString("SRT " + id_first_player);
				}
	    		
	    		break;
	    		
	    	case GAME_NEXT_MOVE:
	    		game_room = Main.players.get(user).curr_game;
	    		sev_data = Main.rooms.get(game_room);
	    		
	    		int id_next = Main.players.get(sev_data.users.get(sev_data.curr_index_player)).id;
	    		
	    		int cells_captured = sev_data.sev_board.getEdges()[Integer.valueOf(token[1])][Integer.valueOf(token[2])].useCell(id_next);
	    		
	    		// if cell has been captured, the current player will keep his/her turn;
	    		// otherwise the next player goes
	    		if (cells_captured == 0) {
	    			// get the next player
		    		sev_data.curr_index_player++;
		    		sev_data.curr_index_player %= sev_data.users.size();
		    		
		    		id_next = Main.players.get(sev_data.users.get(sev_data.curr_index_player)).id;
	    		} else {
	    			sev_data.sev_board.num_cells_open -= cells_captured;
	    		}
				
				// if game has ended
	    		if (sev_data.sev_board.num_cells_open == 0) {
	    			System.out.println("A game has ended; removing game...");
					for (Session s : sev_data.users) {
						s.getRemote().sendString("FIN " + token[1] + " " + token[2]);
						// remove user from room
						removeFromRoom(s);
					}
	    		} else {
		    		// inform other players the next player, and the confirmed move
					for (Session s : sev_data.users) {
						s.getRemote().sendString("CON " + token[1] + " " + token[2] + " " + id_next);
					}
	    		}

	    		break;
	    	}
	    	
	    }
	    
	    public void removeFromRoom(Session user) throws IOException {
	    	// do nothing if player is not in the server or game
    		if (Main.players.get(user) == null || 
    				Main.players.get(user).curr_game == null) {
    			return;
    		}
	    	
    		GameRoom room = Main.players.get(user).curr_game;
    		
    		// remove leaving player
    		Main.rooms.get(room).users.remove(user);
    		Main.players.get(user).curr_game = null;
    		
    		// inform other players the player left
			for (Session s : Main.rooms.get(room).users) {
				PlayerData p_data = Main.players.get(s);
				
				s.getRemote().sendString("QUE 1 " + p_data.id);
			}
			
			// if room has no more players left
			if (Main.rooms.get(room).users.size() == 0) {
				Main.rooms.remove(room);
				System.out.println("Room has been removed");
			}
	    }
}
