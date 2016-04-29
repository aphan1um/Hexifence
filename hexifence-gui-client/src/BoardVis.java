import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

class BoardVis extends JPanel {
	private int dim;
	
	private List<Cell> cells;
	private Edge[][] edges;
	
	private Point2D draw_centre;
	private double radius;

	private Edge sel_edge = null;

	public BoardVis(int dim, Point2D draw_centre, double radius, JFrame frame) {
		this.dim = dim;
		this.draw_centre = draw_centre;
		this.edges = new Edge[4*dim - 1][4*dim - 1];
		this.radius = radius;

		generateCells();
		this.setBackground(Color.WHITE);
		
		this.addMouseMotionListener(new MouseMotionHandler());
		this.addMouseListener(new MouseHandler());
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

				cells.add(new Cell(this, centre));
			}
		}
	}

	public Point2D getDrawCentre() {
		return draw_centre;
	}

	public double getRadius() {
		return radius;
	}

	public Edge[][] getEdges() {
		return edges;
	}
	
	public int getDim() {
		return dim;
	}

    @Override
    public void paintComponent(Graphics g) {
    	Graphics2D g2d = (Graphics2D)g;
    	
    	super.paintComponent(g);
    	
    	g2d.setStroke(new BasicStroke(6f));
    	g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    	
    	for (int x = 0; x < 4*dim - 1; x++) {
    		for (int y = 0; y < 4*dim - 1; y++) {
    			if (edges[x][y] == null) {
    				continue;
    			}

    			edges[x][y].paint(g);
    		}
    	}

    	// to avoid some line cutting a part of the selected edge (by writing above these lines)
    	if (sel_edge != null) {
    		sel_edge.paint(g);
    	}

    	g.dispose();
    }

    private class MouseHandler extends MouseAdapter {
    	@Override
        public void mousePressed(MouseEvent e) {
            
            // e.getComponent().repaint();
        }
    }

    private class MouseMotionHandler extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e){
			int HIT_BOX_SIZE = 4;
			int boxX = e.getX() - HIT_BOX_SIZE / 2;
			int boxY = e.getY() - HIT_BOX_SIZE / 2;

			int width = HIT_BOX_SIZE;
			int height = HIT_BOX_SIZE;

        	for (int x = 0; x < 4*dim - 1; x++) {
        		for (int y = 0; y < 4*dim - 1; y++) {
        			if (edges[x][y] == null) {
        				continue;
        			}

        			if (edges[x][y].getShape().intersects(boxX, boxY, width, height)) {
        				if (sel_edge != null) {
        					sel_edge.color = Color.LIGHT_GRAY;
        				}
        				
        				edges[x][y].color = Color.MAGENTA;
        				sel_edge = edges[x][y];
        				
        				e.getComponent().repaint();
        				return;
        			}
        		}
        	}
        }
    }
}