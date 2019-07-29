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

@Component(value = "capApprovalComplianceChecker")
public class CapApprovalComplianceChecker implements RuleComplianceChecker {
    private int numDaysAllowed = 0;

    public SurveillanceOversightRule getRuleChecked() {
        return SurveillanceOversightRule.CAP_NOT_APPROVED;
    }

    public Date check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
        Date result = null;

        if (nc.getCapApprovalDate() == null) {
            LocalDateTime capDateOfDetermination = null;
            if (nc.getDateOfDetermination() != null) {
                capDateOfDetermination = LocalDateTime
                        .ofInstant(Instant.ofEpochMilli(nc.getDateOfDetermination().getTime()), ZoneId.systemDefault());
                Duration timeBetween = Duration.between(capDateOfDetermination, LocalDateTime.now());
                long numDays = timeBetween.toDays();
                if (numDays > getNumDaysAllowed()) {
                    LocalDateTime dateBroken = capDateOfDetermination.plusDays(getNumDaysAllowed() + 1);
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
