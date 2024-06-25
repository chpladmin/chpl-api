package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
public class CriteriaMigrationReportService {
    public static final Long HTI1_REPORT_ID = 2L;

    private CriteriaMigrationReportDAO criteriaMigrationReportDAO;
    private OriginalToUpdatedCriterionCountService originalToUpdatedCriterionCountService;
    private OriginalCriterionCountService originalCriterionCountService;
    private UpdatedCriterionCountService updatedCriterionCountService;

    @Autowired
    public CriteriaMigrationReportService(CriteriaMigrationReportDAO criteriaMigrationReportDAO,
            OriginalToUpdatedCriterionCountService originalToUpdatedCriterionCountService,
            OriginalCriterionCountService originalCriterionCountService,
            UpdatedCriterionCountService updatedCriterionCountService) {

        this.criteriaMigrationReportDAO = criteriaMigrationReportDAO;
        this.originalToUpdatedCriterionCountService = originalToUpdatedCriterionCountService;
        this.originalCriterionCountService = originalCriterionCountService;
        this.updatedCriterionCountService = updatedCriterionCountService;
    }

    public CriteriaMigrationReport getReport(Long criteriaMigrationReportId) {
        return criteriaMigrationReportDAO.getCriteriaMigrationReport(criteriaMigrationReportId);
    }

    @Transactional
    public void gatherDataForReport(Logger logger) {
        List<CriteriaMigrationReport> reports = criteriaMigrationReportDAO.getAllCriteriaMigrationReportsWithoutCounts();

        reports.forEach(report -> {
            report.getCriteriaMigrationDefinitions().forEach(cmd -> {
                Integer originalToUpdatedCount = originalToUpdatedCriterionCountService.generateCountForDate(cmd, report.getStartDate(), logger);
                Integer originalCount = originalCriterionCountService.generateCountForDate(cmd, LocalDate.now(), report.getStartDate(), logger);
                Integer updatedCount = updatedCriterionCountService.generateCountForDate(cmd, LocalDate.now(), report.getStartDate(), logger);

                logger.info("Count of {} / {} for {} to {} is {}, {}, {}",
                        cmd.getOriginalCriterion().getNumber(),
                        cmd.getUpdatedCriterion().getNumber(),
                        report.getStartDate(),
                        LocalDate.now(),
                        originalCount,
                        updatedCount - originalToUpdatedCount,
                        originalToUpdatedCount);

                criteriaMigrationReportDAO.create(CriteriaMigrationCount.builder()
                        .criteriaMigrationDefinition(cmd)
                        .reportDate(LocalDate.now())
                        .originalCriterionCount(originalCount)
                        .updatedCriterionCount(updatedCount - originalToUpdatedCount)
                        .originalToUpdatedCriterionCount(originalToUpdatedCount)
                        .build());
            });
        });
    }
}
