package hex_test1;

import java.awt.Point;
import java.util.BitSet;

public class Board {
	// Note that what colour the edge is does not matter; it is who captures it last
	
	public BitSet edge_data;
	private int dim;
	
	private int score;
	
	public Board(int dim) {
		this.dim = dim;
		
		score = 0;
		
		edge_data = new BitSet();
	}
	
	public Board(int dim, BitSet data) {
		this(dim);
		this.edge_data = data;
	}
	
	// Get the bit location 
	public int getBitLocation(int r, int c) {
		c -= Math.max(0, r - (2*dim - 1));
		int ret_value;

		if (r == 0) {
			ret_value = c;
		} else if (r <= 2*dim - 1) {
			ret_value = 2*dim * r + r*(r-1)/2 + c;
		} else {
			ret_value = 6*dim*(2*dim - 1);
			int diff = (4*dim - 1) - 1 - r;
			
			ret_value -= 2*dim*(diff + 1) - 1 + diff*(diff + 1)/2;
			ret_value += c;
		}
		
		return ret_value;
	}
	
	public boolean isFinished() {
		return edge_data.cardinality() == getBitLocation(4*dim - 2, 2*dim - 1);
	}
	
	public int occupyEdge(int r, int c, boolean isSelf) {
		System.out.println("Occupying at (" + r + ", " + c + ").");
		edge_data.set(getBitLocation(r, c));

		// now check if the cells with this edge have been filled
		if (r % 2 == 0) { 	// r even => row does not contain cell centres
			
			if (r + 1 < 4*dim - 1) {
				if (c % 2 == 0) {
					checkCell(r + 1, c + 1, isSelf);
				} else {
					checkCell(r + 1, c, isSelf);
				}
			}
			
			if (r == 4*dim - 2 && (r - 1 >= 0 || (c != 0 && c != 2*dim + r - 1))) {
				if (c % 2 == 0) {
					checkCell(r - 1, c + 1, isSelf);
				} else {
					checkCell(r - 1, c, isSelf);
				}
			}
			
			
		} else {		// r odd => row contains cell centres
			if (c - 1 >= 0) {
				checkCell(r, c - 1, isSelf);
			}
			
			if (c + 1 < 2*dim + r - 2*Math.max(0, r - (2*dim - 1))) {
				checkCell(r, c + 1, isSelf);
			}
		}
		
		return 0;
	}
	
	/** Rotates the board state by 60 degrees clockwise.
	 */
	public Board rotateBoard() {
		System.out.println("Beginning rotation");
		
		Point p_start = new Point(0, 2*dim - 1);
		BitSet rotated_data = new BitSet();
		int index_count = 0;
		
		int[] column_read = new int[4*dim - 1];
		for (int i = 0; i < 4*dim - 1; i++) {
			column_read[i] = getNumValidEdges(i);
		}
		
		for (int r = 0; r < 4*dim - 1; r++) {
			System.out.println("Row " + r + " (count " + (2*dim + r) + " ):");
			int columns_count = 0;
			int c = 0;
			
			while (columns_count < getNumValidEdges(r)) {
				int r_rotate = p_start.x + c;
				
				column_read[r_rotate]--;
				c++;
				
				if (column_read[r_rotate] < 0) {
					System.out.println("skipped");
					continue;
				} else {
					rotated_data.set(getBitLocation(r_rotate, column_read[r_rotate]), edge_data.get(index_count++));
					columns_count++;
					
					System.out.println("(" + r_rotate + ", " + column_read[r_rotate] + ")");
				}
				
			}
			
		}
		
		return new Board(dim, rotated_data);
	}
	
	public Board rotateBoard(int numTimes) {
		Board ret_board = this;
		
		for (int i = 0; i < numTimes % 6; i++) {
			ret_board = ret_board.rotateBoard();
		}
		
		return ret_board;
	}
	
	private int getNumValidEdges(int r) {
		return 2*dim + r - 2*Math.max(0, r - (2*dim - 1));
	}
	
	private void checkCell(int r, int c, boolean isSelf) {
		// System.out.println("Checking at: " + r + ", " + c);
		
		int cell_r2 = getBitLocation(r, c);
		
		if (edge_data.get(cell_r2)) {
			return;
		}
		
		int cell_r1 = getBitLocation(r - 1, c);
		int cell_r3 = getBitLocation(r + 1, c);
		
		if (edge_data.get(cell_r2 - 1) && edge_data.get(cell_r2 + 1) &&
				edge_data.get(cell_r1) && edge_data.get(cell_r1 - 1) &&
				edge_data.get(cell_r3) && edge_data.get(cell_r3 + 1)) {
			
			edge_data.set(cell_r2);
			
			score += (isSelf) ? 1 : -1;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int index_count = 0;
		
		for (int r = 0; r < 4*dim - 1; r++) {
			for (int c = 0; c < getNumValidEdges(r); c++) {
				sb.append(edge_data.get(index_count++) ? '1' : '0');
			}
			
			sb.append(' ');
		}
		
		return sb.toString();
	}
}
