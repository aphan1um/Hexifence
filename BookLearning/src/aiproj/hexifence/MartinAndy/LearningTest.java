package aiproj.hexifence.MartinAndy;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import aiproj.hexifence.Piece;

public class LearningTest {
	private static final int TWO_DIM = 2;
	private static final int THREE_DIM = 3;
	private static final int SEED = 54321;
	
	private static final int NUM_SAMPLES = 10000;
	private static final double LEARNING_RATE = 0.01;
	
	public static void main(String[] args) {
		Random rand = new Random(SEED);
		
		for (int i = 0; i < NUM_SAMPLES; i++) {
			int cellFeature = 0;
			
			int dim = rand.nextBoolean() ? TWO_DIM : THREE_DIM;
			
			Board board = new Board(dim, rand.nextBoolean() ? Piece.BLUE : Piece.RED);
			
			GradientDescentLearn utilityCalc = new GradientDescentLearn(dim);
			
			for (int j = 0; j < board.getEdges().length; j++) {
				int offset = Math.max(0, j - (2*dim - 1));
				
				for (int k = 0; k < board.getEdges()[j].length; k++) {
					if (j % 2 == 1 && k % 2 == 1) {
						continue;
					}
					
					board.occupyEdge(j, k + offset, rand.nextBoolean() ? Piece.BLUE : Piece.RED);
				}
			}
			
			int[][] edges = board.getEdges();
			
			List<Point> cells = board.getUncapturedCells();
			
			for (Point cell : cells) {
				if (board.getNumOpen(cell.x, cell.y) == 1) {
					cellFeature++;
				}
			}
			
			int[] ret = utilityCalc.minimax_value(board);
			int minimax = ret[0];
			
			System.out.println("Minimax: " + minimax + " Cell Feature: " + cellFeature);
		}
	}

}
