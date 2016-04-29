import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

class Edge {
	private Shape sh;
	public Color color = Color.LIGHT_GRAY;
	
	public Edge(Point2D start, Point2D end) {
		sh = new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY());
	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(color);
		g2d.draw(sh);
	}

	public Line2D getShape() {
		return (Line2D)sh;
	}
}