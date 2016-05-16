package core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;

public class Main {
	// NOTE: Temporary static variables below (may be removed in future)
	
	/** Dimension of board to be used. */
	private static final int DIM = 2;
	/** Color of our agent. */
	public static final int myColor = Piece.BLUE;
	/** Color of player to start the game. */
	public static final int playerStart = Piece.BLUE;
	
	private static TranspositionTable table = new TranspositionTable(DIM);
	
	private static long num_explored = 0;

	public static void main(String[] args) {
		/*
		Board b = new Board(DIM, myColor);
		
		b.occupyEdge(2, 1);
		b.occupyEdge(1, 2);
		b.occupyEdge(2, 2);
		
		System.out.println(b.toString());
		
		List<ConnectedComponent> c = b.detectSCC();
		
		System.out.println(c.get(1).findCellWithLeastOpen());
		
		System.out.println(c.size());
		*/

		System.out.println("\nPerforming DFS...");
		System.out.println("Minimax value of initial state: " + minimax_value(new Board(DIM, playerStart)));
		System.out.println("Number entries made: " + table.getSize());
		System.out.println("Number of states explored: " + num_explored);

	}

	public static int minimax_value(Board state) {
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

		// go through each child of this state
		Integer minimax = null;
		Integer ch_score = null;
		
		int minimax_child;
		int curr_ch_score;
		// System.out.println("GOT FIRST MINIMAX CHILD");

		// search through each child state with minimax
		Board child = state.deepCopy(true);
		List<Board> sym_childs = new ArrayList<Board>();
		
		for (int r = 0; r < child.getEdges().length; r++) {
			for (int c = 0; c < child.getEdges()[r].length; c++) {
				// create a move
				Move m = new Move();
				m.Row = r;
				m.Col = c + Math.max(0, r - (2 * DIM - 1));
				m.P = child.getCurrTurn();
				
				// if the edge has been occupied (or is cell centre)
				// then move onto the next possible move
				if (!child.occupyEdge(m)) {
					continue;
				}
				

				// if no cells were captured, then give turn to
				// the other player
				if (state.getNumUncaptured() == child.getNumUncaptured()) {
					child.switchTurns();
				}

				// if the child created is not symmetric to a
				// previous child that was made before
				if (!sym_childs.contains(child)) {
					sym_childs.addAll(child.getSymmetricBoards());

					// TODO: present checks
					curr_ch_score = child.getMyScore() - state.getMyScore();
					minimax_child = minimax_value(child);

					if (state.getCurrTurn() == myColor) {
						if (minimax == null ||
							minimax_child + curr_ch_score > minimax) {
							
							minimax = minimax_child;
							ch_score = curr_ch_score;
						}
					} else {
						if (minimax == null ||
							minimax_child + curr_ch_score < minimax) {
							
							minimax = minimax_child;
							ch_score = curr_ch_score;
						}
					}
					

				}

				// create another child
				child = state.deepCopy(true);
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
		 *  	a minimax value of 0, but with ch_score of 1 or 2 (or 0
		 *  	if the enemy or no-one captured).
		 *
		 */
		minimax += ch_score;

		// System.out.println("\nSTATE END: " + state.toString() + "\t\t" + state.getScore() + "\t\t" + minimax_value + "\t\t" + state.getCurrTurn());
		table.storeEntry(state, minimax);
		
		if (table.getSize() % 10000 == 0) {
			System.out.println(table.getSize() + "\t\t" + num_explored + "\t\t" + (double)num_explored/table.getSize());
		}

		return minimax;
	}
}