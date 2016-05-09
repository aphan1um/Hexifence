package core;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Main {
	private static final int DIM = 1;
	
	public static void main(String[] args) {
		Board b = new Board(DIM);
		
		b.occupyEdge(1, 0, true);
		b.occupyEdge(2, 0, true);
		b.occupyEdge(2, 2, true);
		b.occupyEdge(4, 1, true);
		System.out.println(b.toString());
		System.out.println(b.rotateBoard().toString());
		
		System.out.println("Performing DFS...");
		// depth_search();
	}

	/** A iterative version of depth first search.
	 */
	public static void depth_search() {
		// for the time being, assume it is our turn
		SearchTree tree = new SearchTree(new Board(DIM), true);
		Stack<SearchTree.Node> stack = new Stack<SearchTree.Node>();
		long count = 0;
		stack.push(tree.getRoot());
		
		while (!stack.isEmpty()) {
			SearchTree.Node n = stack.pop();
			// System.out.println("POPPED");
			count++;
			
			// if the board is full; terminal state
			if (n.getState().isFinished()) {
			} else {
				Board child_state = n.getState().deepCopy();
				
				for (int r = 0; r < 4*DIM - 1; r++) {
					int column_in_r = 2*DIM + r - 2*Math.max(0, r - (2*DIM - 1));
					
					for (int c = 0; c < column_in_r; c++) {
						if (child_state.occupyEdge(r, c + Math.max(0, r - (2*DIM - 1)), n.isMyTurn()) == 0) {
							stack.push(n.addChild(child_state));
							System.out.println("Added: " + child_state);
						}
						
						child_state = n.getState().deepCopy();
					}
				}
			}
		}
		
		System.out.println(count);
	}
}
