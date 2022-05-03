package gov.healthit.chpl.domain;

import java.time.Duration;
import java.time.LocalDate;

import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;

public class OversightRuleResult {
    private SurveillanceOversightRule rule;
    private LocalDate dateBroken;
    private long numDaysBroken = -1;

    public OversightRuleResult() {
    }

    public OversightRuleResult(SurveillanceOversightRule rule, LocalDate dateBroken, long numDaysBroken) {
        this.rule = rule;
        this.dateBroken = dateBroken;
        this.numDaysBroken = numDaysBroken;
    }

    public SurveillanceOversightRule getRule() {
        return rule;
    }

    public void setRule(SurveillanceOversightRule rule) {
        this.rule = rule;
    }

    public LocalDate getDateBroken() {
        return dateBroken;
    }

    public void setDateBroken(LocalDate dateBroken) {
        this.dateBroken = dateBroken;
    }

    public long getNumDaysBroken() {
        if (this.dateBroken != null && this.numDaysBroken < 0) {
            Duration timeBetween = Duration.between(this.dateBroken, LocalDate.now());
            this.numDaysBroken = timeBetween.toDays();
        }
        return numDaysBroken;
    }

    public void setNumDaysBroken(long numDaysBroken) {
        this.numDaysBroken = numDaysBroken;
    }
}
