package gov.healthit.chpl.scheduler.surveillance.rules;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;

@Component(value = "capStartedComplianceChecker")
public class CapStartedComplianceChecker implements RuleComplianceChecker {
    private int numDaysAllowed = 0;

    public SurveillanceOversightRule getRuleChecked() {
        return SurveillanceOversightRule.CAP_NOT_STARTED;
    }

    public LocalDate check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
        LocalDate result = null;
        if (nc.getCapStartDay() == null && nc.getCapApprovalDay() != null) {
            Duration timeBetween = Duration.between(nc.getCapApprovalDay(), LocalDateTime.now());
            long numDays = timeBetween.toDays();
            if (numDays > getNumDaysAllowed()) {
                result = nc.getCapApprovalDay().plusDays(getNumDaysAllowed() + 1);
            }
        }
        return result;
    }

    public int getNumDaysAllowed() {
        return numDaysAllowed;
    }

    public void setNumDaysAllowed(final int numDaysAllowed) {
        this.numDaysAllowed = numDaysAllowed;
    }
}
