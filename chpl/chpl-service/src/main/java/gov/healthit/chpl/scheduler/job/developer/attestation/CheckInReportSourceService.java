package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.manager.AttestationSubmissionService;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchService;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.auth.UserDTO;

@Component
public class CheckInReportSourceService {

    private AttestationSubmissionService attestationManager;
    private ChangeRequestSearchService changeRequestSearchService;
    private ChangeRequestManager changeRequestManager;
    private UserDAO userDAO;

    public CheckInReportSourceService(AttestationSubmissionService attestationManager, ChangeRequestSearchService changeRequestSearchService,
            ChangeRequestManager changeRequestManager, UserDAO userDAO) {
        this.attestationManager = attestationManager;
        this.changeRequestSearchService = changeRequestSearchService;
        this.changeRequestManager = changeRequestManager;
        this.userDAO = userDAO;
    }

    public CheckInAttestation getCheckinReport(Developer developer, AttestationPeriod period, Logger logger) {
        // try {
        // setSecurityContext(userDAO.getById(User.ADMIN_USER_ID));
        // } catch (UserRetrievalException e) {
        // logger.error("Could not set security context: {}", e.getMessage(),
        // e);
        // return null;
        // }
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

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser splitUser = new JWTAuthenticatedUser();
        splitUser.setFullName(user.getFullName());
        splitUser.setId(user.getId());
        splitUser.setFriendlyName(user.getFriendlyName());
        splitUser.setSubjectName(user.getUsername());
        splitUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(splitUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

}
