package gov.healthit.chpl.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;

@Component
public class CuresUpdateService {
    private static final Logger LOGGER = LogManager.getLogger(CuresUpdateService.class);
    private CertificationCriterionService criteriaService;

    private Long b6Id;
    private List<Long> needsToBeUpdatedCriteriaIds = new ArrayList<Long>();
    private List<Long> newPnSCriteria = new ArrayList<Long>();
    private List<Long> requiresPnSCriteriaIds = new ArrayList<Long>();
    private List<Long> dependentCriteriaIds = new ArrayList<Long>();

    @Autowired
    public CuresUpdateService(CertificationCriterionService criteriaService) {
        this.criteriaService = criteriaService;
    }

    @PostConstruct
    public void postConstruct() {
        b6Id = criteriaService.get(Criteria2015.B_6).getId();

        needsToBeUpdatedCriteriaIds = new ArrayList<Long>(Arrays.asList(
                criteriaService.get(Criteria2015.B_1_OLD).getId(),
                criteriaService.get(Criteria2015.B_2_OLD).getId(),
                criteriaService.get(Criteria2015.B_3_OLD).getId(),
                criteriaService.get(Criteria2015.B_7_OLD).getId(),
                criteriaService.get(Criteria2015.B_8_OLD).getId(),
                criteriaService.get(Criteria2015.B_9_OLD).getId(),
                criteriaService.get(Criteria2015.C_3_OLD).getId(),
                criteriaService.get(Criteria2015.D_2_OLD).getId(),
                criteriaService.get(Criteria2015.D_3_OLD).getId(),
                criteriaService.get(Criteria2015.D_10_OLD).getId(),
                criteriaService.get(Criteria2015.E_1_OLD).getId(),
                criteriaService.get(Criteria2015.F_5_OLD).getId(),
                criteriaService.get(Criteria2015.G_6_OLD).getId(),
                criteriaService.get(Criteria2015.G_8).getId(),
                criteriaService.get(Criteria2015.G_9_OLD).getId()));

        newPnSCriteria = new ArrayList<Long>(Arrays.asList(
                criteriaService.get(Criteria2015.D_12).getId(),
                criteriaService.get(Criteria2015.D_13).getId()));

        requiresPnSCriteriaIds = new ArrayList<Long>(Arrays.asList(
                criteriaService.get(Criteria2015.A_1).getId(),
                criteriaService.get(Criteria2015.A_2).getId(),
                criteriaService.get(Criteria2015.A_3).getId(),
                criteriaService.get(Criteria2015.A_4).getId(),
                criteriaService.get(Criteria2015.A_5).getId(),
                criteriaService.get(Criteria2015.A_9).getId(),
                criteriaService.get(Criteria2015.A_12).getId(),
                criteriaService.get(Criteria2015.A_14).getId(),
                criteriaService.get(Criteria2015.A_15).getId(),
                criteriaService.get(Criteria2015.B_1_CURES).getId(),
                criteriaService.get(Criteria2015.B_2_CURES).getId(),
                criteriaService.get(Criteria2015.B_3_CURES).getId(),
                criteriaService.get(Criteria2015.B_7_CURES).getId(),
                criteriaService.get(Criteria2015.B_8_CURES).getId(),
                criteriaService.get(Criteria2015.B_9_CURES).getId(),
                criteriaService.get(Criteria2015.B_6).getId(),
                criteriaService.get(Criteria2015.C_1).getId(),
                criteriaService.get(Criteria2015.C_2).getId(),
                criteriaService.get(Criteria2015.C_3_CURES).getId(),
                criteriaService.get(Criteria2015.C_4).getId(),
                criteriaService.get(Criteria2015.E_1_CURES).getId(),
                criteriaService.get(Criteria2015.E_3).getId(),
                criteriaService.get(Criteria2015.F_1).getId(),
                criteriaService.get(Criteria2015.F_2).getId(),
                criteriaService.get(Criteria2015.F_3).getId(),
                criteriaService.get(Criteria2015.F_4).getId(),
                criteriaService.get(Criteria2015.F_5_CURES).getId(),
                criteriaService.get(Criteria2015.F_6).getId(),
                criteriaService.get(Criteria2015.F_7).getId(),
                criteriaService.get(Criteria2015.G_7).getId(),
                criteriaService.get(Criteria2015.G_8).getId(),
                criteriaService.get(Criteria2015.G_9_CURES).getId(),
                criteriaService.get(Criteria2015.G_10).getId(),
                criteriaService.get(Criteria2015.H_1).getId(),
                criteriaService.get(Criteria2015.H_2).getId()));

        dependentCriteriaIds = new ArrayList<Long>(Arrays.asList(
                criteriaService.get(Criteria2015.B_10).getId(),
                criteriaService.get(Criteria2015.D_1).getId(),
                criteriaService.get(Criteria2015.D_2_CURES).getId(),
                criteriaService.get(Criteria2015.D_3_CURES).getId(),
                criteriaService.get(Criteria2015.D_4).getId(),
                criteriaService.get(Criteria2015.D_5).getId(),
                criteriaService.get(Criteria2015.D_6).getId(),
                criteriaService.get(Criteria2015.D_7).getId(),
                criteriaService.get(Criteria2015.D_8).getId(),
                criteriaService.get(Criteria2015.D_9).getId(),
                criteriaService.get(Criteria2015.D_10_CURES).getId(),
                criteriaService.get(Criteria2015.D_11).getId(),
                criteriaService.get(Criteria2015.G_1).getId(),
                criteriaService.get(Criteria2015.G_2).getId(),
                criteriaService.get(Criteria2015.G_3).getId(),
                criteriaService.get(Criteria2015.G_4).getId(),
                criteriaService.get(Criteria2015.G_5).getId(),
                criteriaService.get(Criteria2015.G_6_CURES).getId()));
    }

    public Boolean isCuresUpdate(CertifiedProductSearchDetails listing) {
        if (listing.getEdition() == null
                || !listing.getEdition().getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())) {
            return null;
        }

        List<Long> criteriaIds = listing.getCertificationResults().stream()
                .filter(criterion -> BooleanUtils.isTrue(criterion.getSuccess()))
                .map(criterion -> criterion.getCriterion().getId())
                .collect(Collectors.toList());
        return isCuresUpdate(criteriaIds);
    }

    private Boolean isCuresUpdate(List<Long> criteriaIds) {
        try {
            if (!passNeedsToBeUpdatedCriteriaRequirement(criteriaIds)) {
                return false;
            }
            if (!meetsB6RequirementForCuresUpdate(criteriaIds)) {
                return false;
            }
            if (meetsD12D13RequirementForCuresUpdate(criteriaIds)) {
                return true;
            }
            if (hasAnyCriteriaRequiringPnS(criteriaIds)) {
                return false;
            }
            if (passesOnlyDependentCriteriaRule(criteriaIds)) {
                return true;
            }
            throw new Exception("Listing is not valid; has fallen through logic");
        } catch (Exception e) {
            LOGGER.error("Invalid state - " + e.getMessage());
        }
        return false;
    }

    private Boolean meetsB6RequirementForCuresUpdate(List<Long> criteriaIds) throws Exception {
        if (hasB6Criterion(criteriaIds) && isPast36Months()) {
            throw new Exception("Listing is not valid; fails B6 Requirement past 36 months");
        }
        return true;
    }

    private Boolean hasB6Criterion(List<Long> criteriaIds) {
        return criteriaIds.stream()
                .filter(id -> id.equals(b6Id))
                .findAny()
                .isPresent();
    }

    private Boolean passNeedsToBeUpdatedCriteriaRequirement(List<Long> criteriaIds) throws Exception {
        if (hasCriteriaRequiringUpdate(criteriaIds)) {
            return false;
        }
        return true;
    }

    private Boolean hasCriteriaRequiringUpdate(List<Long> criteriaIds) {
        return criteriaIds.stream()
                .filter(id -> needsToBeUpdatedCriteriaIds.contains(id))
                .findAny()
                .isPresent();
    }

    private Boolean meetsD12D13RequirementForCuresUpdate(List<Long> criteriaIds) {
        if (hasD12D13Criteria(criteriaIds)) {
            return true;
        }
        return false;
    }

    private Boolean hasD12D13Criteria(List<Long> criteriaIds) {
        return criteriaIds.stream()
                .filter(id -> newPnSCriteria.contains(id))
                .count() == 2L;
    }

    private Boolean hasAnyCriteriaRequiringPnS(List<Long> criteriaIds) throws Exception {
        if (hasCriterionThatRequiresPnS(criteriaIds)) {
            return true;
        }
        return false;
    }

    private Boolean hasCriterionThatRequiresPnS(List<Long> criteriaIds) {
        return criteriaIds.stream()
                .filter(id -> requiresPnSCriteriaIds.contains(id))
                .findAny()
                .isPresent();
    }

    private Boolean passesOnlyDependentCriteriaRule(List<Long> criteriaIds) throws Exception {
        if (hasOnlyDependentCriteria(criteriaIds)) {
            return true;
        }
        return false;
    }

    private Boolean hasOnlyDependentCriteria(List<Long> criteriaIds) {
        return criteriaIds.stream()
                .filter(id -> !dependentCriteriaIds.contains(id))
                .count() == 0L;
    }

    private Boolean isPast36Months() {
        return false;
    }
}
