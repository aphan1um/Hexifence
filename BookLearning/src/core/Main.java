package core;

import aiproj.hexifence.Piece;

public class Main {
	// NOTE: Temporary static variables below (may be removed in future)
	
	/** Dimension of board to be used. */
	private static final int DIM = 2;
	/** Color of our agent. */
	public static final Piece myColor = Piece.BLUE;
	/** Color of player to start the game. */
	public static final Piece playerStart = Piece.BLUE;
	
	private static long count = 0;
	private static long sym_count = 0;
	private static long certain_lose_count = 0;
	
	private static TranspositionTable table = new TranspositionTable(DIM);
	
	public static void main(String[] args) {
		Board b = new Board(DIM, myColor);
		Board c = new Board(DIM, myColor);
		
		System.out.println("Performing a rotation test:");
		b.occupyEdge(0, 0, myColor);
		b.occupyEdge(1, 0, myColor);
		b.occupyEdge(3, 0, myColor);

		c.occupyEdge(0, 2, myColor);
		c.occupyEdge(1, 4, myColor);
		c.occupyEdge(2, 5, myColor);
		System.out.println(c.toBitString() + " " + c.getCurrTurn());

		System.out.println(b.isRotateSymmetric(c));

		System.out.println("\nPerforming DFS...");
		System.out.println("Minimax value of initial state: " + 
				minimax_value(new SearchTree.Node(new Board(DIM, playerStart), null)));
	}

	public static int minimax_value(SearchTree.Node n) {
		if (n.getState().isFinished()) {		// terminal state
			return n.getState().getScore();
		
		// if we reach to a state which is going to be a 100% loss,
		// no matter if we capture the remaining cells
		} else if (n.getState().getScore() < 0 && 
				n.getState().getNumUncaptured() < Math.abs(n.getState().getScore())) {
			certain_lose_count++;
			
			if (certain_lose_count % 2000 == 0) {
				System.out.println("Certain lose count: " + certain_lose_count);
			}
			
			// using constant DIM
			return -3*DIM*(DIM - 1) - 1;
		}
		
		// detect symmetry
		if (n.getState().getCurrTurn() == myColor) {
			Board sym = n.getState().isRotateSymmetric(table);
			
			if (sym != null) {
				sym_count++;
				
				if (sym_count % 40000 == 0) {
					System.out.println("Symmetry count: " + sym_count);
				}
				
				return table.getMinimax(sym.getEdges());
			}
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
		
		// we are only interested in finding the minimax value based
		// on our turn
		if (n.getState().getCurrTurn() == myColor)
			table.storeMinimax(n.getState(), value);
		
		// n.removeNode();
		
		count++;
		
		if (count % 20000 == 0) {
			System.out.println(count);
		}
		
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