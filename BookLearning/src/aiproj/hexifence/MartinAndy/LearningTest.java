package aiproj.hexifence.MartinAndy;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import aiproj.hexifence.Piece;

public class LearningTest {
	private static final int TWO_DIM = 2;
	private static final int THREE_DIM = 3;
	private static final int SEED = 54322;
	
	private static final int NUM_SAMPLES = 200000;
	private static final double LEARNING_RATE = 0.1;
	
	private static double W_CELL = 1, W_CHAIN = 1, W_SCORE = 1;
	
	public static void main(String[] args) {
		Random rand = new Random(SEED);
		
		for (int i = 0; i < NUM_SAMPLES; i++) {			
			int dim = rand.nextBoolean() ? TWO_DIM : THREE_DIM;
			
			Board board = new Board(dim, rand.nextBoolean() ? Piece.BLUE : Piece.RED);
			GradientDescentLearn utilityCalc = new GradientDescentLearn(board.getDim());
			
			// Randomly generate a board for each training sample.
			for (int j = 0; j < board.getEdges().length; j++) {
				int offset = Math.max(0, j - (2*dim - 1));
				
				for (int k = 0; k < board.getEdges()[j].length; k++) {
					if (j % 2 == 1 && k % 2 == 1) {
						continue;
					}
					
					int randInt = rand.nextInt(3);
					
					int piece;
					if (randInt == 0) {
						piece = Piece.BLUE;
					} else if (randInt == 1) {
						piece = Piece.RED;
					} else {
						piece = Piece.EMPTY;
					}
					
					board.occupyEdge(j, k + offset, piece);
				}
			}
			
			// Get the score margin (the utility value).
			List<Chain> chains = new ChainFinder(board).getChains();
			int[] ret = utilityCalc.minimax_value(board);
			int scoreMargin = ret[0];
			
			double test_eval = getEval(board, chains);
			
			double difference = test_eval - scoreMargin;
			
			// Weight update.			
			W_CELL = W_CELL - LEARNING_RATE* difference *f_getCell(board);
			W_CHAIN = W_CHAIN - LEARNING_RATE* difference *f_getChain(board, chains);
			W_SCORE = W_SCORE - LEARNING_RATE* difference *f_getScore(board);
			
			if (Double.isNaN(W_CELL) || Double.isNaN(W_CHAIN)) {
				System.out.println("ERROR " + i + " " + f_getCell(board));
				System.exit(0);
			}
			
			System.out.println("Cell feature weight: " + W_CELL);
			System.out.println("Chain feature weight: " + W_CHAIN);
			
			test_eval = getEval(board, chains);
			
			difference = test_eval - scoreMargin;
			
			System.out.println("new test eval: " + test_eval + " " + ret[0]);
		}		
	}

	public static double getEval(Board board, List<Chain> chains) {
		// System.out.println("Dim " + board.getDim() + " Cell Feature " + cellFeature + " Chain Feature " + chainFeature + " Utility: " + scoreMargin);
		
		return W_CELL * f_getCell(board) + 
			   W_CHAIN * f_getChain(board, chains) +
			   W_SCORE * f_getScore(board);
		
	}
	
	public static int f_getScore(Board board) {
		return board.getMyScore();
	}
	
	// TODO: Made this zero... but it stops diverging?
	public static double f_getCell(Board board) {
		// Get the number of cells with 5 edges filled in.
		List<Point> cells = board.getUncapturedCells();
		
		int cellFeature = 0;
		for (Point cell : cells) {
			if (board.getNumOpen(cell.x, cell.y) == 1) {
				cellFeature++;
			}
		}

		// return (double)cellFeature/cells.size();
		return 0;
	}
	
	public static int f_getChain(Board board, List<Chain> chains) {
		// Get the number of capturable chains on the board.
		int chainFeature = chains.size();

		return chainFeature;
	}
}
