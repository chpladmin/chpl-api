package gov.healthit.chpl.dao.surveillance.report;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceSummaryDTO;
import gov.healthit.chpl.entity.CertificationStatusType;

public interface ComplaintSummaryDAO {
    public Long getTotalComplaints(Long acbId, Date startDate, Date endDate);
    public Long getTotalComplaintsFromOnc(Long acbId, Date startDate, Date endDate);
    public Long getTotalComplaintsResultingInSurveillance(Long acbId, Date startDate, Date endDate);
    public Long getTotalSurveillanceRelatedToComplaints(Long acbId, Date startDate, Date endDate);
    public Long getTotalComplaintsResultingInNonconformities(Long acbId, Date startDate, Date endDate);
    public Long getTotalNonconformitiesRelatedToComplaints(Long acbId, Date startDate, Date endDate);
}
