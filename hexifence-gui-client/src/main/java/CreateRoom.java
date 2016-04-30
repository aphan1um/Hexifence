import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.border.EmptyBorder;

public class CreateRoom extends JDialog {
	private JTextField txt_room_name;
	private JSpinner spin_dim;
	
	public CreateRoom() {
		setTitle("Create Hexifence Game");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		
		PacketHandler.CURR_WINDOW = this;
		
		initUI();
	}
	
	public void initUI() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 2, 10, 10));
		panel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		
		JLabel lbl_name = new JLabel("Room name:");
		panel.add(lbl_name);
		
		txt_room_name = new JTextField();
		txt_room_name.setPreferredSize(new Dimension(150, 20));
		panel.add(txt_room_name);
		
		JLabel lbl_dim = new JLabel("Dimension:");
		panel.add(lbl_dim);
		
		SpinnerNumberModel model1 = new SpinnerNumberModel(1, 1, 10, 1);  
		spin_dim = new JSpinner(model1);
		((DefaultEditor) spin_dim.getEditor()).getTextField().setEditable(false);
		spin_dim.setPreferredSize(new Dimension(150, 20));
		panel.add(spin_dim);
		
		JButton btn_create = new JButton("Create");
		
		btn_create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (txt_room_name.getText().length() == 0) {
					JOptionPane.showMessageDialog(null,
						    "Please enter a room name.",
						    "Empty room name",
						    JOptionPane.WARNING_MESSAGE);
				} else {
					setEnabled(false);
					Driver.createRoom(txt_room_name.getText(), (Integer)spin_dim.getValue());
				}
			}
		});
		
		panel.add(btn_create);
		
		JButton btn_cancel = new JButton("Cancel");
		btn_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeFrame();
			}
		});
		panel.add(btn_cancel);
		
		this.add(panel);
		
		this.pack();
	}
	
	private void closeFrame() {
		this.dispose();
	}
}
