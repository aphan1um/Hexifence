import java.awt.EventQueue;
import java.awt.geom.Point2D;

import javax.swing.JFrame;

public class Main extends JFrame {
	private void initUI(int dim) {
		setTitle("Hexifence");
		setSize(400, 400);
		setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		add(new BoardVis(dim, new Point2D.Double(200, 200), 40, this));
	}

	/*
	 * Input arguments: first board size, second path of player1 and third path of player2
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                Main ex = new Main();
                ex.initUI(3);
                ex.setVisible(true);
            }
        });
	}
}
