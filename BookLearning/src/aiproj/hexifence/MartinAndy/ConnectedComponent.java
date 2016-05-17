package aiproj.hexifence.MartinAndy;

import java.util.List;
import java.util.Queue;

import aiproj.hexifence.Piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.awt.Point;

public class ConnectedComponent implements Comparable<ConnectedComponent> {
	public List<Point> cells;
	public Board board;
	private int[] chain_data;
	
	public ConnectedComponent(Board board) {
		this.cells = new ArrayList<Point>();
		this.board = board;
		this.chain_data = null;
	}
	
	/** Prepare/return information about the connected component.
	 */
	private int[] prepare_data() {
		// if the info about chain has been calculated previously
		if (chain_data != null)
			return chain_data;
		
		int min_count = 6;
		int num_open = 0;	// number of cells in the component,
							// with only one open edge left
		
		for (Point p : cells) {
			int count = board.getNumOpen(p.x, p.y);
			min_count = Math.min(min_count, count);
			
			if (count == 1)
				num_open++;
		}
		
		int[] ret = new int[2];
		ret[0] = min_count;
		ret[1] = num_open;
		
		// save data calculated into chain_data, so that
		// we do not need to calculate again
		chain_data = ret;
		
		return ret;
	}
	
	public boolean isChain() {
		return prepare_data()[0] == 1;
	}
	
	public boolean isPotentialChain() {
		return prepare_data()[0] == 2;
	}
	
	public int getCellWithLeastOpen() {
		return prepare_data()[0];
	}
	
	/** Check if the chain is half-open (one end of the chain's
	 * edge is open). If it returns <code>false</code>, then
	 * the chain has both ends closed.
	 * <p>
	 * Before calling this, you should call <code>isChain()</code>
	 * to check if this component is really a chain.
	 * </p>
	 */
	public boolean isHalfOpen() {
		return prepare_data()[0] == 1;
	}
	
	/** Get how many cells are in this connected component.
	 */
	public int getLength() {
		return cells.size();
	}
	
	/** Returns a list of (strongly) connected components,
	 * where the cell's represent the vertices.
	 * <p>
	 * An edge exists between the two vertices, if the edge
	 * between them is open/not occupied.
	 * </p>
	 * @param If the list of components should only contain chains.
	 */
	public static List<ConnectedComponent> detectSCC(Board b, boolean chainsOnly) {
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

			// if chainsOnly is true, then check if current
			// component is a chain
			if (!chainsOnly || c.isChain()) {
				ret.add(c);
			}
		}
		
		// sort the list, based on length
		Collections.sort(ret);
		
		return ret;
	}

	@Override
	public int compareTo(ConnectedComponent arg0) {
		if (this.getLength() < arg0.getLength())
			return -1;
		else
			return 1;
	}
}
