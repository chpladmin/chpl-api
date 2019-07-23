package gov.healthit.chpl.dao.surveillance.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.SurveillanceTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceSummaryDTO;
import gov.healthit.chpl.entity.CertificationStatusType;

@Repository("complaintSummaryDao")
public class ComplaintsSummaryDAOImpl extends BaseDAOImpl implements ComplaintSummaryDAO {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintsSummaryDAOImpl.class);

    @Override
    public Long getTotalComplaints(Long acbId, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getTotalComplaintsFromOnc(Long acbId, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getTotalComplaintsResultingInSurveillance(Long acbId, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getTotalSurveillanceRelatedToComplaints(Long acbId, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getTotalComplaintsResultingInNonconformities(Long acbId, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getTotalNonconformitiesRelatedToComplaints(Long acbId, Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

}
