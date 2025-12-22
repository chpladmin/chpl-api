package gov.healthit.chpl.attribute;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.standard.BaselineStandardService;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.standard.StandardGroupService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.Util;

@Component
public class AttributeUpToDateService {

    private BaselineStandardsUpToDateService baselineStandardsUpToDateService;
    private GroupedStandardsUpToDateService groupedStandardsUpToDateService;
    private FunctionalitiesTestedUpToDateService functionalitiesTestedUpToDateService;
    private CodeSetsUpToDateService codeSetsUpToDateService;

    @Autowired
    public AttributeUpToDateService(BaselineStandardService baselineStandardService,
            StandardGroupService standardGroupService,
            StandardDAO standardDao,
            FunctionalityTestedDAO functionalityTestedDao,
            CodeSetDAO codeSetDao,
            CertificationResultRules certificationResultRules) {
        baselineStandardsUpToDateService = new BaselineStandardsUpToDateService(baselineStandardService, standardDao, certificationResultRules);
        groupedStandardsUpToDateService = new GroupedStandardsUpToDateService(standardGroupService, standardDao, certificationResultRules);
        functionalitiesTestedUpToDateService = new FunctionalitiesTestedUpToDateService(functionalityTestedDao, certificationResultRules);
        codeSetsUpToDateService = new CodeSetsUpToDateService(codeSetDao, certificationResultRules);
    }

    public List<StandardUpToDate> getBaselineStandardsUpToDate(CertifiedProductSearchDetails listing, CertificationResult certResult, Logger logger) {
        logger.info("Checking whether all baseline standards are up-to-date for criterion {}", Util.formatCriteriaNumber(certResult.getCriterion()));
        return baselineStandardsUpToDateService.getAttributeUpToDate(listing, certResult, logger);
    }

    public List<StandardGroupUpToDate> getStandardGroupsUpToDate(CertifiedProductSearchDetails listing, CertificationResult certResult, Logger logger) {
        logger.info("Checking whether all grouped standards are up-to-date for criterion {}", Util.formatCriteriaNumber(certResult.getCriterion()));
        return groupedStandardsUpToDateService.getAttributeUpToDate(listing, certResult, logger);
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
