package gov.healthit.chpl.scheduler.surveillance.rules;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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
    public Date check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
        Date result = null;
        if (nc.getNonConformityCloseDate() == null  // SurveillanceNonconformityStatus.OPEN.getName())
                && nc.getCapEndDate() != null) {
            LocalDateTime capCompleteDate = null;
            capCompleteDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapEndDate().getTime()),
                    ZoneId.systemDefault());

            Duration timeBetween = Duration.between(capCompleteDate, LocalDateTime.now());
            long numDays = timeBetween.toDays();
            if (numDays > getNumDaysAllowed()) {
                LocalDateTime dateBroken = capCompleteDate.plusDays(getNumDaysAllowed() + 1);
                result = Date.from(dateBroken.atZone(ZoneId.systemDefault()).toInstant());
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
