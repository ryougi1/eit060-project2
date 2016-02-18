package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

		// Anslut
		System.out.print("Databasen " + DB_URL);
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		System.out.println(" ansluten.");
	}
	
	public boolean createTables() {
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

	public boolean dropTables() {
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
/*
	@Override
	public boolean insertUser(User user) {
		if (user == null) {
			System.out.println("Användaren som skulle läggas till är null.");
			return false;
		}
		System.out.print("Användare " + user.getFirstName() + " ");
		sql = "INSERT INTO 
		try {
			conn.createStatement().executeUpdate(sql);
			System.out.println("tillagd.");
			return true;
		} catch (SQLException e) {
			System.out.println("inte tillagd. SQL Message: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean deleteUser(User user) {
		if (user == null) {
			System.out.println("Användaren som skulle tas bort är null.");
			return false;
		} else if (getUser(user.getPersonnr()) == null) {
			System.out.println("Användare " + user.getFirstName()
					+ " inte borttagen, finns inte.");
			return false;
		} else if (!getBicycles(user).isEmpty()) {
			System.out.println("Användare " + user.getFirstName()
					+ " inte borttagen, har cyklar");
			return false;
		}
		System.out.print("Användare " + user.getFirstName() + " ");
		sql = "DELETE FROM users WHERE personnr = '" + user.getPersonnr() + "'";
		try {
			conn.createStatement().executeUpdate(sql);
			System.out.println("borttagen.");
			return true;
		} catch (SQLException e) {
			System.out.println("inte borttagen. SQL Message: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean updateUser(User user) {
		if (user == null) {
			System.out.println("Användare user som skulle uppdateras är null.");
			return false;
		}
		System.out.print("Användare " + user.getFirstName() + " ");
		sql = "UPDATE users SET " + "first_name = '" + user.getFirstName()
				+ "', " + "last_name = '" + user.getLastName() + "', "
				+ "mail = '" + user.getMail() + "', " + "phonenr = '"
				+ user.getPhonenr() + "', " + "pin = '" + user.getPIN() + "', "
				+ "reservedSlots = " + user.getReserverdSlots() + ", "
				+ "freeSlots = " + user.getFreeSlots() + ", "
				+ "nbrOfBicycles = " + user.getNbrOfBicycles() + " "
				+ "WHERE personnr = '" + user.getPersonnr() + "'";
		try {
			conn.createStatement().executeUpdate(sql);
			System.out.println("uppdaterad.");
			return true;
		} catch (SQLException e) {
			System.out.println("inte uppdaterad. SQL Message: "
					+ e.getMessage());
			return false;
		}
	}

	@Override
	public User getUser(String personnr) {
		if (personnr == null) {
//			System.out.println("Användare med personnr som skulle hittas är null.");
			return null;
		}
		ResultSet rs = extractUsers();
		if (rs == null)
			return null;
		try {
			rs.beforeFirst();
			while (rs.next()) {
				if (rs.getString(1).equals(personnr))
					return new User(rs.getString(1), rs.getString(2),
							rs.getString(3), rs.getString(4), rs.getString(5),
							rs.getString(6), rs.getInt(7), rs.getInt(8),
							rs.getInt(9));
			}
		} catch (SQLException e) {
//			System.out.println("SQL Message i getUser: " + e.getMessage());
		}
		return null;
	}

	@Override
	public User getUserWithPIN(String pin) {
		if (pin == null) {
//			System.out.println("Användare med pin som skulle hittas är null.");
			return null;
		}
		User user = null;
		ResultSet rs = extractUsers();
		if (rs == null)
			return null;
		try {
			rs.beforeFirst();
			while (rs.next()) {
				if (rs.getString(6).equals(pin))
					return new User(rs.getString(1), rs.getString(2),
							rs.getString(3), rs.getString(4), rs.getString(5),
							rs.getString(6), rs.getInt(7), rs.getInt(8),
							rs.getInt(9));
			}
		} catch (SQLException e) {
//			System.out.println("SQL Message i getUserWithPIN: "
//					+ e.getMessage());
		}
		return user;
	}

	@Override
	public List<Bicycle> getBicycles(User user) {
		if (user == null) {
//			System.out.println("Cyklar med användaren user som skulle hittas är null.");
			return null;
		}
		List<Bicycle> bicycles = new LinkedList<Bicycle>();
		ResultSet rs = extractBicycles();
		if (rs == null)
			return bicycles;
		try {
			rs.beforeFirst();
			while (rs.next()) {
				if (rs.getString(2).equals(user.getPersonnr())) {
					String barcode = rs.getString(1);
					Boolean deposited = rs.getBoolean(3);
					bicycles.add(new Bicycle(barcode, user, deposited));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
//			System.out.println("SQL Message getBicycle: " + e.getMessage());
		}
		return bicycles;
	}

	@Override
	public ResultSet extractUsers() {
		ResultSet rs = null;
		try {
			rs = conn.createStatement().executeQuery("SELECT * FROM users");
		} catch (SQLException e) {
//			System.out.println("Användartabell saknas. SQL Message: "
//					+ e.getMessage());
		}
		return rs;
	}

	@Override
	public Bicycle createBicycle(User user) throws Exception {
		if (user == null)
			throw new Exception("Användaren är felaktigt");
		String chars = "0123456789";
		while (true) {
			StringBuilder sb = new StringBuilder();
			while (sb.length() < 5) {
				int index = (int) (Math.random() * chars.length());
				sb.append(chars.charAt(index));
			}
			String barcode = sb.toString();
			if (getBicycle(barcode) == null) {
				return new Bicycle(barcode, user, false);
			}
		}
	}

	@Override
	public boolean insertBicycle(Bicycle bicycle) {
		if (bicycle == null) {
//			System.out.println("Cykeln bicycle som skulle läggas till är null.");
			return false;
		}
//		System.out.print("Cykeln med streckkod " + bicycle.getBarcode() + " ");
		sql = "INSERT INTO bicycles " + "VALUES (" + "'" + bicycle.getBarcode()
				+ "', " + "'" + bicycle.getOwner().getPersonnr() + "', "
				+ bicycle.isDeposited() + ")";
		try {
			conn.createStatement().executeUpdate(sql);
//			System.out.println("tillagd.");
			User user = bicycle.getOwner();
			user.addBicycle();
			updateUser(user);
			return true;
		} catch (SQLException e) {
//			System.out.println("inte tillagd. SQL Message: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean deleteBicycle(Bicycle bicycle) {
		if (bicycle == null) {
//			System.out.println("Cykeln bicycle som skulle tas bort är null.");
			return false;
		}
		String barcode = bicycle.getBarcode();
		if (getBicycle(barcode) == null) {
//			System.out.println("Cykeln med streckkod " + barcode
//					+ " inte borttagen, finns inte.");
			return false;
		}
//		System.out.print("Cykeln med användare "
//				+ bicycle.getOwner().getFirstName() + " ");
		sql = "DELETE FROM bicycles WHERE barcode = '" + barcode + "'";
		try {
			conn.createStatement().executeUpdate(sql);
//			System.out.println("borttagen.");
			User user = bicycle.getOwner();
			user.removeBicycle();
			updateUser(user);
			return true;
		} catch (SQLException e) {
//			System.out.println("inte borttagen. SQL Message: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean updateBicycle(Bicycle bicycle) {
		if (bicycle == null) {
//			System.out.println("Cykeln bicycle som skulle uppdateras är null.");
			return false;
		}
//		System.out.print("Cykel med ägare " + bicycle.getOwner().getFirstName()+ " ");
		sql = "UPDATE bicycles SET " + "user_personnr = '"
				+ bicycle.getOwner().getPersonnr() + "', " + "deposited = "
				+ bicycle.isDeposited() + " " + "WHERE barcode = '"
				+ bicycle.getBarcode() + "'";
		try {
			conn.createStatement().executeUpdate(sql);
//			System.out.println("uppdaterad.");
			return true;
		} catch (SQLException e) {
//			System.out.println("inte uppdaterad. SQL Message: "
//					+ e.getMessage());
			return false;
		}
	}
	*/
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Database db = new Database();
		db.dropTables();
		db.createTables();
	}
}