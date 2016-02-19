package GUI;

import javax.swing.*;
import javax.swing.event.*;

import database.Database;

import java.awt.*;
import java.awt.event.*;

/**
 * MovieGUI is the user interface to the movie database. It sets up the main
 * window and connects to the database.
 */
public class GUI {
	/**
	 * db is the database object
	 */
	private Database db;

	/**
	 * tabbedPane is the contents of the window. It consists of two panes, User
	 * login and Book tickets.
	 */
	private JTabbedPane tabbedPane;

	/**
	 * Create a GUI object and connect to the database.
	 * 
	 * @param db
	 *            The database.
	 */
	public GUI(Database db) {
		this.db = db;

		JFrame frame = new JFrame("Hospital Records");
	
		RecordPane recPane = new RecordPane(db);
		recPane.setVisible(false);
		UserLogin login = new UserLogin(db);
		frame.addWindowListener(new WindowHandler());
		

		frame.setSize(500, 400);
		frame.setVisible(true);

		/* --- change code here --- */
		/* --- change xxx to your user name, yyy to your password --- */
		try {
			db.establishConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ChangeHandler is a listener class, called when the user switches panes.
	 */
	class ChangeHandler implements ChangeListener {
		/**
		 * Called when the user switches panes. The entry actions of the new
		 * pane are performed.
		 * 
		 * @param e
		 *            The change event (not used).
		 */
		public void stateChanged(ChangeEvent e) {
			BasicPane selectedPane = (BasicPane) tabbedPane.getSelectedComponent();
			selectedPane.entryActions();
		}
	}

	/**
	 * WindowHandler is a listener class, called when the user exits the
	 * application.
	 */
	class WindowHandler extends WindowAdapter {
		/**
		 * Called when the user exits the application. Closes the connection to
		 * the database.
		 * 
		 * @param e
		 *            The window event (not used).
		 */
		public void windowClosing(WindowEvent e) {
			try {
				db.terminateConnection();
				System.exit(0);
			} catch (Exception e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
}
