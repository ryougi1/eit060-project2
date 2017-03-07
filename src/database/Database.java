package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import types.Doctor;
import types.Government;
import types.Nurse;
import types.Patient;
import types.User;

public class Database {

	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://eu-cdbr-west-01.cleardb.com/heroku_e980487365507d8?reconnect=true";

	private static final String USER = "b2c82e66ef61f2";
	private static final String PASS = "970d69bc";

	private Connection conn = null;
	private String sql;
	private PreparedStatement pre;
	private ResultSet rs;

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
		sql = "CREATE TABLE records("
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
		System.out.print("Loggtabell ");
		sql = "CREATE TABLE logs("
				+ "logDate		datetime			NOT NULL,"
				+ "data			varchar(1000)	NOT NULL,"
				+ "primary key 	(logDate)"
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
		System.out.print("Loggtabell ");
		sql = "DROP TABLE logs";
		try {
			conn.createStatement().executeUpdate(sql);
			System.out.println("borttagen.");
			dropped = true;
		} catch (SQLException e) {
			System.out.println("inte borttagen, finns inte.");
		}
		return dropped;
	}

	public Record createRecord(User responsible, Patient patient, Nurse nurse, String data) {
		// Is User a doctor?
		if(!(responsible instanceof Doctor))
			return null;
		// SQL prepared statement inserted to database
		sql = "INSERT into records (patient, nurse, doctor, division, data) VALUES(?, ?, ?, ?, ?)";
		try {
			pre = conn.prepareStatement(sql);
			pre.setString(1, patient.getPNbr());
			pre.setString(2, nurse.getPNbr());
			pre.setString(3, ((Doctor)responsible).getPNbr());
			pre.setString(4, ((Doctor)responsible).getDivision());
			pre.setString(5, data.toString());
			pre.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Fetch recordNbr
		int recordNbr = -1;
		sql = "SELECT LAST_INSERT_ID()";
		try {
			rs = conn.createStatement().executeQuery(sql);
			while(rs.next()) {
				recordNbr = rs.getInt("LAST_INSERT_ID()");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Record record = new Record(recordNbr, patient, nurse, (Doctor)responsible, ((Doctor)responsible).getDivision(), data);
		System.out.println(record);
		return record;
	}

	public void createLog(String data) {
		// SQL prepared statement inserted to database
		sql = "INSERT into logs (logDate, data) VALUES(NOW(), ?)";
		try {
			pre = conn.prepareStatement(sql);
			pre.setString(1, data);
			pre.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(data);
	}

	public boolean editRecord(User responsible, int recordNbr, String data) {
		// Is User a doctor or nurse
		if(!(responsible instanceof Nurse || responsible instanceof Doctor)) {
			return false;
		}
		// Check if User is accociated with patient
		if(!isAssociated(recordNbr, responsible)) {
			return false;
		}
		// Free to edit patient's record
		Record record = getRecord(recordNbr);
		record.appendData(data);

		sql = "UPDATE records SET data = ? WHERE recordNbr = ?";
		try {
			pre = conn.prepareStatement(sql);
			pre.setString(1, record.getData());
			pre.setInt(2, recordNbr);
			return pre.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean deleteRecord(User responsible, int recordNbr) {
		// Is User a government?
		if(!(responsible instanceof Government)) {
			return false;
		}
		sql = "DELETE FROM records WHERE recordNbr = ?";
		try {
			pre = conn.prepareStatement(sql);
			pre.setInt(1, recordNbr);
			return pre.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private Record getRecord(int recordNbr) {
		Record record = null;
		sql = "SELECT * FROM records WHERE recordNbr = ?";
		try {
			pre = conn.prepareStatement(sql);
			pre.setInt(1, recordNbr);
			rs = pre.executeQuery();
			while(rs.next()) {
				String patient = rs.getString("patient");
				String nurse = rs.getString("nurse");
				String doctor = rs.getString("doctor");
				String division = rs.getString("division");
				String data = rs.getString("data");
				record = new Record(recordNbr, new Patient(null, patient), new Nurse(null, nurse, division),
						new Doctor(null, doctor, division), division, data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return record;
	}

	public List<Record> getRecords(User responsible) {
		List<Record> records = new LinkedList<Record>();
		// Prepare Statements
		if(responsible instanceof Patient) {
			sql = "SELECT * FROM records WHERE patient = ?";
			try {
				pre = conn.prepareStatement(sql);
				pre.setString(1, responsible.getPNbr());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if(responsible instanceof Nurse || responsible instanceof Doctor) {
			sql = "SELECT * FROM records WHERE division = ?";
			try {
				pre = conn.prepareStatement(sql);
				// Eftersom Doctor är en subklass av Nurse är detta helt ok
				pre.setString(1, ((Nurse)responsible).getDivision());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if(responsible instanceof Government) {
			sql = "SELECT * FROM records";
			try {
				pre = conn.prepareStatement(sql);
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		// Query execution
		try {
			rs = pre.executeQuery();
			while(rs.next()) {
				int recordNbr = rs.getInt("recordNbr");
				String division = rs.getString("division");
				Patient patient = new Patient(null, rs.getString("patient"));
				Nurse nurse = new Nurse(null, rs.getString("nurse"), division);
				Doctor doctor = new Doctor(null, rs.getString("doctor"), division);
				String data = rs.getString("data");
				records.add(new Record(recordNbr, patient, nurse, doctor, division, data));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return records;
	}

	public List<String> getLogs(User responsible) {
		// Is User a government?
		if(!(responsible instanceof Government)) {
			return null;
		}
		List<String> logs = new LinkedList<String>();
		// Prepare Statements
		sql = "SELECT * FROM logs";
		try {
			pre = conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Query execution
		try {
			rs = pre.executeQuery();
			while(rs.next()) {
				String data = rs.getString("data");
				logs.add(data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return logs;
	}

	/**
	 * Checks if the responsible <code>responsible</code> is associated with <code>recordNbr</code>.
	 * Associated is defined as, <code>responsible</code> belongs to the same division as <code>recordNbr</code>.
	 */
	public boolean isAssociated(int recordNbr, User responsible) {
		// Check if responsible user is the same as in a record
		sql = "SELECT patient,nurse,doctor FROM records WHERE recordNbr = ?";
		try {
			pre = conn.prepareStatement(sql);
			pre.setInt(1, recordNbr);
			rs = pre.executeQuery();
			while(rs.next()) {
				String responsiblePNbr = responsible.getPNbr();
				String patientPNbr = rs.getString("patient");
				String nursePNbr = rs.getString("nurse");
				String doctorPnbr = rs.getString("doctor");
				if(responsiblePNbr.equals(nursePNbr) || responsiblePNbr.equals(doctorPnbr)
						|| responsiblePNbr.equals(patientPNbr)) {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		Database db = new Database();
		db.establishConnection();
		db.dropTable();
		db.createTable();
		db.terminateConnection();
	}
}
