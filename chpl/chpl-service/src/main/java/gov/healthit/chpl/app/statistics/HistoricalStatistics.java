package gov.healthit.chpl.app.statistics;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.Statistics;

public class HistoricalStatistics extends Statistics {
	private static final long serialVersionUID = 1935145371792355667L;
	private DateRange dateRange;
	
	public HistoricalStatistics(){}

	public DateRange getDateRange() {
		return dateRange;
	}

	public void setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
	}
	
}
