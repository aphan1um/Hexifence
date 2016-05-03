package hexifence.gui.client;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import hexifence.gui.core.Edge;

class GUIEdge extends Edge {
	public static final Color DEFAULT_COLOUR = Color.LIGHT_GRAY;
	
	private Shape sh;
	private Color colour;
	private boolean selectable;
	
	/** Create a graphical component of an edge.
	 * @param start Start point of the line to be drawn, representing this edge.
	 * @param end End point of the line to be drawn.
	 * @param x x-coordinate of edge in some Board.
	 * @param y y-coordinate of edge in some Board.
	 */
	public GUIEdge(Point2D start, Point2D end, int x, int y) {
		super(new Point(x, y));
		setNewLine(start, end);
		
		// set default colour
		this.colour = DEFAULT_COLOUR;
		// by default, make the edge able to be selected
		this.selectable = true;
	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(colour);
		g2d.draw(sh);
	}
	
	public int useCell(Color colour, int id_capture) {
		int ret = super.useCell(id_capture);
		
		selectable = false;
		this.colour = colour;
		
		return ret;
	}

	public Line2D getShape() {
		return (Line2D)sh;
	}
	
	public void setNewLine(Point2D start, Point2D end) {
		sh = new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY());
	}

	public boolean isSelectable() {
		return selectable;
	}
	
	public Color getColour() {
		return colour;
	}

	public void setColour(Color colour) {
		this.colour = colour;
	}
}