package core;

import java.util.HashMap;

import aiproj.hexifence.Piece;

public class TranspositionTable {
	private ZobristHasher hasher;
	private HashMap<Integer, Integer> table; // Hash map of (key, minimax value) pairs.
	
	public TranspositionTable(int dimension) {
		hasher = new ZobristHasher(dimension);
		table = new HashMap<Integer, Integer>();
	}
	
	public boolean isStored(Board board) {
		return table.get(hasher.generateHashKey(board.getEdges())) != null;
	}
	
	public int getSize() {
		return table.size();
	}
	
	public void storeMinimax(Board board, int minimax) {
		int hashKey = hasher.generateHashKey(board.getEdges());

		// if we get a collision
		if (table.get(board) != null && table.get(board) != minimax) {
			System.out.println("Collision found!");
		}
		
		table.put(hashKey, minimax);
	}
	
	public Integer getMinimax(Piece[][] edges) {
		int hashKey = hasher.generateHashKey(edges);

		return table.get(hashKey);
	}
	
}