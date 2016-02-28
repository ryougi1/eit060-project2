package database;

import types.*;


public class Record {
	
	private int recordNbr;
	private Patient patient;
	private Nurse nurse;
	private Doctor doctor;
	private String division;
	private StringBuilder data;
	
	public Record(int recordNbr, Patient patient, Nurse nurse, Doctor doctor, String division, String data) {
		this.recordNbr = recordNbr;
		this.patient = patient;
		this.nurse = nurse;
		this.doctor = doctor;
		this.division = division;
		this.data = new StringBuilder();
		this.data.append(data);
	}
	
	public int getRecordNbr() 	{	return recordNbr;	}
	public Patient getPatient() {	return patient;		}
	public Nurse getNurse() 	{	return nurse;		}
	public Doctor getDoctor() 	{	return doctor;		}
	public String getDivision()	{	return division;	}
	
	public String getData() {
		return data.toString();
	}
	
	public void appendData(String data) {
		this.data.append(" | " + data);
	}
	
	@Override
	public String toString() {
		return recordNbr + " | " + patient.getPNbr() + " | " + nurse.getPNbr() + " | " + doctor.getPNbr() + " | " + division + " | Data: " + data.toString();
	}
}
