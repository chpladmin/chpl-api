package gov.healthit.chpl.scheduler.surveillance.rules;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;

@Component(value = "capCompletedComplianceChecker")
public class CapCompletedComplianceChecker implements RuleComplianceChecker {
    private int numDaysAllowed = 0;

    public SurveillanceOversightRule getRuleChecked() {
        return SurveillanceOversightRule.CAP_NOT_COMPLETED;
    }

    public LocalDate check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
        LocalDate result = null;
        if (nc.getCapEndDay() == null && nc.getCapMustCompleteDay() != null) {
            long numDays = ChronoUnit.DAYS.between(nc.getCapMustCompleteDay(), LocalDate.now());
            if (numDays > getNumDaysAllowed()) {
                result = nc.getCapMustCompleteDay().plusDays(getNumDaysAllowed() + 1);
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
