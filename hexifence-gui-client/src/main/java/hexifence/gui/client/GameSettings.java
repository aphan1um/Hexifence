package hexifence.gui.client;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.border.EmptyBorder;

import hexifence.gui.client.net.PacketHandler;
import hexifence.gui.core.Cell;

public class GameSettings extends JDialog {
	private JSpinner spin_radius;
	private JSpinner spin_thick;
	
	private FrameBoard frame;
	
	public GameSettings(FrameBoard frame) {
		setTitle("Settings");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		this.setLocationRelativeTo(frame);
		
		this.frame = frame;
		
		initUI();
	}
	
	public void initUI() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 2, 10, 10));
		panel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		
		JLabel lbl_dim = new JLabel("Radius:");
		panel.add(lbl_dim);
		
		SpinnerNumberModel model1 = new SpinnerNumberModel(frame.board.getRadius(), 10, 100, 2);  
		spin_radius = new JSpinner(model1);
		spin_radius.setPreferredSize(new Dimension(150, 20));
		panel.add(spin_radius);
		
		JLabel lbl_thickness = new JLabel("Line thickness:");
		panel.add(lbl_thickness);
		
		SpinnerNumberModel model2 = new SpinnerNumberModel(frame.board.line_thickness, 1, 15, 0.5f);  
		spin_thick = new JSpinner(model2);
		spin_thick.setPreferredSize(new Dimension(150, 20));
		panel.add(spin_thick);
		
		
		// buttons
		JButton btn_create = new JButton("OK");
		
		btn_create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.board.radius = (Double)spin_radius.getValue();
				frame.board.line_thickness = (float)((double)spin_thick.getValue());
				
				frame.board.adjustSize();
				for (Cell c: frame.board.getCells()) {
					frame.board.generateEdges(c);
				}
				
				frame.repaint();
				
				frame.txt_info.setSize(new Dimension(frame.board.getBoardPanel().getPreferredSize().width, 50));
				
				frame.pack();
				GameSettings.this.dispose();
			}
		});
		
		panel.add(btn_create);
		
		JButton btn_cancel = new JButton("Cancel");
		btn_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		panel.add(btn_cancel);
		
		this.add(panel);
		
		this.pack();
	}
}
