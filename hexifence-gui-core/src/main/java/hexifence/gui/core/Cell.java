package hexifence.gui.core;
import java.awt.Color;
import java.awt.Point;

/** Represents a cell in the game.
 * @author Andy
 *
 */
public class Cell {
	/** Coord diff between the centre of cell and the cell's edges. */
	public static final int[][] ADJ_EDGES = { {0, 1}, {1, 1}, {1, 0}, {0, -1}, {-1, -1}, {-1, 0} };
	/** Number of open/un-occupied cells. */
	private int num_open = 6;
	/** Centre point of a cell. */
	private Point centre;
	private int id_occupied;
	public Color cap_color;
	
	/** Create a new instance of a cell, with 'centre' being the
	 * centre of the cell.
	 */
	public Cell(Point centre) {
		this.centre = centre;
	}
	
	/** Get the number of open edges around this cell. */
	public int getNumOpen() {
		return num_open;
	}
	
	public int getIDOccupied() {
		return id_occupied;
	}
	
	void setIDCapturer(int capture_id) {
		this.id_occupied = capture_id;
	}
	
	/** Get the centre location of this cell. */
	public Point getCentre() {
		return centre;
	}
	
	/** Decrease the number of open edges around this cell. */
	protected void decrementNumOpen() {
		num_open--;
	}
}