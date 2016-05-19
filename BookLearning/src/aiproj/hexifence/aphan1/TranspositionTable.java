/* 
 * COMP30024 - Project B
 * Andy Phan (aphan1) and Martin Cheong (cheongm)
 * 
 */

package aiproj.hexifence.aphan1;

import java.util.HashMap;


public class TranspositionTable {
	private Hasher hasher;
	private HashMap<Long, Double> table; 
									// Hash map of (key, minimax value) pairs.

	public TranspositionTable(int dimension, Hasher hasher) {
		this.hasher = hasher;
		this.table = new HashMap<Long, Double>();
	}
	
	/** Check if the given board is already in table. */
	public boolean isStored(Board board) {
		long hashKey = hasher.generateHashKey(board.getEdges());
		
		return table.containsKey(hashKey);
	}
	
	/** Retrieve the size of table. */
	public int getSize() {
		return table.size();
	}
	
	public void storeEntry(Board board, double value) {
		long hashKey = hasher.generateHashKey(board.getEdges());

		// if we get a collision
		if (!table.containsKey(hashKey)) {
			double store_value;
			
			// our agent's turn
			if (board.getCurrTurn() == board.getMyColor()) {
				store_value =  value;
			} else {
				store_value = board.getNumUncaptured() - value;
			}

			table.put(hashKey, store_value);
		} else {
			/*
			
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
			
			*/
			
		}

	}
	
	public double getEntry(Board board) {
		long hashKey = hasher.generateHashKey(board.getEdges());
		
		return (board.getCurrTurn() == board.getMyColor()) ? 
				table.get(hashKey) : 
					board.getNumUncaptured() - table.get(hashKey);
	}

	public long getHash(Board board) {
		return hasher.generateHashKey(board.getEdges());
	}
}