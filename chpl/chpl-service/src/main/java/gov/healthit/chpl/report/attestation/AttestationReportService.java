package gov.healthit.chpl.report.attestation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.developer.search.ActiveListingSearchOptions;
import gov.healthit.chpl.developer.search.AttestationsSearchOptions;
import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.scheduler.job.report.attestation.AttestationReport;
import gov.healthit.chpl.scheduler.job.report.attestation.AttestationReportDAO;
import gov.healthit.chpl.scheduler.job.report.attestation.AttestationReportDeveloper;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AttestationReportService {

    private AttestationReportDAO attestationReportDAO;
    private AttestationPeriodService attestationPeriodService;
    private DeveloperSearchService developerSearchService;

    @Autowired
    public AttestationReportService(AttestationReportDAO attestationReportDAO,
            AttestationPeriodService attestationPeriodService,
            DeveloperSearchService developerSearchService) {
        this.attestationReportDAO = attestationReportDAO;
        this.attestationPeriodService = attestationPeriodService;
        this.developerSearchService = developerSearchService;
    }

    @Transactional
    public List<AttestationReport> getAttestationReports() {
        return attestationReportDAO.getAttestationReportByAttestationPeriod();
    }

    @Transactional
    public List<AttestationReportDeveloper> getAttestationReportDevelopers() {
        return attestationReportDAO.getAttestationReportDeveloperByAttestationPeriod();
    }

    @Transactional
    public AttestationSubmissionStatistics getAttestationSubmissionStatistics() {
        int developersNotSubmitted = developerSearchService.getCount(DeveloperSearchRequest.builder()
                .activeListingsOptions(Stream.of(ActiveListingSearchOptions.HAD_ANY_ACTIVE_DURING_MOST_RECENT_PAST_ATTESTATION_PERIOD,
                        ActiveListingSearchOptions.HAS_ANY_ACTIVE)
                        .collect(Collectors.toSet()))
                .activeListingsOptionsOperator(SearchSetOperator.AND)
                .attestationsOptions(Stream.of(AttestationsSearchOptions.HAS_NOT_SUBMITTED)
                        .collect(Collectors.toSet()))
                .attestationsOptionsOperator(SearchSetOperator.OR)
                .build(), LOGGER);

        int developersSubmittedNotPublished = developerSearchService.getCount(DeveloperSearchRequest.builder()
                .activeListingsOptions(Stream.of(ActiveListingSearchOptions.HAD_ANY_ACTIVE_DURING_MOST_RECENT_PAST_ATTESTATION_PERIOD,
                        ActiveListingSearchOptions.HAS_ANY_ACTIVE)
                        .collect(Collectors.toSet()))
                .activeListingsOptionsOperator(SearchSetOperator.AND)
                .attestationsOptions(Stream.of(AttestationsSearchOptions.HAS_NOT_PUBLISHED,
                        AttestationsSearchOptions.HAS_SUBMITTED)
                        .collect(Collectors.toSet()))
                .attestationsOptionsOperator(SearchSetOperator.AND)
                .build(), LOGGER);

        int developersRequiringAttestationSubmission = developerSearchService.getCount(DeveloperSearchRequest.builder()
                .activeListingsOptions(Stream.of(ActiveListingSearchOptions.HAD_ANY_ACTIVE_DURING_MOST_RECENT_PAST_ATTESTATION_PERIOD,
                        ActiveListingSearchOptions.HAS_ANY_ACTIVE)
                        .collect(Collectors.toSet()))
                .activeListingsOptionsOperator(SearchSetOperator.AND)
                .build(), LOGGER);

        return AttestationSubmissionStatistics.builder()
        .isSubmissionWindowOpen(attestationPeriodService.isTodayDuringAnySubmissionWindow())
        .isApprovalWindowOpen(attestationPeriodService.isTodayDuringSubmissionPlusApprovalPeriod())
        .developersWithoutSubmissionsCount(developersNotSubmitted)
        .developersWithPendingSubmissionsCount(developersSubmittedNotPublished)
        .maxDevelopersWithoutAttestationsCount(developersRequiringAttestationSubmission)
        .minDevelopersWithoutAttestationsCount(0)
        .build();
    }

    @Transactional
    public List<DeveloperSearchResult> getDevelopersNotSubmitted() {
        return developerSearchService.getAllPagesOfSearchResults(DeveloperSearchRequest.builder()
                .activeListingsOptions(Stream.of(ActiveListingSearchOptions.HAD_ANY_ACTIVE_DURING_MOST_RECENT_PAST_ATTESTATION_PERIOD,
                        ActiveListingSearchOptions.HAS_ANY_ACTIVE)
                        .collect(Collectors.toSet()))
                .activeListingsOptionsOperator(SearchSetOperator.AND)
                .attestationsOptions(Stream.of(AttestationsSearchOptions.HAS_NOT_SUBMITTED)
                        .collect(Collectors.toSet()))
                .attestationsOptionsOperator(SearchSetOperator.OR)
                .build(), LOGGER);
    }

    @Transactional
    public List<DeveloperSearchResult> getDevelopersSubmittedAndNotPublished() {
        return developerSearchService.getAllPagesOfSearchResults(DeveloperSearchRequest.builder()
                .activeListingsOptions(Stream.of(ActiveListingSearchOptions.HAD_ANY_ACTIVE_DURING_MOST_RECENT_PAST_ATTESTATION_PERIOD,
                        ActiveListingSearchOptions.HAS_ANY_ACTIVE)
                        .collect(Collectors.toSet()))
                .activeListingsOptionsOperator(SearchSetOperator.AND)
                .attestationsOptions(Stream.of(AttestationsSearchOptions.HAS_NOT_PUBLISHED,
                        AttestationsSearchOptions.HAS_SUBMITTED)
                        .collect(Collectors.toSet()))
                .attestationsOptionsOperator(SearchSetOperator.AND)
                .build(), LOGGER);
    }
}
