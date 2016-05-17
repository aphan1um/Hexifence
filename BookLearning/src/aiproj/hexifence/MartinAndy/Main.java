package aiproj.hexifence.MartinAndy;

import java.util.List;

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
		
		b.occupyEdge(0, 0);
		b.occupyEdge(0, 1);
		b.occupyEdge(0, 2);
		b.occupyEdge(0, 3);
		
		b.occupyEdge(1, 0);
		
		b.occupyEdge(2, 1);
		b.occupyEdge(2, 2);
		b.occupyEdge(2, 3);
		b.occupyEdge(2, 4);
		
		ConnectedGraph c = ConnectedGraph.detectSCC(b);
		System.out.println(c.chains.get(0).cells.length);
		
		/**
		System.out.println(c.get(1).getCellWithLeastOpen());
		System.out.println(c.size());

		GradientDescentLearn gdl = new GradientDescentLearn(DIM);

		System.out.println("\nPerforming DFS...");
		System.out.println("Minimax value of initial state: " + gdl.getInitMinimax());
		**/
	}

	
}