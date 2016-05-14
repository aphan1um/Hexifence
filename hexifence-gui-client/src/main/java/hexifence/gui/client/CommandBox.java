package hexifence.gui.client;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.border.EmptyBorder;

import hexifence.gui.client.net.PacketHandler;
import hexifence.gui.core.Board;
import hexifence.gui.core.Cell;

public class CommandBox extends JDialog {
	private FrameBoard frame;
	
	public CommandBox(FrameBoard frame) {
		setTitle("Type a command");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		this.setLocationRelativeTo(frame);
		
		this.frame = frame;
		
		initUI();
	}
	
	public void initUI() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 1, 10, 10));
		panel.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
		
		final JTextField cmd = new JTextField();
		cmd.setPreferredSize(new Dimension(300, 25));
		panel.add(cmd);
		
		
		// buttons
		JButton btn_create = new JButton("OK");
		
		btn_create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] tokens = cmd.getText().split(" ");
				
				for (int i = 0; i < tokens.length; i++) {
					int offset = Math.max(0, i - (2*frame.board.getDim() - 1));
					
					for (int j = offset; j < offset + tokens[i].length(); j++) {
						Color c = null;
						
						if (i % 2 == 1 && j % 2 == 1) {
							continue;
						}
						
						if (tokens[i].charAt(j - offset) == 'R') {
							c = Color.RED;
						} else if (tokens[i].charAt(j - offset) == 'B') {
							c = Color.BLUE;
						}
						
						if (c != null) {
							System.out.println(tokens[i].charAt(j - offset));
							frame.board.getEdges()[i][j].useCell(c, (c == Color.RED) ? 0 : 1);
						}
						
					}
				}
				
				frame.repaint();
			}
		});
		
		panel.add(btn_create);
		
		this.add(panel);
		
		this.pack();
	}
}
