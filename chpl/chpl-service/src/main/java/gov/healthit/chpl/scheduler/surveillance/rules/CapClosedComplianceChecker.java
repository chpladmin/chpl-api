package gov.healthit.chpl.scheduler.surveillance.rules;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;
import gov.healthit.chpl.entity.CertificationStatusType;

@Component(value = "capClosedComplianceChecker")
public class CapClosedComplianceChecker implements RuleComplianceChecker {
    private int numDaysAllowed = -1;

    public SurveillanceOversightRule getRuleChecked() {
        return SurveillanceOversightRule.CAP_NOT_CLOSED;
    }

    public Date check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
        Date result = null;
        if (nc.getCapEndDate() == null
                && (cp.getCurrentStatus().getStatus().getName().equals(CertificationStatusType.WithdrawnByAcb.getName())
                        || cp.getCurrentStatus().getStatus().getName()
                                .equals(CertificationStatusType.WithdrawnByDeveloper.getName())
                        || cp.getCurrentStatus().getStatus().getName()
                                .equals(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName()))) {
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
                LocalDateTime statusDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(mostRecent.getEventDate()),
                        ZoneId.systemDefault());
                Duration timeBetween = Duration.between(statusDate, LocalDateTime.now());
                long numDays = timeBetween.toDays();
                if (numDays > getNumDaysAllowed()) {
                    LocalDateTime dateBroken = statusDate.plusDays(getNumDaysAllowed() + 1);
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
