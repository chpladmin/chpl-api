package gov.healthit.chpl.dao.statistics;

import java.util.List;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;

public interface SurveillanceStatisticsDAO {
    Long getTotalSurveillanceActivities(DateRange dateRange);

    Long getTotalOpenSurveillanceActivities(DateRange dateRange);

    Long getTotalClosedSurveillanceActivities(DateRange dateRange);

    Long getTotalNonConformities(DateRange dateRange);

    Long getTotalOpenNonconformities(DateRange dateRange);

    Long getTotalClosedNonconformities(DateRange dateRange);

	List<NonconformityTypeStatisticsDTO> getAllNonconformitiesByCriterion();
}
