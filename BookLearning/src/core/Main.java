package core;

import java.util.Stack;

public class Main {
	private static final int DIM = 1;
	
	public static void main(String[] args) {
		Board b = new Board(DIM);
		
		// add sample points (to test for rotation)
		b.occupyEdge(1, 0, true);
		b.occupyEdge(2, 0, true);
		b.occupyEdge(2, 2, true);
		b.occupyEdge(4, 1, true);
		System.out.println(b.toString());
		System.out.println(b.rotateBoard().toString());
		
		System.out.println("Performing DFS...");
		// depth_search();
		System.out.println("Minimax value of initial state: " + minimax_value(new SearchTree.Node(new Board(DIM), null, false)));
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

		if (n.isMyTurn()) {		// my turn => maximise score
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
	
	/** A iterative version of depth first search.
	 */
	public static void expand_node(SearchTree.Node n) {
		// if the board is full; terminal state
		if (n.getState().isFinished()) {
			// do nothing
		} else {
			Board child_state = n.getState().deepCopy();

			for (int r = 0; r < 4 * DIM - 1; r++) {
				int column_in_r = 2 * DIM + r - 2 * Math.max(0, r - (2 * DIM - 1));

				for (int c = 0; c < column_in_r; c++) {
					if (child_state.occupyEdge(r, c + Math.max(0, r - (2 * DIM - 1)), n.isMyTurn()) == 0) {
						n.addChild(child_state);
						// System.out.println("Added: " + child_state);
					}

					child_state = n.getState().deepCopy();
				}
			}
		}
	}
}