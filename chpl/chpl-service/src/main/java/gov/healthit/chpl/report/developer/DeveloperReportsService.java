package gov.healthit.chpl.report.developer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.developer.search.ActiveListingSearchOptions;
import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.report.SummaryStatisticsReportBaseService;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.CertificationStatusIdHelper;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeveloperReportsService extends SummaryStatisticsReportBaseService {
    private CertificationStatusIdHelper statusIdHelper;
    private DeveloperSearchService developerSearchService;

    @Autowired
    public DeveloperReportsService(SummaryStatisticsDAO summaryStatisticsDAO, CertificationStatusDAO certificationStatusDao, DeveloperSearchService developerSearchService,
            CertificationBodyManager certificationBodyManager) {
        super(summaryStatisticsDAO, certificationBodyManager);
        this.statusIdHelper = new CertificationStatusIdHelper(certificationStatusDao);
        this.developerSearchService = developerSearchService;
    }

    public UniqueDeveloperCount getUniqueDeveloperCount() {
        StatisticsSnapshot stats = getStatistics();
        return UniqueDeveloperCount.builder()
                .count(stats.getDeveloperCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds()))
                .build();
    }

    public List<CertificationBodyStatistic> getDeveloperCountsWithActiveListingsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getDeveloperCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()).stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbId()))
                        .build())
                .toList();
    }

    public List<DeveloperSearchResult> getDevelopersWithActiveListingsByAcb() {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
                .activeListingsOptions(Stream.of(ActiveListingSearchOptions.HAS_ANY_ACTIVE).collect(Collectors.toSet()))
                .activeListingsOptionsOperator(SearchSetOperator.AND)
                .build();

        return developerSearchService.getAllPagesOfSearchResults(request, LOGGER).stream()
                .map(result -> result.toBuilder()
                        .acbsForActiveListings(result.getAcbsForWithdrawnListings().stream()
                                .map(idNamePair -> updateAcbNameBasedOnRetired(idNamePair))
                                .collect(Collectors.toSet()))
                        .build())
                .toList();
    }

    public List<CertificationBodyStatistic> getDeveloperCountsWithWithdrawnListingsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getDeveloperCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()).stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbId()))
                        .build())
                .toList();
    }

    public List<DeveloperSearchResult> getDevelopersWithWithdrawnListingsByAcb() {
        StatisticsSnapshot stats = getStatistics();

        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
                .developerIds(stats.getDeveloperCountsByStatus().stream()
                        .filter(x -> statusIdHelper.getWithdrawnByDeveloperStatusIds().contains(x.getStatusId()))
                        .flatMap(x -> x.getIds().stream())
                        .collect(Collectors.toSet()))
                .build();

        return developerSearchService.getAllPagesOfSearchResults(request, LOGGER).stream()
                .map(result -> result.toBuilder()
                        .acbsForWithdrawnListings(result.getAcbsForWithdrawnListings().stream()
                                .map(idNamePair -> updateAcbNameBasedOnRetired(idNamePair))
                                .collect(Collectors.toSet()))
                        .build())
                .toList();
    }


    public List<CertificationBodyStatistic> getDeveloperCountsWithSuspendedListingsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getDeveloperCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()).stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbId()))
                        .build())
                .toList();
    }

    public List<DeveloperSearchResult> getDevelopersWithSuspendedListingsByAcb() {
        StatisticsSnapshot stats = getStatistics();

        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
                .developerIds(stats.getDeveloperCountsByStatus().stream()
                        .filter(x -> statusIdHelper.getSuspendedStatusIds().contains(x.getStatusId()))
                        .flatMap(x -> x.getIds().stream())
                        .collect(Collectors.toSet()))
                .build();

        return developerSearchService.getAllPagesOfSearchResults(request, LOGGER).stream()
                .map(result -> result.toBuilder()
                        .acbsForSuspendedListings(result.getAcbsForSuspendedListings().stream()
                                .map(idNamePair -> updateAcbNameBasedOnRetired(idNamePair))
                                .collect(Collectors.toSet()))
                        .build())
                .toList();

    }

}
