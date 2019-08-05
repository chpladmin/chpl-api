package gov.healthit.chpl.dao.surveillance.report;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.SurveillanceTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceSummaryDTO;
import gov.healthit.chpl.entity.listing.ListingWithPrivilegedSurveillanceEntity;

public interface SurveillanceSummaryDAO {
    public SurveillanceSummaryDTO getCountOfListingsSurveilledByType(
            Long acbId, Date startDate, Date endDate);
    public SurveillanceSummaryDTO getCountOfSurveillanceProcessTypesBySurveillanceType(Long acbId,
            List<SurveillanceProcessTypeDTO> procTypes, Date startDate, Date endDate);
    public SurveillanceSummaryDTO getCountOfSurveillanceOutcomesBySurveillanceType(Long acbId,
            List<SurveillanceOutcomeDTO> outcomes, Date startDate, Date endDate);
    public List<QuarterlyReportRelevantListingDTO> getListingsBySurveillanceType(Long acbId,
            SurveillanceTypeDTO survType, Date startDate, Date endDate);
}
