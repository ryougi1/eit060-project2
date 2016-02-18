package types;

public abstract class User {
	protected String name, pNbr, password;
	
	public User(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public User(String name, String pNbr, String password) {
		this.name = name;
		this.pNbr = pNbr;
	}

	public String getName() {
		return name;
	}
	
	public String getPNbr() {
		return pNbr;
	}
}
