package gov.healthit.chpl.attestation.manager;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.attestation.domain.AttestationPeriodForm;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.form.FormService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class AttestationManager {
    private AttestationDAO attestationDAO;
    private AttestationPeriodService attestationPeriodService;
    private FormService formService;
    private AttestationSubmissionService attestationSubmissionService;
    private ChangeRequestDAO changeRequestDAO;
    private ErrorMessageUtil errorMessageUtil;
    private Integer attestationExceptionWindowInDays;

    @Autowired
    public AttestationManager(AttestationDAO attestationDAO, AttestationPeriodService attestationPeriodService, FormService formService,
            AttestationSubmissionService attestationSubmissionService, ChangeRequestDAO changeRequestDAO, ErrorMessageUtil errorMessageUtil,
            @Value("${attestationExceptionWindowInDays}") Integer attestationExceptionWindowInDays) {
        this.attestationDAO = attestationDAO;
        this.attestationPeriodService = attestationPeriodService;
        this.formService = formService;
        this.attestationSubmissionService = attestationSubmissionService;
        this.changeRequestDAO = changeRequestDAO;
        this.errorMessageUtil = errorMessageUtil;
        this.attestationExceptionWindowInDays = attestationExceptionWindowInDays;
    }

    public List<AttestationPeriod> getAllPeriods() {
        return attestationPeriodService.getAllPeriods();
    }

    public AttestationPeriodForm getAttestationForm(Long attestationPeriodId) throws EntityRetrievalException {
        AttestationPeriod attestationPeriod = getAllPeriods().stream()
                .filter(ap -> ap.getId().equals(attestationPeriodId))
                .findAny()
                .orElse(null);

        if (attestationPeriod != null) {
            return AttestationPeriodForm.builder()
                    .period(getAllPeriods().stream()
                            .filter(per -> per.getId().equals(attestationPeriodId))
                            .findAny()
                            .orElse(null))
                    .form(formService.getForm(attestationPeriod.getForm().getId()))
                    .build();
        } else {
            return null;
        }
    }

    @Transactional
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED
    }, allEntries = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ATTESTATION, "
            + "T(gov.healthit.chpl.permissions.domains.AttestationDomainPermissions).CREATE, #attestationSubmission)")
    public AttestationSubmission saveDeveloperAttestation(Long developerId, AttestationSubmission attestationSubmission) throws EntityRetrievalException {
        return attestationDAO.saveAttestationSubmssion(developerId, attestationSubmission);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ATTESTATION, "
            + "T(gov.healthit.chpl.permissions.domains.AttestationDomainPermissions).GET_BY_DEVELOPER_ID, #developerId)")
    public List<AttestationSubmission> getDeveloperAttestations(Long developerId) {
        return attestationSubmissionService.getAttestationSubmissions(developerId);
    }

    @Transactional
    public Boolean canDeveloperSubmitChangeRequest(Long developerId) throws EntityRetrievalException {
        AttestationPeriod mostRecentPastAttestationPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();

        if (doesPendingAttestationChangeRequestForDeveloperExist(developerId)) {
            return false;
        }
        if (doesValidExceptionExistForDeveloper(developerId)) {
            return true;
        }
        if (attestationPeriodService.isDateWithinSubmissionPeriodForDeveloper(developerId, LocalDate.now())) {
            return !doesAttestationForDeveloperExist(developerId, mostRecentPastAttestationPeriod.getId());
        }

        return false;
    }

    @Transactional
    public AttestationPeriod getSubmittablePeriod(Long developerId) {
        try {
            if (canDeveloperSubmitChangeRequest(developerId)) {
                return attestationPeriodService.getSubmittableAttestationPeriod(developerId);
            } else {
                return null;
            }
        } catch (EntityRetrievalException e) {
            return null;
        }
    }

    @Transactional
    public Boolean canCreateException(Long developerId) throws EntityRetrievalException {
        if (doesPendingAttestationChangeRequestForDeveloperExist(developerId)) {
            return false;
        } else {
            return getSubmittablePeriod(developerId) == null;
        }
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ATTESTATION, "
            + "T(gov.healthit.chpl.permissions.domains.AttestationDomainPermissions).CREATE_EXCEPTION, #developerId)")
    public AttestationPeriodDeveloperException createAttestationPeriodDeveloperException(Long developerId, Long attestationPeriodId)
            throws EntityRetrievalException, ValidationException {

        AttestationPeriod attestationPeriod = attestationPeriodService.getAllPeriods().stream()
                .filter(per -> per.getId().equals(attestationPeriodId))
                .findAny()
                .orElseThrow(() -> new ValidationException(errorMessageUtil.getMessage("attestation.submissionPeriodException.cannotCreate")));

        if (!canCreateException(developerId)) {
            throw new ValidationException(errorMessageUtil.getMessage("attestation.submissionPeriodException.cannotCreate"));
        }

        return attestationDAO.createAttestationPeriodDeveloperException(AttestationPeriodDeveloperException.builder()
                .developer(Developer.builder()
                        .id(developerId)
                        .build())
                .period(attestationPeriod)
                .exceptionEnd(getNewExceptionDate())
                .build());
    }

    @Transactional
    public void deleteAttestationPeriodDeveloperExceptions(Long developerId, Long periodId) {
        attestationDAO.deleteAttestationPeriodDeveloperExceptions(developerId, periodId);
    }

    @Transactional
    public AttestationPeriod getMostRecentPastAttestationPeriod() {
        return attestationPeriodService.getMostRecentPastAttestationPeriod();
    }

    private boolean doesPendingAttestationChangeRequestForDeveloperExist(Long developerId) throws EntityRetrievalException {
        return changeRequestDAO.getByDeveloper(developerId).stream()
                .filter(cr -> cr.getDeveloper().getId().equals(developerId)
                        && cr.getChangeRequestType().isAttestation()
                        && (cr.getCurrentStatus().getChangeRequestStatusType().getName().equals("Pending Developer Action")
                                || cr.getCurrentStatus().getChangeRequestStatusType().getName().equals("Pending ONC-ACB Action")))
                .count() > 0;
    }

    private LocalDate getNewExceptionDate() {
        LocalDate exceptionDate = LocalDate.now().plusDays(attestationExceptionWindowInDays);
        LocalDate lastDayBeforeNextSubmissionStart = attestationPeriodService.getCurrentAttestationPeriod().getSubmissionStart().minusDays(1);
        if (lastDayBeforeNextSubmissionStart.isBefore(exceptionDate)) {
            exceptionDate = lastDayBeforeNextSubmissionStart;
        }
        return exceptionDate;
    }

    private Boolean doesAttestationForDeveloperExist(Long developerId, Long attestationPeriodId) {
        List<AttestationSubmission> submissions = attestationDAO.getAttestationSubmissionsByDeveloper(developerId).stream()
                .filter(as -> as.getAttestationPeriod().getId().equals(attestationPeriodId))
                .toList();
        return submissions != null && submissions.size() > 0;
    }

    private Boolean doesValidExceptionExistForDeveloper(Long developerId) {
        return isDateInFuture(attestationPeriodService.getCurrentExceptionEndDateForDeveloper(developerId));
    }

    private Boolean isDateInFuture(LocalDate dateToCheck) {
        return dateToCheck != null
                && (dateToCheck.equals(LocalDate.now())
                        || dateToCheck.isAfter(LocalDate.now()));
    }

}
