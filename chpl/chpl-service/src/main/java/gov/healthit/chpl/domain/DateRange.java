package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

public class DateRange implements Serializable {
	private static final long serialVersionUID = 6702794674787630221L;
	Date startDate;
	Date endDate;

	public DateRange(Date startDate, Date endDate) {
		setStartDate(startDate);
		setEndDate(endDate);
	}

	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}
