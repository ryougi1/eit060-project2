package database;

import java.util.List;

import types.*;


public class Record {
	
	private int recordNbr;
	private Patient patient;
	private Nurse nurse;
	private Doctor doctor;
	private String division;
	private List<String> data;
	
	public Record(int recordNbr, Patient patient, Nurse nurse, Doctor doctor, String division, List<String> data) {
		this.recordNbr = recordNbr;
		this.patient = patient;
		this.nurse = nurse;
		this.doctor = doctor;
		this.division = division;
		this.data = data;
	}
	
	public int getRecordNbr() 	{	return recordNbr;	}
	public Patient getPatient() {	return patient;		}
	public Nurse getNurse() 	{	return nurse;		}
	public Doctor getDoctor() 	{	return doctor;		}
	public String getDivision()	{	return division;	}
	
	public String getData() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.size(); i++) {
			sb.append(data.get(i) + "\n");
		}
		return sb.toString();
	}
}
