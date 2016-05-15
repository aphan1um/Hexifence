package core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import aiproj.hexifence.Piece;

public class Main {
	// NOTE: Temporary static variables below (may be removed in future)
	
	/** Dimension of board to be used. */
	private static final int DIM = 2;
	/** Color of our agent. */
	public static final Piece myColor = Piece.RED;
	/** Color of player to start the game. */
	public static final Piece playerStart = Piece.BLUE;
	
	private static TranspositionTable table = new TranspositionTable(DIM);
	
	private static long num_explored = 0;

	public static void main(String[] args) {
		System.out.println("\nPerforming DFS...");
		System.out.println("Minimax value of initial state: " + minimax_value(new Board(DIM, playerStart)));
		System.out.println("Number entries made: " + table.getSize());
		System.out.println("Number of states explored: " + num_explored);
	}

	public static int minimax_value(Board state) {
		Queue<Board> child_states = new LinkedList<Board>();
		Queue<Integer> child_score_ch = new LinkedList<Integer>();

		num_explored++;
		
		if (state.isFinished()) {		// terminal state
			// System.out.println("TERMINAL: " + state.toBitString() + "\t\t" + state.getCurrTurn() + "\t" + state.getScore());
			return 0;
		}
		
		// System.out.println("STATE START: " + state.toString() + "\t\t" + state.getCurrTurn());

		// detect symmetry
		Board sym = state.isRotateSymmetric(table);
			
		if (sym != null) {
			/*
			System.out.println("SYMMETRY: " + state.toString() + "\t " + table.getFullEntry(sym).rep +
					"\t" + (state.toString().equals(sym.toString())) + "\t\t" + state.getCurrTurn() 
					+ "\t HASH = " + table.getHash(sym) + "\t" + state.getScore() + "\t" + 
					table.getEntry(sym));
			*/
			
			
			return table.getEntry(sym);
		}
		
		// get child states
		expand_node(child_states, child_score_ch, state);

		// go through each child of this state
		int minimax_value = minimax_value(child_states.poll());
		int ch_score = child_score_ch.poll();
		// System.out.println("GOT FIRST MINIMAX CHILD");

		// search through each child state with minimax
		if (state.getCurrTurn() == myColor) {		// my turn => maximise score
			while (!child_states.isEmpty()) {
				int new_value = minimax_value(child_states.poll());
				int new_ch_score = child_score_ch.poll();
				
				if (new_value + new_ch_score > minimax_value) {
					minimax_value = new_value;
					ch_score = new_ch_score;
				}
			}
		} else {									// enemy's turn => minimise score
			while (!child_states.isEmpty()) {
				int new_value = minimax_value(child_states.poll());
				int new_ch_score = child_score_ch.poll();
				
				if (new_value + new_ch_score < minimax_value) {
					minimax_value = new_value;
					ch_score = new_ch_score;
				}
			}
		}

		/*
		 * If current turn is self:
		 * 		minimax_value has the maximum optimal number of cells that
		 * 		can be captured, STARTING from the child state with this
		 * 		minimax value.
		 * 
		 * 		ch_score considers if a cell was captured (by self) when
		 * 		ENTERING this child state.
		 * 
		 *  	Thus the total number of cells captured FROM this state
		 *  	is minimax_value + ch_score.
		 *  
		 *  	If the child state was a terminal state (ie. this state
		 *  	has only one open edge left), then the child state returns
		 *  	a minimax value of 0, but with ch_score of 1 or 2.
		 *
		 */
		minimax_value += ch_score;

		// System.out.println("\nSTATE END: " + state.toString() + "\t\t" + state.getScore() + "\t\t" + minimax_value + "\t\t" + state.getCurrTurn());
		table.storeEntry(state, minimax_value);
		
		if (table.getSize() % 10000 == 0) {
			System.out.println(table.getSize() + "\t\t" + num_explored + "\t\t" + (double)num_explored/table.getSize());
		}

		return minimax_value;
	}
	
	public static void expand_node(Queue<Board> child_states, Queue<Integer> child_score_ch, Board curr_state) {
		// terminal state => no child nodes to expand
		if (curr_state.isFinished()) {
			return;
		}
		
		//System.out.println("max number of child nodes : " + curr_state.num_edges_left);
		
		Board child = curr_state.deepCopy(true);
		List<Board> sym_childs = new ArrayList<Board>();
		
		// capture previous data before making a child move
		int num_cells_open = curr_state.getNumUncaptured();
		int prev_score = curr_state.getScore();
		
		for (int r = 0; r < child.getEdges().length; r++) {
			for (int c = 0; c < child.getEdges()[r].length; c++) {
				if (child.occupyEdge(r, c + Math.max(0, r - (2 * DIM - 1)))) {
					// if no cells were captured, then give turn to the other player
					if (num_cells_open == child.getNumUncaptured()) {
						child.setCurrTurn(child.getCurrTurn() == 
								Piece.RED ? Piece.BLUE : Piece.RED);
					}

					// if the child created is not symmetric to a previous child
					// that was made before
					if (!sym_childs.contains(child)) {
						sym_childs.addAll(child.getSymmetricBoards());
						
						// TODO: present checks
						child_score_ch.add(child.getScore() - prev_score);
						
						/*
						System.out.println("CHILD MADE: " + child.toString() + "\tMade by: " + curr_state.toString()
									+ "ch score = " + (child.getScore() - prev_score));
						*/
						
						child_states.add(child);
					}
				}

				// create another child
				child = curr_state.deepCopy(true);
			}
		}
	}
}