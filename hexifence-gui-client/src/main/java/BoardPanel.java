import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

class BoardFrame extends JFrame {
	JTextArea txt_info = new JTextArea();
	HashMap<Integer, String> players = new HashMap<Integer, String>();
	JButton btn_start;
	public static boolean my_turn = false;
	BoardPanel board;
	
	public BoardFrame(int r, int offset, int dim, String roomName) {
		double x_cen = r * (2*dim - 1) * Math.cos(Math.PI/6) + offset;
		double y_cen = r * Math.sin(Math.PI/6) * Math.floor((2*dim -1)/2)  + r*Math.ceil((2*dim - 1)/2.0) + offset;
		
		setTitle("Hexifence - " + roomName);

		board = new BoardPanel(dim, new Point2D.Double(x_cen, y_cen), r);
		board.setPreferredSize(new Dimension((int)x_cen * 2, (int)y_cen * 2 + offset));
		add(board);
		
		// add the main player into 'players' (no need supplied)
		players.put(PacketHandler.USER_ID, null);
		
		// info textbox
		JPanel bottom_panel = new JPanel();
		add(bottom_panel, BorderLayout.SOUTH);
		bottom_panel.setLayout(new BoxLayout(bottom_panel, BoxLayout.Y_AXIS));
		
		txt_info.setBorder(BorderFactory.createEtchedBorder());
		txt_info.setPreferredSize(new Dimension((int)x_cen * 2, 100));
		txt_info.setEditable(false);
		txt_info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		bottom_panel.add(txt_info);
		
		txt_info.append("Welcome to Hexifence (waiting for players)!");
		
		// when the board is closed, indicate player left
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Driver.leaveRoom();
			}
		});
		
		
		// start button
		btn_start = new JButton("Start Game");
		btn_start.setSize(new Dimension((int)x_cen * 2, 20));
		btn_start.setAlignmentX(Component.CENTER_ALIGNMENT);
		bottom_panel.add(btn_start);
		
		// button to start game event
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Driver.beginGame();
			}
		});
		
		pack();
		setVisible(true);
	}
	
	public void addPlayer(String name, int player_id) {
		System.out.println("HIHIHI");
		players.put(player_id, name);
		txt_info.append("\n" + name + " has entered the room.");
	}
	
	public void removePlayer(int player_id) {
		txt_info.append("\n" + players.get(player_id) + " has left the room.");
		players.remove(player_id);
	}
	
	public void startGame(int player_id) {
		if (player_id == PacketHandler.USER_ID) {
			my_turn = true;
			txt_info.append("\n" + "It is your turn.");
		}
	}

	public void confirmMove(int x, int y, int next_id) {
		board.getEdges()[x][y].selectable = false;
		board.getEdges()[x][y].color = Color.ORANGE;
		startGame(next_id);
	}
}

class BoardPanel extends JPanel {
	private int dim;
	
	private List<Cell> cells;
	private Edge[][] edges;
	
	private Point2D draw_centre;
	private double radius;

	private Edge sel_edge = null;

	public BoardPanel(int dim, Point2D draw_centre, double radius) {
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
    	
    	@Override
    	public void mouseClicked(MouseEvent evt) {
    		Driver.sendMove(sel_edge.x, sel_edge.y);
    		sel_edge = null;
    		BoardFrame.my_turn = false;
		}
    }

    private class MouseMotionHandler extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e){
        	if (!BoardFrame.my_turn) {
        		return;
        	}
        	
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

        			if (edges[x][y].getShape().intersects(boxX, boxY, width, height) &&
        					edges[x][y].selectable) {
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