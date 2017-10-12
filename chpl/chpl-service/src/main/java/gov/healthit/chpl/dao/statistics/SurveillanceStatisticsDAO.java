package gov.healthit.chpl.dao.statistics;

import gov.healthit.chpl.domain.DateRange;

public interface SurveillanceStatisticsDAO {
    Long getTotalSurveillanceActivities(DateRange dateRange);

    Long getTotalOpenSurveillanceActivities(DateRange dateRange);

    Long getTotalClosedSurveillanceActivities(DateRange dateRange);

    Long getTotalNonConformities(DateRange dateRange);

    Long getTotalOpenNonconformities(DateRange dateRange);

    Long getTotalClosedNonconformities(DateRange dateRange);
}
