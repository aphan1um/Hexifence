package aiproj.hexifence.MartinAndy;

import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import aiproj.hexifence.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.awt.Point;

public class ConnectedGraph {
	public List<List<Point>> components;
	public List<Chain> chains;
	public Board board;
	
	/*
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
		
		potential_chain = false;
		
		return potential_chain;
	}
	*/
	
	/** Get a list of chains from this board.
	 */
	public List<Chain> getChains() {
		return chains;
	}
	
	/** Returns a list of 'components'. That is, a list
	 * of connected components, excluding chains in the
	 * connected component.
	 */
	public List<List<Point>> getComponents() {
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
	public ConnectedGraph(Board b) {
		this.components = new ArrayList<List<Point>>();
		this.board = b;
		this.chains = new ArrayList<Chain>();

		findComponents();
	}
	
	/** Find all connected components, and its chains for a
	 * given game board.
	 */
	private void findComponents() {
		// get list of cells which haven't been captured
		List<Point> unexplored = board.getUncapturedCells();
		
		while (!unexplored.isEmpty()) {
			Point p = unexplored.remove(0);
			Queue<Point> explore_lst = new LinkedList<Point>();

			List<Point> component = new ArrayList<Point>();
			Queue<Point> cell_open = new LinkedList<Point>();
			
			explore_lst.add(p);
			unexplored.remove(p);
			
			// search for cells in a connected component
			while (!explore_lst.isEmpty()) {
				p = explore_lst.poll();
				component.add(p);

				// if current cell has only one open edge left
				if (board.getNumOpen(p.x, p.y) == 1) {
					cell_open.add(p);
				}
				
				for (int[] dif : Board.EDGE_DIFF) {
					Point adj_c = new Point(p.x + 2*dif[0], p.y + 2*dif[1]);
					Point adj_ed = new Point(p.x + dif[0], p.y + dif[1]);
					
					if (!board.isCentreCell(adj_c.x, adj_c.y) ||
						board.isOutOfRange(adj_ed.x, adj_ed.y) ||
						board.getEdge(adj_ed.x, adj_ed.y) != Piece.EMPTY ||
						!unexplored.contains(adj_c)) {
						continue;
					}
					
					explore_lst.add(adj_c);

					// mark current cell as explored
					unexplored.remove(adj_c);
				}
			}
			
			if (!cell_open.isEmpty()) {
				chains.addAll(getChains(cell_open, board.deepCopy(true), component));
			}
			
			components.add(component);
		}
	}
	
	private static List<Chain> getChains(Queue<Point> cell_open, Board bclone,
			List<Point> component) {
		
		HashMap<Point, Chain> wait_lst = 
				new HashMap<Point, Chain>();
		
		List<Chain> final_chains = new ArrayList<Chain>();
		
		while (!cell_open.isEmpty()) {
			Chain new_chain = new Chain();
			Point chain_stop = createChain(cell_open, bclone, new_chain);
		

			if (new_chain.cells.isEmpty()) {
				continue;
			}
			
			// merge two chains, if one chain was able to extend further
			// than the other one
			for (Iterator<Entry<Point, Chain>> iterator = 
					wait_lst.entrySet().iterator();
					iterator.hasNext(); ) {
				Entry<Point, Chain> entry = iterator.next();
				int index;
				
				if ((index = new_chain.cells.indexOf(entry.getKey())) != -1) {
					new_chain.cells.addAll(index, entry.getValue().cells);
					
					if (entry.getValue().isClosed == false)
						new_chain.isClosed = false;
					
					iterator.remove();
				}
			}

			if (chain_stop != null) {
				wait_lst.put(chain_stop, new_chain);
			} else {
				final_chains.add(new_chain);
			}
		}

		
		// remaining chains that weren't able to be connected
		for (Entry<Point, Chain> entry : wait_lst.entrySet()) {
			entry.getValue().isClosed = false;
			final_chains.add(entry.getValue());
		}
		
		for (Chain c : final_chains) {
			component.removeAll(c.cells);
		}
		
		return final_chains;
	}
	
	private static Point createChain(Queue<Point> cell_open,
			Board bclone, Chain chain) {
		
		Point start = cell_open.poll();
		
		return exploreChains(start, bclone, cell_open, chain);
	}

	private static Point exploreChains(Point p, Board bclone,
			Queue<Point> cell_open,
			Chain chain) {

		Queue<Point> queue = new LinkedList<Point>();
		
		// list of cells already explored, when finding a chain
		List<Point> explored = new ArrayList<Point>();
		
		// add start cell to list (should be a cell with
		// only one edge left)
		queue.offer(p);

		while (!queue.isEmpty()) {
			Point curr_p = queue.poll();
			
			// number of edges open for curr_p
			int curr_p_open = bclone.getNumOpen(curr_p.x, curr_p.y);
			
			// if we reach a dead end, then the chain ends there
			if (curr_p_open == 0) {
				if (curr_p != p) {
					chain.cells.add(curr_p);
				}
				
				return null;
			
			// if the current cell being explored has more than open
			// edge, then we cannot capture it yet; we wait until
			// hopefully by capturing another cell with one edge left,
			// that curr_p will also lose an edge
			} else if (curr_p_open > 1) {
				return curr_p;
			}

			for (int[] dif : Board.EDGE_DIFF) {
				// get the adjacent cell, and the edge between the
				// two cells
				Point adj_c = new Point(curr_p.x + 2*dif[0], curr_p.y + 2*dif[1]);
				Point adj_ed = new Point(curr_p.x + dif[0], curr_p.y + dif[1]);

				// ensure point is in range, the edge between the two
				// cells is NOT occupied, and that it is unexplored
				if (bclone.isOutOfRange(adj_ed.x, adj_ed.y) 
					  || bclone.getEdge(adj_ed.x, adj_ed.y) != Piece.EMPTY) {
					continue;
				}
				
				// check that the adjacent cell is valid in the board,
				// or that the valid edge lies at the end of the board,
				// and that it has not been explored
				if (!bclone.isOuterEdge(adj_ed.x, adj_ed.y) &&
					  (!bclone.isCentreCell(adj_c.x, adj_c.y) || 
							  explored.contains(adj_c))) {
					continue;
				}

				explored.add(adj_c);

				// if we get here, then we found the last edge of the
				// current cell; we move to the next adjacent cell

				bclone.occupyEdge(adj_ed.x, adj_ed.y);

				queue.offer(adj_c);
				chain.cells.add(curr_p);

				// remove the cell with only one edge left into
				// cell_open (if it is in there)
				cell_open.remove(p);

				// if edge is at the end of game board, then there
				// is no point looking further
				if (bclone.isOuterEdge(adj_ed.x, adj_ed.y)) {
					chain.isClosed = false;
					return null;
				}
			}
		}
		
		// we get here if there no edges around current
		// cell to traverse through
		return null;
	}
}
