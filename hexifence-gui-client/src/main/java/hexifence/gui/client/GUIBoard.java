package hexifence.gui.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import hexifence.gui.core.Board;
import hexifence.gui.core.Cell;

class GUIBoard extends Board<GUIEdge> {
    	private Point2D draw_centre;
    	private double radius;
    	GUIEdge sel_edge = null;

    	private GUIBoardPanel board_panel;
    	private FrameBoard frame;
    	
    	HashMap<Cell, Point2D> cell_cen = new HashMap<Cell, Point2D>();

    	/** Initialise an instance of a game board.
    	 * @param dim Dimension of game board.
    	 * @param draw_centre Centre of game board, to be drawn on window.
    	 * @param radius Size of a cell in the game board.
    	 */
    	public GUIBoard(int dim, Point2D draw_centre, double radius, FrameBoard board) {
    		super(GUIEdge.class, dim);
    		
    		this.draw_centre = draw_centre;
    		this.radius = radius;
    		
    		// initalise the panel object for the board
    		board_panel = new GUIBoardPanel(this);
    		
    		this.frame = board;
    		
    		prepare();
    	}
    	
    	public void paint(Graphics g) {
    		Font f = new Font("Segoe UI", Font.BOLD, 20);
    		g.setFont(f);
    		FontMetrics fm = g.getFontMetrics(f);
    		
    		for (Map.Entry<Cell, Point2D> kv : cell_cen.entrySet()) {
    			if (kv.getKey().getNumOpen() == 0) {
    				g.setFont(f);
    				g.setColor(FrameBoard.USER_COLOURS[frame.players_c.get(kv.getKey().getIDOccupied())]);
    				
    				String text_render = FrameBoard.USER_COLOURS_STR[frame.players_c.get(kv.getKey().getIDOccupied())];
    				int x_draw = (int)(kv.getValue().getX() - fm.stringWidth(text_render)/2.0);
    				int y_draw = (int)(kv.getValue().getY() - fm.getHeight()/2.0) + fm.getAscent();
    				
    				g.drawString(text_render, x_draw, y_draw);
    			}
    		}
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
    		Point cell_coord = new Point(2*c.getCentre().x + 1, 2*c.getCentre().y + 1);

    		// create edges around cell
    		for (int i = 0; i <= 6; i++) {
    			// to create a line from the previous point
    			if (curr_p != null) {
    				init_p = curr_p;
    			}

    			double new_x = getDrawCentre().getX();
    			double new_y = getDrawCentre().getY() + getRadius();
    			
    			// adjust point based on p_diff (see above)
    			new_x += 2 * Math.cos(Math.PI/6) * getRadius() * p_diff.y;
    			new_x -= p_diff.x * Math.cos(Math.PI/6) * getRadius();

    			new_y += Math.cos(Math.PI/6) * 1/Math.tan(Math.PI/6) * getRadius() * p_diff.x;
    			
    			cell_cen.put(c, new Point2D.Double(new_x, new_y));

    			new_x += getRadius() * Math.cos((i-0.5)*Math.PI*2/6);
    			new_y += getRadius() * Math.sin((i-0.5)*Math.PI*2/6);
    			
    			curr_p = new Point2D.Double(new_x, new_y);
    			
    			if (init_p == null) {
    				continue;
    			}

    			Point edge_coord = new Point (cell_coord.x + Cell.ADJ_EDGES[i - 1][0], cell_coord.y + Cell.ADJ_EDGES[i - 1][1]);

    			
    			// store edge, and make sure to add cell to edge
    			if (getEdges()[edge_coord.x][edge_coord.y] == null)
    				getEdges()[edge_coord.x][edge_coord.y] = new GUIEdge(init_p, curr_p, edge_coord.x, edge_coord.y);
    			
    			getEdges()[edge_coord.x][edge_coord.y].addCell(c);
    			
    			System.out.println(c.getCentre() + " ---> " + cell_coord + " : " + edge_coord + "    p_diff=" + p_diff);
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
    	    	g2d.setStroke(new BasicStroke(9f));
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
    	    	
    	    	board.paint(g);

    	    	g.dispose();
    	    }
    	    
    	    private class MouseHandler extends MouseAdapter {
    	    	@Override
    	    	public void mouseClicked(MouseEvent evt) {
    	    		if (board.sel_edge != null) {
    	    			board.sel_edge.setColour(Color.BLACK);
    	    			Driver.sendMove(board.sel_edge.getLocation().x, board.sel_edge.getLocation().y);
    	    			board.sel_edge = null;
    	    			FrameBoard.IS_LOCKED = true;
    	    		}
    			}
    	    }

    	    private class MouseMotionHandler extends MouseMotionAdapter {
    	        @Override
    	        public void mouseMoved(MouseEvent e){
    	        	if (!FrameBoard.isMyTurn() || FrameBoard.IS_LOCKED) {
    	        		return;
    	        	}
    	        	
    				int HIT_BOX_SIZE = 6;
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