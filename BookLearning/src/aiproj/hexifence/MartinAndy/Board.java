package aiproj.hexifence.MartinAndy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;

public class Board {
	/** Number of edges surrounding a cell. */
	public static final int NUM_EDGES = 6;
	/** Coordinate difference between a cell centre
	 * and any of the six edges making up that cell. */
	public static final int[][] EDGE_DIFF =
		{ {0, -1}, {0, 1}, {-1, -1}, {-1, 0}, {1, 0}, {1, 1} };
	
	/** Represents the edges on the board
	 * (refer to constructor for more details). */
	private int[][] edges;
	
	/** Number of uncaptured cells left. */
	private int num_uncaptured;
	/** Number of cells captured by self. */
	private int my_score;
	/** Number of cells captured by enemy. */
	private int enemy_score;
	
	/** Dimension of board. */
	private int dim;
	/** Number of valid open edges left on the board. */
	public int num_edges_left;
	/** The current turn for this board state (initially
	 * null, until we are told it his/her turn). */
	private Integer curr_turn;
	
	/** The color of the main player (our agent) */
	private int my_color;


	/** Create an empty Hexifence board (ie. an initial state).
	 * @param dim Dimension of board.
	 * @param shallow_init If true, then only initalize the edges;
	 * everything else is left uninitalized.
	 */
	public Board(int dim, int my_color) {
		// initialize jagged array
		edges = new int[4*dim - 1][];
		
		// initialise the 2D edge array
		for (int r = 0; r < 4*dim - 1; r++) {
			
			/* For each row, only allocate enough edges (to save space),
			 * so that we only include:
			 * 
			 * 		- Valid edges.
			 * 		- Invalid edges representing a centre of a cell.
			 * 
			 * In other words, we ignore invalid which are not "on or 
			 * inside the game board".
			 */
			
			edges[r] = new int[2*dim + r - 2*Math.max(0, r - (2*dim - 1))];
			Arrays.fill(edges[r], Piece.EMPTY);
		}

		this.dim = dim;
		this.my_score = 0;
		this.enemy_score = 0;
		this.num_edges_left = 3*dim*(3*dim - 1);
		this.num_uncaptured = 3*dim*(dim - 1) + 1;
		this.curr_turn = null;
		this.my_color = my_color;
	}
	
	/** Private constructor that only initalizes the edges array.
	 */
	private Board(int dim) {
		// initialize jagged array
		edges = new int[4*dim - 1][];
	}
	
	/** Initalize the board, if we have a player number prepared.
	 */
	public Board(int dim, int my_color, int curr_turn) {
		this(dim, my_color);
		assert(curr_turn == Piece.RED || curr_turn == Piece.BLUE);
		this.curr_turn = curr_turn;
	}
	
	/** Create a deep copy of the board.
	 * 
	 * @param alternate If the current turn of the copied board
	 * should switch.
	 */
	public Board deepCopy(boolean copyArrays) {
		Board copy = new Board(dim);
		
		if (copyArrays) {
			for (int i = 0; i < copy.edges.length; i++) {
				copy.edges[i] = Arrays.copyOf(edges[i], edges[i].length);
			}
		} else {
			// initialise the 2D edge array
			for (int r = 0; r < 4*dim - 1; r++) {
				copy.edges[r] = 
						new int[2*dim + r - 2*Math.max(0, r - (2*dim - 1))];
				Arrays.fill(copy.edges[r], Piece.EMPTY);
			}
		}
		
		copy.curr_turn = curr_turn;
		copy.dim = dim;
		copy.my_score = my_score;
		copy.enemy_score = enemy_score;
		copy.num_edges_left = num_edges_left;
		copy.num_uncaptured = num_uncaptured;
		copy.my_color = my_color;
		
		return copy;
	}
	
	/** Make an edge closed, caused by some player.
	 * <p>
	 * If a player does not a cell, as a result of calling
	 * this function, then the current turn for this board is
	 * switched.
	 * </p>
	 * @param r Row of edge.
	 * @param c Column of edge.
	 * @param color The player who occupied this edge.
	 * @return <code>true</code> if the board successfully
	 * registers this move.
	 * <p>
	 * <code>false</code> if the specified coordinates
	 * <code>(r, c)</code> is not a valid edge, or has
	 * already been occupied. In this case, occupyEdge does
	 * nothing.
	 * </p>
	 */
	public boolean occupyEdge(int r, int c, int color) {
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
		this.num_edges_left--;
		
		// keep track if any cell was captured
		boolean captured = false;
		
		// now tell cells with this edge that the edge is now 'closed',
		// based on its position
		if (r % 2 == 0) { 	// r even => row does not contain cell centres
			
			// check if this edge is connected to cell below
			if (r + 1 < 4*dim - 1) {
				if (c % 2 == 0) {
					captured |= decrementCell(r + 1, c + 1, color);
				} else {
					captured |= decrementCell(r + 1, c, color);
				}
			}
			
			// now check above
			if (r - 1 > 0) {
				if (c % 2 == 0) {
					captured |= decrementCell(r - 1, c - 1, color);
				} else {
					captured |= decrementCell(r - 1, c, color);
				}
			}
			
			
		} else {		// r odd => row contains cell centres
			if (c - 1 >= 0) {
				captured |= decrementCell(r, c - 1, color);
			}
			
			if (c + 1 <= getMaxColumn(r, dim)) {
				captured |= decrementCell(r, c + 1, color);
			}
		}
		
		// if no cell was captured, then switch turns;
		// otherwise keep
		if (!captured) {
			switchTurns();
		}
		
		return true;
	}
	
	/** Occupy an edge, based on the Move class data.
	 */
	public boolean occupyEdge(Move move) {
		return occupyEdge(move.Row, move.Col, move.P);
	}
	
	/** Occupy an edge at the edge location (row, column)
	 */
	public boolean occupyEdge(int r, int c) {
		return occupyEdge(r, c, curr_turn);
	}
	
	/** Get the our agent's color for this game.
	 */
	public int getMyColor() {
		return my_color;
	}
	
	/** Indicate to the cell that one of its edges has been
	 * occupied/closed.
	 * <p>
	 * If the coordinate (r, c) is not "on or in the game
	 * board", then <code>false</code> is returned.
	 * </p>
	 * @return <code>true</code> if a cell got captured.
	 */
	private boolean decrementCell(int r, int c, int color) {
		// do nothing if out or range
		if (!isCentreCell(r, c)) {
			return false;
		}

		// all of cell's edges occupied  ==>  cell captured
		if (getNumOpen(r, c) == 0) {
			// color centre cell as captured
			setEdge(r, c, color);

			if (color == this.my_color) {
				my_score++;
			} else {
				enemy_score++;
			}

			// decrement amount of uncaptured cells
			num_uncaptured--;
			
			return true;
		}

		return false;
	}
	
	/** Check if the coordinate (r, c) actually represents the
	 * centre of some cell.
	 */
	public boolean isCentreCell(int r, int c) {
		return !isOutOfRange(r, c) && (r % 2 == 1) && (c % 2 == 1);
	}
	
	/** Get the number of edges open around a cell, centred
	 * at (r, c). This includes 'inner edges' and
	 * 'outer edges'
	 * @return Number of open edges around the cell centre.
	 * If (r, c) is not a centre of cell, then it returns
	 * <code>-1</code> instead.
	 */
	public int getNumOpen(int r, int c) {
		// return negative value if (r,c) does not represent
		// the centre of a cell
		if (!isCentreCell(r, c)) {
			return -1;
		}
		
		int num_open = 0;
		
		// count number of edges around cell which are open
		for (int[] diff : EDGE_DIFF) {
			if (getEdge(r + diff[0], c + diff[1]) == Piece.EMPTY) {
				num_open++;
			}
		}
		
		return num_open;
	}
	
	/** Get a list of uncaptured cells in edge coordinates.
	 */
	public List<Point> getUncapturedCells() {
		List<Point> uncapt_cells = new ArrayList<Point>();
		
		// go through each cell in the board, in cell
		// coordinates
		for (int i = 0; i < 2*dim - 1; i++) {
			int offset = Math.max(0, i - (dim - 1));
			int num_cells = dim + i - 2*offset;
			
			for (int j = offset; j < offset + num_cells; j++) {
				// convert into edge coordinates
				int r_edge = 2*i + 1;
				int c_edge = 2*j + 1;
				Point p = new Point(r_edge, c_edge);
				
				// if cell is not captured, then add to list
				if (getEdge(r_edge, c_edge) == Piece.EMPTY)
					uncapt_cells.add(p);
			}
		}
		
		return uncapt_cells;
	}
	
	/** Check if the board has been completely filled.
	 * That is, the board has reached a 'terminal state'.
	 */
	public boolean isFinished() {
		return num_edges_left == 0;
	}
	
	public int getNumEdgesLeft() {
		return num_edges_left;
	}
	
	/** Get the maximum column number 'K', given a row
	 * from (r, c) such that (r, K) is the last valid
	 * edge in that row. 
	 */
	private static int getMaxColumn(int r, int dim) {
		return 2*dim + Math.min(r, 2*dim - 1) - 1;
	}
	
	/** Get number of cells captured by self.
	 */
	public int getMyScore() {
		return my_score;
	}
	
	/** Get number of cells captured by opponent.
	 */
	public int getEnemyScore() {
		return enemy_score;
	}
	
	/** Get the current turn for this board. */
	public Integer getCurrTurn() {
		return curr_turn;
	}
	
	/** Set the current turn for this board.
	 */
	public void setCurrTurn(Integer player) {
		this.curr_turn = player;
	}
	
	/** Get the score difference between self and enemy.
	 * Higher value means better result.
	 */
	public int getScoreDiff() {
		return my_score - enemy_score;
	}
	
	/** Switch the turn to the next player.
	 */
	private void switchTurns() {
		curr_turn = (curr_turn == Piece.RED) ?
				Piece.BLUE : Piece.RED;
	}
	
	/** Get the status of an edge (empty, or captured)
	 * <p>
	 * If the edge is out of range, an exception is thrown.
	 * </p>
	 */
	public int getEdge(int r, int c) {
		// in the case the edge is not even defined
		// 'in the range'
		if (isOutOfRange(r, c)) {
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return edges[r][c - Math.max(0, r - (2*dim - 1))];
	}
	
	public void forceSetEdge(int r, int c) {
		if (isOutOfRange(r, c)) {
			return;
		}
		
		edges[r][c - Math.max(0, r - (2*dim - 1))] = curr_turn;
	}
	
	public void forceRevertEdge(int r, int c) {
		if (isOutOfRange(r, c)) {
			return;
		}
		
		edges[r][c - Math.max(0, r - (2*dim - 1))] = Piece.EMPTY;
	}
	
	private void setEdge(int r, int c, int value) {
		edges[r][c - Math.max(0, r - (2*dim - 1))] = value;
	}
	
	/** Get a 2D array of edges representing the board. */
	public int[][] getEdges() {
		return edges;
	}
	
	/** Retrieve the number of cells that have not been
	 * captured.
	 */
	public int getNumUncaptured() {
		return num_uncaptured;
	}
	
	/** Checks if (r, c) is not "on or inside the game board".
	 */
	public boolean isOutOfRange(int r, int c) {
		return (r < 0 || r >= 4*dim - 1 ||
				c - Math.max(0, r - (2*dim - 1)) < 0  ||
				c > getMaxColumn(r, dim));
	}
	
	/** Retrieve the dimension of board.
	 */
	public int getDim() {
		return dim;
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
		for (int i = 0; i < 3; i++) {
			cube_rotate[i] = sign * cube_coord[(numRotate + i) % 3];
		}

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
			return this.deepCopy(true);
		}

		Board b2 = this.deepCopy(false);
		
		for (int i = 0; i < edges.length; i++) {
			for (int j = Math.max(0, i - (2*dim - 1));
					j <= getMaxColumn(i, dim); j++) {

				// only choose edges which are not empty (since 'b2'
				// initially starts with all pieces empty)
				if (getEdge(i, j) == Piece.EMPTY) {
					continue;
				}

				int[] rotate_edge = rotateEdge(i, j, dim, numRotate, reflect);
				b2.setEdge(rotate_edge[0], rotate_edge[1], getEdge(i, j));
			}
		}

		return b2;
	}
	
	public Board isRotateSymmetric(TranspositionTable table) {
		List<Board> sym_boards = getSymmetricBoards();
		
		for (Board r : sym_boards) {
			if (table.isStored(r)) {
				return r;
			}
		}

		return null;
	}
	
	/** Get a list of boards which are geometrically symmetric to
	 * this board state (including the original board state).
	 * <p>
	 * We note that there are 12 different symmetries for a hexagon.
	 * These are achieved through rotations, a single reflection,
	 * or mix of both.
	 * </p>
	 * 
	 * @see <a href="http://wki.pe/Dihedral_group#Elements">Wiki article</a>
	 * for more info on how to achieve the symmetries.
	 */
	public List<Board> getSymmetricBoards() {
		List<Board> sym_boards = new ArrayList<Board>(NUM_EDGES * 2);
		
		for (int numRotate = 0; numRotate < NUM_EDGES * 2; numRotate++) {
			Board r_board = rotateBoard(numRotate % NUM_EDGES,
					numRotate >= NUM_EDGES);
			
			if (!sym_boards.contains(r_board)) {
				sym_boards.add(r_board);
			}
		}
		
		return sym_boards;
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

			if (isOuterEdge(r, c)) {
				count += (getEdge(edge_r, edge_c) == Piece.EMPTY) ? 0 : 1;
			}
		}

		return count;
	}
	
	public boolean isOuterEdge(int r, int c) {
		// the distance between an "outer edge" and the centre of
		// the board is: 2*dim - 1
		return getEdgeDist(r, c, dim) == 2*dim - 1;
	}
	
	/** Check if the corresponding interior edges of this board is the
	 * same as the one from b2.
	 * <p>
	 * An edge is an "interior edge" if it is a side of two cells.
	 * </p>
	 */
	private boolean isInteriorEqual(Board b2) {		
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
		if (obj instanceof Board) {
			Board b2 = (Board)obj;
			
			// ensure the dimension, score, and the number of occupied
			// edges are the same
			if (b2.dim != this.dim || b2.my_score != this.my_score ||
					b2.num_edges_left != this.num_edges_left) {
				
				return false;
			}

			if (isInteriorEqual(b2) && isOuterSymmetric(b2) &&
					b2.getCurrTurn() == this.getCurrTurn()) {
				return true;
			}
		}

		return false;
	}
	
	public String toString() {
		// print string, based on position (cell or edge), and if it
		// is 'in the game board'
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 4*dim - 1; i++) {
			
			for (int j = 0; j < 4*dim - 1; j++) {
				if (isOutOfRange(i, j)) {
					sb.append('-');
				} else if (isCentreCell(i, j)) {
					
					if (getEdge(i, j) == Piece.RED)
						sb.append('r');
					else if (getEdge(i, j) == Piece.BLUE)
						sb.append('b');
					else
						sb.append('-');
				} else {
					
					if (getEdge(i, j) == Piece.RED)
						sb.append('R');
					else if (getEdge(i, j) == Piece.BLUE)
						sb.append('B');
					else
						sb.append('+');
					
				}
				
				sb.append(' ');
			}
			
			sb.setLength(sb.length() - 1);
			sb.append('\n');
		}
		
		sb.setLength(sb.length() - 1);
		
		return sb.toString();
	}
}
