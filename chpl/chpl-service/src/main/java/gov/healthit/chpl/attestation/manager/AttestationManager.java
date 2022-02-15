package gov.healthit.chpl.attestation.manager;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class AttestationManager {
    private AttestationDAO attestationDAO;
    private AttestationPeriodService attestationPeriodService;
    private ChangeRequestDAO changeRequestDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AttestationManager(AttestationDAO attestationDAO, AttestationPeriodService attestationPeriodService, ChangeRequestDAO changeRequestDAO, ErrorMessageUtil errorMessageUtil) {
        this.attestationDAO = attestationDAO;
        this.attestationPeriodService = attestationPeriodService;
        this.changeRequestDAO = changeRequestDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    public List<AttestationPeriod> getAllPeriods() {
        return attestationPeriodService.getAllPeriods();
    }

    public AttestationForm getAttestationForm() {
        return new AttestationForm(attestationDAO.getAttestationForm());
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ATTESTATION, "
            + "T(gov.healthit.chpl.permissions.domains.AttestationDomainPermissions).CREATE, #developerAttestationSubmission)")
    public DeveloperAttestationSubmission saveDeveloperAttestation(DeveloperAttestationSubmission developerAttestationSubmission) throws EntityRetrievalException {
        attestationDAO.getDeveloperAttestationSubmissionsByDeveloperAndPeriod(
                developerAttestationSubmission.getDeveloper().getDeveloperId(),
                developerAttestationSubmission.getPeriod().getId())
                .stream()
                        .forEach(da -> {
                            try {
                                attestationDAO.deleteDeveloperAttestationSubmission(da.getId());
                            } catch (EntityRetrievalException e) {
                                LOGGER.catching(e);
                                throw new RuntimeException(e);
                            }
                        });
        return attestationDAO.createDeveloperAttestationSubmission(developerAttestationSubmission);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ATTESTATION, "
            + "T(gov.healthit.chpl.permissions.domains.AttestationDomainPermissions).GET_BY_DEVELOPER_ID, #developerId)")
    public List<DeveloperAttestationSubmission> getDeveloperAttestations(Long developerId) {
        return attestationDAO.getDeveloperAttestationSubmissionsByDeveloper(developerId);
    }

    @Transactional
    //@PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ATTESTATION, "
    //        + "T(gov.healthit.chpl.permissions.domains.AttestationDomainPermissions).GET_BY_DEVELOPER_ID, #developerId)")
    public Boolean canDeveloperSubmitChangeRequest(Long developerId) throws EntityRetrievalException {
        return (!doesPendingAttestationChangeRequestForDeveloperExist(developerId))
                && attestationPeriodService.isDateWithinSubmissionPeriodForDeveloper(developerId, LocalDate.now());
    }

    @Transactional
    public Boolean canCreateException(Long developerId) {
        return !attestationPeriodService.isDateWithinSubmissionPeriodForDeveloper(developerId, LocalDate.now());
    }

    @Transactional
    public AttestationPeriodDeveloperException createAttestationPeriodDeveloperException(Long developerId) throws EntityRetrievalException, ValidationException{
        return attestationDAO.createAttestationPeriodDeveloperException(AttestationPeriodDeveloperException.builder()
                .developer(Developer.builder()
                        .developerId(developerId)
                        .build())
                .period(getMostRecentPastAttestationPeriod())
                .exceptionEnd(getNewExceptionDate())
                .build());
    }

    @Transactional
    public AttestationPeriod getMostRecentPastAttestationPeriod() {
        return attestationPeriodService.getMostRecentPastAttestationPeriod();
    }

    private boolean doesPendingAttestationChangeRequestForDeveloperExist(Long developerId) throws EntityRetrievalException {
        return changeRequestDAO.getByDeveloper(developerId).stream()
                .filter(cr -> cr.getDeveloper().getDeveloperId().equals(developerId)
                        && cr.getChangeRequestType().isAttestation()
                        && (cr.getCurrentStatus().getChangeRequestStatusType().getName().equals("Pending Developer Action")
                                || cr.getCurrentStatus().getChangeRequestStatusType().getName().equals("Pending ONC-ACB Action")))
                .count() > 0;
    }

    private LocalDate getNewExceptionDate() {
        LocalDate exceptionDate = LocalDate.now().plusDays(7);
        AttestationPeriod currentAttestationPeriod = attestationPeriodService.getCurrentAttestationPeriod();
        if (currentAttestationPeriod.getPeriodEnd().isBefore(exceptionDate)) {
            exceptionDate = currentAttestationPeriod.getPeriodEnd();
        }
        return exceptionDate;
    }
}
