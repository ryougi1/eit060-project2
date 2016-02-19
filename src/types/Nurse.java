package types;

public class Nurse extends User {
	
	private String division;
	
	public Nurse(String name, String pNbr, String division) {
		super(name, pNbr);
		this.division = division;
	}
	
	public String getDivision() {
		return division;
	}
}
