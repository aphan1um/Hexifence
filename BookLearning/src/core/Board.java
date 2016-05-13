package core;

import java.util.Arrays;

import aiproj.hexifence.Piece;

public class Board {
	/** Number of edges surrounding a cell. */
	private static final int NUM_EDGES = 6;
	
	/** Represents the edges on the board
	 * (refer to constructor for more details). */
	private Piece[][] edges;
	
	/** Represents a cell, where its value is the number
	 * of occupied cells (so if cells[a][b] = 6, then this
	 * cell would be captured).
	 */
	private int[][] cells;

	/** Dimension of board. */
	private int dim;
	/** Difference between number of cells captured by self & enemy. */
	private int score;
	/** Number of colored edges on the board. */
	private int num_colored_edges;

	
	/** Create an empty Hexifence board (ie. an initial state).
	 * @param dim Dimension of board.
	 */
	public Board(int dim) {
		// initialize jagged array
		edges = new Piece[4*dim - 1][];
		cells = new int[2*dim - 1][];
		
		// initialise the 2D edge array
		for (int r = 0; r < 4*dim - 1; r++) {
			
			/* For each row, only allocate enough edges, so that
			 * we only include:
			 * 
			 * 		- Valid edges.
			 * 		- Invalid edges representing a centre of a cell.
			 * 
			 * In other words, we ignore invalid which are not "on or 
			 * inside the game board".
			 */
			
			edges[r] = new Piece[2*dim + r - 2*Math.max(0, r - (2*dim - 1))];
			Arrays.fill(edges[r], Piece.EMPTY);
		}
		
		// now initialise the 2D cell array
		// Note: the number of rows with a 'cell centre' is: 2*dim - 1
		for (int j = 0; j < 2*dim - 1; j++) {
			cells[j] = new int[dim + j - 2*Math.max(0, j - (dim - 1))];
		}
		
		this.dim = dim;
		this.score = 0;
		this.num_colored_edges = 0;
	}
	
	/** Create a board with its fields initialized, except for the two
	 * 2D arrays <code>edges</code> and <code>cells</code>.
	 */
	private Board(int dim, int score, int num_colored_edges) {
		// prepare the jagged arrays
		edges = new Piece[4*dim - 1][];
		cells = new int[2*dim - 1][];
		
		this.dim = dim;
		this.score = score;
		this.num_colored_edges = num_colored_edges;
	}
	
	/** Create a deep copy of the board.
	 */
	public Board deepCopy() {
		Board copy = new Board(dim, score, num_colored_edges);
		
		for (int i = 0; i < copy.edges.length; i++) {
			copy.edges[i] = Arrays.copyOf(edges[i], edges[i].length);
		}
		
		for (int j = 0; j < copy.cells.length; j++) {
			copy.cells[j] = Arrays.copyOf(cells[j], cells[j].length);
		}
		
		return copy;
	}

	/** Check if the board has been completely filled.
	 * That is, the board has reached a 'terminal state'.
	 */
	public boolean isFinished() {
		return num_colored_edges == 3*dim*(3*dim - 1);
	}
	
	public boolean occupyEdge(int r, int c, Piece color) {
		assert(color == Piece.BLUE || color == Piece.RED);

		/* Check these three things:
		 * 
		 * 		- (r, c) doesn't represent a cell centre.
		 * 		- (r, c) is "on or inside" the game game.
		 * 		- (r, c) is an empty valid edge.
		 */
		
		if (isOutOfRange(r, c) || isCentreCell(r, c) ||
				getEdge(r, c) != Piece.EMPTY) {
			return false;
		}
		
		// set the color of edge
		edges[r][c - Math.max(0, r - (2*dim - 1))] = color;
		num_colored_edges++;
		
		// now tell cells with this edge that the edge is now 'closed'
		if (r % 2 == 0) { 	// r even => row does not contain cell centres
			
			// check if this edge is connected to cell below
			if (r + 1 < 4*dim - 1) {
				if (c % 2 == 0) {
					decrementCell(r + 1, c + 1, color);
				} else {
					decrementCell(r + 1, c, color);
				}
			}
			
			// now check above
			if (r - 1 > 0) {
				if (c % 2 == 0) {
					decrementCell(r - 1, c - 1, color);
				} else {
					decrementCell(r - 1, c, color);
				}
			}
			
			
		} else {		// r odd => row contains cell centres
			if (c - 1 >= 0) {
				decrementCell(r, c - 1, color);
			}
			
			if (c + 1 < 2*dim + r - 2*Math.max(0, r - (2*dim - 1))) {
				decrementCell(r, c + 1, color);
			}
		}
		
		return true;
	}

	/** Indicate to the cell that one of its edges has been
	 * occupied/closed.
	 */
	private void decrementCell(int r, int c, Piece color) {
		// convert (r, c) into "cell numbering" coordinates
		int r_cell = (r - 1)/2;
		int c_cell = (c - Math.max(0, r - (2*dim - 1)) - 1)/2;
		
		if (cells[r_cell][c_cell] != NUM_EDGES) {
			cells[r_cell][c_cell]++;

			if (cells[r_cell][c_cell] == NUM_EDGES) {
				// color centre cell as captured
				edges[r][c] = color;
				
				// if the cell has been captured, change score based if
				// myself or enemy occupied the last edge last
				score += (Main.myColor == color) ? 1 : -1;
			}
		}
	}
	
	/** Get score difference between self and enemy.
	 */
	public int getScore() {
		return score;
	}
	
	/** Get the status of an edge (empty, or captured)
	 * <p>
	 * If the edge is out of range, <code>null</code> is
	 * returned instead.
	 * </p>
	 */
	public Piece getEdge(int r, int c) {
		if (isOutOfRange(r, c)) {
			return null;
		}
		
		return edges[r][c - Math.max(0, r - (2*dim - 1))];
	}
	
	/** Get a 2D array of edges representing the board. */
	public Piece[][] getEdges() {
		return edges;
	}
	
	/** Check if the coordinate (r, c) actually represents the
	 * centre of some cell.
	 */
	private boolean isCentreCell(int r, int c) {
		return (r % 2 == 1) &&
				(c - Math.max(0, r - (2*dim -1)) % 2 == 1);
	}
	
	/** Checks if (r, c) is not "on or inside the game board".
	 */
	private boolean isOutOfRange(int r, int c) {
		return (r < 0 || r >= 4*dim ||
				c < Math.max(0, r - (2*dim - 1)) ||
				c - Math.max(0, r - (2*dim - 1)) >= edges[r].length);
	}
	
	/** Rotate a point (r, c) 60 degrees in a hexagonal game board.
	 * 
	 * Big credit to: http://gamedev.stackexchange.com/a/55493
	 * for an answer to rotating a hexagonal board.
	 * 
	 * Code was edited to adjust to a different edge coordinate
	 * system Hexifence uses.
	 */
	private static int[] rotateEdge(int r, int c, int dim, int numRotate) {
		// get difference between (r,c) and the centre of game board
		r = r - (2*dim - 1);
		c = c - (2*dim - 1);
		
		// adjust point to be ready for rotation
		c = c - r;

		// convert into 3D coordinates (xx, yy, zz)
		int xx = c - (r - r&1)/2;
		int zz = r;
		int yy = -xx - zz;
		
		int[] cube_coord = { xx, yy, zz };

		// rotate 60 degrees clockwise
		xx = (numRotate % 2 == 0 ? 1 : -1) * cube_coord[numRotate % 3];
		yy = (numRotate % 2 == 0 ? 1 : -1) * cube_coord[numRotate + 1 % 3];
		zz = (numRotate % 2 == 0 ? 1 : -1) * cube_coord[numRotate + 2 % 3];
		
		// convert back to (r, c) coordinates
		c = xx + (zz - zz&1)/2;
		r = zz;
		
		// adjust point
		c = c + r;

		return new int[] {r + (2*dim - 1), c + (2*dim - 1)};
	}
	
	/** Get a rotated board by <code>(60*numRotate)</code>
	 * degrees clockwise.
	 */
	public Board rotateBoard(int numRotate) {
		// if we are just rotating by 0 degrees..
		if (numRotate % NUM_EDGES == 0) {
			return this;
		}

		Board b2 = new Board(this.dim);
		
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges[i].length; j++) {
				int[] rotated_edge = rotateEdge(i, j, dim, numRotate);
				
				b2.edges[i][j] = edges[rotated_edge[0]][rotated_edge[1]];
			}
		}
		
		// TODO: rotate edge numbers..
		b2.edges = this.edges;
		b2.num_colored_edges = this.num_colored_edges;
		b2.score = b2.score;
		
		return b2;
	}
	
	public boolean isRotateSymmetric(Board b2) {
		for (int numRotate = 0; numRotate < NUM_EDGES; numRotate++) {
			if (this.equals(rotateBoard(numRotate))) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Board) {
			Board b2 = (Board)obj;
			
			// ensure the dimension, score, and the number of occupied
			// edges are the same
			if (b2.dim != this.dim || b2.score != this.score ||
					b2.num_colored_edges != this.num_colored_edges) {
				return false;
			}
			
			// check each corresponding edge between this board and obj2
			// are the same
			for (int i = 0; i < b2.edges.length; i++) {
				for (int j = 0; j < b2.edges[i].length; j++) {
					// if the two corresponding edges are not the same
					if (this.edges[i][j] != b2.edges[i][j]) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	/** Return a bit string view of the board, where <code>0</code>
	 * represents an unoccupied cell/edge, and <code>1</code> otherwise.
	 */
	public String toBitString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < edges.length; i++) {
			
			for (int j = 0; j < edges[i].length; j++) {
				sb.append((edges[i][j] == Piece.EMPTY) ? 0 : 1);
			}
			
			sb.append(' ');
		}
		
		
		return sb.toString();
	}
}
