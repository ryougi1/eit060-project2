package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import types.Doctor;
import types.Nurse;
import types.Patient;
import types.User;

public class Database {

	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://puccini.cs.lth.se:3306/db33";

	private static final String USER = "db33";
	private static final String PASS = "qkg284wp";

	private Connection conn = null;
	private String sql;

	public Database() throws ClassNotFoundException, SQLException {
		// Registrera JDBC drivrutin
		Class.forName(JDBC_DRIVER);
	}
	
	public void establishConnection() throws SQLException {
		// Anslut
		System.out.print("Databasen " + DB_URL);
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		System.out.println(" ansluten.");
	}
	
	public void terminateConnection() throws SQLException {
		System.out.print("Databasen " + DB_URL);
		conn.close();
		System.out.println(" stängd.");
	}
	
	public boolean createTable() {
		boolean created = false;
		System.out.println();
		System.out.print("Journaltabell ");
		sql = "CREATE TABLE Records("
				+ "recordNbr	integer auto_increment,"
				+ "patient		char(12)  		NOT NULL,"
				+ "nurse		char(12)  		NOT NULL,"
				+ "doctor		char(12)  		NOT NULL,"
				+ "division		varchar(30)		NOT NULL,"
				+ "data			varchar(1000)	NOT NULL,"
				+ "primary key 	(recordNbr)"
			+ ");";
		try {
			conn.createStatement().executeUpdate(sql);
			System.out.println("skapad.");
			created = true;
		} catch (SQLException e) {
			System.out.println("inte skapad, finns redan.");
		}
		return created;
	}

	public boolean dropTable() {
		boolean dropped = false;
		System.out.println();
		System.out.print("Journaltabell ");
		sql = "DROP TABLE records";
		try {
			conn.createStatement().executeUpdate(sql);
			System.out.println("borttagen.");
			dropped = true;
		} catch (SQLException e) {
			System.out.println("inte borttagen, finns inte.");
		}
		return dropped;
	}
	
	public Record createRecord(User responsible, Patient patient, Nurse nurse, List<String> data) {
		// Is User a doctor?
		
		// SQL prepared statement
		
		// SQL Create record and fetch recordNbr
		
		Record record = new Record(0, patient, nurse, (Doctor)responsible, ((Doctor)responsible).getDivision(), data);
		return record;
	}
	
	public boolean editRecord(User responsible, Patient patient, int recordNbr, List<String> data) {
		// Is User a doctor or nurse and is at the stored records division?
		// ...
		return false;
	}
	
	public boolean deleteRecord(User responsible, int recordNbr) {
		// Is User a government?
		// ...
		return false;
	}
	
	public List<Record> getRecords(User responsible, Patient patient) {
		// Is User associated with patient?
		// ...
		return null;
	}
	
	/**
	 * Checks if the responsible <code>User</code> is associated with patient <code>Patient</code>
	 */
	public boolean isAssociated(User responsible, Patient patient) {
		// Check if User is accociated with patient
		return false;
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Database db = new Database();
		db.establishConnection();
		db.dropTable();
		db.createTable();
		db.terminateConnection();
	}
}