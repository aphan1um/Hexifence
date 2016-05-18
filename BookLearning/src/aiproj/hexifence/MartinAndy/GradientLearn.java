package aiproj.hexifence.MartinAndy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aiproj.hexifence.Move;

public class GradientLearn {
	private TranspositionTable table;

	private int dim;
	
	public HashMap<Board, Integer> samples = new HashMap<Board, Integer>();
	
	private int sample_num;
	
	public GradientLearn(int dim, int sample_num) {
		this.dim = dim;
		
		this.table = new TranspositionTable(dim);
		this.sample_num = sample_num;
	}
	
	/** Perform minimax, starting at <code>start</code>.
	 * 
	 * @param state State to begin minimax
	 * @return An integer array of two elements.
	 * <p>
	 * The first element represents the minimax value, found
	 * by looking to the terminal state(s). The utility function
	 * is the score difference between our player, and the
	 * enemy.
	 * </p>
	 * <p>
	 * The second element tells how much cells that can be
	 * captured by us, if we follow the best path to the terminal
	 * state (from minimax search)
	 * </p>
	 */
	public int[] minimax_value(Board state) {
		if (sample_num != -1 && sample_num == 0) {
			return null;
		}

		int[] ret = new int[2]; // index 0 = minimax value
								// index 1 = capture value
		
		if (state.isFinished()) {		// terminal state
			ret[0] = state.getScoreDiff();
			ret[1] = 0;
			
			return ret;
		}

		// detect symmetry
		Board sym = state.isRotateSymmetric(table);
			
		if (sym != null) {
			int possible_capt = table.getEntry(sym);
			
			ret[0] = state.getScoreDiff() + 
					2*possible_capt - state.getNumUncaptured();
			
			ret[1] = possible_capt;
			
			if (sample_num != -1 && sample_num > 0) {
				samples.put(state, ret[0]);
				sample_num--;
			}
			
			return ret;
		}

		// go through each child of this state
		int[] minimax = null;
		int capt_score = 0;

		int curr_ch_score;
		int[] child_minimax;

		// search through each child state with minimax
		Board child = state.deepCopy(true);
		List<Board> sym_childs = new ArrayList<Board>();
		
		try {
			// naively look through each possible move, based on
			// current board state
			for (int r = 0; r < child.getEdges().length; r++) {
				for (int c = 0; c < child.getEdges()[r].length; c++) {
					// create a move
					Move m = new Move();
					m.Row = r;
					m.Col = c + Math.max(0, r - (2*dim - 1));
					m.P = child.getCurrTurn();
					
					// if the edge has been occupied (or is cell centre)
					// then move onto the next possible move
					if (!child.occupyEdge(m)) {
						continue;
					}

					// if the child created is not symmetric to a
					// previous child that was made before
					if (!sym_childs.contains(child)) {
						sym_childs.addAll(child.getSymmetricBoards());

						// TODO: present checks
						curr_ch_score = child.getMyScore() - state.getMyScore();
						child_minimax = minimax_value(child);

						if (state.getCurrTurn() == state.getMyColor()) {
							if (minimax == null ||
								child_minimax[0] > minimax[0]) {
								
								minimax = child_minimax;
								capt_score = curr_ch_score;
							}
						} else {
							if (minimax == null ||
								child_minimax[0] < minimax[0]) {
								
								minimax = child_minimax;
								capt_score = curr_ch_score;
							}
						}
						

					}

					// create another child
					child = state.deepCopy(true);
				}
			}

			/*
			 * If current turn is self:
			 * 		minimax[1] has the maximum optimal number of cells that
			 * 		can be captured by self, STARTING from the child state
			 * 		with this minimax value.
			 * 
			 * 		capt_score counts the number of cells captured by self,
			 *		due to a move BEFORE reaching to the child state.
			 * 
			 *  	Thus the total number of cells captured FROM this state
			 *  	is minimax[1] + capt_score.
			 *  
			 *  	If the child state was a terminal state (ie. this state
			 *  	has only one open edge left), then the child state returns
			 *  	minimax[1] = 0.
			 *
			 */
			minimax[1] += capt_score;

			table.storeEntry(state, minimax[1]);
			
			if (sample_num != -1 && sample_num > 0) {
				samples.put(state, minimax[0]);
				sample_num--;
			}

		} catch (Exception e) {
			return null;
		}
		
		return minimax;
	}
}
