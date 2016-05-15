package core;

import java.util.HashMap;


public class TranspositionTable {
	private ZobristHasher hasher;
	private HashMap<Long, Integer> table; // Hash map of (key, minimax value) pairs.

	public TranspositionTable(int dimension) {
		hasher = new ZobristHasher(dimension);
		table = new HashMap<Long, Integer>();
	}
	
	public boolean isStored(Board board) {
		long hashKey = hasher.generateHashKey(board.getEdges());
		
		return table.containsKey(hashKey);
	}
	
	public int getSize() {
		return table.size();
	}
	
	public void storeEntry(Board board, int value) {
		long hashKey = hasher.generateHashKey(board.getEdges());

		if (value < 0) {
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}
		}

		// System.out.println("STORED: " + board.toString() + "\t\t" + hashKey + "\t\t" + board.getCurrTurn() + "\t" + value);
		
		// if we get a collision
		if (!table.containsKey(hashKey)) {
			// TODO: For some strange reason, the opposing side causes many collisions,
			// but the main player does not?

			int store_value;
			
			// our agent's turn
			if (board.getCurrTurn() == Main.myColor) {
				store_value =  value;
			} else {
				store_value = board.getNumUncaptured() - value;
			}

			table.put(hashKey, store_value);
		} else {
			
			if (getEntry(board) != value) {
				try {
					throw new Exception();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(0);
				}
			}
			
		}

	}
	
	public int getEntry(Board board) {
		long hashKey = hasher.generateHashKey(board.getEdges());
		
		return (board.getCurrTurn() == Main.myColor) ? table.get(hashKey) : 
			board.getNumUncaptured() - table.get(hashKey);
	}

	public long getHash(Board board) {
		return hasher.generateHashKey(board.getEdges());
	}
}