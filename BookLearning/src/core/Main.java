package core;

import java.util.LinkedList;
import java.util.Queue;

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
		System.out.println("Minimax value of initial state: " + minimax_value(new Board(DIM, playerStart)));
	}

	public static int minimax_value(Board state) {
		Queue<Board> child_states = new LinkedList<Board>();
		
		if (++count % 1000000 == 0) {
			System.out.println("Count: " + count/1000000 + " million");
		}
		
		if (state.isFinished()) {		// terminal state
			return state.getScore();
		
		// if we reach to a state which is going to be a 100% loss,
		// no matter if we capture the remaining cells
		} else if (state.getScore() < 0 && 
				state.getNumUncaptured() < Math.abs(state.getScore())) {
			certain_lose_count++;
			
			if (certain_lose_count % 50000 == 0) {
				System.out.println("Certain lose count: " + certain_lose_count);
			}
			
			// using constant DIM
			return -3*DIM*(DIM - 1) - 1;
		}
		
		// detect symmetry
		if (state.getCurrTurn() == myColor) {
			Board sym = state.isRotateSymmetric(table);
			
			if (sym != null) {
				sym_count++;
				
				if (sym_count % 500000 == 0) {
					System.out.println("Symmetry count: " + sym_count);
				}
				
				return table.getMinimax(sym.getEdges());
			}
		}
		
		expand_node(child_states, state);

		// assume a child will always be made (this is checked from above)
		// TODO: fix duplicity
		int minimax_value = minimax_value(child_states.poll());

		if (state.getCurrTurn() == myColor) {		// my turn => maximise score
			while (!child_states.isEmpty()) {
				minimax_value = Math.max(minimax_value, minimax_value(child_states.poll()));
			}
		} else {
			while (!child_states.isEmpty()) {
				minimax_value = Math.min(minimax_value, minimax_value(child_states.poll()));
			}
		}

		int value_store;
		
		// if the minimax value is worse than our current score
		// (bad result)
		if (minimax_value < state.getScore()) {
			value_store = -Math.abs(state.getScore() - minimax_value);
		} else {
			value_store = Math.abs(state.getScore() - minimax_value);
		}
		
		// we are only interested in finding the minimax value based
		// on our turn (for now..)
		if (state.getCurrTurn() == myColor) {
			// note that we are storing 'value_store', not minimax
			table.storeMinimax(state, value_store);
			
			if (table.getSize() % 50000 == 0) {
				System.out.println("Table size: " + table.getSize());
			}
		}
		
		// n.removeNode();
		
		return minimax_value;
	}
	
	public static void expand_node(Queue<Board> child_states, Board curr_state) {
		if (curr_state.isFinished()) {
			return;
		}
		
		Board child = curr_state.deepCopy(true);

		for (int r = 0; r < child.getEdges().length; r++) {
			for (int c = 0; c < child.getEdges()[r].length; c++) {
				if (child.occupyEdge(r, c + Math.max(0, r - (2 * DIM - 1)), child.getCurrTurn())) {
					child_states.add(child);
				}

				child = curr_state.deepCopy(true);
			}
		}
	}
}