package aiproj.hexifence.MartinAndy;

import java.io.PrintStream;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;
import aiproj.hexifence.Player;

// TODO: Rename this class (a mix of names?)
public class MartinAndyAgent implements Player, Piece {
	private Board board;
	private boolean receivedIllegal = false;
	
	/** Cutoff depth for minimax and a-b search */
	private static final int CUTOFF_DEPTH = 3;
	
	@Override
	public int init(int n, int p) {
		// check we received correct parameters
		// (piece not a color, etc.)
		if (n <= 0 || p != Piece.RED ||
				p != Piece.BLUE) {
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

		
		return null;
	}

	@Override
	public int opponentMove(Move m) {
		// check if move is legal (in the board, not a
		// centre cell, and the edge is still open)
		if (board.isOutOfRange(m.Row, m.Col) ||
				board.isCentreCell(m.Row, m.Col) ||
				board.getEdge(m.Row, m.Col) != Piece.EMPTY) {
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
	
	public Move minimax() {
		// TODO: look at child states, apply a-b pruning
		// and choose best child state/move
		
		return null;
	}
	
	/** Perform an alpha-beta pruning search.
	 * 
	 * @param a Represents the 'alpha' in the search.
	 * @param b Represents 'beta'
	 * @param depth Depth of search tree, from current state.
	 * @param max If we're looking for maximum value from the
	 * Board <code>state</code>, and vice-versa.
	 * @return
	 */
	private double alpha_beta(Board state, double a, double b,
			int depth, boolean max) {
		
		// use utility function if we reach to terminal state
		if (state.isFinished()) {		
			return state.getScoreDiff();
		} else if (depth >= CUTOFF_DEPTH) {
			return eval();
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
				m.Col = c + Math.max(0, r - (2 * child.getDim() - 1));
				m.P = child.getCurrTurn();

				// if the edge has been occupied (or is cell centre)
				// then move onto the next possible move
				if (!child.occupyEdge(m)) {
					continue;
				}
				
				if (max) {
					a = Math.max(a, alpha_beta(child, a, b, depth + 1, !max));
					
					if (a >= b)
						return b;
				} else {
					b = Math.min(b, alpha_beta(child, a, b, depth + 1, !max));
					
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
	private double eval() {
		return 0;
	}
}