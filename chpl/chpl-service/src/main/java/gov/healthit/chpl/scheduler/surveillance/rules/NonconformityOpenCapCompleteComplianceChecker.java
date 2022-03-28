package gov.healthit.chpl.scheduler.surveillance.rules;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;

@Component(value = "nonconformityOpenCapCompleteComplianceChecker")
public class NonconformityOpenCapCompleteComplianceChecker implements RuleComplianceChecker {
    private int numDaysAllowed = 0;

    @Override
    public SurveillanceOversightRule getRuleChecked() {
        return SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE;
    }

    @Override
    public LocalDate check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
        LocalDate result = null;
        if (nc.getNonconformityCloseDay() == null && nc.getCapEndDay() != null) {
            Duration timeBetween = Duration.between(nc.getCapEndDay(), LocalDate.now());
            long numDays = timeBetween.toDays();
            if (numDays > getNumDaysAllowed()) {
                result = nc.getCapEndDay().plusDays(getNumDaysAllowed() + 1);
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
