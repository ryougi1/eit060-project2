package GUI;

import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import database.Database;

/**
 * MovieBooking is the main class for the movie ticket booking 
 * application. It creates a database object and the GUI to
 * interface to the database.
 */
public class HospitalMain {
    public static void main(String[] args) {
        Database db = null;
		try {
			db = new Database();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		if(db != null) {
			new GUI(db);			
		}
		JFrame frame = new JFrame("Login");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		frame.add(panel);
		frame.setVisible(true);
    }
}
