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
import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import gov.healthit.chpl.caching.CacheNames;
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
    private Integer attestationExceptionWindowInDays;

    @Autowired
    public AttestationManager(AttestationDAO attestationDAO, AttestationPeriodService attestationPeriodService, ChangeRequestDAO changeRequestDAO,
            ErrorMessageUtil errorMessageUtil, @Value("${attestationExceptionWindowInDays}") Integer attestationExceptionWindowInDays) {
        this.attestationDAO = attestationDAO;
        this.attestationPeriodService = attestationPeriodService;
        this.changeRequestDAO = changeRequestDAO;
        this.errorMessageUtil = errorMessageUtil;
        this.attestationExceptionWindowInDays = attestationExceptionWindowInDays;
    }

    public List<AttestationPeriod> getAllPeriods() {
        return attestationPeriodService.getAllPeriods();
    }

    public AttestationForm getAttestationForm() {
        AttestationPeriod period = attestationPeriodService.getMostRecentPastAttestationPeriod();
        return new AttestationForm(attestationDAO.getAttestationForm(), period);
    }

    @Transactional
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED
    }, allEntries = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ATTESTATION, "
            + "T(gov.healthit.chpl.permissions.domains.AttestationDomainPermissions).CREATE, #developerAttestationSubmission)")
    public DeveloperAttestationSubmission saveDeveloperAttestation(DeveloperAttestationSubmission developerAttestationSubmission) throws EntityRetrievalException {
        attestationDAO.getDeveloperAttestationSubmissionsByDeveloperAndPeriod(
                developerAttestationSubmission.getDeveloper().getId(),
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
    public Boolean canDeveloperSubmitChangeRequest(Long developerId) throws EntityRetrievalException {
        AttestationPeriod mostRecentPastAttestationPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();
        if (doesAttestationForDeveloperExist(developerId, mostRecentPastAttestationPeriod.getId())) {
            if (doesPendingAttestationChangeRequestForDeveloperExist(developerId)) {
                return false;
            } else {
                return isDateInFuture(attestationPeriodService.getMostRecentPeriodExceptionDateForDeveloper(developerId));
            }
        } else {
            return attestationPeriodService.isDateWithinSubmissionPeriodForDeveloper(developerId, LocalDate.now())
                    && !doesPendingAttestationChangeRequestForDeveloperExist(developerId);
        }
    }

    @Transactional
    public Boolean canCreateException(Long developerId) throws EntityRetrievalException {
        if (canDeveloperSubmitChangeRequest(developerId)) {
            return false;
        } else {
            AttestationPeriod mostRecentPastAttestationPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();
            if (withinStandardSubmissionPeriod(mostRecentPastAttestationPeriod)) {
                if (doesAttestationForDeveloperExist(developerId, mostRecentPastAttestationPeriod.getId())) {
                    return !doesPendingAttestationChangeRequestForDeveloperExist(developerId);
                } else {
                    return false;
                }
            } else {
                return !doesPendingAttestationChangeRequestForDeveloperExist(developerId);
            }
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

        //if (!canCreateException(developerId)) {
        //    throw new ValidationException(errorMessageUtil.getMessage("attestation.submissionPeriodException.cannotCreate"));
        //}

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
        List<DeveloperAttestationSubmission> submissions = attestationDAO.getDeveloperAttestationSubmissionsByDeveloperAndPeriod(developerId, attestationPeriodId);
        return submissions != null && submissions.size() > 0;
    }

    private Boolean isDateInFuture(LocalDate dateToCheck) {
        return dateToCheck != null
                && (dateToCheck.equals(LocalDate.now())
                        || dateToCheck.isAfter(LocalDate.now()));
    }

    private Boolean withinStandardSubmissionPeriod(AttestationPeriod period) {
        return (period.getSubmissionStart().equals(LocalDate.now()) || period.getSubmissionStart().isBefore(LocalDate.now()))
                && (period.getSubmissionEnd().equals(LocalDate.now()) || period.getSubmissionEnd().isAfter(LocalDate.now()));
    }
}
