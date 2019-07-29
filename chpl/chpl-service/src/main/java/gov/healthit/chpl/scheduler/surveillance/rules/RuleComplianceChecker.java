package gov.healthit.chpl.scheduler.surveillance.rules;

import java.util.Date;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;

public interface RuleComplianceChecker {
    SurveillanceOversightRule getRuleChecked();

    Date check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc);
}
