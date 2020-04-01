package gov.healthit.chpl.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;

@Component
public class CuresUpdateService {
    private static final Logger LOGGER = LogManager.getLogger(CuresUpdateService.class);
    private FF4j ff4j;
    private CertificationCriterionService criteriaService;

    private Long b6Id;
    private Long g8Id;
    private List<Long> originalCriteriaIds = new ArrayList<Long>();
    private List<Long> newPnSCriteria = new ArrayList<Long>();
    private List<Long> curesCriteriaIds = new ArrayList<Long>();
    private List<Long> dependentCriteriaIds = new ArrayList<Long>();

    @Autowired
    public CuresUpdateService(FF4j ff4j, CertificationCriterionService criteriaService) {
        this.ff4j = ff4j;
        this.criteriaService = criteriaService;
    }

    @PostConstruct
    public void postConstruct() {
        b6Id = criteriaService.get(Criteria2015.B_6).getId();
        g8Id = criteriaService.get(Criteria2015.G_8).getId();

        originalCriteriaIds = new ArrayList<Long>(Arrays.asList(
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
                criteriaService.get(Criteria2015.G_9_OLD).getId()));

        newPnSCriteria = new ArrayList<Long>(Arrays.asList(
                criteriaService.get(Criteria2015.D_12).getId(),
                criteriaService.get(Criteria2015.D_13).getId()));

        curesCriteriaIds = new ArrayList<Long>(Arrays.asList(
                criteriaService.get(Criteria2015.A_1).getId(),
                criteriaService.get(Criteria2015.A_2).getId(),
                criteriaService.get(Criteria2015.A_3).getId(),
                criteriaService.get(Criteria2015.A_4).getId(),
                criteriaService.get(Criteria2015.A_5).getId(),
                criteriaService.get(Criteria2015.A_9).getId(),
                criteriaService.get(Criteria2015.A_10).getId(),
                criteriaService.get(Criteria2015.A_12).getId(),
                criteriaService.get(Criteria2015.A_13).getId(),
                criteriaService.get(Criteria2015.A_14).getId(),
                criteriaService.get(Criteria2015.A_15).getId(),
                criteriaService.get(Criteria2015.B_1_CURES).getId(),
                criteriaService.get(Criteria2015.B_2_CURES).getId(),
                criteriaService.get(Criteria2015.B_3_CURES).getId(),
                criteriaService.get(Criteria2015.B_7_CURES).getId(),
                criteriaService.get(Criteria2015.B_8_CURES).getId(),
                criteriaService.get(Criteria2015.B_9_CURES).getId(),
                criteriaService.get(Criteria2015.C_1).getId(),
                criteriaService.get(Criteria2015.C_2).getId(),
                criteriaService.get(Criteria2015.C_3_CURES).getId(),
                criteriaService.get(Criteria2015.C_4).getId(),
                criteriaService.get(Criteria2015.E_1_CURES).getId(),
                criteriaService.get(Criteria2015.E_2).getId(),
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
        List<Long> criteriaIds = listing.getCertificationResults().stream()
                .filter(criterion -> criterion.isSuccess() && !criterion.getCriterion().getRemoved())
                .map(criterion -> criterion.getCriterion().getId())
                .collect(Collectors.toList());
        return isCuresUpdate(criteriaIds);
    }

    public Boolean isCuresUpdate(PendingCertifiedProductDTO listing) {
        List<Long> criteriaIds = listing.getCertificationCriterion().stream()
                .filter(criterion -> criterion.getMeetsCriteria() && !criterion.getCriterion().getRemoved())
                .map(criterion -> criterion.getCriterion().getId())
                .collect(Collectors.toList());
        return isCuresUpdate(criteriaIds);
    }

    private Boolean isCuresUpdate(List<Long> criteriaIds) {
        try {
            if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
                return false;
            }
            if (!meetsB6RequirementForCuresUpdate(criteriaIds)) {
                return false;
            }
            if (!passOriginalCriteriaRequirement(criteriaIds)) {
                return false;
            }
            if (meetsD12D13RequirementForCuresUpdate(criteriaIds)) {
                return meetsG8RequirementForCuresUpdate(criteriaIds);
            } else {
                if (hasNoCuresCriteria(criteriaIds)) {
                    return passesOnlyDependentCriteriaRule(criteriaIds);
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Invalid state - " + e.getMessage());
        }
        return false;
    }

    private Boolean meetsB6RequirementForCuresUpdate(List<Long> criteriaIds) throws Exception {
        if (hasB6Criterion(criteriaIds)) {
            if (isPast24Months()) {
                if (isPast36Months()) {
                    throw new Exception("Listing is not valid; fails B6 Requirement past 36 months");
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private Boolean hasB6Criterion(List<Long> criteriaIds) {
        return criteriaIds.stream()
                .filter(id -> id.equals(b6Id))
                .findAny()
                .isPresent();
    }

    private Boolean passOriginalCriteriaRequirement(List<Long> criteriaIds) throws Exception {
        if (hasOriginalCriteria(criteriaIds)) {
            if (isPast24Months()) {
                throw new Exception("Listing is not valid; has revised criteria past 24 months");
            } else {
                return false;
            }
        }
        return true;
    }

    private Boolean hasOriginalCriteria(List<Long> criteriaIds) {
        return criteriaIds.stream()
                .filter(id -> originalCriteriaIds.contains(id))
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

    private Boolean meetsG8RequirementForCuresUpdate(List<Long> criteriaIds) throws Exception {
        if (hasG8Criteria(criteriaIds)) {
            if (isPast24Months()) {
                throw new Exception("Listing is not valid; fails g8 requirement past 24 months");
            } else {
                return false;
            }
        }
        return true;
    }

    private Boolean hasG8Criteria(List<Long> criteriaIds) {
        return criteriaIds.stream()
                .filter(id -> id.equals(g8Id))
                .findAny()
                .isPresent();
    }

    private Boolean hasNoCuresCriteria(List<Long> criteriaIds) throws Exception {
        if (hasCuresCriteria(criteriaIds)) {
            if (isPast24Months()) {
                throw new Exception("Listing is not valid; doesn't have d12/d13 and is past 24 months");
            } else {
                return false;
            }
        }
        return true;
    }

    private Boolean hasCuresCriteria(List<Long> criteriaIds) {
        return criteriaIds.stream()
                .filter(id -> curesCriteriaIds.contains(id))
                .findAny()
                .isPresent();
    }

    private Boolean passesOnlyDependentCriteriaRule(List<Long> criteriaIds) throws Exception {
        if (hasOnlyDependentCriteria(criteriaIds)) {
            return true;
        }
        if (isPast24Months()) {
            throw new Exception("Listing is not valid; has criteria that require P&S framework and is past 24 months");
        } else {
            return false;
        }
    }

    private Boolean hasOnlyDependentCriteria(List<Long> criteriaIds) {
        return criteriaIds.stream()
                .filter(id -> !dependentCriteriaIds.contains(id))
                .count() == 0L;
    }

    private Boolean isPast24Months() {
        return false;
    }

    private Boolean isPast36Months() {
        return false;
    }
}
