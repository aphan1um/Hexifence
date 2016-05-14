package core;

import java.util.HashMap;

import aiproj.hexifence.Piece;

public class TranspositionTable {
	private ZobristHasher hasher;
	private HashMap<Integer, TableEntry> table; // Hash map of (key, minimax value) pairs.
	
	public TranspositionTable(int dimension) {
		hasher = new ZobristHasher(dimension);
		table = new HashMap<Integer, TableEntry>();
	}
	
	public boolean isStored(Board board) {
		return table.get(hasher.generateHashKey(board.getEdges())) != null;
	}
	
	public void storeMinimax(Board board, int minimax) {
		int hashKey = hasher.generateHashKey(board.getEdges());
		
		TableEntry entry = new TableEntry();
		entry.minimax = minimax;
		entry.conf = board.toString();
		entry.turn = board.getCurrTurn();

		// display info, if we do get to a collision (messy)
		if (table.get(hashKey) != null && table.get(hashKey).minimax != minimax) {
			System.out.println("COLLI: " + table.get(hashKey).conf + "\t\t" + 
						board.toString() + "\t" + (board.toString().equals(table.get(hashKey).conf) +
								"\t" + table.get(hashKey).minimax + " " + minimax) +
						"\t" + board.getCurrTurn().toString() + " " + table.get(hashKey).turn);
		}
		
		table.put(hashKey, entry);
	}
	
	public Integer getMinimax(Piece[][] edges) {
		int hashKey = hasher.generateHashKey(edges);

		return table.get(hashKey).minimax;
	}
	
}