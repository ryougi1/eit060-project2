package database;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import types.*;


public class Record {
	
	private List<String> data;
	private Patient pat;
	private Doctor doc;
	private Nurse nur;
	
	public Record(Patient pat, Doctor doc, Nurse nur, List<String> data) {
		this.pat = pat;
		this.doc = doc;
		this.nur = nur;
		this.data = data;
	}

	public String getData() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.size(); i++) {
			sb.append(data.get(i) + "\n");
		}
		return sb.toString();
	}

	public Patient getPat() {
		return pat;
	}

	public Doctor getDoc() {
		return doc;
	}

	public Nurse getNur() {
		return nur;
	}	
}
