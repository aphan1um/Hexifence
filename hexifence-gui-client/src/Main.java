import java.awt.EventQueue;
import java.awt.geom.Point2D;

import javax.swing.JFrame;

public class Main extends JFrame {
	private static int dim;
	
	private void initUI(int dim) {
		int r = 40;
		int offset = 20;
		
		double x_cen = r * (2*dim - 1) * Math.cos(Math.PI/6) + offset;
		double y_cen = r * Math.sin(Math.PI/6) * Math.floor((2*dim -1)/2)  + r*Math.ceil((2*dim - 1)/2.0) + offset;
		
		setTitle("Hexifence");
		setSize((int)x_cen * 2 + offset, (int)y_cen * 2 + 30);
		setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		System.out.println(Math.ceil((2*dim - 1)/2.0));

		add(new BoardVis(dim,
					new Point2D.Double(x_cen, y_cen), r, this));
	}

	/*
	 * Input arguments: first board size, second path of player1 and third path of player2
	 */
	public static void main(String[] args)
	{
		dim = Integer.valueOf(args[0]);
		
		EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                Main ex = new Main();
                ex.initUI(Main.dim);
                ex.setVisible(true);
            }
        });
	}
}
