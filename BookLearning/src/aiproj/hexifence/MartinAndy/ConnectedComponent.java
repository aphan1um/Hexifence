package aiproj.hexifence.MartinAndy;

import java.util.List;
import java.util.Queue;

import aiproj.hexifence.Piece;

import java.util.ArrayList;
import java.util.LinkedList;
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
	
	/** Returns a list of strongly connected components, where
	 * the cell's represent the vertices.
	 * <p>
	 * An edge exists between the two vertices, if the edge
	 * between them is open/not occupied.
	 * </p>
	 */
	public static List<ConnectedComponent> detectSCC(Board b) {
		List<ConnectedComponent> ret = new ArrayList<ConnectedComponent>();
		List<Point> unexplored;
		
		// cell coordinates, described in spec
		
		// get all uncaptured cells, and add all of them as
		// unexplored cells
		unexplored = b.getUncapturedCells();

		while (!unexplored.isEmpty()) {
			ConnectedComponent c = new ConnectedComponent(b);
			Queue<Point> cells_to_explore = new LinkedList<Point>();
			Point p_start = unexplored.remove(0);
			
			// add first cell to connected component
			c.cells.add(p_start);
			cells_to_explore.add(p_start);
			
			// if cell is captured, then forget about adding it as a
			// singular connected component
			if (b.getEdge(p_start.x, p_start.y) != Piece.EMPTY) {
				continue;
			}
			
			while (!cells_to_explore.isEmpty()) {
				Point p = cells_to_explore.poll();
				
				for (int[] dif : Board.EDGE_DIFF) {
					Point adj_cell = new Point(p.x + 2*dif[0], p.y + 2*dif[1]);
					
					// ensure point is in range, the edge between the two
					// cells is NOT occupied, and that it is unexplored
					if (!b.isOutOfRange(p.x + dif[0], p.y + dif[1]) &&
							b.getEdge(p.x + dif[0], p.y + dif[1]) == Piece.EMPTY
							&& unexplored.contains(adj_cell)) {
						
						cells_to_explore.add(adj_cell);
						
						c.cells.add(adj_cell);
						unexplored.remove(adj_cell);
					}
				}
			}

			ret.add(c);
		}
		
		return ret;
	}
}
