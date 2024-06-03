package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CriteriaMigrationReportService {

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


    @Transactional
    public void gatherDataForReport() {
        List<CriteriaMigrationReport> reports = criteriaMigrationReportDAO.getAllCriteriaMigrationReportsWithoutCounts();

        reports.forEach(report -> {
            report.getCriteriaMigrationDefinitions().forEach(cmd -> {
                Integer originalToUpdatedCount = originalToUpdatedCriterionCountService.generateCountForDate(cmd, LocalDate.now(), report.getStartDate());
                Integer originalCount = originalCriterionCountService.generateCountForDate(cmd, LocalDate.now(), report.getStartDate());
                Integer updatedCount = updatedCriterionCountService.generateCountForDate(cmd, LocalDate.now(), report.getStartDate());

                LOGGER.info("Count of {} for {} to {} is {}, {}, {}",
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
