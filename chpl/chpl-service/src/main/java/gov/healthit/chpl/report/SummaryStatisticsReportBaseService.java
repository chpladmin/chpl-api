package gov.healthit.chpl.report;

import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.IdNamePair;
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
        return summaryStatisticsDAO.getCurrentSummaryStatistics();
    }

    private CertificationBody getAcb(Long acbId) {
        try {
            return certificationBodyManager.getById(acbId);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error retrieving ACB with ID " + acbId + ": " + e.getMessage());
            return null;
        }
    }

    private CertificationBody getAcb(String acbName) {
        return certificationBodyManager.getAll().stream()
                .filter(acb -> acb.getName().equals(acbName))
                .findFirst()
                .orElse(null);
    }

    protected String getGeneratedAcbName(Long acbId) {
        CertificationBody certificationBody = getAcb(acbId);
        if (certificationBody == null) {
            return "Unknown";
        }
        return certificationBody.getName() + (certificationBody.isRetired() ? " (Retired)" : "");
    }

    protected String getGeneratedAcbName(String acbName) {
        CertificationBody certificationBody = getAcb(acbName);
        if (certificationBody == null) {
            return "Unknown";
        }
        return certificationBody.getName() + (certificationBody.isRetired() ? " (Retired)" : "");
    }

    protected IdNamePair updateAcbNameBasedOnRetired(IdNamePair acb) {
        CertificationBody certificationBody = getAcb(acb.getId());
        return acb.toBuilder()
                .name(acb.getName() + (certificationBody.isRetired() ? " (Retired)" : ""))
                .build();
    }
}
