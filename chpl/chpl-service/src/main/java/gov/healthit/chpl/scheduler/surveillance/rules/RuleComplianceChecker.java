package gov.healthit.chpl.scheduler.surveillance.rules;

import java.time.LocalDate;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;

public interface RuleComplianceChecker {
    SurveillanceOversightRule getRuleChecked();

    LocalDate check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc);
}
