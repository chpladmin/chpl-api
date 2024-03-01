package gov.healthit.chpl.attribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.util.CertificationResultRules;

public class CodeSetsUpToDateService {

    private CertificationResultRules certificationResultRules;

    public CodeSetsUpToDateService(CertificationResultRules certificationResultRules) {
        this.certificationResultRules = certificationResultRules;
    }

    public AttributeUpToDate getAttributeUpToDate(CertificationResult certificationResult) {
        return AttributeUpToDate.builder()
                .attributeType(AttributeType.CODE_SETS)
                //.eligibleForAttribute(isCriteriaEligibleForFunctionalitiesTested(certificationResult.getCriterion()))
                //.upToDate(areFunctionalitiesTestedUpToDate(certificationResult))
                .criterion(certificationResult.getCriterion())
                .build();
    }

    private Boolean isCriteriaEligibleForCodeSets(CertificationCriterion criterion) {
        //return certificationResultRules.hasCertOption(criterion.getId(), CertificationResultRules.);
    }

}
