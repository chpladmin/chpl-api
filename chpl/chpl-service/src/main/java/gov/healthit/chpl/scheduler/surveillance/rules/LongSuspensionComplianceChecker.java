package gov.healthit.chpl.scheduler.surveillance.rules;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;
import gov.healthit.chpl.entity.CertificationStatusType;

@Component(value = "longSuspensionComplianceChecker")
public class LongSuspensionComplianceChecker implements RuleComplianceChecker {
    private int numDaysAllowed = 0;

    public SurveillanceOversightRule getRuleChecked() {
        return SurveillanceOversightRule.LONG_SUSPENSION;
    }

    public LocalDate check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
        LocalDate result = null;
        if (cp.getCurrentStatus().getStatus().getName().equals(CertificationStatusType.SuspendedByAcb.getName())) {
            List<CertificationStatusEvent> statusEvents = cp.getCertificationEvents();
            // find the most recent one
            CertificationStatusEvent mostRecent = null;
            for (CertificationStatusEvent statusEvent : statusEvents) {
                if (mostRecent == null
                        || (statusEvent.getEventDate().longValue() > mostRecent.getEventDate().longValue())) {
                    mostRecent = statusEvent;
                }
            }

            if (mostRecent != null) {
                LocalDate statusDate = LocalDate.ofInstant(Instant.ofEpochMilli(mostRecent.getEventDate()),
                        ZoneId.systemDefault());
                long numDays = ChronoUnit.DAYS.between(statusDate, LocalDate.now());
                if (numDays > getNumDaysAllowed()) {
                    result = statusDate.plusDays(getNumDaysAllowed() + 1);
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
