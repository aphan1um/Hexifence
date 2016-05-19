/* 
 * COMP30024 - Project B
 * Andy Phan (aphan1) and Martin Cheong (cheongm)
 * 
 */

package aiproj.hexifence.aphan1;

import java.awt.Point;
import java.io.PrintStream;
import java.util.List;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;
import aiproj.hexifence.Player;


public class APhan1 implements Player, Piece {
	private Board board;
	private boolean receivedIllegal = false;
	
	/** Cutoff depth for minimax and a-b search */
	private static final int CUTOFF_DEPTH = 6;
	
	private static final double W_CHAIN = 1;
	private static final double W_SCORE = 1;
	
	private Move next_move = null;
	
	@Override
	public int init(int n, int p) {
		// check we received correct parameters
		// (piece not a color, etc.)
		if (n <= 0 || (p != Piece.RED && p != Piece.BLUE)) {
			return -1;
		}
		
		// prepare board, and set color
		board = new Board(n, p);
		
		return 0;
	}

	@Override
	public Move makeMove() {
		// set color of board to our turn, if not 
		// initalized yet
		if (board.getCurrTurn() == null) {
			board.setCurrTurn(board.getMyColor());
		}

		// reset next_move
		next_move = null;
		
		// if we can find a chain (through preprocessing)
		Move pre_move = null;
		if ((pre_move = preprocess()) != null) {
			next_move = pre_move;
		} else {
			// large alpha and beta values to start minimax a-b search
			// note that we use ZobristHasherB, for the agent
			// (refer to comment.txt for more details)
			minimax(board, -1000, 1000, 0, true,
					new TranspositionTable(board.getDim(),
							new ZobristHasherB(board.getDim())));
		}
		
		// now on our end of the board, occupy this best
		// next move onto our board
		board.occupyEdge(next_move);
		
		return next_move;
	}
	
	/** For current state, if a chain exists, then we should take it.
	 * If a potential chain exists, then we choose the smallest one.
	 */
	public Move preprocess() {
		ChainFinder chainFinder = new ChainFinder(board);	// finds chains
		Chain small_pot = null;		// smallest potential chain
		
		// find smallest potential chain (to give to the
		// opponent as sacrifice)
		if (!chainFinder.potentials.isEmpty()) {
			small_pot = chainFinder.potentials.get(0);
			
			for (Chain c : chainFinder.potentials) {
				// if we found a potential chain smaller than
				// the current potential chain
				if (c.cells.size() < small_pot.cells.size()) {
					small_pot = c;
				}
			}
		}
		
		
		// if a chain exists, then we should capture it,
		// before the opponent does
		if (!chainFinder.chains.isEmpty()) {
			
			for (Chain c : chainFinder.chains) {
				// first cell in the chain
				Point first_cell = c.cells.get(0);
				
				return getFirstFreeEdge(first_cell);
			}
			
		} else if (small_pot != null) {
			// if there are no chains, but a potential chain exists
			
			// first cell in the potential chain
			Point first_cell = small_pot.cells.get(0);
			return getFirstFreeEdge(first_cell);
		}
		
		return null;
	}
	
	/** Returns any free edge, for a given 'centre cell' (ie.
	 * a point (r,c) which is invalid but represents the centre
	 * of some cell)
	 */
	private Move getFirstFreeEdge(Point centre_cell) {
		Move ret_move = null;
		
		// check chain is a centre cell (as a precaution)
		if (board.isCentreCell(centre_cell.x, centre_cell.y)) {
			
			// find the empty edge around the cell, and occupy it
			// so we can capture the cell in the current chain
			for (int[] dif : Board.EDGE_DIFF) {
				Point curr_ed = new Point(dif[0] + centre_cell.x,
						dif[1] + centre_cell.y);
				
				if (board.getEdge(curr_ed.x, curr_ed.y) ==
						Piece.EMPTY)
				{
					ret_move = new Move();
					ret_move.Row = curr_ed.x;
					ret_move.Col = curr_ed.y;
					ret_move.P = board.getCurrTurn();
					
					return ret_move;
				}
			}
		}
		
		return null;
	}

	@Override
	public int opponentMove(Move m) {
		// check if move is legal (in the board, not a
		// centre cell, and the edge is still open)
		if (board.isOutOfRange(m.Row, m.Col) ||
				board.isCentreCell(m.Row, m.Col) ||
				board.getEdge(m.Row, m.Col) != Piece.EMPTY) {
			receivedIllegal = true;
			return -1;
		}
		
		// set color of board, if not set yet
		if (board.getCurrTurn() == null) {
			board.setCurrTurn(board.getMyColor() == Piece.RED ? 
					Piece.BLUE : Piece.RED);
		}

		int prev_score = board.getEnemyScore();
		board.occupyEdge(m);
		
		// if opponent captured a cell
		if (board.getEnemyScore() - prev_score > 0) {
			return 1;
		}
		
		return 0;
	}

	@Override
	public int getWinner() {
		// illegal move performed by opponent
		if (receivedIllegal) {
			return Piece.INVALID;
		}
		
		if (board.isFinished()) {
			if (board.getScoreDiff() == 0) {		// game ended in draw
				return Piece.DEAD;
			}
			else if (board.getScoreDiff() < 0) {	// game lost
				return (board.getMyColor() == Piece.RED) ?
						Piece.BLUE : Piece.RED;
			} else {								// game won by us
				return board.getMyColor();
			}
		}

		// game not finished yet
		return Piece.EMPTY;
	}

	@Override
	public void printBoard(PrintStream output) {
		output.print(board.toString());
	}
	
	/** Perform minimax search with alpha-beta pruning.
	 * 
	 * @param a Represents the 'alpha' in the search.
	 * @param b Represents 'beta'
	 * @param depth Depth of search tree, from current state.
	 * @param max If we're looking for maximum value from the
	 * Board <code>state</code>, and vice-versa.
	 * @return
	 */
	private double minimax(Board state, double a, double b,
			int depth, boolean max, TranspositionTable table) {
		
		Board possible_sym = null;
		
		// use utility function if we reach to terminal state
		if (state.isFinished()) {		
			return state.getScoreDiff();
		} else if (depth >= CUTOFF_DEPTH) {
			return eval(state);
		// if we find a symmetric board to state, which is already
		// stored in the transposition table
		} else if ((possible_sym = state.isRotateSymmetric(table)) != null) {
			return table.getEntry(possible_sym);
		}
		
		// create a copy of board, to use as a child state
		Board child = board.deepCopy(true);

		// naively look through each possible move, based on
		// current board state
		for (int r = 0; r < child.getEdges().length; r++) {
			for (int c = 0; c < child.getEdges()[r].length; c++) {
				// create a move
				Move m = new Move();
				m.Row = r;
				m.Col = c + Math.max(0, r - (2*child.getDim() - 1));
				m.P = child.getCurrTurn();

				
				// if the edge has been occupied (or is cell centre)
				// then move onto the next possible move
				if (!child.occupyEdge(m)) {
					continue;
				}
				
				
				if (max) {		// our turn => maximize score
					double result = minimax(child, a, b, depth + 1, !max,
							table);
					
					// store symmetric states with same minimax value
					List<Board> state_sym = child.getSymmetricBoards();
					for (Board s : state_sym) {
						table.storeEntry(s, result);
					}
					
					if (result > a) {
						a = result;
						
						// store best move, if we are exploring from
						// current state
						if (depth == 0) {
							next_move = m;
						}
					}
					
					if (a >= b)
						return b;
				} else {		// opponent turn => minimize score
					b = Math.min(b, minimax(child, a, b, depth + 1, !max,
							table));
					
					if (b <= a)
						return a;
				}
				
			}
		}
		
		if (max) {
			return a;
		} else {
			return b;
		}
	}
	
	/** Our evaluation function to use for non-terminal states. */
	private double eval(Board state) {
		ChainFinder chainFinder = new ChainFinder(state);
		List<Chain> chains = chainFinder.chains;
		
		return W_CHAIN*LearningTest.f_getChain(state, chains) +
				W_SCORE*LearningTest.f_getScore(state);
	}
}
