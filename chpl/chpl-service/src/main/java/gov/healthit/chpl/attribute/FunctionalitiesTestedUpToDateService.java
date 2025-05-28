package gov.healthit.chpl.attribute;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;

@Component
public class FunctionalitiesTestedUpToDateService {
    private FunctionalityTestedDAO functionalityTestedDao;
    private CertificationResultRules certificationResultRules;

    public FunctionalitiesTestedUpToDateService(FunctionalityTestedDAO functionalityTestedDao,
            CertificationResultRules certificationResultRules) {
        this.functionalityTestedDao = functionalityTestedDao;
        this.certificationResultRules = certificationResultRules;
    }

    public List<FunctionalityTestedUpToDate> getAttributeUpToDate(CertificationResult certResult, Logger logger) {
        List<FunctionalityTestedUpToDate> functionalityTestedUpToDateReports = new ArrayList<FunctionalityTestedUpToDate>();
        Boolean isCriteriaEligible = isCriteriaEligibleForFunctionalitiesTested(certResult.getCriterion(), logger);
        if (isCriteriaEligible) {
            List<FunctionalityTestedUpToDate> upToDateReportsForAttestedFunctionalitiesTested = getUpToDateReportsForAttestedFunctionalitiesTested(certResult, logger);
            if (!CollectionUtils.isEmpty(upToDateReportsForAttestedFunctionalitiesTested)) {
                functionalityTestedUpToDateReports.addAll(upToDateReportsForAttestedFunctionalitiesTested);
            }
            List<FunctionalityTestedUpToDate> upToDateReportsForUnattestedFunctionalitiesTested = getUpToDateReportsForUnattestedFunctionalitiesTested(certResult, logger);
            if (!CollectionUtils.isEmpty(upToDateReportsForUnattestedFunctionalitiesTested)) {
                functionalityTestedUpToDateReports.addAll(upToDateReportsForUnattestedFunctionalitiesTested);
            }
        }

        return functionalityTestedUpToDateReports;
    }

    private List<FunctionalityTestedUpToDate> getUpToDateReportsForAttestedFunctionalitiesTested(CertificationResult certResult, Logger logger) {
        logger.info("Checking attested functionalities tested for " + Util.formatCriteriaNumber(certResult.getCriterion()));

        List<FunctionalityTestedUpToDate> functionalityTestedUpToDateReports = new ArrayList<FunctionalityTestedUpToDate>();
        if (CollectionUtils.isNotEmpty(certResult.getFunctionalitiesTested())) {
            certResult.getFunctionalitiesTested().stream()
                    .peek(certResultFt -> logger.info("Checking attested functionality tested " + certResultFt.getFunctionalityTested().getRegulatoryTextCitation()))
                    .filter(certResultFt -> certResultFt.getFunctionalityTested().getEndDay() != null)
                    .peek(certResultFt -> logger.info("Attested functionality tested " + certResultFt.getFunctionalityTested().getRegulatoryTextCitation() + " is present but should not be"))
                    .map(certResultFt -> FunctionalityTestedUpToDate.builder()
                            .criterion(certResult.getCriterion())
                            .eligibleForAttribute(true)
                            .expiringButPresent(true)
                            .requiredButNotPresent(false)
                            .functionalityTested(certResultFt.getFunctionalityTested())
                            .build())
                    .forEach(ftUpToDate -> functionalityTestedUpToDateReports.add(ftUpToDate));
        }
        return functionalityTestedUpToDateReports;
    }

    private List<FunctionalityTestedUpToDate> getUpToDateReportsForUnattestedFunctionalitiesTested(CertificationResult certResult, Logger logger) {
        logger.info("Checking unattested functionalities tested for " + Util.formatCriteriaNumber(certResult.getCriterion()));
        List<FunctionalityTested> unattestedFunctionalitiesTested = getUnattestedToFunctionalitiesTested(certResult, logger);
        logger.info("There are " + unattestedFunctionalitiesTested.size() + " unattested functionalities tested for " + Util.formatCriteriaNumber(certResult.getCriterion()));

        List<FunctionalityTestedUpToDate> functionalityTestedUpToDateReports = new ArrayList<FunctionalityTestedUpToDate>();
        if (CollectionUtils.isNotEmpty(unattestedFunctionalitiesTested)) {
            unattestedFunctionalitiesTested.stream()
                    .peek(ft -> logger.info("Checking unattested functionality tested " + ft.getRegulatoryTextCitation()))
                    .filter(ft -> DateUtil.isDateBetweenInclusive(Pair.of(ft.getStartDay(), ft.getEndDay() == null ? LocalDate.MAX : ft.getEndDay()), LocalDate.now()))
                    .peek(ft -> logger.info("Unattested functionality tested " + ft.getRegulatoryTextCitation() + " is required but not found"))
                    .map(ft -> FunctionalityTestedUpToDate.builder()
                            .criterion(certResult.getCriterion())
                            .eligibleForAttribute(true)
                            .expiringButPresent(false)
                            .requiredButNotPresent(true)
                            .functionalityTested(ft)
                            .build())
                    .forEach(ftUpToDate -> functionalityTestedUpToDateReports.add(ftUpToDate));
        }
        return functionalityTestedUpToDateReports;
    }

    private Boolean isCriteriaEligibleForFunctionalitiesTested(CertificationCriterion criterion, Logger logger) {
        List<FunctionalityTested> functionalitiesTested = getAllFunctionalitiesTestedForCriterion(criterion, logger);
        return certificationResultRules.hasCertOption(criterion.getId(), CertificationResultRules.FUNCTIONALITY_TESTED)
                && !CollectionUtils.isEmpty(functionalitiesTested);
    }

    private List<FunctionalityTested> getUnattestedToFunctionalitiesTested(CertificationResult certResult, Logger logger) {
        return getAllFunctionalitiesTestedForCriterion(certResult.getCriterion(), logger).stream()
                .filter(ft -> !isFunctionalityTestedInList(ft, certResult.getFunctionalitiesTested().stream().map(crft -> crft.getFunctionalityTested()).toList()))
                .toList();
    }

    private Boolean isFunctionalityTestedInList(FunctionalityTested functionalityTestedToCheck, List<FunctionalityTested> functionalitiesTested) {
        return functionalitiesTested.stream()
                .filter(ft -> ft.getId().equals(functionalityTestedToCheck.getId()))
                .findAny()
                .isPresent();
    }

    private List<FunctionalityTested> getAllFunctionalitiesTestedForCriterion(CertificationCriterion criterion, Logger logger) {
        try {
            return functionalityTestedDao.getAllFunctionalityTestedCriteriaMap().stream()
                    .filter(map -> map.getCriterion().getId().equals(criterion.getId()))
                    .map(map -> map.getFunctionalityTested())
                    .toList();
        } catch (EntityRetrievalException e) {
            logger.error("Could not retrieve functionality tested for Criterion.", e);
            return List.of();
        }
    }

}
