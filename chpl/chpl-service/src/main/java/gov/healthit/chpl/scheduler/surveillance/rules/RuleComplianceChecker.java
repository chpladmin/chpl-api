package gov.healthit.chpl.scheduler.surveillance.rules;

import java.util.Date;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceOversightRule;

public interface RuleComplianceChecker {
    SurveillanceOversightRule getRuleChecked();

    Date check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc);
}
