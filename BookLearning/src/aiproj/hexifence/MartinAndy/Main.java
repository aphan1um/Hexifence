package aiproj.hexifence.MartinAndy;

import java.util.ArrayList;
import java.util.List;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;

public class Main {
	// NOTE: Temporary static variables below (may be removed in future)
	
	/** Dimension of board to be used (>= 1). */
	private static final int DIM = 2;
	/** Color of our agent. */
	public static final int myColor = Piece.RED;
	/** Color of player to start the game. */
	public static final int playerStart = Piece.BLUE;
	
	private static TranspositionTable table = new TranspositionTable(DIM);
	
	// statistics to keep track on minimax
	private static long num_explored = 0;
	private static int static_minimax_best = 0;
	private static int static_lowest_level = 9000;

	public static void main(String[] args) {
		System.out.println("\nPerforming DFS...");
		
		int result = minimax_value(new Board(DIM, playerStart), 0);
		
		System.out.println("Minimax value of initial state: " + result);
		System.out.println("Number entries made: " + table.getSize());
		System.out.println("Number of states explored: " + num_explored);
		System.out.println(static_lowest_level + "  " + static_minimax_best);

	}

	public static int minimax_value(Board state, int level) {
		num_explored++;

		if (state.isFinished()) {		// terminal state
			return state.getScoreDiff();
		}

		// detect symmetry
		Board sym = state.isRotateSymmetric(table);
			
		if (sym != null) {
			return table.getMinimax(sym.getEdges());
		}

		
		Integer minimax_value = null;
		int child_minimax;

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
					child_minimax = minimax_value(child, level + 1);

					if (state.getCurrTurn() == myColor) {		// our turn => maximize score
						if (minimax_value == null ||
							child_minimax > minimax_value) {
							
							minimax_value = child_minimax;
						}
					} else {								// enemy turn => minimize score
						if (minimax_value == null ||
								child_minimax < minimax_value) {
							
							minimax_value = child_minimax;
						}
					}
				}

				// create another child
				child = state.deepCopy(true);
			}
		}
		
		table.storeMinimax(state, minimax_value);
		
		if (level < static_lowest_level || (level == static_lowest_level && minimax_value > static_minimax_best)) {
			static_lowest_level = level;
			static_minimax_best = minimax_value;
		}
		
		if (table.getSize() % 10000 == 0) {
			System.out.println(table.getSize() + "\t\t" + num_explored + "\t\t" + 
		(double)num_explored/table.getSize() + "\t\t" + static_lowest_level + "  " + static_minimax_best);
		}

		return minimax_value;
	}
}