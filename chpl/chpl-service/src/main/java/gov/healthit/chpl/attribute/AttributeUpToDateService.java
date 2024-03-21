package gov.healthit.chpl.attribute;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CertificationResultCodeSetDAO;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTestedDAO;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.standard.CertificationResultStandardDAO;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.util.CertificationResultRules;

@Component
public class AttributeUpToDateService {

    private StandardsUpToDateService standardsUpToDateService;
    private FunctionalitiesTestedUpToDateService functionalitiesTestedUpToDateService;
    private CodeSetsUpToDateService codeSetsUpToDateService;

    @Autowired
    public AttributeUpToDateService(StandardDAO standardDAO, CertificationResultStandardDAO certificationResultStandardDAO,
            FunctionalityTestedDAO functionalityTestedDAO, CertificationResultFunctionalityTestedDAO certificationResultFunctionalityTestedDAO,
            CodeSetDAO codeSetDAO, CertificationResultCodeSetDAO certificationResultCodeSetDAO,
            CertificationResultRules certificationResultRules) {
        standardsUpToDateService = new StandardsUpToDateService(standardDAO, certificationResultStandardDAO, certificationResultRules);
        functionalitiesTestedUpToDateService = new FunctionalitiesTestedUpToDateService(functionalityTestedDAO, certificationResultFunctionalityTestedDAO, certificationResultRules);
        codeSetsUpToDateService = new CodeSetsUpToDateService(codeSetDAO, certificationResultCodeSetDAO, certificationResultRules);
    }

    public AttributeUpToDate getAttributeUpToDate(AttributeType attributeType, CertificationResult certificationResults, Logger logger) {
        AttributeUpToDate attributeUpToDate = null;
        if (attributeType == AttributeType.STANDARDS) {
            attributeUpToDate = standardsUpToDateService.getAttributeUpToDate(certificationResults);
        } else if (attributeType == AttributeType.FUNCTIONALITIES_TESTED) {
            attributeUpToDate = functionalitiesTestedUpToDateService.getAttributeUpToDate(certificationResults);
        } else if (attributeType == AttributeType.CODE_SETS) {
            attributeUpToDate = codeSetsUpToDateService.getAttributeUpToDate(certificationResults);
        }
        return attributeUpToDate;
    }
}
