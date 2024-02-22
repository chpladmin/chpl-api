package gov.healthit.chpl.attribute;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class StandardsUpToDateService {

    private StandardDAO standardDAO;
    private CertificationResultRules certificationResultRules;

    public StandardsUpToDateService(StandardDAO standardDAO, CertificationResultRules certificationResultRules) {
        this.standardDAO = standardDAO;
        this.certificationResultRules = certificationResultRules;
    }

    public AttributeUpToDate getAttributeUpToDate(CertificationResult certificationResult) {
        return AttributeUpToDate.builder()
                .attributeType(AttributeType.STANDARDS)
                .eligibleForAttribute(isCriteriaEligibleForStandards(certificationResult.getCriterion()))
                .upToDate(areStandardsUpToDate(certificationResult))
                .criterion(certificationResult.getCriterion())
                .build();
    }

    private Boolean areStandardsUpToDate(CertificationResult certificationResult) {
        return areAttestedToStandardsUpToDate(certificationResult)
                && areUnattestedStandardsUpToDate(certificationResult);
    }

    private Boolean areUnattestedStandardsUpToDate(CertificationResult certificationResult) {
        return getUnattestedToStandards(certificationResult).stream()
                .filter(std -> DateUtil.isDateBetweenInclusive(Pair.of(std.getStartDay(), std.getEndDay() == null ? LocalDate.MAX : std.getEndDay()), LocalDate.now()))
                .findAny()
                .isEmpty();
    }

    private Boolean areAttestedToStandardsUpToDate(CertificationResult certificationResult) {
        Boolean upToDate = false;
        if (CollectionUtils.isNotEmpty(certificationResult.getStandards())) {
            upToDate = certificationResult.getStandards().stream()
                    .filter(certResultStandard -> certResultStandard.getStandard().getEndDay() != null)
                    .findAny()
                    .isEmpty();
        }
        return upToDate;
    }

    private Boolean isCriteriaEligibleForStandards(CertificationCriterion criterion) {
        return certificationResultRules.hasCertOption(criterion.getId(), CertificationResultRules.STANDARD);
    }

    private List<Standard> getUnattestedToStandards(CertificationResult certificationResult) {
        return getAllStandardsForCriterion(certificationResult.getCriterion()).stream()
                .filter(std -> !isStandardInList(std, certificationResult.getStandards().stream().map(crs -> crs.getStandard()).toList()))
                .toList();
    }

    private Boolean isStandardInList(Standard standardToCheck, List<Standard> standards) {
        return standards.stream()
                .filter(std -> std.getId().equals(standardToCheck.getId()))
                .findAny()
                .isPresent();
    }

    private List<Standard> getAllStandardsForCriterion(CertificationCriterion criterion) {
        try {
            return standardDAO.getAllStandardCriteriaMap().stream()
                    .filter(map -> map.getCriterion().getId().equals(criterion.getId()))
                    .map(map -> map.getStandard())
                    .toList();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve Standards for Criterion.", e);
            return List.of();
        }
    }
}
