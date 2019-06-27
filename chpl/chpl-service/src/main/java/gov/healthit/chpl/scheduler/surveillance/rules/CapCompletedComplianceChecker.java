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

@Component(value = "capCompletedComplianceChecker")
public class CapCompletedComplianceChecker implements RuleComplianceChecker {
    int numDaysAllowed = 0;

    public SurveillanceOversightRule getRuleChecked() {
        return SurveillanceOversightRule.CAP_NOT_COMPLETED;
    }

    public Date check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
        Date result = null;
        if (nc.getCapEndDate() == null) {
            LocalDateTime capMustCompleteDate = null;
            if (nc.getCapMustCompleteDate() != null) {
                capMustCompleteDate = LocalDateTime
                        .ofInstant(Instant.ofEpochMilli(nc.getCapMustCompleteDate().getTime()), ZoneId.systemDefault());
                Duration timeBetween = Duration.between(capMustCompleteDate, LocalDateTime.now());
                long numDays = timeBetween.toDays();
                if (numDays > getNumDaysAllowed()) {
                    LocalDateTime dateBroken = capMustCompleteDate.plusDays(getNumDaysAllowed() + 1);
                    result = Date.from(dateBroken.atZone(ZoneId.systemDefault()).toInstant());
                }
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
