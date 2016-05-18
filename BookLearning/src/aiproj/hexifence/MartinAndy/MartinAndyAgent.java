package aiproj.hexifence.MartinAndy;

import java.io.PrintStream;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;
import aiproj.hexifence.Player;

// TODO: Rename this class (a mix of names?)
public class MartinAndyAgent implements Player, Piece {
	private Board board;
	private boolean receivedIllegal = false;
	
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
		
		// if not, then switch turn to us
		board.switchTurns();
		
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

}
