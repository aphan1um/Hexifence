package core;

import java.util.Stack;

import aiproj.hexifence.Piece;

public class Main {
	private static final int DIM = 1;
	public static final Piece myColor = Piece.RED;
	
	public static void main(String[] args) {
		Board b = new Board(DIM);
		
		System.out.println("Performing DFS...");
		System.out.println("Minimax value of initial state: " + 
				minimax_value(new SearchTree.Node(new Board(DIM), null, myColor)));
	}

	public static int minimax_value(SearchTree.Node n) {
		if (n.getState().isFinished()) {
			// ystem.out.println(n.getState().getScore());
			return n.getState().getScore();
		}
		
		expand_node(n);
		// assume a child will always be made (this is checked from above)
		// TODO: fix duplicity
		int value = minimax_value(n.getChildren().get(0));

		if (n.getColorTurn() == myColor) {		// my turn => maximise score
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
		
		Board child_state = n.getState().deepCopy();

		for (int r = 0; r < 4 * DIM - 1; r++) {
			for (int c = 0; c < child_state.getEdges()[r].length; c++) {
				if (child_state.occupyEdge(r, c + Math.max(0, r - (2 * DIM - 1)), n.getColorTurn())) {
					n.addChild(child_state);
				}

				child_state = n.getState().deepCopy();
			}
		}
	}
}