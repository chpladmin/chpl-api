package gov.healthit.chpl.attestation.manager;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;

@Component
public class AttestationPeriodService {
    private AttestationDAO attestationDAO;

    @Autowired
    public AttestationPeriodService(AttestationDAO attestationDAO) {
        this.attestationDAO = attestationDAO;
    }

    public List<AttestationPeriod> getAllPeriods() {
        return attestationDAO.getAllPeriods();
    }

    public Boolean isDateWithinSubmissionPeriodForDeveloper(Long developerId, LocalDate dateToCheck) {
        AttestationPeriod mostRecentPeriod = getMostRecentPastAttestationPeriodForDeveloperWrtExceptions(developerId);
        return (mostRecentPeriod.getSubmissionStart().equals(dateToCheck) || mostRecentPeriod.getSubmissionStart().isBefore(dateToCheck))
                && (mostRecentPeriod.getSubmissionEnd().equals(dateToCheck) || mostRecentPeriod.getSubmissionEnd().isAfter(dateToCheck));
    }

    public AttestationPeriod getCurrentAttestationPeriod() {
        return attestationDAO.getAllPeriods().stream()
                .filter(period -> (LocalDate.now().equals(period.getPeriodStart()) || LocalDate.now().isAfter(period.getPeriodStart()))
                        && (LocalDate.now().equals(period.getPeriodEnd()) || LocalDate.now().isBefore(period.getPeriodEnd())))
                .findFirst()
                .get();
    }

    public AttestationPeriod getMostRecentPastAttestationPeriod() {
        List<AttestationPeriod> periods = getAllPeriods();
        if (periods == null || periods.size() == 0) {
            return null;
        }

        periods = periods.stream()
                .sorted(Comparator.comparing(AttestationPeriod::getPeriodEnd).reversed())
                .filter(per -> per.getPeriodEnd().isBefore(LocalDate.now()))
                .toList();

        if (periods == null || periods.size() == 0) {

            return null;
        }

        return periods.get(0);
    }

    public LocalDate getCurrentExceptionEndDateForDeveloper(Long developerId) {
        AttestationPeriodDeveloperException periodException = attestationDAO.getCurrentAttestationPeriodDeveloperException(developerId);
        if (periodException != null) {
            return periodException.getExceptionEnd();
        } else {
            return null;
        }
    }

    public AttestationPeriod getSubmittableAttestationPeriod(Long developerId) {
        AttestationPeriodDeveloperException apde = attestationDAO.getCurrentAttestationPeriodDeveloperException(developerId);
        if (apde != null
              && (apde.getExceptionEnd().isEqual(LocalDate.now())
              || apde.getExceptionEnd().isAfter(LocalDate.now()))) {
          return apde.getPeriod();
        }

        return getMostRecentPastAttestationPeriod();
    }

    private LocalDate getMostRecentPeriodExceptionDateForDeveloper(Long developerId) {
        AttestationPeriod period = getMostRecentPastAttestationPeriod();
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

    private AttestationPeriod getMostRecentPastAttestationPeriodForDeveloperWrtExceptions(Long developerId) {
        AttestationPeriod mostRecentPeriod = getMostRecentPastAttestationPeriod();
        if (mostRecentPeriod == null) {
            return null;
        }

        LocalDate periodExceptionDate = getMostRecentPeriodExceptionDateForDeveloper(developerId);
        if (periodExceptionDate != null) {
            mostRecentPeriod.setSubmissionEnd(periodExceptionDate);
        }

        return mostRecentPeriod;
    }

}
