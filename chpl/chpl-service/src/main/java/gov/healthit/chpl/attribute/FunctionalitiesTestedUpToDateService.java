package gov.healthit.chpl.attribute;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.OptionalLong;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTestedDAO;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FunctionalitiesTestedUpToDateService {
    private FunctionalityTestedDAO functionalityTestedDAO;
    private CertificationResultFunctionalityTestedDAO certificationResultFunctionalityTestedDAO;
    private CertificationResultRules certificationResultRules;

    public FunctionalitiesTestedUpToDateService(FunctionalityTestedDAO functionalityTestedDAO, CertificationResultFunctionalityTestedDAO certificationResultFunctionalityTestedDAO,
            CertificationResultRules certificationResultRules) {
        this.functionalityTestedDAO = functionalityTestedDAO;
        this.certificationResultFunctionalityTestedDAO = certificationResultFunctionalityTestedDAO;
        this.certificationResultRules = certificationResultRules;
    }

    public AttributeUpToDate getAttributeUpToDate(CertificationResult certificationResult, Logger logger) {
        Boolean isCriteriaEligible = isCriteriaEligibleForFunctionalitiesTested(certificationResult.getCriterion());
        Boolean upToDate = false;
        OptionalLong daysUpdatedEarly = OptionalLong.empty();

        if (isCriteriaEligible) {
            upToDate = areFunctionalitiesTestedUpToDate(certificationResult, logger);
            if (upToDate) {
                daysUpdatedEarly = getDaysUpdatedEarlyForCriteriaBasedOnFunctionalitiesTested(certificationResult);
            }
        }

        return AttributeUpToDate.builder()
                .attributeType(AttributeType.FUNCTIONALITIES_TESTED)
                .eligibleForAttribute(isCriteriaEligible)
                .upToDate(upToDate)
                .daysUpdatedEarly(daysUpdatedEarly)
                .criterion(certificationResult.getCriterion())
                .attributesExistForCriteria(Boolean.valueOf(CollectionUtils.isNotEmpty(getAllFunctionalitiesTestedForCriterion(certificationResult.getCriterion(), logger))))
                .build();    }

    private OptionalLong getDaysUpdatedEarlyForCriteriaBasedOnFunctionalitiesTested(CertificationResult certificationResult) {
        //Get the CertificationResultFunctionalitiesTested using DAO, so that we have the create date
        List<CertificationResultFunctionalityTested> certificationResultFunctionalitiesTested =
                certificationResultFunctionalityTestedDAO.getFunctionalitiesTestedForCertificationResult(certificationResult.getId());

        OptionalLong daysUpdatedEarly = OptionalLong.empty();
        if (CollectionUtils.isNotEmpty(certificationResultFunctionalitiesTested)) {
            daysUpdatedEarly = certificationResultFunctionalitiesTested.stream()
                    .filter(certResultFT -> certResultFT.getFunctionalityTested().getRequiredDay() != null
                            && LocalDate.now().isBefore(certResultFT.getFunctionalityTested().getRequiredDay())
                            && DateUtil.toLocalDate(certResultFT.getCreationDate().getTime()).isBefore(certResultFT.getFunctionalityTested().getRequiredDay()))
                    .mapToLong(certResultFT -> ChronoUnit.DAYS.between(DateUtil.toLocalDate(certResultFT.getCreationDate().getTime()), certResultFT.getFunctionalityTested().getRequiredDay()))
                    .min();

        }
        return daysUpdatedEarly;
    }

    private Boolean areFunctionalitiesTestedUpToDate(CertificationResult certificationResult, Logger logger) {
        return (areAttestedToFunctionalitiesTestedUpToDate(certificationResult, logger)
                && areUnattestedFunctionalitiesTestedUpToDate(certificationResult, logger));
    }

    private Boolean areUnattestedFunctionalitiesTestedUpToDate(CertificationResult certificationResult, Logger logger) {
        List<FunctionalityTested> unattestedFts = getUnattestedToFunctionalitiesTested(certificationResult, logger);
        logger.info("Got " + unattestedFts.size() + " unattested functionalities tested for criteria " + Util.formatCriteriaNumber(certificationResult.getCriterion()));
        unattestedFts.stream()
            .forEach(ft -> logger.info("\t" + ft.getRegulatoryTextCitation()));

        return getUnattestedToFunctionalitiesTested(certificationResult, logger).stream()
                .filter(ft -> DateUtil.isDateBetweenInclusive(Pair.of(ft.getStartDay(), ft.getEndDay() == null ? LocalDate.MAX : ft.getEndDay()), LocalDate.now()))
                .peek(ft -> logger.info("\t\t" + ft.getRegulatoryTextCitation() + " is NOT Up-To-Date"))
                .findAny()
                .isEmpty();
    }

    private Boolean areAttestedToFunctionalitiesTestedUpToDate(CertificationResult certificationResult, Logger logger) {
        Boolean upToDate = false;
        logger.info("Checking attested functionalities tested for " + Util.formatCriteriaNumber(certificationResult.getCriterion()));

        if (CollectionUtils.isNotEmpty(certificationResult.getFunctionalitiesTested())) {
            upToDate = certificationResult.getFunctionalitiesTested().stream()
                    .peek(certResultFT -> logger.info("\t" + certResultFT.getFunctionalityTested().getRegulatoryTextCitation()))
                    .filter(certResultFT -> certResultFT.getFunctionalityTested().getEndDay() != null)
                    .peek(certResultFT -> logger.info("\t\tNOT Up-To-Date"))
                    .findAny()
                    .isEmpty();
        }
        return upToDate;
    }

    private Boolean isCriteriaEligibleForFunctionalitiesTested(CertificationCriterion criterion) {
        return certificationResultRules.hasCertOption(criterion.getId(), CertificationResultRules.FUNCTIONALITY_TESTED);
    }

    private List<FunctionalityTested> getUnattestedToFunctionalitiesTested(CertificationResult certificationResult, Logger logger) {
        return getAllFunctionalitiesTestedForCriterion(certificationResult.getCriterion(), logger).stream()
                .filter(ft -> !isFunctionalityTestedInList(ft, certificationResult.getFunctionalitiesTested().stream().map(crft -> crft.getFunctionalityTested()).toList()))
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
            return functionalityTestedDAO.getAllFunctionalityTestedCriteriaMap().stream()
                    .filter(map -> map.getCriterion().getId().equals(criterion.getId()))
                    .map(map -> map.getFunctionalityTested())
                    .toList();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve Standards for Criterion.", e);
            return List.of();
        }
    }

}
