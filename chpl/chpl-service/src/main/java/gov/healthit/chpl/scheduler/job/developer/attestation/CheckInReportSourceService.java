package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalTime;
import java.util.Set;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.manager.AttestationSubmissionService;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchService;
import gov.healthit.chpl.domain.Developer;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CheckInReportSourceService {

    private AttestationSubmissionService attestationManager;
    private ChangeRequestSearchService changeRequestSearchService;
    private ChangeRequestManager changeRequestManager;

    public CheckInReportSourceService(AttestationSubmissionService attestationManager,
            ChangeRequestSearchService changeRequestSearchService, ChangeRequestManager changeRequestManager) {
        this.attestationManager = attestationManager;
        this.changeRequestSearchService = changeRequestSearchService;
        this.changeRequestManager = changeRequestManager;
    }

    public CheckInAttestation getCheckinReport(Developer developer, AttestationPeriod period) {
        AttestationSubmission fromDeveloper = getMostRecentAttestationSubmission(developer, period);
        ChangeRequest fromChangeRequest = getMostRecentChangeRequest(developer, period);

        if (fromDeveloper == null && fromChangeRequest == null) {
            return null;
        } else if (fromDeveloper != null && fromChangeRequest == null) {
            return CheckInAttestation.builder().attestationSubmission(fromDeveloper)
                    .source(CheckInReportSource.DEVELOPER_ATTESTATION).build();
        } else if (fromDeveloper == null && fromChangeRequest != null) {
            return CheckInAttestation.builder().changeRequest(fromChangeRequest)
                    .source(CheckInReportSource.CHANGE_REQUEST).build();
        } else if (fromDeveloper.getDatePublished().atTime(LocalTime.MAX)
                .isAfter(fromChangeRequest.getSubmittedDateTime())) {
            return CheckInAttestation.builder().attestationSubmission(fromDeveloper)
                    .source(CheckInReportSource.DEVELOPER_ATTESTATION).build();
        } else {
            return CheckInAttestation.builder().changeRequest(fromChangeRequest)
                    .source(CheckInReportSource.CHANGE_REQUEST).build();
        }
    }

    private AttestationSubmission getMostRecentAttestationSubmission(Developer developer, AttestationPeriod period) {
        return attestationManager.getAttestationSubmissions(developer.getId()).stream()
                .filter(att -> att.getAttestationPeriod().getId().equals(period.getId()))
                .sorted((result1, result2) -> result1.getDatePublished().compareTo(result2.getDatePublished()) * -1)
                .findFirst().orElse(null);
    }

    private ChangeRequest getMostRecentChangeRequest(Developer developer, AttestationPeriod period) {
        ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
                .developerId(developer.getId())
                .changeRequestTypeNames(Set.of(ChangeRequestType.ATTESTATION_TYPE.toString()))
                .build();

        try {
            return changeRequestSearchService.searchChangeRequests(request).getResults().stream().map(result -> {
                try {
                    return changeRequestManager.getChangeRequest(result.getId());
                } catch (Exception e) {
                    LOGGER.error("Could not retrieve Change Request where Id: {}", result.getId(), e);
                    return null;
                }
            }).filter(cr -> cr != null && ((ChangeRequestAttestationSubmission) cr.getDetails()).getAttestationPeriod()
                    .getId().equals(period.getId()))
                    .sorted((result1,
                            result2) -> result1.getSubmittedDateTime().compareTo(result2.getSubmittedDateTime()) * -1)
                    .findFirst().orElse(null);
        } catch (Exception e) {
            LOGGER.error("Could not execute Change Request search with the following request: {}", request.toString(), e);
            return null;
        }
    }
}
