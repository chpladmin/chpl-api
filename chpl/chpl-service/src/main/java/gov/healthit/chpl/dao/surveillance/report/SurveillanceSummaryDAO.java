package gov.healthit.chpl.dao.surveillance.report;

import java.util.Date;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceSummaryDTO;

public interface SurveillanceSummaryDAO {
    public SurveillanceSummaryDTO getCountOfListingsSurveilledByType(
            Long acbId, Date startDate, Date endDate);
    public SurveillanceSummaryDTO getCountOfSurveillanceProcessTypesBySurveillanceType(Long acbId,
            List<SurveillanceProcessTypeDTO> procTypes, Date startDate, Date endDate);
    public SurveillanceSummaryDTO getCountOfSurveillanceOutcomesBySurveillanceType(Long acbId,
            List<SurveillanceOutcomeDTO> outcomes, Date startDate, Date endDate);
}
