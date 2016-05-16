package core;

import java.io.PrintStream;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;
import aiproj.hexifence.Player;

// TODO: Rename this class (a mix of names?)
public class PlayerAgent implements Player, Piece {
	private Board board;
	
	
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void printBoard(PrintStream output) {
		output.print(board.toString());
	}

}
