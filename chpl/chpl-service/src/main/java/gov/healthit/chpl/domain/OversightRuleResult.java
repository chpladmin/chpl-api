package gov.healthit.chpl.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;
import gov.healthit.chpl.util.Util;

public class OversightRuleResult {
    private SurveillanceOversightRule rule;
    private Date dateBroken;
    private long numDaysBroken = -1;

    public OversightRuleResult() {
    }

    public OversightRuleResult(SurveillanceOversightRule rule, Date dateBroken, long numDaysBroken) {
        this.rule = rule;
        this.dateBroken = Util.getNewDate(dateBroken);
        this.numDaysBroken = numDaysBroken;
    }

    public SurveillanceOversightRule getRule() {
        return rule;
    }

    public void setRule(final SurveillanceOversightRule rule) {
        this.rule = rule;
    }

    public Date getDateBroken() {
        return Util.getNewDate(dateBroken);
    }

    public void setDateBroken(final Date dateBroken) {
        this.dateBroken = Util.getNewDate(dateBroken);
    }

    public long getNumDaysBroken() {
        if (this.dateBroken != null && this.numDaysBroken < 0) {
            LocalDateTime statusDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.dateBroken.getTime()),
                    ZoneId.systemDefault());
            Duration timeBetween = Duration.between(statusDate, LocalDateTime.now());
            this.numDaysBroken = timeBetween.toDays();
        }

        return numDaysBroken;
    }

    public void setNumDaysBroken(final long numDaysBroken) {
        this.numDaysBroken = numDaysBroken;
    }
}
