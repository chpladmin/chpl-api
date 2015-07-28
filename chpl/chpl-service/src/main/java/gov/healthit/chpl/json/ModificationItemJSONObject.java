package gov.healthit.chpl.json;

import java.util.Date;

public class ModificationItemJSONObject {

	
	private String subject;
	private String action;
	private Date date;
	
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
}
