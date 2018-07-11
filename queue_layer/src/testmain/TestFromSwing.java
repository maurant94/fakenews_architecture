package testmain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class TestFromSwing {
	
	static JFrame frame;
	static JPanel panel = new JPanel(); // the panel is not visible in output
	static JLabel labelClaims;
	static JLabel labelFile;
	
	public static void main(String args[]) {
		
		// Creating the Frame
		frame = new JFrame("File INFO");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 400);
		FileContentListener filecontentlistener = new FileContentListener();
		AttachListener attachlistener = new AttachListener();

		// Creating the panel at bottom and adding components
		JLabel labelName = new JLabel("File Name");
		JTextField tf = new JTextField(20); // accepts upto 20 characters

		labelClaims = new JLabel("Claims");
		JButton button = new JButton("UPLOAD");
		button.addActionListener(attachlistener);
		

		labelFile = new JLabel("File Content");
		JButton uploadFile = new JButton("UPLOAD");
		uploadFile.addActionListener(filecontentlistener);
		
		JButton send = new JButton("Send");
		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Create a file chooser
				final JFileChooser fc = new JFileChooser();
				// In response to a button click:
				String[] args = new String[7];
				args[1] = "1"; // byz
				args[2] = "1"; // peer
				args[3] = "3"; // request
				if (tf.getText() != null)
					args[4] = tf.getText();
				if (attachlistener.getName() != null) {
					args[5] = attachlistener.getName();
				}
				if (filecontentlistener.getName() != null) {
					args[6] = filecontentlistener.getName();
				}
				TestMain.main(args);
			}
		});
		JButton reset = new JButton("Reset");

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(labelName); // Components Added using Flow Layout
		panel.add(tf);
		panel.add(labelClaims); // Components Added using Flow Layout
		panel.add(button);
		panel.add(labelFile); // Components Added using Flow Layout
		panel.add(uploadFile);
		panel.add(send);
		panel.add(reset);

		// Adding Components to the frame.
		frame.getContentPane().add(BorderLayout.CENTER, panel);
		frame.setVisible(true);
	}
	
	private static class FileContentListener implements ActionListener {

	    private String name;

	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	// Create a file chooser
			final JFileChooser fc = new JFileChooser();
			// In response to a button click:
			int returnVal = fc.showOpenDialog(panel);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fc.getSelectedFile();
				name = selectedFile.getAbsolutePath();
				labelFile.setText("File Content : " + name);
			}
	    }

	    public String getName() {
	        return name;
	    }

	}
	
	private static class AttachListener implements ActionListener {

	    private String name;

	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	// Create a file chooser
			final JFileChooser fc = new JFileChooser();
			// In response to a button click:
			int returnVal = fc.showOpenDialog(panel);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fc.getSelectedFile();
				name = selectedFile.getAbsolutePath();
				labelClaims.setText("Claims : " + name);
			}
	    }

	    public String getName() {
	        return name;
	    }

	}
}
