import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
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
	private Point2D draw_centre;
	private double radius;
	private static final int[][] MOVE_DIR = {{-1, -1}, {0, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 0}};
	private Edge[][] edges;

	public BoardVis(int dim, Point2D draw_centre, double radius, JFrame frame) {
		this.dim = dim;
		this.draw_centre = draw_centre;
		this.edges = new Edge[4*dim - 1][4*dim - 1];
		this.radius = radius;
		
		generateCells();
		
		this.addMouseMotionListener(new MouseMotionHandler());
	}

	/** Find and generate cells on the gaming board. */
	private void generateCells() {
		// Initalise list of cells
		cells = new ArrayList<Cell>();

		Point centre = new Point(dim - 1, (2*dim - 1)/2);
		cells.add(new Cell(this, centre));

		for (int r = 1; r < dim; r++) {
			System.out.println("r = " + r);
			int curr_dir = 0;
			int side_added = 0;

			Point curr_cell = new Point(centre.x, centre.y + r);
			cells.add(new Cell(this, curr_cell));
			for (int s = 0; s < 6*r - 1; s++) {

				curr_cell = new Point(curr_cell.x + MOVE_DIR[curr_dir][0], curr_cell.y + MOVE_DIR[curr_dir][1]);
				cells.add(new Cell(this, curr_cell));
				side_added++;

				if (side_added == r) {
					curr_dir++;
					side_added = 0;
				}
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
    	
    	g2d.setStroke(new BasicStroke(4.5f));
    	g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    	
    	for (int x = 0; x < 4*dim - 1; x++) {
    		for (int y = 0; y < 4*dim - 1; y++) {
    			if (edges[x][y] == null) {
    				continue;
    			}

    			edges[x][y].paint(g);
    		}
    	}

    	g.dispose();
    }


    private class MouseMotionHandler extends MouseMotionAdapter {
    	Edge sel_edge = null;

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
        					sel_edge.color = Color.BLACK;
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