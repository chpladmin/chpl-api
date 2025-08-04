package gov.healthit.chpl.report.criteriauptodate;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;

@Log4j2
@Component
public class CriteriaUpToDateReportService {
    private static final int ONE_YEAR_IN_MONTHS = 12;

    private CriteriaUpToDateStatusReportDateService reportDateService;
    private CertificationCriteriaManager criteriaManager;
    private UpdatedCriterionStatusReportDao updatedCriteriaStatusReportDao;
    private SummaryStatisticsDAO summaryStatisticsDao;
    private List<CertificationStatus> allCertificationStatuses;

    @Autowired
    public CriteriaUpToDateReportService(CriteriaUpToDateStatusReportDateService reportDateService,
            CertificationCriteriaManager criteriaManager,
            UpdatedCriterionStatusReportDao updatedCriteriaStatusReportDao,
            SummaryStatisticsDAO summaryStatisticsDao,
            CertificationStatusDAO certificationStatusDao) {
        this.reportDateService = reportDateService;
        this.criteriaManager = criteriaManager;
        this.updatedCriteriaStatusReportDao = updatedCriteriaStatusReportDao;
        this.summaryStatisticsDao = summaryStatisticsDao;
        this.allCertificationStatuses = certificationStatusDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<CriteriaUpToDateReport> getMonthlyCriteriaUpToDateReports() {
        List<LocalDate> allReportDates = reportDateService.calculateAllMonthsOfReportDatesBasedOnAvailableData(ONE_YEAR_IN_MONTHS);
        LOGGER.info("Generating criteria up-to-date counts for the past year using report dates: "
                + Util.joinListGrammatically(allReportDates.stream().map(reportDate -> reportDate.toString()).collect(Collectors.toList())));

        return allReportDates.stream()
            .flatMap(reportDate -> getAllCriteriaUpToDateReports(reportDate).stream())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CriteriaUpToDateReport> getAllCriteriaUpToDateReports(LocalDate reportDate) {
        List<CertificationCriterion> activeCriteria = criteriaManager.getActiveToday();
        StatisticsSnapshot statisticsSnapshot = getSummaryStatisticsSnapshotForDate(reportDate);
        List<UpdatedCriterionStatusReport> criteriaStatusReports = updatedCriteriaStatusReportDao.getUpdatedCriterionStatusReportsByDay(reportDate);

        return activeCriteria.stream()
            .map(criterion -> buildCriteriaUpToDateReport(criterion, statisticsSnapshot, criteriaStatusReports, reportDate))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CriteriaUpToDateReport> getAllCriteriaUpToDateReports() {
        LocalDate reportDate = reportDateService.findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(
                LocalDate.now());

        List<CertificationCriterion> activeCriteria = criteriaManager.getActiveToday();
        StatisticsSnapshot statisticsSnapshot = getSummaryStatisticsSnapshotForDate(reportDate);
        List<UpdatedCriterionStatusReport> criteriaStatusReports = updatedCriteriaStatusReportDao.getUpdatedCriterionStatusReportsByDay(reportDate);

        return activeCriteria.stream()
            .map(criterion -> buildCriteriaUpToDateReport(criterion, statisticsSnapshot, criteriaStatusReports, reportDate))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ListingNotUpToDateReport> getAllListingNotUpToDateReports() {
        LocalDate reportDate = reportDateService.findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(
                LocalDate.now());

        List<UpdatedCriterionStatusReport> criteriaStatusReports = updatedCriteriaStatusReportDao.getUpdatedCriterionStatusReportsByDay(reportDate);

        Set<ListingNotUpToDateReport> results = criteriaStatusReports.stream()
            .map(report -> ListingNotUpToDateReport.builder()
                    .criterion(report.getCertificationCriterion())
                    .chplProductNumber(report.getChplProductNumber())
                    .certifiedProductId(report.getCertifiedProductId())
                    .build())
            .collect(Collectors.toSet());
        return results.stream().collect(Collectors.toList());
    }

    private CriteriaUpToDateReport buildCriteriaUpToDateReport(CertificationCriterion criterion, StatisticsSnapshot statisticsSnapshot,
            List<UpdatedCriterionStatusReport> criteriaStatusReports, LocalDate reportDate) {
        long totalActiveListingsWithCriterion = calculateActiveListingsWithCriterionCount(statisticsSnapshot, criterion);
        long totalListingsRequiringUpdates = calculateListingsRequiringUpdatesCount(criteriaStatusReports, criterion, reportDate);

        return CriteriaUpToDateReport.builder()
                .accurateAsOfDate(reportDate)
                .activeListingsAttestingToCriterionCount(totalActiveListingsWithCriterion)
                .activeListingsUpToDateOnCriterionCount(totalActiveListingsWithCriterion - totalListingsRequiringUpdates)
                .criterion(criterion)
                .build();
    }

    private StatisticsSnapshot getSummaryStatisticsSnapshotForDate(LocalDate reportDate) {
        return summaryStatisticsDao.getSummaryStatistics(reportDate);
    }

    private long calculateActiveListingsWithCriterionCount(StatisticsSnapshot statisticsSnapshot, CertificationCriterion criterion) {
        if (statisticsSnapshot == null
                || CollectionUtils.isEmpty(statisticsSnapshot.getAttestedCriterionStatistics())) {
            LOGGER.info("No attested criterion statistics were found in the statistics snapshot for date " + statisticsSnapshot.getSnapshotDate());
            return 0;
        }
        List<Long> activeStatusIds = allCertificationStatuses.stream()
                .filter(certStatus -> CertificationStatusUtil.getActiveStatusNames().contains(certStatus.getName()))
                .map(certStatus -> certStatus.getId())
                .collect(Collectors.toList());

        long countOfListingsInActiveStatusWithCriteria = statisticsSnapshot.getAttestedCriterionStatistics().stream()
            .filter(stat -> stat.getCertificationCriterionId().equals(criterion.getId())
                    && activeStatusIds.contains(stat.getListingStatusId()))
            .map(stat -> stat.getListingIds().size())
            .collect(Collectors.summingInt(Integer::intValue));
        return countOfListingsInActiveStatusWithCriteria;
    }

    private long calculateListingsRequiringUpdatesCount(List<UpdatedCriterionStatusReport> reports, CertificationCriterion criterion,
            LocalDate reportDate) {
        List<UpdatedCriterionStatusReport> reportsForCriterion = reports.stream()
            .filter(report -> report.getCertificationCriterion().getId().equals(criterion.getId()))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(reportsForCriterion)) {
            LOGGER.info("No updated criteria status reports were found for " + Util.formatCriteriaNumber(criterion) + " from date " + reportDate);
            return 0;
        }

        return StreamEx.of(reportsForCriterion)
                .distinct(UpdatedCriterionStatusReport::getCertifiedProductId)
                .count();
    }
}
