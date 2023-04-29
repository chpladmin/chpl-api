package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.service.AttestationSubmissionService;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchService;
import gov.healthit.chpl.domain.Developer;

@Component
public class CheckInReportSourceService {

    private AttestationSubmissionService attestationManager;
    private ChangeRequestSearchService changeRequestSearchService;
    private ChangeRequestManager changeRequestManager;

    public CheckInReportSourceService(AttestationSubmissionService attestationManager, ChangeRequestSearchService changeRequestSearchService,
            ChangeRequestManager changeRequestManager) {
        this.attestationManager = attestationManager;
        this.changeRequestSearchService = changeRequestSearchService;
        this.changeRequestManager = changeRequestManager;
    }

    public CheckInAttestation getCheckinReport(Developer developer, AttestationPeriod period, Logger logger) {
        return CheckInAttestation.builder()
                .attestationSubmission(getMostRecentAttestationSubmission(developer, period))
                .changeRequest(getMostRecentChangeRequest(developer, period, logger))
                .build();
    }

    private AttestationSubmission getMostRecentAttestationSubmission(Developer developer, AttestationPeriod period) {
        return attestationManager.getAttestationSubmissions(developer.getId()).stream()
                .filter(att -> att.getAttestationPeriod().getId().equals(period.getId()))
                .sorted((result1, result2) -> result1.getDatePublished().compareTo(result2.getDatePublished()) * -1)
                .findFirst().orElse(null);
    }

    private ChangeRequest getMostRecentChangeRequest(Developer developer, AttestationPeriod period, Logger logger) {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
                .developerId(developer.getId())
                .changeRequestTypeNames(Set.of(ChangeRequestType.ATTESTATION_TYPE.toString()))
                .build();

        try {
            return changeRequestSearchService.searchChangeRequests(request).getResults().stream().map(result -> {
                try {
                    return changeRequestManager.getChangeRequest(result.getId());
                } catch (Exception e) {
                    logger.error("Could not retrieve Change Request where Id: {}", result.getId(), e);
                    return null;
                }
            }).filter(cr -> cr != null && ((ChangeRequestAttestationSubmission) cr.getDetails()).getAttestationPeriod()
                    .getId().equals(period.getId()))
                    .sorted((result1,
                            result2) -> result1.getSubmittedDateTime().compareTo(result2.getSubmittedDateTime()) * -1)
                    .findFirst().orElse(null);
        } catch (Exception e) {
            logger.error("Could not execute Change Request search with the following request: {}", request.toString(), e);
            return null;
        }
    }
}
