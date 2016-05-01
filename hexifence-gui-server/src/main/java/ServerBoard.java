import java.awt.Point;

import hexifence.gui.core.Board;
import hexifence.gui.core.Cell;
import hexifence.gui.core.Edge;

public class ServerBoard extends Board<Edge> {
	public int num_cells_open;
	
	public ServerBoard(int dim) {
		super(Edge.class, dim);
		this.prepare();
		
		num_cells_open = this.getCells().size();
	}

	@Override
	public void generateEdges(Cell c) {
		for (int[] adj : Cell.ADJ_EDGES) {
			// let row R be the row with the most cells, and P be the row of cell_centre
    		// if P > R, then there is an 'offset' on where the first cell begins
    		int column_start = Math.max(0, c.getCentre().x - (getDim() - 1));

			// calculate the centre of cell (in edge coordinates)
    		Point cell_coord = new Point(2*c.getCentre().x + 1, 2*c.getCentre().y - column_start + 1);

    		Point edge_coord = new Point(cell_coord.x + adj[0], cell_coord.y + adj[1]);
			
			// store edge, and make sure to add cell to edge
			if (getEdges()[edge_coord.x][edge_coord.y] == null)
				getEdges()[edge_coord.x][edge_coord.y] = new Edge(edge_coord);
			
			getEdges()[edge_coord.x][edge_coord.y].addCell(c);
		}
	}

}
