package core;

import aiproj.hexifence.Piece;

public class Main {
	// NOTE: Temporary static variables below (may be removed in future)
	
	/** Dimension of board to be used. */
	private static final int DIM = 2;
	/** Color of our agent. */
	public static final Piece myColor = Piece.RED;
	/** Color of player to start the game. */
	public static final Piece playerStart = Piece.BLUE;
	
	public static void main(String[] args) {
		Board b = new Board(DIM, myColor);
		Board c = new Board(DIM, myColor);
		
		System.out.println("Performing a rotation test:");
		b.occupyEdge(0, 0, myColor);
		b.occupyEdge(1, 0, myColor);
		b.occupyEdge(3, 0, myColor);

		c.occupyEdge(0, 2, myColor);
		c.occupyEdge(1, 4, myColor);
		c.occupyEdge(0, 0, myColor);
		System.out.println(c.toBitString() + " " + c.getCurrTurn());

		System.out.println(b.isRotateSymmetric(c));

		/*
		System.out.println("\nPerforming DFS...");
		System.out.println("Minimax value of initial state: " + 
				minimax_value(new SearchTree.Node(new Board(DIM, playerStart), null)));
		*/
	}

	public static int minimax_value(SearchTree.Node n) {
		if (n.getState().isFinished()) {		// terminal state
			return n.getState().getScore();
		}
		
		expand_node(n);
		// assume a child will always be made (this is checked from above)
		// TODO: fix duplicity
		int value = minimax_value(n.getChildren().get(0));

		if (n.getState().getCurrTurn() == myColor) {		// my turn => maximise score
			for (SearchTree.Node child : n.getChildren()) {
				value = Math.max(value, minimax_value(child));
			}
		} else {
			for (SearchTree.Node child : n.getChildren()) {
				value = Math.min(value, minimax_value(child));
			}
		}
		
		n.setMiniMax(value);
		
		
		return value;
	}
	
	public static void expand_node(SearchTree.Node n) {
		if (n.getState().isFinished()) {
			return;
		}
		
		Board child_state = n.getState().deepCopy(true);

		for (int r = 0; r < child_state.getEdges().length; r++) {
			for (int c = 0; c < child_state.getEdges()[r].length; c++) {
				if (child_state.occupyEdge(r, c + Math.max(0, r - (2 * DIM - 1)), child_state.getCurrTurn())) {
					n.addChild(child_state);
				}

				child_state = n.getState().deepCopy(true);
			}
		}
	}
}