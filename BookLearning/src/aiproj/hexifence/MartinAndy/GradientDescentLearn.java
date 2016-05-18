package aiproj.hexifence.MartinAndy;

import java.util.ArrayList;
import java.util.List;

import aiproj.hexifence.Move;

public class GradientDescentLearn {
	private TranspositionTable table;
	
	private long num_explored = 0;
	
	private int dim;
	
	public GradientDescentLearn(int dim) {
		this.dim = dim;
		
		this.table = new TranspositionTable(dim);
	}
	
	public int getInitMinimax(Board state) {
		int[] result = minimax_value(state);
		
		return result[0];
	}
	
	
	public int[] minimax_value(Board state) {
		num_explored++;
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
			
			ret[0] = state.getScoreDiff() + 2*possible_capt - state.getNumUncaptured();
			ret[1] = possible_capt;
			
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
					child_minimax = minimax_value(child);

					if (state.getCurrTurn() == Main.myColor) {
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

		// System.out.println("\nSTATE END: " + state.toString() + "\t\t" + state.getScore() + "\t\t" + minimax_value + "\t\t" + state.getCurrTurn());
		table.storeEntry(state, minimax[1]);
		
		if (table.getSize() % 10000 == 0) {	
			System.out.println(table.getSize() + "\t\t" + num_explored);
		}

		return minimax;
	}
	/* TODO: Finish this
	private void preprocess_step(Board b) {
		// ensure preprocess step happens at our turn
		if (b.getCurrTurn() != Main.myColor)
			return;
		
		List<ConnectedComponent> chains = ConnectedComponent.detectSCC(b, true);
		
		// if there chains, then there is nothing to do
		if (chains.size() == 0)
			return;

		List<ConnectedComponent> comps = ConnectedComponent.detectSCC(b, false);
		List<ConnectedComponent> potent_chain = new ArrayList<ConnectedComponent>();
		
		// find all potential chains and add it to filtered list
		for (ConnectedComponent c : comps) {
			if (c.isPotentialChain())
				potent_chain.add(c);
		}
		
		// largest chain is half open
		if (chains.get(chains.size() - 1).isHalfOpen()) {
			// if a potential chain exists, then choose the smallest
			// potential chain
			if (potent_chain.size() > 0) {
				
			} else if (comps.size() > 0) {
				
			}
			
		} else {
			// closed chain
			
		}
	}
	*/
}
