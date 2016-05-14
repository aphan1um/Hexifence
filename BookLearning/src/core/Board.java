package core;

import java.util.Arrays;

import aiproj.hexifence.Piece;

public class Board {
	/** Number of edges surrounding a cell. */
	private static final int NUM_EDGES = 6;
	/** Coordinate difference between a cell centre
	 * and any of the six edges making up that cell. */
	private static final int[][] EDGE_DIFF =
		{ {0, -1}, {0, 1}, {-1, -1}, {-1, 0}, {1, 0}, {1, 1} };
	
	/** Represents the edges on the board
	 * (refer to constructor for more details). */
	private Piece[][] edges;
	
	/** Represents a cell, where its value is the number
	 * of closed cells (so if cells[a][b] = 6, then the
	 * cell would be captured).
	 */
	private int[][] cells;
	
	private int num_open_cells;

	/** Dimension of board. */
	private int dim;
	/** Difference between number of cells captured by self & enemy. */
	private int score;
	/** Number of colored edges on the board. */
	public int num_colored_edges;
	/** The current turn for this board state. */
	private Piece curr_turn;

	
	/** Create an empty Hexifence board (ie. an initial state).
	 * @param dim Dimension of board.
	 */
	public Board(int dim, Piece initTurn) {
		// ensure Piece is a player color
		assert(initTurn == Piece.BLUE || initTurn == Piece.RED);
		
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
		this.curr_turn = initTurn;
		this.num_open_cells = 3*dim*(dim - 1) + 1;
	}
	
	/** Create a board with only the 2D array's <code>edges</code>
	 * and <code>cells</code> partly initialized. 
	 */
	private Board(int dim) {
		// prepare the jagged arrays
		edges = new Piece[4*dim - 1][];
		cells = new int[2*dim - 1][];
	}
	
	/** Create a deep copy of the board.
	 * 
	 * @param alternate If the current turn of the copied board
	 * should switch.
	 */
	public Board deepCopy(boolean alternate) {
		Board copy = new Board(dim);
		
		for (int i = 0; i < copy.edges.length; i++) {
			copy.edges[i] = Arrays.copyOf(edges[i], edges[i].length);
		}
		
		for (int j = 0; j < copy.cells.length; j++) {
			copy.cells[j] = Arrays.copyOf(cells[j], cells[j].length);
		}
		
		if (alternate) {
			copy.curr_turn = (curr_turn == Piece.BLUE) ? Piece.RED : Piece.BLUE;
		}
		
		copy.dim = dim;
		copy.score = score;
		copy.num_colored_edges = num_colored_edges;
		copy.num_open_cells = num_open_cells;
		
		return copy;
	}
	
	/** Make an edge closed, caused by some player.
	 * @param r Row of edge.
	 * @param c Column of edge.
	 * @param color The player who occupied this edge.
	 * @return <code>true</code> if the board successfully
	 * registers this move.
	 * <p>
	 * <code>false</code> if the specified coordinates <code>(r, c)</code>
	 * is not a valid edge, or has already been occupied.
	 * </p>
	 */
	public boolean occupyEdge(int r, int c, Piece color) {
		// ensure the color of player is only BLUE or RED
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
		setEdge(r, c, color);
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
			
			if (c + 1 <= getMaxColumn(r, dim)) {
				decrementCell(r, c + 1, color);
			}
		}
		
		return true;
	}
	
	/** Indicate to the cell that one of its edges has been
	 * occupied/closed.
	 * 
	 * If the coordinate (r, c) is not "on or in the game
	 * board", then no action is taken.
	 */
	private void decrementCell(int r, int c, Piece color) {
		// do nothing if out or range
		if (isOutOfRange(r, c)) {
			return;
		}

		// convert (r, c) into "cell numbering" coordinates
		int r_cell = (r - 1)/2;
		int c_cell = (c - Math.max(0, r - (2*dim - 1)) - 1)/2;

		if (cells[r_cell][c_cell] != NUM_EDGES) {
			cells[r_cell][c_cell]++;

			if (cells[r_cell][c_cell] == NUM_EDGES) {
				// color centre cell as captured
				setEdge(r, c, color);
				
				// if the cell has been captured, change score based if
				// myself or enemy occupied the last edge last
				score += (Main.myColor == color) ? 1 : -1;
				
				// decrement amount of uncaptured cells
				num_open_cells--;
			}
		}
	}
	
	/** Check if the board has been completely filled.
	 * That is, the board has reached a 'terminal state'.
	 */
	public boolean isFinished() {
		// Note: the number of 'valid' edges on the board is
		// 3d(3d - 1) where d = dim
		return num_colored_edges == 3*dim*(3*dim - 1);
	}
	
	/** Get the maximum column number 'K', given a row
	 * from (r, c) such that (r, K) is the last valid
	 * edge in that row. 
	 */
	private static int getMaxColumn(int r, int dim) {
		return 2*dim + Math.min(r, 2*dim - 1) - 1;
	}
	
	/** Get score difference between self and enemy.
	 */
	public int getScore() {
		return score;
	}
	
	/** Get the current turn for this board. */
	public Piece getCurrTurn() {
		return curr_turn;
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
	
	private void setEdge(int r, int c, Piece value) {
		edges[r][c - Math.max(0, r - (2*dim - 1))] = value;
	}
	
	/** Get a 2D array of edges representing the board. */
	public Piece[][] getEdges() {
		return edges;
	}
	
	/** Retrieve the number of cells that have not been
	 * captured.
	 */
	public int getNumUncaptured() {
		return num_open_cells;
	}
	
	/** Check if the coordinate (r, c) actually represents the
	 * centre of some cell.
	 */
	private static boolean isCentreCell(int r, int c) {
		return (r % 2 == 1) && (c % 2 == 1);
	}
	
	/** Checks if (r, c) is not "on or inside the game board".
	 */
	private boolean isOutOfRange(int r, int c) {
		return (r < 0 || r >= 4*dim ||
				c - Math.max(0, r - (2*dim - 1)) < 0  ||
				c > getMaxColumn(r, dim));
	}

	/** Convert a given (r, c) coordinate into a 3D cube coordinate
	 * system.
	 * <p>
	 * Refer to: http://www.redblobgames.com/grids/hexagons/ for a
	 * more detailed explanation. Apparently, calculating distance
	 * and rotations is easier when using this 3D coordinate system.
	 * </p>
	 */
	private static int[] to3DCoord(int r, int c, int dim) {
		int[] ret = new int[3];
		
		// get difference between (r,c) and the centre of game board
		r = r - (2*dim - 1);
		c = c - (2*dim - 1);
				
		// adjust point
		c = c - r;
		
		ret[0] = c - (r - r&1)/2;
		ret[2] = r;
		ret[1] = -ret[0] - ret[2];
		
		return ret;
	}
	
	/** Convert a given cube coordinate (xx, yy, zz) back into
	 * edge coordinates for Hexifence.
	 */
	private static int[] to2DCoord(int[] cube_coord, int dim) {
		// initialize array representing (r, c)
		int[] ret = new int[2];
		
		// convert back to (r, c) coordinates
		ret[1] = cube_coord[0] + (cube_coord[2] - cube_coord[2]&1)/2;
		ret[0] = cube_coord[2];

		// adjust point to be in Hexifence coordinates
		ret[1] = ret[1] + ret[0];
		
		ret[0] += + (2*dim - 1);
		ret[1] += + (2*dim - 1);
		
		return ret;
	}
	
	/** Get the distance between a given coordinate (r, c), and
	 * the centre of the game board.
	 */
	public static int getEdgeDist(int r, int c, int dim) {
		int[] p = to3DCoord(r, c, dim);
		
		return (Math.abs(p[0]) + Math.abs(p[1]) + Math.abs(p[2]))/2;
	}
	
	/** Rotate a point <code>(r, c) (60*numRotate)</code> degrees
	 * anti-clockwise, and a reflection along a vertical line if
	 * <code>reflect</code> is <code>true</code>.
	 * 
	 * <p>
	 * Big credit to: http://gamedev.stackexchange.com/a/55493
	 * for an answer to rotating a hexagonal board.
	 * Code was edited to adjust to a different edge coordinate
	 * system Hexifence uses, and to include reflection.
	 * </p>
	 */
	public static int[] rotateEdge(int r, int c, int dim,
			int numRotate, boolean reflect) {

		// convert into 3D coordinates (xx, yy, zz)
		int[] cube_coord = to3DCoord(r, c, dim);
		int[] cube_rotate = new int[3];

		// rotate 60 degrees anti-clockwise
		int sign = (numRotate % 2 == 0) ? 1 : -1;
		cube_rotate[0] = sign * cube_coord[numRotate % 3];
		cube_rotate[1] = sign * cube_coord[(numRotate + 1) % 3];
		cube_rotate[2] = sign * cube_coord[(numRotate + 2) % 3];
		
		// if we want to reflect along a vertical line
		if (reflect) {
			int temp = cube_rotate[0];
			
			cube_rotate[0] = cube_rotate[1];
			cube_rotate[1] = temp;
		}
		
		return to2DCoord(cube_rotate, dim);
	}
	
	/** Get a rotated board by <code>(60*numRotate)</code>
	 * degrees anti-clockwise, and reflection if <code>reflect</code>
	 * is true.
	 */
	public Board rotateBoard(int numRotate, boolean reflect) {
		// if we are just rotating by 0 degrees, without reflection..
		if (numRotate % NUM_EDGES == 0 && !reflect) {
			return this;
		}

		Board b2 = new Board(dim, curr_turn);
		
		for (int i = 0; i < edges.length; i++) {
			for (int j = Math.max(0, i - (2*dim - 1));
					j <= getMaxColumn(i, dim); j++) {
				
				// only choose edges which are not empty (since 'b2'
				// initially starts with all pieces empty)
				if (getEdge(i, j) == Piece.EMPTY) {
					continue;
				}

				int[] rotated_edge = rotateEdge(i, j, dim, numRotate, reflect);
				b2.occupyEdge(rotated_edge[0], rotated_edge[1], getEdge(i, j));
			}
		}
		
		// TODO: rotate cell numbers..
		b2.cells = this.cells;

		return b2;
	}
	
	/** Compare two boards, and see if b2 is rotationally symmetric
	 * to this board.
	 * <p>
	 * We note that there are 12 different symmetries for a hexagon.
	 * These are achieved through rotations, a single reflection,
	 * or mix of both.
	 * </p>
	 * 
	 * @see <a href="http://wki.pe/Dihedral_group#Elements">Wiki article</a>
	 * for more info on how to achieve the symmetries.
	 * 
	 * @param b2 The board to compare.
	 * @return <code>true</code> if b2 can be rotated by a multiple of
	 * 60 degrees (with or without reflection) and match the corresponding
	 * edges of this board.
	 */
	public boolean isRotateSymmetric(Board b2) {
		// ensure the dimension, score, and the number of occupied
		// edges are the same
		if (b2.dim != this.dim || b2.score != this.score ||
				b2.num_colored_edges != this.num_colored_edges) {
			return false;
		}

		for (int numRotate = 0; numRotate < NUM_EDGES * 2; numRotate++) {
			if (this.equals(b2.rotateBoard(numRotate % NUM_EDGES,
					numRotate >= NUM_EDGES))) {
				return true;
			}
		}
		
		return false;
	}
	
	public Board isRotateSymmetric(TranspositionTable table) {
		for (int numRotate = 0; numRotate < NUM_EDGES * 2; numRotate++) {
			Board r = rotateBoard(numRotate % NUM_EDGES,
					numRotate >= NUM_EDGES);
			
			if (table.isStored(r)) {
				return r;
			}
		}
		
		
		return null;
	}
	
	/** Check if this board is "outer symmetric" with b2.
	 * <p>
	 * Two boards are considered "outer symmetric" if the number
	 * of "outer edges" for each cell that contains the is the same
	 * between the two boards.
	 * </p>
	 */
	private boolean isOuterSymmetric(Board b2) {
		for (int i = 1; i < edges.length; i += 2) {
			for (int j = Math.max(0, i - (2*dim - 1)) + 1;
					j < getMaxColumn(i, dim); j += 2) {
				if (this.countOuterEdges(i, j) != b2.countOuterEdges(i, j)) {
					return false;
				}

				/* 
				 * If we're NOT at the 1st or last row containing
				 * 'cell centres', then we can move to the last centre
				 * cell in that row, since the cells inbetween have
				 * no 'outer edges'.
				 */
				if ((i != 1 || i != edges.length - 2) && j == 1) {
					j = getMaxColumn(i, dim) - 3;
				}
			}
		}
		
		return true;
	}
	
	/** Count the number of "outer edges" surrounding a centre cell
	 * <code>(r, c)</code>.
	 * <p>
	 * An edge is called an "outer edge" if it is a side of ONLY
	 * one cell.
	 * </p>
	 */
	private int countOuterEdges(int r, int c) {
		int count = 0;
		
		// ensure (r, c) is a centre of some cell
		if (!isCentreCell(r, c)) {
			return count;
		}
		
		// go around each of the cell's adjacent sides
		for (int[] diff : EDGE_DIFF) {
			int edge_r = r + diff[0];
			int edge_c = c + diff[1];
			
			// the distance between an "outer edge" and the centre of
			// the board is: 2*dim - 1
			if (getEdgeDist(edge_r, edge_c, dim) == 2*dim - 1) {
				count += (getEdge(edge_r, edge_c) == Piece.EMPTY) ? 0 : 1;
			}
		}

		return count;
	}
	
	/** Check if the corresponding interior edges of this board is the
	 * same as the one from b2.
	 * <p>
	 * An edge is an "interior edge" if it is a side of two cells.
	 * </p>
	 */
	private boolean isInteriorEqual(Board b2) {
		// ensure the dimension, score, and the number of occupied
		// edges are the same
		if (b2.dim != this.dim || b2.score != this.score ||
				b2.num_colored_edges != this.num_colored_edges) {
			return false;
		}
		
		// consider only interior edges, excluding 'outer' ones
		for (int i = 1; i < b2.edges.length - 1; i++) {
			for (int j = 1; j < b2.edges[i].length - 1; j++) {
				// if two corresponding edges do not match
				if (this.edges[i][j] != b2.edges[i][j]) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Board && isInteriorEqual((Board)obj) && 
				isOuterSymmetric((Board)obj) &&
				((Board)obj).getCurrTurn() == this.getCurrTurn()) {
			return true;
		}
		
		return false;
	}
	
	/* TODO: OLD EQUAL OVERRIDE (kept in case of future use)
	
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
	
	*/
	
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
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < edges.length; i++) {
			
			for (int j = 0; j < edges[i].length; j++) {
				char c = '-';
				
				if (edges[i][j] == Piece.RED) {
					c = 'R';
				} else if (edges[i][j] == Piece.BLUE) {
					c = 'B';
				}
				
				sb.append(c);
			}
			
			sb.append(' ');
		}
		
		
		return sb.toString();
	}
}
