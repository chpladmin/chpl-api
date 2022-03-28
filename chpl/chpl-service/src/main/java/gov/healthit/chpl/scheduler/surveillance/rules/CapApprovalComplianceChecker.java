package gov.healthit.chpl.scheduler.surveillance.rules;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;

@Component(value = "capApprovalComplianceChecker")
public class CapApprovalComplianceChecker implements RuleComplianceChecker {
    private int numDaysAllowed = 0;

    public SurveillanceOversightRule getRuleChecked() {
        return SurveillanceOversightRule.CAP_NOT_APPROVED;
    }

    public LocalDate check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
        if (nc.getCapApprovalDay() == null) {
            if (nc.getDateOfDeterminationDay() != null) {
                Duration timeBetween = Duration.between(nc.getDateOfDeterminationDay(), LocalDateTime.now());
                long numDays = timeBetween.toDays();
                if (numDays > getNumDaysAllowed()) {
                    return nc.getDateOfDeterminationDay().plusDays(getNumDaysAllowed() + 1);
                }
            }
        }

        return null;
    }

    public int getNumDaysAllowed() {
        return numDaysAllowed;
    }

    public void setNumDaysAllowed(final int numDaysAllowed) {
        this.numDaysAllowed = numDaysAllowed;
    }
}
