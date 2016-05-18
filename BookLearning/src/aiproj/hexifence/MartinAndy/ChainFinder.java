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


public class ChainFinder {
	/** List of 'potential chains'. */
	public List<Chain> potentials;
	/** List of chains for a board. */
	public List<Chain> chains;
	/** The board the <code>ChainFinder</code> analysed on,
	 * in finding its chains.
	 */
	public Board board;

	/** Get a list of chains from this board.
	 */
	public List<Chain> getChains() {
		return chains;
	}
	
	/** Get a list of potential chains from this board.
	 */
	public List<Chain> getPotentials() {
		return potentials;
	}
	
	/** Returns a list of (strongly) connected components,
	 * where the cell's represent the vertices, and the edges
	 * between the cell's being the 'edges of the graph'.
	 */
	public ChainFinder(Board b) {
		this.potentials = new ArrayList<Chain>();
		this.board = b;
		this.chains = new ArrayList<Chain>();

		findComponents();
	}
	
	/** Find all connected components, chain and potential
	 * chains for a given state.
	 */
	private void findComponents() {
		// get list of cells which haven't been captured
		List<Point> unexplored = board.getUncapturedCells();
		// use a deep copy board, so that we can simulate/find
		// chains without modifying the current state
		Board bclone = board.deepCopy(true);
		
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
					
					// if cell has already been explored
					if (!unexplored.contains(adj_c)) {
						continue;
					}
					
					// adjacent cell and edge are inside the board, and
					// that the edge between them is not occupied
					if (!board.isCentreCell(adj_c.x, adj_c.y) ||
						board.isOutOfRange(adj_ed.x, adj_ed.y) ||
						board.getEdge(adj_ed.x, adj_ed.y) != Piece.EMPTY) {
						continue;
					}
					
					explore_lst.add(adj_c);

					// mark current cell as explored
					unexplored.remove(adj_c);
				}
			}
			
			// now in this connected component, find all possible
			// chains
			if (!cell_open.isEmpty()) {
				chains.addAll(getChains(cell_open, bclone, component, false));
			}
			
			findPotentChains(bclone, component, this);
		}
	}
	
	/** Find 'potential chains' for a given connected component.
	 * 
	 * @param bclone A deep copy of a given board.
	 * @param component Group of cells as a connected component.
	 * @param chain_finder <code>ChainFinder</code> class containing
	 * the component and board.
	 */
	private static void findPotentChains(Board bclone,
			List<Point> component, ChainFinder chain_finder) {

		// cells with only two edges left
		List<Point> potent_cells = new ArrayList<Point>();
		
		for (Point p : component) {
			if (bclone.getNumOpen(p.x, p.y) == 2) {
				potent_cells.add(p);
			}
		}
		
		for (Point potent : potent_cells) {
			Point curr_ed = null;
			
			// find edge to occupy on potential cell (any will do)
			for (int[] dif : Board.EDGE_DIFF) {
				curr_ed = new Point(potent.x + dif[0], potent.y + dif[1]);
				
				if (bclone.getEdge(curr_ed.x, curr_ed.y) == Piece.EMPTY) {
					break;
				}
			}
			
			// for the time being, occupy this edge, and use
			// getChains to find a chain path, if the 'potential
			// chain' became a chain
			bclone.forceSetEdge(curr_ed.x, curr_ed.y);
			
			Queue<Point> cell_open = new LinkedList<Point>();
			cell_open.add(potent);

			List<Chain> potent_chain = getChains(cell_open,
					bclone.deepCopy(true), component, true);

			// add all potential chains to list
			chain_finder.potentials.addAll(potent_chain);
			 
			bclone.forceRevertEdge(curr_ed.x, curr_ed.y);
		}
	}
	
	/** Find chains in a connected component, given cells in the
	 * list <code>cell_open</code>, containing cells with only
	 * one edge left.
	 */
	private static List<Chain> getChains(Queue<Point> cell_open, 
			Board bclone, List<Point> component, boolean isPotential) {
		
		HashMap<Point, Chain> wait_lst = 
				new HashMap<Point, Chain>();
		
		List<Chain> final_chains = new ArrayList<Chain>();
		
		while (!cell_open.isEmpty()) {
			Chain new_chain = new Chain();
			Point chain_stop = createChain(cell_open, bclone, new_chain);
			new_chain.isPotential = isPotential;

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
		
		// remove all cells in a given connected component, if
		// part of a chain (potential chains are exempt from this)
		if (!isPotential) {
			for (Chain c : final_chains) {
				component.removeAll(c.cells);
			}
		}

		return final_chains;
	}
	
	/** Create a new chain, starting from a cell with only
	 * one free edge left.
	 */
	private static Point createChain(Queue<Point> cell_open,
			Board bclone, Chain chain) {
		
		Point start = cell_open.poll();
		
		return exploreChains(start, bclone, cell_open, chain);
	}

	/** Start exploring a chain path, starting from cell
	 * centre (which has only one edge left).
	 */
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
				Point adj_c =
						new Point(curr_p.x + 2*dif[0], curr_p.y + 2*dif[1]);
				Point adj_ed =
						new Point(curr_p.x + dif[0], curr_p.y + dif[1]);

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
