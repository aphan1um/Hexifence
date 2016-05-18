/* 
 * COMP30024 - Project B
 * Andy Phan (aphan1) and Martin Cheong (cheongm)
 * 
 */

package aiproj.hexifence.MartinAndy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aiproj.hexifence.Move;


public class GradientLearn {
	private TranspositionTable table;

	private int dim;
	
	/** List of board samples, each of them with a minimax value. */
	public HashMap<Board, Integer> samples = new HashMap<Board, Integer>();
	
	/** Maximum number of samples to generate. If it is equal to
	 * <code>-1</code>, then there is no limit.
	 */
	private int sample_num;
	
	public GradientLearn(int dim, int sample_num) {
		this.dim = dim;
		
		this.table = new TranspositionTable(dim);
		this.sample_num = sample_num;
	}
	
	/** 
	 * Perform minimax, starting at <code>start</code>, looking
	 * all the way down to terminal state (infeasible with limited
	 * space and memory).
	 * 
	 * @param state State to begin minimax
	 * @return An integer array with two elements.
	 * <p>
	 * 1st element: Represents the minimax value of <code>start</code>,
	 * found by looking down to the terminal state(s).
	 * The utility function is the score difference between our player
	 * and the enemy.
	 * </p>
	 * <p>
	 * 2nd element: Represents how many cells that can be captured by us,
	 * if we follow the best path from <code>start</code> to the
	 * terminal state.
	 * </p>
	 */
	public int[] minimax_value(Board state) {
		// if we have exceeded number of samples to be made
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
			
			/*
			 * If we were able to find a symmetric board, then we can 
			 * infer the minimax value, if know the max. number of cells
			 * that can be captured from that symmetric state (call this
			 * value K).
			 * 
			 * So then the minimax value would be
			 * (current score diff) + K - 
			 * 		(current num of uncaptured cells - K)
			 * 
			 */
			ret[0] = state.getScoreDiff() + 
					2*possible_capt - state.getNumUncaptured();
			
			ret[1] = possible_capt;
			
			// place state in sample list, if permitted
			if (sample_num != -1 && sample_num > 0) {
				samples.put(state, ret[0]);
				sample_num--;
			}
			
			return ret;
		}

		int[] minimax = null;
		int capt_score = 0;

		int curr_capt_score;
		int[] child_minimax;

		// search through each child state
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

						// calculate the number of cells that would be
						// capture by making the move 'm'
						curr_capt_score = 
								child.getMyScore() - state.getMyScore();
						
						child_minimax = minimax_value(child);

						// our turn => maximize score
						if (state.getCurrTurn() == state.getMyColor()) {
							if (minimax == null ||
								child_minimax[0] > minimax[0]) {
								
								minimax = child_minimax;
								capt_score = curr_capt_score;
							}
							
						// enemy turn => minimize score
						} else {
							if (minimax == null ||
								child_minimax[0] < minimax[0]) {
								
								minimax = child_minimax;
								capt_score = curr_capt_score;
							}
						}
						

					}

					// create another child
					child = state.deepCopy(true);
				}
			}

			/*
			 * 		minimax[1] has the maximum optimal number of cells that
			 * 		can be captured by us/self, from child state to the
			 * 		best terminal state.
			 * 
			 * 		capt_score counts the number of cells captured by self,
			 *		due to a move that has led to a child state.
			 * 
			 *  	Thus the total number of cells captured from the CURRENT
			 *  	state is minimax[1] + capt_score.
			 *  
			 *  	If the child state was a terminal state (ie. this state
			 *  	has only one open edge left), then the child state
			 *      returns minimax[1] = 0.
			 *
			 */
			minimax[1] += capt_score;

			// store minimax[1] into table
			table.storeEntry(state, minimax[1]);
			
			// if it permits, also store the Board into samples
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
