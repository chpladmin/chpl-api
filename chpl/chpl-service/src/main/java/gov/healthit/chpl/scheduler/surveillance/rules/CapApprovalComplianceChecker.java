package gov.healthit.chpl.scheduler.surveillance.rules;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
        LocalDate result = null;
        if (nc.getCapApprovalDay() == null && nc.getDateOfDeterminationDay() != null) {
                long numDays = ChronoUnit.DAYS.between(nc.getDateOfDeterminationDay(), LocalDate.now());
                if (numDays > getNumDaysAllowed()) {
                    result = nc.getDateOfDeterminationDay().plusDays(getNumDaysAllowed() + 1);
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
