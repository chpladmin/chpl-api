package gov.healthit.chpl.dao.statistics;

import gov.healthit.chpl.domain.DateRange;

public interface SurveillanceStatisticsDAO {
    public Long getTotalSurveillanceActivities(DateRange dateRange);

    public Long getTotalOpenSurveillanceActivities(DateRange dateRange);

    public Long getTotalClosedSurveillanceActivities(DateRange dateRange);

    public Long getTotalNonConformities(DateRange dateRange);

    public Long getTotalOpenNonconformities(DateRange dateRange);

    public Long getTotalClosedNonconformities(DateRange dateRange);
}
