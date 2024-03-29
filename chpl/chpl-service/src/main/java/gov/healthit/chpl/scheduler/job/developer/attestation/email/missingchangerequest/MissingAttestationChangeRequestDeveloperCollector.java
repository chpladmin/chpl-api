package gov.healthit.chpl.scheduler.job.developer.attestation.email.missingchangerequest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResponse;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchService;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.scheduler.SecurityContextCapableJob;
import gov.healthit.chpl.scheduler.job.developer.attestation.DeveloperAttestationPeriodCalculator;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.DeveloperCollector;
import lombok.extern.log4j.Log4j2;


@Component
@Log4j2(topic = "missingAttestationChangeRequestEmailJobLogger")
public class MissingAttestationChangeRequestDeveloperCollector extends SecurityContextCapableJob implements DeveloperCollector {
    private AttestationPeriodService attestationPeriodService;
    private ChangeRequestSearchService changeRequestSearchService;
    private DeveloperAttestationPeriodCalculator developerAttestationPeriodCalculator;
    private ChangeRequestManager changeRequestManager;

    @Autowired
    public MissingAttestationChangeRequestDeveloperCollector(AttestationPeriodService attestationPeriodService,
            ChangeRequestSearchService changeRequestSearchService, DeveloperAttestationPeriodCalculator developerAttestationPeriodCalculator,
            ChangeRequestManager changeRequestManager) {
        this.attestationPeriodService = attestationPeriodService;
        this.changeRequestSearchService = changeRequestSearchService;
        this.developerAttestationPeriodCalculator = developerAttestationPeriodCalculator;
        this.changeRequestManager = changeRequestManager;
    }

    @Override
    public List<Developer> getDevelopers() {
        setSecurityContext(Authority.ROLE_ADMIN);
        AttestationPeriod mostRecentPastPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();
        return getDevelopersWithActiveListingsDuringAttestationPeriodAndMissingChangeRequest(mostRecentPastPeriod);
    }

    private List<Developer> getDevelopersWithActiveListingsDuringAttestationPeriodAndMissingChangeRequest(AttestationPeriod mostRecentPastPeriod) {
        return developerAttestationPeriodCalculator.getDevelopersWithActiveListingsDuringMostRecentPastAttestationPeriod(LOGGER).stream()
                .filter(developer -> !hasDeveloperSubmittedChangeRequestForPeriod(developer, mostRecentPastPeriod))
                .toList();
    }

    private Boolean hasDeveloperSubmittedChangeRequestForPeriod(Developer developer, AttestationPeriod mostRecentPastPeriod) {
        return getAllAttestationChangeRequestsForDeveloper(developer).stream()
                .filter(attestationSubmission -> attestationSubmission.getAttestationPeriod().getId().equals(mostRecentPastPeriod.getId()))
                .findAny()
                .isPresent();
    }

    private List<ChangeRequestAttestationSubmission> getAllAttestationChangeRequestsForDeveloper(Developer developer) {
        List<ChangeRequestSearchResult> searchResults = new ArrayList<ChangeRequestSearchResult>();
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .developerId(developer.getId())
                .build();

        try {
            ChangeRequestSearchResponse searchResponse = changeRequestSearchService.searchChangeRequests(searchRequest);
            searchResults.addAll(searchResponse.getResults());
            while (searchResponse.getRecordCount() > searchResults.size()) {
                searchRequest.setPageSize(searchResponse.getPageSize());
                searchRequest.setPageNumber(searchResponse.getPageNumber() + 1);
                searchResponse = changeRequestSearchService.searchChangeRequests(searchRequest);
                searchResults.addAll(searchResponse.getResults());
            }
            List<ChangeRequestAttestationSubmission> changeRequestAttestationSubmissions =  searchResults.stream()
                    .filter(result -> result.isAttestation())
                    .map(result -> getAttestationSubmission(getChangeRequest(result.getId())))
                    .toList();

            LOGGER.debug("Found {} Attestation Change Requests for {}", changeRequestAttestationSubmissions.size(), developer.getName());
            return changeRequestAttestationSubmissions;
        } catch (ValidationException e) {
            LOGGER.error("Error getting change requests for developer: {} {}", developer.getId(), developer.getName());
            return null;
        }

    }

    private ChangeRequest getChangeRequest(Long changeRequestId) {
        try {
            return changeRequestManager.getChangeRequest(changeRequestId);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve Change Request Id: {}", changeRequestId);
            return null;
        }
    }

    private ChangeRequestAttestationSubmission getAttestationSubmission(ChangeRequest cr) {
        return (ChangeRequestAttestationSubmission) cr.getDetails();
    }
}
