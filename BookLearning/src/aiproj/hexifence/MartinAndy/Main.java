package aiproj.hexifence.MartinAndy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

	public static void main(String[] args) {
		Board b = new Board(DIM, myColor);
		
		b.occupyEdge(2, 1);
		b.occupyEdge(1, 2);
		b.occupyEdge(2, 2);
		
		System.out.println(b.toString());
		
		List<ConnectedComponent> c = b.detectSCC();
		
		System.out.println(c.get(1).findCellWithLeastOpen());
		System.out.println(c.size());

		GradientDescentLearn gdl = new GradientDescentLearn(DIM);

		System.out.println("\nPerforming DFS...");
		System.out.println("Minimax value of initial state: " + gdl.getInitMinimax());
	}

	
}