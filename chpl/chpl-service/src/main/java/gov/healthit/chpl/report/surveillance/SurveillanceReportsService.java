package gov.healthit.chpl.report.surveillance;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.compliance.surveillance.SurveillanceDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.report.SummaryStatisticsReportBaseService;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.NonConformitySearchOptions;
import gov.healthit.chpl.search.domain.SearchRequest;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SurveillanceReportsService extends SummaryStatisticsReportBaseService {

    private ListingSearchService listingSearchService;
    private SurveillanceDAO surveillanceDao;

    @Autowired
    public SurveillanceReportsService(SummaryStatisticsDAO summaryStatisticsDAO,
            ListingSearchService listingSearchService,
            CertificationBodyManager certificationBodyManager,
            SurveillanceDAO surveillanceDao) {
        super(summaryStatisticsDAO, certificationBodyManager);
        this.listingSearchService = listingSearchService;
        this.surveillanceDao = surveillanceDao;
    }

    @Transactional
    public List<SurveillanceByDeveloper> getSurveillanceOpenDuringTheLastYearForActiveDevelopers() {
        return surveillanceDao.getSurveillanceOpenDuringTheLastYearForActiveDevelopers();
    }

    public SurveillanceActivityCounts getSurveiilanceActivityCounts() {
        StatisticsSnapshot stats = getStatistics();
        return SurveillanceActivityCounts.builder()
                .closedActivities(stats.getSurveillanceClosedStatusTotal())
                .openActivities(
                        stats.getSurveillanceOpenStatus().getAcbStatistics().stream()
                                .collect(Collectors.summingLong(CertificationBodyStatistic::getCount)))
                .totalActivities(stats.getSurveillanceAllStatusTotal())
                .averageDurationClosedSurveillance(stats.getSurveillanceAvgTimeToClose())
                .build();
    }

    public List<CertificationBodyStatistic> getOpenSurveillanceActivityCountsByAcb() {
        return getStatistics().getSurveillanceOpenStatus().getAcbStatistics().stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbName()))
                        .build())
                .toList();

    }

    public List<ListingSearchResult> getListingsWithOpenSurveillance() {
        try {
            return listingSearchService.getAllPagesOfSearchResults(
                    SearchRequest.builder()
                        .complianceActivity(ComplianceSearchFilter.builder()
                                .hasHadComplianceActivity(true)
                                .build())
                        .pageSize(SearchRequest.MAX_PAGE_SIZE)
                        .build())
                    .stream()
                    .filter(result -> result.getOpenSurveillanceCount() > 0)
                    .map(result -> result.toBuilder()
                            .certificationBody(updateAcbNameBasedOnRetired(result.getCertificationBody())).build())
                    .toList();
        } catch (ValidationException e) {
            LOGGER.error("Could not retrieve listing search for listings with open surveillance.", e);
            return List.of();
        }
    }

    public NonconformityCounts getNonconformityCounts() {
        StatisticsSnapshot stats = getStatistics();
        return NonconformityCounts.builder()
                .totalNonconformities(stats.getNonConfStatusAllTotal())
                .openNonconformities(stats.getNonConfStatusOpen().getCount())
                .closedNonconformities(stats.getNonConfStatusClosedTotal())
                .avgDaysToAssessConformity(stats.getNonConfAvgTimeToAssessConformity())
                .avgDaysToApproveCap(stats.getNonConfAvgTimeToApproveCAP())
                .avgDaysOfCap(stats.getNonConfAvgDurationOfCAP())
                .avgDaysFromCapApprovalToSurveillanceClose(stats.getNonConfAvgTimeFromCAPAprrovalToSurveillanceEnd())
                .avgDaysFromCapCloseToSurveillanceClose(stats.getNonConfAvgTimeFromCAPEndToSurveillanceEnd())
                .avgDaysToCloseNonconformity(stats.getNonConfAvgTimeFromSurveillanceOpenToSurveillanceClose())
                .build();
    }

    public List<CertificationBodyStatistic> getOpenNonconformityCountsByAcb() {
        return getStatistics().getNonConfStatusOpen().getAcbStatistics().stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbName()))
                        .build())
                .toList();
    }

    public List<ListingSearchResult> getListingsWithOpenNonconformity() {
        try {
            return listingSearchService.getAllPagesOfSearchResults(
                    SearchRequest.builder()
                        .complianceActivity(ComplianceSearchFilter.builder()
                                .hasHadComplianceActivity(true)
                                .build())
                        .pageSize(SearchRequest.MAX_PAGE_SIZE)
                        .build())
                    .stream()
                    .filter(result -> result.getOpenSurveillanceNonConformityCount() > 0)
                    .map(result -> result.toBuilder()
                            .certificationBody(updateAcbNameBasedOnRetired(result.getCertificationBody())).build())
                    .toList();
        } catch (ValidationException e) {
            LOGGER.error("Could not retrieve listing search for listings with open surveillance.", e);
            return List.of();
        }
    }

    public CapCounts getCapCounts() {
        StatisticsSnapshot stats = getStatistics();
        Long openCaps = stats.getNonConfCAPStatusOpen().stream().collect(Collectors.summingLong(s -> s.getCount()));
        Long closedCaps = stats.getNonConfCAPStatusClosed().stream().collect(Collectors.summingLong(s -> s.getCount()));
        return CapCounts.builder()
                .totalCaps(openCaps + closedCaps)
                .openCaps(openCaps)
                .closedCaps(closedCaps)
                .build();
    }

    public List<CertificationBodyStatistic> getOpenCapCountsByAcb() {
        return getStatistics().getNonConfCAPStatusOpen().stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbName()))
                        .build())
                .toList();
    }

    public List<CertificationBodyStatistic> getClosedCapCountsByAcb() {
        return getStatistics().getNonConfCAPStatusClosed().stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbName()))
                        .build())
                .toList();
    }

    public List<ListingSearchResult> getListingsWithOpenCap() {
        try {
            return listingSearchService.getAllPagesOfSearchResults(
                    SearchRequest.builder()
                        .certificationEditions(null)
                        .certificationStatuses(Set.of(
                                CertificationStatusType.Active.toString(),
                                CertificationStatusType.SuspendedByAcb.toString(),
                                CertificationStatusType.SuspendedByOnc.toString(),
                                CertificationStatusType.TerminatedByOnc.toString(),
                                CertificationStatusType.WithdrawnByDeveloper.toString(),
                                CertificationStatusType.WithdrawnByDeveloperUnderReview.toString(),
                                CertificationStatusType.WithdrawnByAcb.toString()))
                        .complianceActivity(ComplianceSearchFilter.builder()
                                .nonConformityOptions(Set.of(NonConformitySearchOptions.OPEN_CAP))
                                .build())
                        .pageSize(SearchRequest.MAX_PAGE_SIZE)
                        .build())
                    .stream()
                    .map(result -> result.toBuilder()
                            .certificationBody(updateAcbNameBasedOnRetired(result.getCertificationBody())).build())
                    .toList();
        } catch (ValidationException e) {
            LOGGER.error("Could not retrieve listing search for listings with open CAP.", e);
            return List.of();
        }
    }

    public List<ListingSearchResult> getListingsWithClosedCap() {
        try {
            return listingSearchService.getAllPagesOfSearchResults(
                    SearchRequest.builder()
                        .certificationEditions(null)
                        .certificationStatuses(Set.of(
                                CertificationStatusType.Active.toString(),
                                CertificationStatusType.SuspendedByAcb.toString(),
                                CertificationStatusType.SuspendedByOnc.toString(),
                                CertificationStatusType.TerminatedByOnc.toString(),
                                CertificationStatusType.WithdrawnByDeveloper.toString(),
                                CertificationStatusType.WithdrawnByDeveloperUnderReview.toString(),
                                CertificationStatusType.WithdrawnByAcb.toString()))
                        .complianceActivity(ComplianceSearchFilter.builder()
                                .nonConformityOptions(Set.of(NonConformitySearchOptions.CLOSED_CAP))
                                .build())
                        .pageSize(SearchRequest.MAX_PAGE_SIZE)
                        .build())
                    .stream()
                    .map(result -> result.toBuilder()
                            .certificationBody(updateAcbNameBasedOnRetired(result.getCertificationBody())).build())
                    .toList();
        } catch (ValidationException e) {
            LOGGER.error("Could not retrieve listing search for listings with closed CAP.", e);
            return List.of();
        }
    }
}
