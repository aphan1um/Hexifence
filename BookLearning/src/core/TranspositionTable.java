package core;

import java.util.HashMap;


public class TranspositionTable {
	private ZobristHasher hasher;
	private HashMap<Long, TableEntry[]> table; // Hash map of (key, minimax value) pairs.

	public TranspositionTable(int dimension) {
		hasher = new ZobristHasher(dimension);
		table = new HashMap<Long, TableEntry[]>();
	}
	
	public boolean isStored(Board board) {
		long hashKey = hasher.generateHashKey(board.getEdges());
		int index = (Main.myColor == board.getCurrTurn()) ? 0 : 1;
		
		return table.containsKey(hashKey) && table.get(hashKey)[index] != null;
	}
	
	public int getSize() {
		return table.size();
	}
	
	public void storeEntry(Board board, int value) {
		long hashKey = hasher.generateHashKey(board.getEdges());
		int index = (Main.myColor == board.getCurrTurn()) ? 0 : 1;

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
		if (table.containsKey(hashKey)) {
			// TODO: For some strange reason, the opposing side causes many collisions,
			// but the main player does not?

			if (table.get(hashKey)[index] != null &&
					table.get(hashKey)[index].value != value) {
				
				System.out.println("Collision:\t\t" + value + "  " + table.get(hashKey)[index].value + "\t" + 
						board.toString() + "\t\t" + table.get(hashKey)[index].rep + "\t" +
						table.get(hashKey)[index].rep.equals(board.toString()) + "\t" + board.getCurrTurn().toString());
				
				
				System.exit(0);
			}
			
			table.get(hashKey)[index] = new TableEntry();
			table.get(hashKey)[index].value = value;
			table.get(hashKey)[index].rep = board.toString();
		
			if (table.get(hashKey)[0].value + table.get(hashKey)[1].value != board.getNumUncaptured()) {
				System.out.println(value + "  " + board.getCurrTurn() + "\t\t" + board.toString() + "\t " + 
						table.get(hashKey)[(index+1)%2].value + "\tEXPECTED SUM= " + board.getNumUncaptured());
				System.exit(0);
			}
			
		} else {
			TableEntry[] values = new TableEntry[2];
			values[index] = new TableEntry();
			
			values[index].value = value;
			values[index].rep = board.toString();

			table.put(hashKey, values);
			
			if (table.get(hashKey)[index].value != value) {
				System.out.println("ERROR");
				System.exit(0);
			}
		}
		
		
	}
	
	public int getEntry(Board board) {
		long hashKey = hasher.generateHashKey(board.getEdges());
		int index = (Main.myColor == board.getCurrTurn()) ? 0 : 1;
		
		return table.get(hashKey)[index].value;
	}
	
	public TableEntry getFullEntry(Board board) {
		long hashKey = hasher.generateHashKey(board.getEdges());
		int index = (Main.myColor == board.getCurrTurn()) ? 0 : 1;

		return table.get(hashKey)[index];
	}
	
	public long getHash(Board board) {
		return hasher.generateHashKey(board.getEdges());
	}
}