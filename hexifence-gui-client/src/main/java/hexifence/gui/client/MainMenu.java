package hexifence.gui.client;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import hexifence.gui.core.GameRoom;

public class MainMenu extends JFrame {
	private JList<GameRoom> list_rooms;
	private DefaultListModel<GameRoom> listModel = new DefaultListModel<GameRoom>();
	
	public JTextField txt_name;
	JDialog init_dialog;
	
	public MainMenu() {
		this.setEnabled(false);
		initUI();
		
		JOptionPane pane = new JOptionPane("Please wait while connecting to server...\nIf this takes too long, press X to exit, and restart it.\nMost of the time, this should let you in.",
				JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
		init_dialog = pane.createDialog(this, "Connecting");
		init_dialog.setModalityType(ModalityType.MODELESS);
		
		// when dialog is deleted
		init_dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		init_dialog.setVisible(true);
		
		setTitle("Hexifence GUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		readRooms();
	}
	
	public void enableWindow() {
		setEnabled(true);
		setVisible(true);
		init_dialog.dispose();
	}
	
	public void readRooms() {
		try {
	        list_rooms.setEnabled(false);
			listModel.addElement(new GameRoom("Loading rooms...", 2, -1));
			
			URL url = new URL(Driver.SERVER_ADDRESS + "/rooms");
			
			URLConnection con = url.openConnection();
			InputStream is = con.getInputStream();
			GZIPInputStream gis = new GZIPInputStream(is);
			ObjectInputStream ois = new ObjectInputStream(gis);

			GameRoom[] rooms = (GameRoom[])ois.readObject();
			
			listModel.clear();
			for (GameRoom r : rooms) {
				if (!r.started)
					listModel.addElement(r);
			}
			
			list_rooms.setEnabled(true);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initUI() {
		JPanel main_panel = new JPanel();
		JPanel room_panel = new JPanel();
		
		// create main border
		main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.X_AXIS));
		main_panel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		main_panel.add(room_panel);
		
		// left part of main border
		room_panel.setLayout(new BoxLayout(room_panel, BoxLayout.Y_AXIS));
		
		// for the textbox and name
		JPanel name_panel = new JPanel();
		name_panel.setLayout(new BoxLayout(name_panel, BoxLayout.X_AXIS));
		
		JLabel lbl_name = new JLabel("Name:");
		name_panel.add(lbl_name);
		name_panel.add(Box.createRigidArea(new Dimension(10, 0)));
		
		txt_name = new JTextField();
		// TODO: Find a better name generator!
		txt_name.setText(UUID.randomUUID().toString());
		txt_name.setPreferredSize(new Dimension(200, 25));
		// TODO: make the name changeable
		txt_name.setEnabled(false);
		name_panel.add(txt_name);
		
		// room section
		JPanel list_panel = new JPanel(new GridBagLayout());
		list_panel.setBorder(new EmptyBorder(new Insets(10, 0, 10, 0)));
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
		
		// make room list initially unselectable
		list_rooms = new JList(listModel);
		list_rooms.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		
		JLabel lbl_room = new JLabel("Available rooms:");
		lbl_room.setHorizontalAlignment(SwingConstants.CENTER);
		list_panel.add(lbl_room, gbc);
		
		list_rooms.setPreferredSize(new Dimension(100, 200));
		list_panel.add(list_rooms, gbc);
		
		list_rooms.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					// double click
					int index = list_rooms.locationToIndex(evt.getPoint());
					Driver.joinRoom(listModel.getElementAt(index));
				}
			}
		});
		
		
		// buttons
		JPanel button_panel = new JPanel();
		button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.X_AXIS));
		
		JButton b_create_room = new JButton("Create Room");
		JButton b_refresh_room = new JButton("Refresh");
		JButton b_exit = new JButton("Exit");
		
		// button events
		b_refresh_room.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readRooms();
			}
		});
		
		b_create_room.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreateRoom cr_room = new CreateRoom();
				cr_room.setLocationRelativeTo(getFrames()[0]);
			}
		});
		
		b_exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		button_panel.add(b_create_room);
		button_panel.add(Box.createRigidArea(new Dimension(5, 0)));
		button_panel.add(b_refresh_room);
		button_panel.add(Box.createRigidArea(new Dimension(5, 0)));
		button_panel.add(b_exit);
		
		// add sub-panels to main_panel
		room_panel.add(name_panel);
		room_panel.add(list_panel);
		room_panel.add(button_panel);
		
		this.add(main_panel);
		pack();
	}
}