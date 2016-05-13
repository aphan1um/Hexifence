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
	
	public void storeMinimax(Piece[][] edges, int minimax) {
		int hashKey = hasher.generateHashKey(edges);
		
		table.put(hashKey, minimax);
	}
	
	public Integer getMinimax(Piece[][] edges) {
		int hashKey = hasher.generateHashKey(edges);
		
		return table.get(hashKey);
	}
	
}