package gov.healthit.chpl.report.listing;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.report.SummaryStatisticsReportBaseService;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.CertificationStatusIdHelper;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.util.CertificationStatusUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ListingReportsService extends SummaryStatisticsReportBaseService {
    private CertificationStatusIdHelper statusIdHelper;
    private ListingSearchService listingSearchService;

    @Autowired
    public ListingReportsService(SummaryStatisticsDAO summaryStatisticsDAO, CertificationStatusDAO certificationStatusDao, ListingSearchService listingSearchService,
            CertificationBodyManager certificationBodyManager) {
        super(summaryStatisticsDAO, certificationBodyManager);
        this.statusIdHelper = new CertificationStatusIdHelper(certificationStatusDao);
        this.listingSearchService = listingSearchService;
    }

    public UniqueListingCount getUniqueListingCount() {
        StatisticsSnapshot stats = getStatistics();
        return UniqueListingCount.builder()
                .totalCount(stats.getListingCountForStatuses(statusIdHelper.getNonRetiredStatusIds()))
                .activeCount(stats.getListingCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds()))
                .suspendedCount(stats.getListingCountForStatuses(statusIdHelper.getSuspendedStatusIds()))
                .withdrawnCount(stats.getListingCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds()))
                .build();
    }

    public List<CertificationBodyStatistic> getActiveListingCountsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getListingCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()).stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbId()))
                        .build())
                .toList();
    }

    public List<CertificationBodyStatistic> getSuspendedListingCountsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getListingCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()).stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbId()))
                        .build())
                .toList();
    }

    public List<CertificationBodyStatistic> getWithdrawnListingCountsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getListingCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()).stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbId()))
                        .build())
                .toList();
    }

    public List<ListingSearchResult> getActiveListings() {
        return getListingSearchResults(CertificationStatusUtil.getActiveStatusNames().stream().collect(Collectors.toSet())).stream()
                .map(result -> result.toBuilder()
                        .certificationBody(updateAcbNameBasedOnRetired(result.getCertificationBody()))
                        .build())
                .toList();
    }

    public List<ListingSearchResult> getSuspendedListings() {
        return getListingSearchResults(CertificationStatusUtil.getSuspendedStatuses().stream()
                .map(status -> status.getName()).collect(Collectors.toSet()))
                        .stream()
                        .map(result -> result.toBuilder()
                                .certificationBody(updateAcbNameBasedOnRetired(result.getCertificationBody())).build())
                        .toList();
    }

    public List<ListingSearchResult> getWithdrawnListings() {
        return getListingSearchResults(CertificationStatusUtil.getWithdrawnStatuses().stream()
                .map(status -> status.getName()).collect(Collectors.toSet()))
                        .stream()
                        .map(result -> result.toBuilder()
                                .certificationBody(updateAcbNameBasedOnRetired(result.getCertificationBody())).build())
                        .toList();
    }

    private List<ListingSearchResult> getListingSearchResults(Set<String> statusNames) {
        try {
            return listingSearchService.getAllPagesOfSearchResults(SearchRequest.builder()
                    .certificationStatuses(statusNames)
                    .build());

        } catch (ValidationException e) {
            LOGGER.error("Error validating SearchRequest: {}", e.getMessage(), e);
            return List.of();
        }
    }

}
