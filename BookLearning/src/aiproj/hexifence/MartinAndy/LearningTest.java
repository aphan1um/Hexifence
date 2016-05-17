package aiproj.hexifence.MartinAndy;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import aiproj.hexifence.Piece;

public class LearningTest {
	private static final int TWO_DIM = 2;
	private static final int THREE_DIM = 3;
	private static final int SEED = 54321;
	
	private static final int NUM_SAMPLES = 100;
	private static final double LEARNING_RATE = 0.1;
	
	public static void main(String[] args) {
		Random rand = new Random(SEED);
		
		double cellWeight = 0, chainWeight = 0;
		
		for (int i = 0; i < NUM_SAMPLES; i++) {			
			int dim = rand.nextBoolean() ? TWO_DIM : THREE_DIM;
			
			Board board = new Board(dim, rand.nextBoolean() ? Piece.BLUE : Piece.RED);
			
			GradientDescentLearn utilityCalc = new GradientDescentLearn(dim);
			
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
			
			// Get the number of cells with 5 edges filled in.
			int[][] edges = board.getEdges();
			
			List<Point> cells = board.getUncapturedCells();
			
			int cellFeature = 0;
			for (Point cell : cells) {
				if (board.getNumOpen(cell.x, cell.y) == 1) {
					cellFeature++;
				}
			}
			
			// Get the number of capturable chains on the board.
			List<Chain> chains = ConnectedGraph.detectSCC(board).getChains();
			int chainFeature = chains.size();
			
			// Get the score margin (the utility value).
			int[] ret = utilityCalc.minimax_value(board);
			int scoreMargin = ret[0];
			
			System.out.println("Dim " + board.getDim() + " Cell Feature " + cellFeature + " Chain Feature " + chainFeature + " Utility: " + scoreMargin);
			
			// Weight update.			
			cellWeight = cellWeight - LEARNING_RATE*(-1*cellFeature - chainFeature - scoreMargin)*cellFeature;
			chainWeight = chainWeight - LEARNING_RATE*(-1*cellFeature - chainFeature - scoreMargin)*chainFeature;
			System.out.println("Cell feature weight: " + cellWeight);
			System.out.println("Chain feature weight: " + chainWeight);
		}		
	}

}
