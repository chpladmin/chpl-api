package gov.healthit.chpl.dao.surveillance.report;

import java.util.Date;

import gov.healthit.chpl.dto.SurveillanceTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;

public interface SurveillanceSummaryDAO {
    public int getCountOfListingsSurveilled(Long acbId, SurveillanceTypeDTO survType, Date startDate, Date endDate);
    public int getCountOfSurveillancesByProcessType(Long acbId, SurveillanceProcessTypeDTO procType,
            SurveillanceTypeDTO survType, Date startDate, Date endDate);
}
