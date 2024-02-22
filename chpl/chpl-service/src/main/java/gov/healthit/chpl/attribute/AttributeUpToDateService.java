package gov.healthit.chpl.attribute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.util.CertificationResultRules;

@Component
public class AttributeUpToDateService {

    private StandardsUpToDateService standardsUpToDateService;
    private FunctionalitiesTestedUpToDateService functionalitiesTestedUpToDateService;

    @Autowired
    public AttributeUpToDateService(StandardDAO standardDAO, FunctionalityTestedDAO functionalityTestedDAO, CertificationResultRules certificationResultRules) {
        standardsUpToDateService = new StandardsUpToDateService(standardDAO, certificationResultRules);
        functionalitiesTestedUpToDateService = new FunctionalitiesTestedUpToDateService(functionalityTestedDAO, certificationResultRules);
    }

    public AttributeUpToDate getAttributeUpToDate(AttributeType attributeType, CertificationResult certificationResults) {
        if (attributeType == AttributeType.STANDARDS) {
            return standardsUpToDateService.getAttributeUpToDate(certificationResults);
        } else if (attributeType == AttributeType.FUNCTIONALITIES_TESTED) {
            return functionalitiesTestedUpToDateService.getAttributeUpToDate(certificationResults);
        } else {
            return null;
        }
    }
}
