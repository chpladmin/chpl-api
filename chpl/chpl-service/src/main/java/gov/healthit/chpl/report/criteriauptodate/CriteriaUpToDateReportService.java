package gov.healthit.chpl.report.criteriauptodate;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.DateUtil;
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
    private String unformattedListingDetailsUrl;
    private String unformattedDeveloperDetailsUrl;

    @Autowired
    public CriteriaUpToDateReportService(CriteriaUpToDateStatusReportDateService reportDateService,
            CertificationCriteriaManager criteriaManager,
            UpdatedCriterionStatusReportDao updatedCriteriaStatusReportDao,
            SummaryStatisticsDAO summaryStatisticsDao,
            CertificationStatusDAO certificationStatusDao,
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${listingDetailsUrlPart}") String listingDetailsUrlPart,
            @Value("${developerUrlPart}") String developerDetailsUrlPart) {
        this.reportDateService = reportDateService;
        this.criteriaManager = criteriaManager;
        this.updatedCriteriaStatusReportDao = updatedCriteriaStatusReportDao;
        this.summaryStatisticsDao = summaryStatisticsDao;
        this.allCertificationStatuses = certificationStatusDao.findAll();
        this.unformattedListingDetailsUrl = chplUrlBegin + listingDetailsUrlPart;
        this.unformattedDeveloperDetailsUrl = chplUrlBegin + developerDetailsUrlPart;

    }

    @Transactional(readOnly = true)
    public List<CriteriaUpToDateReport> getMonthlyCriteriaUpToDateReports(List<CertificationBody> acbs,
            Pair<LocalDate, LocalDate> requiredByDateRange) {
        List<LocalDate> allReportDates = reportDateService.calculateAllMonthsOfReportDatesBasedOnAvailableData(ONE_YEAR_IN_MONTHS);
        LOGGER.info("Generating criteria up-to-date counts for the past year using report dates: "
                + Util.joinListGrammatically(allReportDates.stream().map(reportDate -> reportDate.toString()).collect(Collectors.toList())));

        return allReportDates.stream()
            .flatMap(reportDate -> getAllCriteriaUpToDateReports(reportDate, acbs, requiredByDateRange).stream())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CriteriaUpToDateReport> getAllCriteriaUpToDateReports(LocalDate reportDate, List<CertificationBody> acbs,
            Pair<LocalDate, LocalDate> requiredByDateRange) {
        List<CertificationCriterion> activeCriteria = criteriaManager.getActiveToday();
        StatisticsSnapshot statisticsSnapshot = getSummaryStatisticsSnapshotForDate(reportDate);
        List<UpdatedCriterionStatusReport> criteriaStatusReports = updatedCriteriaStatusReportDao.getUpdatedCriterionStatusReportsByDay(reportDate).stream()
                .filter(dataRecord -> DateUtil.isDateBetweenInclusive(requiredByDateRange, dataRecord.getRequiredDay()))
                .collect(Collectors.toList());

        return activeCriteria.stream()
            .flatMap(criterion ->
                buildCriteriaUpToDateReports(criterion, acbs, statisticsSnapshot, criteriaStatusReports, reportDate).stream())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CriteriaUpToDateReport> getAllCriteriaUpToDateReports(List<CertificationBody> acbs) {
        LocalDate reportDate = reportDateService.findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(
                LocalDate.now());

        List<CertificationCriterion> activeCriteria = criteriaManager.getActiveToday();
        StatisticsSnapshot statisticsSnapshot = getSummaryStatisticsSnapshotForDate(reportDate);
        List<UpdatedCriterionStatusReport> criteriaStatusReports = updatedCriteriaStatusReportDao.getUpdatedCriterionStatusReportsByDay(reportDate);

        return activeCriteria.stream()
            .flatMap(criterion ->
                buildCriteriaUpToDateReports(criterion, acbs, statisticsSnapshot, criteriaStatusReports, reportDate).stream())
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
                    .listingDetailsUrl(String.format(unformattedListingDetailsUrl, report.getCertifiedProductId()))
                    .developerName(report.getDeveloper())
                    .developerDetailsUrl(String.format(unformattedDeveloperDetailsUrl, report.getDeveloperId()))
                    .requiredDay(report.getRequiredDay())
                    .build())
            .collect(Collectors.toSet());
        return results.stream().collect(Collectors.toList());
    }

    private List<CriteriaUpToDateReport> buildCriteriaUpToDateReports(CertificationCriterion criterion,
            List<CertificationBody> acbs,
            StatisticsSnapshot statisticsSnapshot,
            List<UpdatedCriterionStatusReport> criteriaStatusReports,
            LocalDate reportDate) {
        return acbs.stream()
            .map(acb -> buildCriteriaUpToDateReport(criterion, acb, statisticsSnapshot, criteriaStatusReports, reportDate))
            .collect(Collectors.toList());
    }

    private CriteriaUpToDateReport buildCriteriaUpToDateReport(CertificationCriterion criterion,
            CertificationBody acb,
            StatisticsSnapshot statisticsSnapshot,
            List<UpdatedCriterionStatusReport> criteriaStatusReports,
            LocalDate reportDate) {
        long totalActiveListingsWithCriterion = calculateActiveListingsWithCriterionCount(statisticsSnapshot, criterion, Stream.of(acb.getId()).toList());
        long totalListingsRequiringUpdates = calculateListingsRequiringUpdatesCount(criteriaStatusReports, criterion, Stream.of(acb.getId()).toList(), reportDate);

        return CriteriaUpToDateReport.builder()
                .acbId(acb.getId())
                .acbName(acb.getName())
                .accurateAsOfDate(reportDate)
                .activeListingsAttestingToCriterionCount(totalActiveListingsWithCriterion)
                .activeListingsUpToDateOnCriterionCount(totalActiveListingsWithCriterion - totalListingsRequiringUpdates)
                .criterion(criterion)
                .build();
    }

    private StatisticsSnapshot getSummaryStatisticsSnapshotForDate(LocalDate reportDate) {
        return summaryStatisticsDao.getSummaryStatistics(reportDate);
    }

    private long calculateActiveListingsWithCriterionCount(StatisticsSnapshot statisticsSnapshot, CertificationCriterion criterion,
            List<Long> acbIds) {
        if (statisticsSnapshot == null) {
            LOGGER.info("No statistics snapshot was found");
            return 0;
        } else if (CollectionUtils.isEmpty(statisticsSnapshot.getAttestedCriterionStatistics())) {
            LOGGER.info("No attested criterion statistics were found in the statistics snapshot for date " + statisticsSnapshot.getSnapshotDate());
            return 0;
        }
        List<Long> activeStatusIds = allCertificationStatuses.stream()
                .filter(certStatus -> CertificationStatusUtil.getActiveStatusNames().contains(certStatus.getName()))
                .map(certStatus -> certStatus.getId())
                .collect(Collectors.toList());

        long countOfListingsInActiveStatusWithCriteria = statisticsSnapshot.getAttestedCriterionStatistics().stream()
            .filter(stat -> stat.getCertificationCriterionId().equals(criterion.getId())
                    && acbIds.contains(stat.getAcbId())
                    && activeStatusIds.contains(stat.getListingStatusId()))
            .map(stat -> stat.getListingIds().size())
            .collect(Collectors.summingInt(Integer::intValue));
        return countOfListingsInActiveStatusWithCriteria;
    }

    private long calculateListingsRequiringUpdatesCount(List<UpdatedCriterionStatusReport> reports, CertificationCriterion criterion,
            List<Long> acbIds, LocalDate reportDate) {
        List<UpdatedCriterionStatusReport> reportsForCriterionAndAcb = reports.stream()
            .filter(report -> report.getCertificationCriterion().getId().equals(criterion.getId())
                    && acbIds.contains(report.getCertificationBodyId()))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(reportsForCriterionAndAcb)) {
            LOGGER.info("No updated criteria status reports were found for "
                + Util.formatCriteriaNumber(criterion) + " and ACB(s) " + acbIds
                + " from date " + reportDate);
            return 0;
        }

        return StreamEx.of(reportsForCriterionAndAcb)
                .distinct(UpdatedCriterionStatusReport::getCertifiedProductId)
                .count();
    }
}
