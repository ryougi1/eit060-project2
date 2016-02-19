package types;

public abstract class User {
	protected String name, pNbr;
	
	public User(String name) {
		this.name = name;
	}

	public User(String name, String pNbr) {
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
