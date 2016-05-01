package hexifence.gui.core;

import java.awt.Point;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

// NOTE: The user must generate their own edges

public abstract class Board<T extends Edge> {
	private List<Cell> cells;
	private T[][] edges;
	private int dim;
	
	/** Create a general board instance.
	 * @param cls Class of T
	 * @param dim Dimension of board
	 * @param edge_gen Interface containing function meant to create edges,
	 * based on (x, y) location
	 */
	public Board(Class<? extends T> cls, int dim) {
		this.dim = dim;
		
		// create an array of edges (unchecked)
		edges = (T[][])Array.newInstance(cls, 4*dim - 1, 4*dim - 1);
	}
	
	/** Prepare the board, initalising the cells and edges.
	 * <p>
	 * If <code>generateEdges(...)</code> depends on variables NOT in this
	 * class, you should prepare them before calling this.
	 * </p>
	 */
	public void prepare() {
		generateCells();
	}
	
	/** Get the dimension of this game board.
	 */
	public int getDim() {
		return dim;
	}
	
	public List<Cell> getCells() {
		return cells;
	}
	
	/** Get the edges of the game board.
	 */
	public T[][] getEdges() {
		return edges;
	}
	
	/** Find and generate cells on the gaming board. */
	private void generateCells() {
		// Initalise list of cells
		cells = new ArrayList<Cell>();

		// Find the cells of the gaming board
		int total_cells_row = dim - 1;		// Total number of cells for a given row
		int column_start = 0;				// First cell on some cell

		for (int r = 0; r <= 2*dim - 2; r++) {
			// As we are moving down the rows, the number of cells increase
			// (up to the row with the most number of cells; we call this row P)
			if (r < dim) {
				total_cells_row++;
			} else {
				// If we are at a row R for which R > P, then the column of the first
				// cell moves to the right by one, and the number of cells begin to
				// decrease back to 'dim' cells (ie. at the last row).
				total_cells_row--;
				column_start++;
			}

			// Add each cell to 'cells'
			for (int c = 0; c < total_cells_row; c++) {
				// Centre point of cell (cell coord)
				Point centre = new Point(r, c + column_start);
				Cell new_cell = new Cell(centre);

				cells.add(new_cell);
				
				// for each cell, generate the edges around it
				generateEdges(new_cell);
			}
		}
	}
	
	/** Generate edges around the centre of a cell.
	 * 
	 * @param c Cell to create edges around.
	 */
	public abstract void generateEdges(Cell c);
}
