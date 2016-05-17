package aiproj.hexifence.MartinAndy;

import java.util.HashMap;

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
	
	public void storeMinimax(Board board, int minimax) {
		int hashKey = hasher.generateHashKey(board.getEdges());
		
		if (table.containsKey(hashKey) &&
				table.get(hashKey) != minimax) {
			System.out.println("Collision");
		}
		
		table.put(hashKey, minimax);
	}
	
	public Integer getMinimax(int[][] edges) {
		int hashKey = hasher.generateHashKey(edges);

		return table.get(hashKey);
	}
	
	public int getSize() {
		return table.size();
	}
}