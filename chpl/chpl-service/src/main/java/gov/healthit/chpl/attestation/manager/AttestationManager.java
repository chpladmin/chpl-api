package gov.healthit.chpl.attestation.manager;

import java.time.LocalDate;
import java.util.Comparator;
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
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class AttestationManager {
    private AttestationDAO attestationDAO;
    private ChangeRequestDAO changeRequestDAO;

    @Autowired
    public AttestationManager(AttestationDAO attestationDAO, ChangeRequestDAO changeRequestDAO) {
        this.attestationDAO = attestationDAO;
        this.changeRequestDAO = changeRequestDAO;
    }

    public List<AttestationPeriod> getAllPeriods() {
        return attestationDAO.getAllPeriods();
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
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ATTESTATION, "
            + "T(gov.healthit.chpl.permissions.domains.AttestationDomainPermissions).GET_BY_DEVELOPER_ID, #developerId)")
    public Boolean canDeveloperSubmitChangeRequest(Long developerId) throws EntityRetrievalException {
        LocalDate exceptionDate = getMostRecentPeriodExceptionDateForDeveloper(developerId);
        return (!doesPendingAttestationChangeRequestForDeveloperExist(developerId))
                && isCurrentDateWithAttestationPeriod(exceptionDate);
    }

    private boolean isCurrentDateWithAttestationPeriod(LocalDate periodExceptionDate) {
        AttestationPeriod mostRecentPeriod = getMostRecentPastPeriod();
        if (mostRecentPeriod == null) {
            return false;
        }

        if (periodExceptionDate != null) {
            mostRecentPeriod.setSubmissionEnd(periodExceptionDate);
        }

        LocalDate now = LocalDate.now();
        return (mostRecentPeriod.getSubmissionStart().equals(now) || mostRecentPeriod.getSubmissionStart().isBefore(now))
                && (mostRecentPeriod.getSubmissionEnd().equals(now) || mostRecentPeriod.getSubmissionEnd().isAfter(now));
    }

    private AttestationPeriod getMostRecentPastPeriod() {
        List<AttestationPeriod> periods = getAllPeriods();
        if (periods == null || periods.size() < 0) {
            return null;
        }

        periods = periods.stream()
                .sorted(Comparator.comparing(AttestationPeriod::getPeriodEnd).reversed())
                .filter(per -> per.getPeriodEnd().isBefore(LocalDate.now()))
                .toList();

        if (periods == null || periods.size() < 0) {
            return null;
        }

        return periods.get(0);
    }

    //TODO: Need to implement
    private LocalDate getMostRecentPeriodExceptionDateForDeveloper(Long developerId) {
        AttestationPeriod period = getMostRecentPastPeriod();
        List<AttestationPeriodDeveloperException> periodExceptions =
                attestationDAO.getAttestationPeriodDeveloperExceptions(developerId, period.getId());

        if (periodExceptions == null || periodExceptions.size() == 0) {
            return null;
        }

        return periodExceptions.stream()
                .sorted(Comparator.comparing(AttestationPeriodDeveloperException::getExceptionEnd).reversed())
                .toList()
                .get(0)
                .getExceptionEnd();
    }

    private boolean doesPendingAttestationChangeRequestForDeveloperExist(Long developerId) throws EntityRetrievalException {
        return changeRequestDAO.getByDeveloper(developerId).stream()
                .filter(cr -> cr.getDeveloper().getDeveloperId().equals(developerId)
                        && cr.getChangeRequestType().isAttestation()
                        && (cr.getCurrentStatus().getChangeRequestStatusType().getName().equals("Pending Developer Action")
                                || cr.getCurrentStatus().getChangeRequestStatusType().getName().equals("Pending ONC-ACB Action")))
                .count() > 0;
    }
}
