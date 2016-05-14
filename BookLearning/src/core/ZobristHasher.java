package core;

import java.util.Random;

import aiproj.hexifence.Piece;

public class ZobristHasher {
	private static final int BLUE_ROW = 0;
	private static final int RED_ROW = 1;
	private static final int NUM_COLOURS = 2;
	
	private static final int SEED = 12345; // We can randomise the seed if needed.
	
	private int[][] elementValueTable;
	
	public ZobristHasher(int dimension) {
		// Total number of edges for a game of dimension N.
		int totalEdges = 3*dimension * (3*dimension - 1);
		
		elementValueTable = new int[NUM_COLOURS][totalEdges]; // Indexed left-to-right and top-to-bottom.
		
		Random rand = new Random(SEED);
		
		for (int i = 0; i < NUM_COLOURS; i++) {
			for (int j = 0; j < totalEdges; j++){
				// Longer bit-strings => less hash collisions.
				elementValueTable[i][j] = rand.nextInt(Integer.MAX_VALUE);
			}
		}
	}
	
	public int generateHashKey(Piece[][] edges) {
		int hashKey = 0;
		int edgeIndex = 0;
		
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges[i].length; j++) {
				// if edges[i][j] is a cell centre (invalid edge)
				if (i % 2 == 1 && j % 2 == 1) {
					continue;
				}
				
				if (edges[i][j] == Piece.BLUE) {
					hashKey ^= elementValueTable[BLUE_ROW][edgeIndex];
				} else if (edges[i][j] == Piece.RED) {
					hashKey ^= elementValueTable[RED_ROW][edgeIndex];
				}
	
				edgeIndex++;
			}
		}
		
		return hashKey;
	}
	
}