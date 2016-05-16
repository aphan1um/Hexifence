package aiproj.hexifence.MartinAndy;

import java.io.PrintStream;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;
import aiproj.hexifence.Player;

// TODO: Rename this class (a mix of names?)
public class PlayerAgent implements Player, Piece {
	private Board board;
	private int myColor;
	private boolean receivedIllegal = false;
	
	@Override
	public int init(int n, int p) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Move makeMove() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int opponentMove(Move m) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWinner() {
		// illegal move performed by opponent
		if (receivedIllegal) {
			return Piece.INVALID;
		}
		
		if (board.isFinished()) {
			if (board.getScoreDiff() == 0)
				return Piece.DEAD;
			else if (board.getScoreDiff() < 0) {
				return (myColor == Piece.RED) ?
						Piece.BLUE : Piece.RED;
			} else {
				return myColor;
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
