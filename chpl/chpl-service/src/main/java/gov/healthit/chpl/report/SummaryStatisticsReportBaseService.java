package gov.healthit.chpl.report;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SummaryStatisticsReportBaseService {
    private CertificationBodyManager certificationBodyManager;
    private SummaryStatisticsDAO summaryStatisticsDAO;

    public SummaryStatisticsReportBaseService(SummaryStatisticsDAO summaryStatisticsDAO, CertificationBodyManager certificationBodyManager) {
        this.summaryStatisticsDAO = summaryStatisticsDAO;
        this.certificationBodyManager = certificationBodyManager;
    }

    protected StatisticsSnapshot getStatistics() {
        try {
            SummaryStatisticsEntity summaryStatistics = summaryStatisticsDAO.getCurrentSummaryStatistics();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(summaryStatistics.getSummaryStatistics(), StatisticsSnapshot.class);
        } catch (Exception e) {
            LOGGER.error("Error retrieving summary statistics: {}", e.getMessage());
            return null;
        }
    }

    private CertificationBody getAcb(Long acbId) {
        try {
            return certificationBodyManager.getById(acbId);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error retrieving ACB with ID " + acbId + ": " + e.getMessage());
            return null;
        }
    }

    protected String getGeneratedAcbName(Long acbId) {
        CertificationBody certificationBody = getAcb(acbId);
        return certificationBody.getName() + (certificationBody.isRetired() ? " (Retired)" : "");
    }

    protected IdNamePair updateAcbNameBasedOnRetired(IdNamePair acb) {
        CertificationBody certificationBody = getAcb(acb.getId());
        return acb.toBuilder()
                .name(acb.getName() + (certificationBody.isRetired() ? " (Retired)" : ""))
                .build();
    }
}
