package aiproj.hexifence.MartinAndy;

import java.util.List;
import java.util.Queue;

import aiproj.hexifence.Piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.awt.Point;

public class ConnectedGraph {
	public List<Point[]> components;
	public List<Chain> chains;
	public Board board;
	private Boolean potential_chain;
	
	public ConnectedGraph(Board board) {
		this.components = new ArrayList<Point[]>();
		this.board = board;
		this.potential_chain = null;
		this.chains = new ArrayList<Chain>();
	}

	/** Check if there is a cell with 2 open edges left; then
	 * 
	 */
	private boolean possiblePotentChain() {
		// if the info about chain has been calculated previously
		if (potential_chain != null)
			return potential_chain;

		for (Point[] comp : components) {
			for (Point p : comp) {
				int count = board.getNumOpen(p.x, p.y);

				// if a cell has two open edges, then one open edge turns
				// it into a chain
				if (count == 2) {
					potential_chain = true;
					break;
				}
			}
		}
		
		return potential_chain;
	}
	
	/** Get a list of chains from this board.
	 */
	public List<Chain> getChains() {
		return chains;
	}
	
	/** Returns a list of 'components'. That is, a list
	 * of connected components, excluding chains in the
	 * connected component.
	 */
	public List<Point[]> getComponents() {
		return components;
	}
	
	/** Get how many connected components (that do not include
	 * chains) there are.
	 */
	public int getLength() {
		return components.size();
	}
	
	/** Returns a list of (strongly) connected components,
	 * where the cell's represent the vertices, and the edges
	 * between the cell's being the 'edges of the graph'.
	 */
	public static ConnectedGraph detectSCC(Board b) {
		// list of connected components of game board
		ConnectedGraph ret = new ConnectedGraph(b);
		
		// unexplored cells
		List<Point> unexplored = b.getUncapturedCells();
		
		// deep copy of the original board; we will use this
		// to find chains, by modifying the cloned board over time
		Board bclone = b.deepCopy(true);

		while (!unexplored.isEmpty()) {
			List<Point> comp = new ArrayList<Point>();
			Point p_start = unexplored.remove(0);
			
			// cells to explore, in a connected component
			comp.add(p_start);
			Queue<Point> cells_to_explore = new LinkedList<Point>();
			
			// add first cell to the connected component
			cells_to_explore.add(p_start);

			
			List<Point> cell_path = new ArrayList<Point>();
			int num_closed = 0;
			
			while (!cells_to_explore.isEmpty()) {
				Point p = cells_to_explore.poll();
				boolean is_part_chain = false;

				// cell only has one edge left => part of a chain
				if (b.getNumOpen(p.x, p.y) == 1) {
					cell_path.add(p);
				}
								
				for (int[] dif : Board.EDGE_DIFF) {
					
					Point adj_cell = new Point(p.x + 2*dif[0], p.y + 2*dif[1]);
					Point adj_edge = new Point(p.x + dif[0], p.y + dif[1]);

					// ensure point is in range, the edge between the two
					// cells is NOT occupied, and that it is unexplored
					if (!bclone.isOutOfRange(adj_edge.x, adj_edge.y) &&
						bclone.getEdge(adj_edge.x, adj_edge.y) == Piece.EMPTY
						&& unexplored.contains(adj_cell)) {
						
						int num_open = bclone.getNumOpen(
								adj_cell.x, adj_cell.y);
						
						// if the current cell has only one edge open,
						// and the next one has only 2 (so taking this edge
						// will capture the current one, and allow us to take
						// the adjacent as well)
						if (num_open == 2 &&
								bclone.getNumOpen(p.x, p.y) == 1) {
							
							bclone.occupyEdge(adj_edge.x, adj_edge.y);
							cell_path.add(p);
							
							cells_to_explore.add(adj_cell);
							
							// since current cell has only one open edge, then
							// we can skip looking around the cell further
							unexplored.remove(adj_cell);
							continue;
						
						// adjacent cell has only one edge left
						} else if (num_open == 1) {
							bclone.occupyEdge(adj_edge.x, adj_edge.y);
							
							// no need to explore adjacent cell, since the
							// current cell is the only one connected to it
							cell_path.add(adj_cell);
							num_closed++;
						} else {
							// for other cases
							cells_to_explore.add(adj_cell);
							comp.add(adj_cell);
							is_part_chain = true;
						}
						
						unexplored.remove(adj_cell);
					}
				}
				
				if (is_part_chain && !comp.contains(p)) {
					comp.add(p);
				}

				// a chain path, if one was formed
				if (cell_path.size() > 0) {
					Chain new_chain = new Chain();
					new_chain.isClosed = (num_closed == 0) ? false : true;
					new_chain.cells = cell_path.toArray(
										new Point[cell_path.size()]);
					
					ret.chains.add(new_chain);
					cell_path.clear();
				}
			}

			// add connected component to list, once we have
			// searched every uncaptured cell in it
			ret.components.add(comp.toArray(new Point[comp.size()]));
		}
		
		return ret;
	}
}
