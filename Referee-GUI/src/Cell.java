import java.awt.Point;
import java.awt.geom.Point2D;

class Cell {
	/** Coord diff between the centre of cell and the cell's edges. */
	// DO NOT EDIT THIS
	private static final int[][] ADJ_EDGES = { {0, 1}, {1, 1}, {1, 0}, {0, -1}, {-1, -1}, {-1, 0} };

	public Cell(BoardVis board, Point cell_centre) {
		Point2D init_p = null;
		Point2D curr_p = null;

		Point p_diff = new Point(cell_centre.x - (board.getDim() - 1), cell_centre.y - (2*board.getDim() - 1)/2);

		int column_start = Math.max(0, p_diff.x);
		Point cell_coord = new Point(2*cell_centre.x + 1, 2*cell_centre.y - column_start + 1);
		System.out.println(cell_centre + " ----->  " + cell_coord + '\t' + column_start);
		for (int i = 0; i <= 6; i++) {
			if (curr_p != null) {
				init_p = curr_p;
			}

			double new_x = board.getDrawCentre().getX() + board.getRadius() * Math.cos((i-0.5)*Math.PI*2/6);
			double new_y = board.getDrawCentre().getY() + board.getRadius() * Math.sin((i-0.5)*Math.PI*2/6);
			
			new_x += 2 * Math.cos(Math.PI/6) * board.getRadius() * p_diff.y;
			new_x -= p_diff.x * Math.cos(Math.PI/6) * board.getRadius();
			
			new_y += Math.cos(Math.PI/6) * 1/Math.tan(Math.PI/6) * board.getRadius() * p_diff.x;
			
		
			curr_p = new Point2D.Double(new_x, new_y);
			
			if (init_p == null) {
				continue;
			}

			Point edge_coord = new Point (cell_coord.x + ADJ_EDGES[i - 1][0], cell_coord.y + ADJ_EDGES[i - 1][1]);


			if (board.getEdges()[edge_coord.x][edge_coord.y] == null)
				board.getEdges()[edge_coord.x][edge_coord.y] = new Edge(init_p, curr_p);
		}
	}
}