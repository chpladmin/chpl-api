package gov.healthit.chpl.report.surveillance;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SurveillanceReportsService {

    private SummaryStatisticsDAO summaryStatisticsDAO;
    private ListingSearchService listingSearchService;

    @Autowired
    public SurveillanceReportsService(SummaryStatisticsDAO summaryStatisticsDAO, ListingSearchService listingSearchService) {
        this.summaryStatisticsDAO = summaryStatisticsDAO;
        this.listingSearchService = listingSearchService;
    }

    public SurveillanceActivityCounts getSurveiilanceActivityCounts() {
        StatisticsSnapshot stats = getStatistics();
        return SurveillanceActivityCounts.builder()
                .closedActivities(stats.getSurveillanceClosedStatusTotal())
                .openActivities(
                        stats.getSurveillanceOpenStatus().getAcbStatistics().stream()
                                .collect(Collectors.summingLong(CertificationBodyStatistic::getCount)))
                .totalActivities(stats.getSurveillanceAllStatusTotal())
                .build();
    }

    public List<CertificationBodyStatistic> getOpenSurveillanceActivityCountsByAcb() {
        return getStatistics().getSurveillanceOpenStatus().getAcbStatistics();
    }

    public List<ListingSearchResult> getListingsWithOpenSurveillance() {
        try {
            return listingSearchService.getAllPagesOfSearchResults(
                    SearchRequest.builder()
                        .complianceActivity(ComplianceSearchFilter.builder()
                                .hasHadComplianceActivity(true)
                                .build())
                        .build())
                    .stream()
                    .filter(result -> result.getOpenSurveillanceCount() > 0)
                    .toList();
        } catch (ValidationException e) {
            LOGGER.error("Could not retrieve listing search for listings with oipen surveillance.", e);
            return List.of();
        }
    }

    public NonconformityCounts getNonconformityCounts() {
        StatisticsSnapshot stats = getStatistics();
        return NonconformityCounts.builder()
                .totalNonconformities(stats.getTotalNonConformities())
                .openNonconformities(stats.getOpenNonConformities())
                .closedNonconformities(stats.getClosedNonConformities())
                .avgDaysToAssessConformity(stats.getNonConfAvgTimeToAssessConformity())
                .avgDaysToApproveCap(stats.getNonConfAvgTimeToApproveCAP())
                .avgDaysOfCap(stats.getNonConfAvgDurationOfCAP())
                .avgDaysFromCapApprovalToSurveillanceClose(stats.getNonConfAvgTimeFromCAPAprrovalToSurveillanceEnd())
                .avgDaysFromCapCloseToSurveillanceClose(stats.getNonConfAvgTimeFromCAPEndToSurveillanceEnd())
                .avgDaysToCloseNonconformity(stats.getNonConfAvgTimeFromSurveillanceOpenToSurveillanceClose())
                .build();
    }

    public List<CertificationBodyStatistic> getOpenNonconformityCountsByAcb() {
        return getStatistics().getNonConfStatusOpen().getAcbStatistics();
    }

    public List<ListingSearchResult> getListingsWithOpenNonconformity() {
        try {
            return listingSearchService.getAllPagesOfSearchResults(
                    SearchRequest.builder()
                        .complianceActivity(ComplianceSearchFilter.builder()
                                .hasHadComplianceActivity(true)
                                .build())
                        .build())
                    .stream()
                    .filter(result -> result.getOpenSurveillanceNonConformityCount() > 0)
                    .toList();
        } catch (ValidationException e) {
            LOGGER.error("Could not retrieve listing search for listings with oipen surveillance.", e);
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
        return getStatistics().getNonConfCAPStatusOpen();
    }

    public List<CertificationBodyStatistic> getClosedCapCountsByAcb() {
        return getStatistics().getNonConfCAPStatusClosed();
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
