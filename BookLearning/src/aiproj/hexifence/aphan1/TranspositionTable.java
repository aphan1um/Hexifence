/* 
 * COMP30024 - Project B
 * Andy Phan (aphan1) and Martin Cheong (cheongm)
 * 
 */

package aiproj.hexifence.aphan1;

import java.util.HashMap;


public class TranspositionTable {
	private ZobristHasher hasher;
	private HashMap<Long, Integer> table; 
									// Hash map of (key, minimax value) pairs.

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
				e.printStackTrace();
				System.exit(0);
			}
		}

		// if we get a collision
		if (!table.containsKey(hashKey)) {
			int store_value;
			
			// our agent's turn
			if (board.getCurrTurn() == board.getMyColor()) {
				store_value =  value;
			} else {
				store_value = board.getNumUncaptured() - value;
			}

			table.put(hashKey, store_value);
		} else {
			// if we happen to come across a board with different
			//  value, then the one already stored in table
			if (getEntry(board) != value) {
				try {
					throw new Exception();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			
		}

	}
	
	public int getEntry(Board board) {
		long hashKey = hasher.generateHashKey(board.getEdges());
		
		return (board.getCurrTurn() == board.getMyColor()) ? 
				table.get(hashKey) : 
					board.getNumUncaptured() - table.get(hashKey);
	}

	public long getHash(Board board) {
		return hasher.generateHashKey(board.getEdges());
	}
}