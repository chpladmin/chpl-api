package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
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

    public List<Todd> getHtiReportData(Long criteriaMigrationReportId) {
        CriteriaMigrationReport cmr = criteriaMigrationReportDAO.getCriteriaMigrationReport(criteriaMigrationReportId);

        List<Todd> real = cmr.getCriteriaMigrationDefinitions().get(0).getCriteriaMigrationCounts().stream()
                .sorted(Comparator.comparing(CriteriaMigrationCount::getReportDate).reversed())
                .filter(cmc -> cmc.getReportDate().getDayOfMonth() == 1)
                .map(cmc -> Todd.builder()
                        .originalCriterion(cmr.getCriteriaMigrationDefinitions().get(0).getOriginalCriterion())
                        .updatedCriterion(cmr.getCriteriaMigrationDefinitions().get(0).getUpdatedCriterion())
                        .reportDate(cmc.getReportDate())
                        .newCertificationCount(cmc.getUpdatedCriterionCount())
                        .upgradedCertificationCount(cmc.getOriginalToUpdatedCriterionCount())
                        .requiresUpdateCount(cmc.getOriginalCriterionCount())
                        .percentUpdated(getPercentUpdate(cmc))
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        LocalDate checkDate = real.get(0).getReportDate();
        CertificationCriterion originalCriterion = real.get(0).getOriginalCriterion();
        CertificationCriterion updatedCriterion = real.get(0).getUpdatedCriterion();
        while (real.size() < 12) {
            if (!doesReportDateExistInList(real, checkDate)) {
                real.add(Todd.builder()
                        .originalCriterion(originalCriterion)
                        .updatedCriterion(updatedCriterion)
                        .reportDate(checkDate)
                        .newCertificationCount(200)
                        .upgradedCertificationCount(100)
                        .requiresUpdateCount(40)
                        .percentUpdated(Double.valueOf("42.454"))
                        .build());

            }
            checkDate = checkDate.minusMonths(1);
        }

        return real.stream().sorted(Comparator.comparing(Todd::getReportDate)).toList();
    }

    private Boolean doesReportDateExistInList(List<Todd> list, LocalDate reportDate) {
        return list.stream()
                .filter(t -> t.getReportDate().equals(reportDate))
                .findAny()
                .isPresent();
    }

    private Double getPercentUpdate(CriteriaMigrationCount criteriaMigratrionCount) {
        Integer updatedCount = criteriaMigratrionCount.getOriginalToUpdatedCriterionCount()
                + criteriaMigratrionCount.getUpdatedCriterionCount();
        Integer totalCount = criteriaMigratrionCount.getOriginalToUpdatedCriterionCount()
                + criteriaMigratrionCount.getUpdatedCriterionCount()
                + criteriaMigratrionCount.getOriginalCriterionCount();

        if (totalCount.equals(0)) {
            return Double.valueOf("0");
        } else {
            return (updatedCount.doubleValue() / totalCount.doubleValue()) * 100;
        }
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
