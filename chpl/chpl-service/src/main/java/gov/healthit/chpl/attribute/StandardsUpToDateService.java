package gov.healthit.chpl.attribute;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.Util;

@Component
public class StandardsUpToDateService {

    private StandardDAO standardDao;
    private CertificationResultRules certificationResultRules;

    public StandardsUpToDateService(StandardDAO standardDao,
            CertificationResultRules certificationResultRules) {
        this.standardDao = standardDao;
        this.certificationResultRules = certificationResultRules;
    }

    public List<StandardUpToDate> getAttributeUpToDate(CertificationResult certResult, Logger logger) {
        List<StandardUpToDate> standardUpToDateReports = new ArrayList<StandardUpToDate>();

        Boolean isCriteriaEligible = isCriteriaEligibleForStandards(certResult.getCriterion(), logger);
        if (isCriteriaEligible) {
            List<StandardUpToDate> upToDateReportsForUnattestedStandards = getUpToDateReportsForUnattestedStandards(certResult, logger);
            if (!CollectionUtils.isEmpty(upToDateReportsForUnattestedStandards)) {
                standardUpToDateReports.addAll(upToDateReportsForUnattestedStandards);
            }
        }

        return standardUpToDateReports;
    }

    private Boolean isCriteriaEligibleForStandards(CertificationCriterion criterion, Logger logger) {
        List<Standard> standardsForCriterion = getAllStandardsForCriterion(criterion, logger);
        return certificationResultRules.hasCertOption(criterion.getId(), CertificationResultRules.STANDARD)
                && !CollectionUtils.isEmpty(standardsForCriterion);
    }

    private List<StandardUpToDate> getUpToDateReportsForUnattestedStandards(CertificationResult certResult, Logger logger) {
        logger.info("Checking unattested standards for " + Util.formatCriteriaNumber(certResult.getCriterion()));
        List<Standard> unattestedStandards = getUnattestedToStandards(certResult, logger);
        logger.info("There are " + unattestedStandards.size() + " unattested standards for " + Util.formatCriteriaNumber(certResult.getCriterion()));

        List<StandardUpToDate> standardUpToDateReports = new ArrayList<StandardUpToDate>();
        if (CollectionUtils.isNotEmpty(unattestedStandards)) {
            unattestedStandards.stream()
                    .peek(std -> logger.info("Checking unattested standard " + std.getRegulatoryTextCitation()))
                    .filter(std -> std.getRequiredDay() != null)
                    .peek(std -> logger.info("Unattested standard " + std.getRegulatoryTextCitation() + " is required but not found"))
                    .map(std -> StandardUpToDate.builder()
                            .criterion(certResult.getCriterion())
                            .eligibleForAttribute(true)
                            .expiringButPresent(false)
                            .requiredButNotPresent(true)
                            .standard(std)
                            .build())
                    .forEach(stdUpToDate -> standardUpToDateReports.add(stdUpToDate));
        }
        return standardUpToDateReports;
    }

    private List<Standard> getUnattestedToStandards(CertificationResult certResult, Logger logger) {
        return getAllStandardsForCriterion(certResult.getCriterion(), logger).stream()
                .filter(std -> !isStandardInList(std, certResult.getStandards().stream().map(crs -> crs.getStandard()).toList()))
                .toList();
    }

    private Boolean isStandardInList(Standard standardToCheck, List<Standard> standards) {
        return standards.stream()
                .filter(std -> std.getId().equals(standardToCheck.getId()))
                .findAny()
                .isPresent();
    }

    private List<Standard> getAllStandardsForCriterion(CertificationCriterion criterion, Logger logger) {
        try {
            return standardDao.getAllStandardCriteriaMap().stream()
                    .filter(map -> map.getCriterion().getId().equals(criterion.getId()))
                    .map(map -> map.getStandard())
                    .toList();
        } catch (EntityRetrievalException e) {
            logger.error("Could not retrieve Standards for Criterion.", e);
            return List.of();
        }
    }
}
