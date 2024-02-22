package gov.healthit.chpl.attribute;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FunctionalitiesTestedUpToDateService {
    private FunctionalityTestedDAO functionalityTestedDAO;
    private CertificationResultRules certificationResultRules;

    public FunctionalitiesTestedUpToDateService(FunctionalityTestedDAO functionalityTestedDAO, CertificationResultRules certificationResultRules) {
        this.functionalityTestedDAO = functionalityTestedDAO;
        this.certificationResultRules = certificationResultRules;
    }

    public AttributeUpToDate getAttributeUpToDate(CertificationResult certificationResult) {
        return AttributeUpToDate.builder()
                .attributeType(AttributeType.FUNCTIONALITIES_TESTED)
                .eligibleForAttribute(isCriteriaEligibleForFunctionalitiesTested(certificationResult.getCriterion()))
                .upToDate(areFunctionalitiesTestedUpToDate(certificationResult))
                .criterion(certificationResult.getCriterion())
                .build();
    }

    private Boolean areFunctionalitiesTestedUpToDate(CertificationResult certificationResult) {
        return areAttestedToFunctionalitiesTestedUpToDate(certificationResult)
                && areUnattestedFunctionalitiesTestedUpToDate(certificationResult);
    }

    private Boolean areUnattestedFunctionalitiesTestedUpToDate(CertificationResult certificationResult) {
        return getUnattestedToFunctionalitiesTested(certificationResult).stream()
                .filter(ft -> DateUtil.isDateBetweenInclusive(Pair.of(ft.getStartDay(), ft.getEndDay() == null ? LocalDate.MAX : ft.getEndDay()), LocalDate.now()))
                .findAny()
                .isEmpty();
    }

    private Boolean areAttestedToFunctionalitiesTestedUpToDate(CertificationResult certificationResult) {
        Boolean upToDate = false;
        if (CollectionUtils.isNotEmpty(certificationResult.getFunctionalitiesTested())) {
            upToDate = certificationResult.getFunctionalitiesTested().stream()
                    .filter(certResultFT -> certResultFT.getFunctionalityTested().getEndDay() != null)
                    .findAny()
                    .isEmpty();
        }
        return upToDate;
    }

    private Boolean isCriteriaEligibleForFunctionalitiesTested(CertificationCriterion criterion) {
        return certificationResultRules.hasCertOption(criterion.getId(), CertificationResultRules.FUNCTIONALITY_TESTED);
    }

    private List<FunctionalityTested> getUnattestedToFunctionalitiesTested(CertificationResult certificationResult) {
        return getAllFunctionalitiesTestedForCriterion(certificationResult.getCriterion()).stream()
                .filter(ft -> !isFunctionalityTestedInList(ft, certificationResult.getFunctionalitiesTested().stream().map(crft -> crft.getFunctionalityTested()).toList()))
                .toList();
    }

    private Boolean isFunctionalityTestedInList(FunctionalityTested functionalityTestedToCheck, List<FunctionalityTested> functionalitiesTested) {
        return functionalitiesTested.stream()
                .filter(ft -> ft.getId().equals(functionalityTestedToCheck.getId()))
                .findAny()
                .isPresent();
    }

    private List<FunctionalityTested> getAllFunctionalitiesTestedForCriterion(CertificationCriterion criterion) {
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
