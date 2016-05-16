package core;

import java.util.List;

import aiproj.hexifence.Piece;

import java.util.ArrayList;
import java.awt.Point;

public class ConnectedComponent {
	public List<Point> cells;
	public Board board;
	
	public ConnectedComponent(Board board) {
		this.cells = new ArrayList<Point>();
		this.board = board;
	}
	
	/** Return the cell in a connected component with the least
	 * amount of open edges.
	 */
	public int findCellWithLeastOpen() {
		int min_count = 6;
		
		for (Point p : cells) {
			int count = 0;
			
			for (int[] dif : Board.EDGE_DIFF) {
				if (board.getEdge(p.x + dif[0], p.y + dif[1]) == Piece.EMPTY) {
					count++;
				}
			}
			
			min_count = Math.min(min_count, count);
		}
		
		return min_count;
	}
	
	public boolean isChain() {
		return findCellWithLeastOpen() == 1;
	}
	
	public boolean isPotentialChain() {
		return findCellWithLeastOpen() == 2;
	}
	
	public int getLength() {
		return cells.size();
	}
}
