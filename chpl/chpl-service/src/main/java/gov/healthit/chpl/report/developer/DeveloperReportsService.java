package gov.healthit.chpl.report.developer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.CertificationStatusIdHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeveloperReportsService {
    private SummaryStatisticsDAO summaryStatisticsDAO;
    private CertificationStatusIdHelper statusIdHelper;
    private DeveloperSearchService developerSearchService;

    @Autowired
    public DeveloperReportsService(SummaryStatisticsDAO summaryStatisticsDAO, CertificationStatusDAO certificationStatusDao, DeveloperSearchService developerSearchService) {
        this.summaryStatisticsDAO = summaryStatisticsDAO;
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
        return stats.getDeveloperCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds());
    }

    public List<CertificationBodyStatistic> getDeveloperCountsWithWithdrawnListingsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getDeveloperCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds());
    }

    public List<DeveloperSearchResult> getDevelopersWithWithdrawnListingsByAcb() {
        StatisticsSnapshot stats = getStatistics();

        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
                .developerIds(stats.getDeveloperCountsByStatus().stream()
                        .filter(x -> statusIdHelper.getWithdrawnByDeveloperStatusIds().contains(x.getStatusId()))
                        .flatMap(x -> x.getIds().stream())
                        .collect(Collectors.toSet()))
                .build();

        return developerSearchService.getAllPagesOfSearchResults(request, LOGGER);
    }

    public List<CertificationBodyStatistic> getDeveloperCountsWithSuspendedListingsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getDeveloperCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds());
    }

    private StatisticsSnapshot getStatistics() {
        try {
            SummaryStatisticsEntity summaryStatistics = summaryStatisticsDAO.getCurrentSummaryStatistics();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(summaryStatistics.getSummaryStatistics(), StatisticsSnapshot.class);
        } catch (Exception e) {
            LOGGER.error("Error retrieving summary statistics: {}", e.getMessage());
            return null;
        }
    }

}
