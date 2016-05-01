package hexifence.gui.client;

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

import javax.swing.JPanel;

import hexifence.gui.core.Board;
import hexifence.gui.core.Cell;

class GUIBoard extends Board<GUIEdge> {
    	private Point2D draw_centre;
    	private double radius;
    	GUIEdge sel_edge = null;
    	private GUIBoardPanel board_panel;

    	/** Initialise an instance of a game board.
    	 * @param dim Dimension of game board.
    	 * @param draw_centre Centre of game board, to be drawn on window.
    	 * @param radius Size of a cell in the game board.
    	 */
    	public GUIBoard(int dim, Point2D draw_centre, double radius) {
    		super(GUIEdge.class, dim);
    		
    		this.draw_centre = draw_centre;
    		this.radius = radius;
    		
    		// initalise the panel object for the board
    		board_panel = new GUIBoardPanel(this);
    		
    		prepare();
    	}

    	@Override
    	public void generateEdges(Cell c) {
    		Point2D init_p = null;
    		Point2D curr_p = null;

    		// get the cell coordinate difference between cell_centre and the centre cell on game board
    		Point board_cen = new Point(getDim() - 1, (2*getDim() - 1)/2);
    		Point p_diff = new Point(c.getCentre().x - board_cen.x, c.getCentre().y - board_cen.y);

    		// let row R be the row with the most cells, and P be the row of cell_centre
    		// if P > R, then there is an 'offset' on where the first cell begins
    		int column_start = Math.max(0, p_diff.x);
    		
    		// calculate the centre of cell (in edge coordinates)
    		Point cell_coord = new Point(2*c.getCentre().x + 1, 2*c.getCentre().y - column_start + 1);

    		// create edges around cell
    		for (int i = 0; i <= 6; i++) {
    			// to create a line from the previous point
    			if (curr_p != null) {
    				init_p = curr_p;
    			}

    			double new_x = getDrawCentre().getX() + getRadius() * Math.cos((i-0.5)*Math.PI*2/6);
    			double new_y = getDrawCentre().getY() + getRadius() * Math.sin((i-0.5)*Math.PI*2/6);
    			
    			// adjust point based on p_diff (see above)
    			new_x += 2 * Math.cos(Math.PI/6) * getRadius() * p_diff.y;
    			new_x -= p_diff.x * Math.cos(Math.PI/6) * getRadius();

    			new_y += Math.cos(Math.PI/6) * 1/Math.tan(Math.PI/6) * getRadius() * p_diff.x;
    			

    			curr_p = new Point2D.Double(new_x, new_y);
    			
    			if (init_p == null) {
    				continue;
    			}

    			Point edge_coord = new Point (cell_coord.x + Cell.ADJ_EDGES[i - 1][0], cell_coord.y + Cell.ADJ_EDGES[i - 1][1]);


    			if (getEdges()[edge_coord.x][edge_coord.y] == null)
    				getEdges()[edge_coord.x][edge_coord.y] = new GUIEdge(init_p, curr_p, edge_coord.x, edge_coord.y);
    		}
    	}

    	public Point2D getDrawCentre() {
    		return draw_centre;
    	}

    	public double getRadius() {
    		return radius;
    	}

    	public JPanel getBoardPanel() {
    		return board_panel;
    	}
    	
    	
    	private static class GUIBoardPanel extends JPanel {
    		private GUIBoard board;
    		
    		/** Initialise the graphical component of game board */
    		public GUIBoardPanel(GUIBoard board) {
    			this.board = board;

    			// swing related stuff
    			setBackground(Color.WHITE);
    			addMouseMotionListener(new MouseMotionHandler());
    			addMouseListener(new MouseHandler());
    		}
    		
    	    @Override
    	    public void paintComponent(Graphics g) {
    	    	Graphics2D g2d = (Graphics2D)g;
    	    	
    	    	super.paintComponent(g);
    	    	
    	    	// set thickness of line, and enable antialiasing (for smooth lines)
    	    	g2d.setStroke(new BasicStroke(6f));
    	    	g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
    	    			RenderingHints.VALUE_ANTIALIAS_ON));

    	    	// paint each edge
    	    	for (int x = 0; x < 4* board.getDim() - 1; x++) {
    	    		for (int y = 0; y < 4* board.getDim() - 1; y++) {
    	    			if (board.getEdges()[x][y] == null) {
    	    				continue;
    	    			}

    	    			board.getEdges()[x][y].paint(g);
    	    		}
    	    	}

    	    	// to avoid some line cutting a part of the selected edge (by writing above these lines)
    	    	if (board.sel_edge != null) {
    	    		board.sel_edge.paint(g);
    	    	}

    	    	g.dispose();
    	    }
    	    
    	    private class MouseHandler extends MouseAdapter {
    	    	@Override
    	    	public void mouseClicked(MouseEvent evt) {
    	    		if (board.sel_edge != null) {
    	    			Driver.sendMove(board.sel_edge.getLocation().x, board.sel_edge.getLocation().y);
    	    			board.sel_edge = null;
    	    			FrameBoard.my_turn = false;
    	    		}
    			}
    	    }

    	    private class MouseMotionHandler extends MouseMotionAdapter {
    	        @Override
    	        public void mouseMoved(MouseEvent e){
    	        	if (!FrameBoard.my_turn) {
    	        		return;
    	        	}
    	        	
    				int HIT_BOX_SIZE = 4;
    				int boxX = e.getX() - HIT_BOX_SIZE / 2;
    				int boxY = e.getY() - HIT_BOX_SIZE / 2;

    				int width = HIT_BOX_SIZE;
    				int height = HIT_BOX_SIZE;

    	        	for (int x = 0; x < 4*board.getDim() - 1; x++) {
    	        		for (int y = 0; y < 4*board.getDim() - 1; y++) {
    	        			if (board.getEdges()[x][y] == null) {
    	        				continue;
    	        			}

    	        			if (board.getEdges()[x][y].getShape().intersects(boxX, boxY, width, height) &&
    	        					board.getEdges()[x][y].isSelectable()) {
    	        				if (board.sel_edge != null) {
    	        					board.sel_edge.setColour(Color.LIGHT_GRAY);
    	        				}
    	        				
    	        				board.getEdges()[x][y].setColour(Color.MAGENTA);
    	        				board.sel_edge = board.getEdges()[x][y];
    	        				
    	        				e.getComponent().repaint();
    	        				return;
    	        			}
    	        		}
    	        	}
    	        }
    	    }
    	}
    }