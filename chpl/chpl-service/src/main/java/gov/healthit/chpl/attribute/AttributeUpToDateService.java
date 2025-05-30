package gov.healthit.chpl.attribute;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.Util;

@Component
public class AttributeUpToDateService {

    private StandardsUpToDateService standardsUpToDateService;
    private FunctionalitiesTestedUpToDateService functionalitiesTestedUpToDateService;
    private CodeSetsUpToDateService codeSetsUpToDateService;

    @Autowired
    public AttributeUpToDateService(StandardDAO standardDao,
            FunctionalityTestedDAO functionalityTestedDao,
            CodeSetDAO codeSetDao,
            CertificationResultRules certificationResultRules) {
        standardsUpToDateService = new StandardsUpToDateService(standardDao, certificationResultRules);
        functionalitiesTestedUpToDateService = new FunctionalitiesTestedUpToDateService(functionalityTestedDao, certificationResultRules);
        codeSetsUpToDateService = new CodeSetsUpToDateService(codeSetDao, certificationResultRules);
    }

    public List<StandardUpToDate> getStandardsUpToDate(CertificationResult certResult, Logger logger) {
        logger.info("Checking whether all standards are up-to-date for criterion {}", Util.formatCriteriaNumber(certResult.getCriterion()));
        return standardsUpToDateService.getAttributeUpToDate(certResult, logger);
    }

    public List<FunctionalityTestedUpToDate> getFunctionalitiesTestedUpToDate(CertificationResult certResult, Logger logger) {
        logger.info("Checking whether all functionalities tested are up-to-date for criterion {}", Util.formatCriteriaNumber(certResult.getCriterion()));
        return functionalitiesTestedUpToDateService.getAttributeUpToDate(certResult, logger);
    }

    public List<CodeSetUpToDate> getCodeSetsUpToDate(CertificationResult certResult, Logger logger) {
        logger.info("Checking whether all code sets are up-to-date for criterion {}", Util.formatCriteriaNumber(certResult.getCriterion()));
        return codeSetsUpToDateService.getAttributeUpToDate(certResult, logger);
    }
}
