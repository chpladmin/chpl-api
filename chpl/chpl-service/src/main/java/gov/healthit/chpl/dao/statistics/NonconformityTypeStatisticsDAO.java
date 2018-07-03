package gov.healthit.chpl.dao.statistics;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.SurveillanceNonconformity;

import java.util.List;

public interface NonconformityTypeStatisticsDAO {
	Long getAllNonconformities(DateRange dateRange);
	Long getAllNonconformities2014Edition(DateRange dateRange);
	Long getAllNonconformities2015Edition(DateRange dateRange);
	Long getAllProgramNonconformities(DateRange dateRange);
}
