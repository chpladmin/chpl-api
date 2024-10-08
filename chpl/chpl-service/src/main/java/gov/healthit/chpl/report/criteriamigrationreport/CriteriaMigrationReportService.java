package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CriteriaMigrationReportService {
    public static final Long HTI1_REPORT_ID = 2L;
    private static final Integer MONTHS_IN_YEAR = 12;
    private static final Integer MAX_DAYS_TO_CHECK_FOR_DATA = 7;

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
        List<Todd> todds = new ArrayList<Todd>();
        CriteriaMigrationReport cmr = criteriaMigrationReportDAO.getCriteriaMigrationReportWithoutCounts(criteriaMigrationReportId);

        for (LocalDate reportDate : getTargetDatesForPastYear()) {
            Todd todd = getToddAtOrNearReport(cmr.getId(), reportDate);
            todd.setOriginalCriterion(cmr.getCriteriaMigrationDefinitions().get(0).getOriginalCriterion());
            todd.setUpdatedCriterion(cmr.getCriteriaMigrationDefinitions().get(0).getUpdatedCriterion());
            todds.add(todd);
        }

        return todds.stream().sorted(Comparator.comparing(Todd::getReportDate)).toList();
    }

    private Todd getToddAtOrNearReport(Long criteriaMigrationReportId, LocalDate targetDate) {
        LocalDate originalTargetDate = targetDate;
        for (Integer offset : getDayOffsetList()) {
            Optional<CriteriaMigrationCount> criteriaMigrationCount =
                    criteriaMigrationReportDAO.getCriteriaMigrationCount(criteriaMigrationReportId, targetDate);
            if (criteriaMigrationCount.isPresent()) {
                return Todd.builder()
                        .newCertificationCount(criteriaMigrationCount.get().getUpdatedCriterionCount())
                        .requiresUpdateCount(criteriaMigrationCount.get().getOriginalCriterionCount())
                        .upgradedCertificationCount(criteriaMigrationCount.get().getOriginalToUpdatedCriterionCount())
                        .percentUpdated(getPercentUpdate(criteriaMigrationCount.get()))
                        .reportDate(criteriaMigrationCount.get().getReportDate())
                        .build();
            }
            targetDate.plusDays(offset);
        }
        return Todd.builder()
                .newCertificationCount(120)
                .requiresUpdateCount(120)
                .upgradedCertificationCount(120)
                .percentUpdated(66.6d)
                .reportDate(originalTargetDate)
                .build();
    }

    private List<Integer> getDayOffsetList() {
        //This generates a list in the pattern 0, -1, 1, -2, 2, -3, 3 ....
        List<Integer> dayOffsets = new ArrayList<Integer>();

        for (Integer i = 0; i < MAX_DAYS_TO_CHECK_FOR_DATA; i++) {
            Integer offset = i / 2;
            if (i % 2 == 1) {
                offset = offset * -1;
            }
            dayOffsets.add(offset);
        }
        return dayOffsets;
    }


    private List<LocalDate> getTargetDatesForPastYear() {
        List<LocalDate> targetDates = new ArrayList<LocalDate>();
        for (Integer i = 0; i < MONTHS_IN_YEAR; ++i) {
            //targetDates.add(LocalDate.now().minusMonths(i).with(TemporalAdjusters.firstDayOfMonth()));
            targetDates.add(LocalDate.now().minusMonths(i).with(TemporalAdjusters.firstDayOfMonth()).plusDays(LocalDate.now().getDayOfMonth()-1));
        }
        return targetDates;
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
            return updatedCount.doubleValue() / totalCount.doubleValue() * 100;
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
