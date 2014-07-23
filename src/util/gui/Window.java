package util.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import samcl.SAMCL;

public class Window extends JFrame{
	public SAMCL samcl = null;
	public Window(String name, SAMCL samcl){
		super(name);
		this.samcl = samcl;
		this.setSize(samcl.precomputed_grid.width, samcl.precomputed_grid.height);
		this.addWindowListener(new CustomAdapter());
	}
	
	public class CustomAdapter extends WindowAdapter{
		@Override
		public void windowClosing(WindowEvent e) {
			System.out.println("close table!!!!!!!!!!!!!!!!!!!!!!!");
			if (JOptionPane.showConfirmDialog(Window.this,
					"Are you sure to close this window?", "Really Closing?", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
				try {
					if(samcl.onCloud)
						samcl.precomputed_grid.closeTable();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
			
			super.windowClosing(e);
		}
		
	}
	
}
