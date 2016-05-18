package aiproj.hexifence.MartinAndy;

import java.util.List;
import java.util.Map.Entry;

import aiproj.hexifence.Piece;

public class LearningTest {
	private static final int TWO_DIM = 2;
	private static final int THREE_DIM = 3;
	private static final int SEED = 9834;

	/** Number of samples to use in our gradient descent learning */
	private static final int NUM_SAMPLES = 100000;
	/** Parameter for learning rate (how fast weights change) */
	private static final double LEARNING_RATE = 0.1;
	
	// weights for our features
	private static double W_CELL = 1, W_CHAIN = 1, W_SCORE = 1, W_SCORE_ENEMY = 1,
						  W_DUMMY = 1;
	
	// our player color
	private static final int MY_COLOR = Piece.RED;
	
	public static void main(String[] args) {
		System.out.println("First test");
		GradientLearn utilityCalc = new GradientLearn(2, NUM_SAMPLES);
		utilityCalc.minimax_value(new Board(2, MY_COLOR, Piece.BLUE));
		doTest(utilityCalc);

		System.out.println("Second test");
		utilityCalc = new GradientLearn(3, NUM_SAMPLES);
		utilityCalc.minimax_value(new Board(3, MY_COLOR, Piece.BLUE));
		doTest(utilityCalc);

	}
	
	private static void doTest(GradientLearn utilityCalc) {
		for (Entry<Board, Integer> entry : utilityCalc.samples.entrySet()) {
			Board board = entry.getKey();
			int scoreMargin = entry.getValue();
			ChainFinder chain_finder = new ChainFinder(board);

			List<Chain> chains = chain_finder.chains;
			
			double test_eval = getEval(board, chain_finder);
			double difference = test_eval - scoreMargin;
			
			// Weight update.			
			W_CHAIN = W_CHAIN - LEARNING_RATE* difference *f_getChain(board, chains);
			W_SCORE = W_SCORE - LEARNING_RATE* difference *f_getScore(board);
			W_SCORE_ENEMY = W_SCORE_ENEMY - LEARNING_RATE* difference * f_getEnemyScore(board);
			W_DUMMY = W_DUMMY - LEARNING_RATE* difference * f_getDummy(board, chain_finder);
			
			if (Double.isNaN(test_eval)) {
				System.out.println("ERROR INFINITY");
				System.exit(0);
			}
			
			System.out.println("Cell feature weight: " + W_CELL);
			System.out.println("Chain feature weight: " + W_CHAIN);
			System.out.println("Score enemy weight: " + W_SCORE_ENEMY);
			System.out.println("Score weight: " + W_SCORE);
			System.out.println("Dummy weight: " + W_DUMMY);
			
			test_eval = getEval(board, chain_finder);
			difference = test_eval - scoreMargin;
			
			System.out.println("New test eval: " + test_eval + " " + scoreMargin + "   " + board.getNumEdgesLeft());
		}
	}

	public static double getEval(Board board, ChainFinder chain_finder) {
		// System.out.println("Dim " + board.getDim() + " Cell Feature " + cellFeature + " Chain Feature " + chainFeature + " Utility: " + scoreMargin);
		
		return W_CHAIN * f_getChain(board, chain_finder.chains) +
			   W_SCORE * f_getScore(board) +
			   W_SCORE_ENEMY * f_getEnemyScore(board) +
			   W_DUMMY * f_getDummy(board, chain_finder);
		
	}
	
	public static int f_getScore(Board board) {
		return board.getScoreDiff();
	}
	
	public static int f_getChain(Board board, List<Chain> chains) {
		// Get the number of capturable chains on the board.
		int chainFeature = chains.size();

		return chainFeature;
	}
	
	public static double f_getDummy(Board board, ChainFinder chain_finder) {
		return 0;
	}
	
	// TODO: a peculiar function
	public static double f_getEnemyScore(Board board) {
		if (board.getNumEdgesLeft() % 2 == 0 && board.getCurrTurn() == board.getMyColor())
			return 1;
		
		return -1;
	}
}
