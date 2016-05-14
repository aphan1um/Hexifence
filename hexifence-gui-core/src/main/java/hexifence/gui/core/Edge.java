package hexifence.gui.core;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/** Represents a side of one or more cells. */
public class Edge {
	public Point location;
	private List<Cell> adj_cells;
	
	/** Create a new edge, with its specified coordinates
	 * @param location
	 */
	public Edge(Point location) {
		this.location = location;
		this.adj_cells = new ArrayList<Cell>();
	}
	
	/** Attach a cell (which should be adjacent to this edge) to
	 * the edge.
	 */
	public void addCell(Cell c) {
		adj_cells.add(c);
	}
	
	/** Get location of edge in the 2D array of edges of Board.
	 */
	public Point getLocation() {
		return location;
	}

	/** Get list of adjacent cells, as an array.
	 * 
	 * Returns <code>null</code> if the edge is not open.
	 */
	public Cell[] getAdjCells() {
		// if edge is not open
		if (adj_cells == null) {
			return null;
		}
		
		return adj_cells.toArray(new Cell[adj_cells.size()]);
	}

	/** Mark the cell as being occupied by a player.
	 * 
	 * @return Returns the number of cells captured by player
	 * (0 if none).
	 */
	public int useCell(int id_capture, Color color) {
		int cells_captured = 0;
		
		for (Cell c : adj_cells) {
			c.decrementNumOpen();

			if (c.getNumOpen() == 0) {
				cells_captured++;
				
				c.setIDCapturer(id_capture);
				c.cap_color = color;
			}
		}
		
		// now make adj_cells null, as indication this edge is not open
		adj_cells = null;
		
		return cells_captured;
	}
}