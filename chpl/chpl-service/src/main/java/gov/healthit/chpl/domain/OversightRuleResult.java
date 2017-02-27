package gov.healthit.chpl.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class OversightRuleResult {
	private SurveillanceOversightRule rule;
	private Date dateBroken;
	private long numDaysBroken = -1;
	
	public OversightRuleResult() {}
	public OversightRuleResult(SurveillanceOversightRule rule, Date dateBroken, long numDaysBroken) {
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
	public Date getDateBroken() {
		return dateBroken;
	}
	public void setDateBroken(Date dateBroken) {
		this.dateBroken = dateBroken;
	}
	public long getNumDaysBroken() {
		if(this.dateBroken != null && this.numDaysBroken < 0) {
			LocalDateTime statusDate = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(this.dateBroken.getTime()), 
				    ZoneId.systemDefault());
			Duration timeBetween = Duration.between(statusDate, LocalDateTime.now());
			this.numDaysBroken = timeBetween.toDays();
		}
		
		return numDaysBroken;
	}
	public void setNumDaysBroken(long numDaysBroken) {
		this.numDaysBroken = numDaysBroken;
	}
}
