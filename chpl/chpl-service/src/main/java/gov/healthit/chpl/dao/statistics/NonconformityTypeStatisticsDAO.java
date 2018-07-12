package gov.healthit.chpl.dao.statistics;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import java.util.List;

public interface NonconformityTypeStatisticsDAO {
	List<NonconformityTypeStatisticsDTO> getAllNonconformityStatistics(DateRange dateRange);
	public void create(NonconformityTypeStatisticsDTO dto);
}
