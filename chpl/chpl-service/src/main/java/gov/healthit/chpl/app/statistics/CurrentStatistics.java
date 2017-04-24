package gov.healthit.chpl.app.statistics;

import java.util.Date;

import gov.healthit.chpl.domain.Statistics;

public class CurrentStatistics extends Statistics {
	private static final long serialVersionUID = 7589642212881685646L;
	private Date date;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
}
