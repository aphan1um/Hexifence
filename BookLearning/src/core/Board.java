package core;

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
		this.edge_data = (BitSet)data.clone();
	}
	
	public Board deepCopy() {
		Board clone = new Board(dim, this.edge_data);
		clone.score = this.score;
		
		return clone;
	}
	
	// Get the bit location 
	public int getBitLocation(int r, int c) {
		c -= Math.max(0, r - (2*dim - 1));
		int ret_value;

		if (r <= 2*dim - 1) {
			ret_value = 2*dim * r + r*(r-1)/2 + c;
		} else {
			ret_value = 6*dim*(2*dim - 1);
			int diff = (4*dim - 1) - 1 - r;
			
			ret_value -= 2*dim*(diff + 1) - 1 + diff*(diff + 1)/2;
			ret_value += c;
		}
		
		return ret_value;
	}
	
	/** Check if the board has been completely filled.
	 * That is, the board has reached a 'terminal state'.
	 */
	public boolean isFinished() {
		return edge_data.cardinality() == 6*dim*(2*dim - 1) + 1;
	}
	
	/** Make a move by placing a piece on a free edge on the board.
	 * <p>
	 * If this edge is invalid AND represents the centre of a cell,
	 * then nothing is done, and <code>-1</code> is returned.
	 * </p>
	 * @param r Row number of edge.
	 * @param c Column number of edge.
	 * @param isSelf If you (the agent) is making this move.
	 * <p>
	 * If the enemy is making the move, then this should be <code>false</false>.
	 * </p>
	 * @return
	 */
	public int occupyEdge(int r, int c, boolean isSelf) {
		// if the point represents a centre cell
		if (r % 2 == 1 && (c - Math.max(0, r - (2*dim -1)) % 2 == 1)) {
			return -1;
		} else if (edge_data.get(getBitLocation(r, c))) {
			return -2;
		}
		
		// set the edge as been occupied
		edge_data.set(getBitLocation(r, c));

		// now check if the cells with this edge have been filled
		if (r % 2 == 0) { 	// r even => row does not contain cell centres
			
			// check if this edge is connected to cell below
			if (r + 1 < 4*dim - 1) {
				if (c % 2 == 0) {
					checkCell(r + 1, c + 1, isSelf);
				} else {
					checkCell(r + 1, c, isSelf);
				}
			}
			
			// now check above
			if (r - 1 > 0) {
				if (c % 2 == 0) {
					checkCell(r - 1, c - 1, isSelf);
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
	
	/** Check if the edge location has been occupied.
	 * @param r
	 * @param c
	 * @return
	 */
	public boolean isOccupied(int r, int c) {
		return edge_data.get(getBitLocation(r, c));
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
			column_read[i] = getNumValidEdges(i) + Math.max(0, i - (2*dim - 1));
		}
		
		for (int r = 0; r < 4*dim - 1; r++) {
			int columns_count = 0;
			int c = Math.max(0, r - (2*dim - 1));
			
			while (columns_count < getNumValidEdges(r)) {
				int r_rotate = p_start.x + c;
				
				column_read[r_rotate]--;
				c++;
				
				rotated_data.set(
						getBitLocation(r_rotate, column_read[r_rotate]),
						edge_data.get(index_count++));
				
				columns_count++;
			}
			
		}
		
		return new Board(dim, rotated_data);
	}
	
	/** Rotate the board clockwise by (60*numTimes) degrees.
	 * @param numTimes Number of times to rotate board clockwise.
	 * @return A board which has its edges rotated.
	 */
	public Board rotateBoard(int numTimes) {
		Board ret_board = this;
		
		for (int i = 0; i < numTimes % 6; i++) {
			ret_board = ret_board.rotateBoard();
		}
		
		return ret_board;
	}
	
	/** Retrieve the number of valid edges AND invalid edges which
	 * represent the centre of some cell, for a given row.
	 * @param r Row number on the board.
	 */
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
	
	/** Retrieve the score difference between yourself and the enemy.
	 */
	public int getScore() {
		return score;
	}
	
	public boolean equals(Object obj1) {
		if (obj1 instanceof Board) {
			return edge_data.equals(((Board)obj1).edge_data);
		}
		
		return false;
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
