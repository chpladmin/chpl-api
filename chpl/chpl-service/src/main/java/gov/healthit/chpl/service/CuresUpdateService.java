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
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.Util;
import lombok.Data;

@Component
public class CuresUpdateService {
    private static final Logger LOGGER = LogManager.getLogger(CuresUpdateService.class);
    private FF4j ff4j;
    private CertificationCriterionService criteriaService;

    List<String> curesCriteriaNumbers = new ArrayList<String>();
    List<String> revisedCriteriaNumbers = new ArrayList<String>();

    @Autowired
    public CuresUpdateService(FF4j ff4j, CertificationCriterionService criteriaService) {
        this.ff4j = ff4j;
        this.criteriaService = criteriaService;
    }

    @PostConstruct
    public void postConstruct() {
        this.curesCriteriaNumbers = new ArrayList<String>(Arrays.asList(
                criteriaService.get(Criteria2015.A_1).getNumber(),
                criteriaService.get(Criteria2015.A_2).getNumber(),
                criteriaService.get(Criteria2015.A_3).getNumber(),
                criteriaService.get(Criteria2015.A_4).getNumber(),
                criteriaService.get(Criteria2015.A_5).getNumber(),
                criteriaService.get(Criteria2015.A_9).getNumber(),
                criteriaService.get(Criteria2015.A_10).getNumber(),
                criteriaService.get(Criteria2015.A_12).getNumber(),
                criteriaService.get(Criteria2015.A_13).getNumber(),
                criteriaService.get(Criteria2015.A_14).getNumber(),
                criteriaService.get(Criteria2015.A_15).getNumber(),
                criteriaService.get(Criteria2015.B_1).getNumber(),
                criteriaService.get(Criteria2015.B_2).getNumber(),
                criteriaService.get(Criteria2015.B_3_REVISED).getNumber(),
                criteriaService.get(Criteria2015.B_7_REVISED).getNumber(),
                criteriaService.get(Criteria2015.B_8_REVISED).getNumber(),
                criteriaService.get(Criteria2015.B_9_REVISED).getNumber(),
                criteriaService.get(Criteria2015.C_1).getNumber(),
                criteriaService.get(Criteria2015.C_2).getNumber(),
                criteriaService.get(Criteria2015.C_3_REVISED).getNumber(),
                criteriaService.get(Criteria2015.C_4).getNumber(),
                criteriaService.get(Criteria2015.E_1_REVISED).getNumber(),
                criteriaService.get(Criteria2015.E_2).getNumber(),
                criteriaService.get(Criteria2015.E_3).getNumber(),
                criteriaService.get(Criteria2015.F_1).getNumber(),
                criteriaService.get(Criteria2015.F_2).getNumber(),
                criteriaService.get(Criteria2015.F_3).getNumber(),
                criteriaService.get(Criteria2015.F_4).getNumber(),
                criteriaService.get(Criteria2015.F_5_REVISED).getNumber(),
                criteriaService.get(Criteria2015.F_6).getNumber(),
                criteriaService.get(Criteria2015.F_7).getNumber(),
                criteriaService.get(Criteria2015.G_7).getNumber(),
                criteriaService.get(Criteria2015.G_8).getNumber(),
                criteriaService.get(Criteria2015.G_9_REVISED).getNumber(),
                criteriaService.get(Criteria2015.H_1).getNumber(),
                criteriaService.get(Criteria2015.H_2).getNumber()));

        revisedCriteriaNumbers = new ArrayList<String>(Arrays.asList(
                criteriaService.get(Criteria2015.B_1).getNumber(),
                criteriaService.get(Criteria2015.B_2).getNumber(),
                criteriaService.get(Criteria2015.B_3).getNumber(),
                criteriaService.get(Criteria2015.B_7).getNumber(),
                criteriaService.get(Criteria2015.B_8).getNumber(),
                criteriaService.get(Criteria2015.B_9).getNumber(),
                criteriaService.get(Criteria2015.C_3).getNumber(),
                criteriaService.get(Criteria2015.D_2).getNumber(),
                criteriaService.get(Criteria2015.D_3).getNumber(),
                criteriaService.get(Criteria2015.D_10).getNumber(),
                criteriaService.get(Criteria2015.E_1).getNumber(),
                criteriaService.get(Criteria2015.F_5).getNumber(),
                criteriaService.get(Criteria2015.G_6).getNumber(),
                criteriaService.get(Criteria2015.G_9).getNumber()));
    }

    public Boolean isCuresUpdate(CertifiedProductSearchDetails listing) {
        List<CuresUpdateCriterion> criteria = listing.getCertificationResults().stream()
                .map(criterion -> new CuresUpdateCriterion(criterion))
                .collect(Collectors.<CuresUpdateCriterion> toList());
        return isCuresUpdate(criteria);
    }

    public Boolean isCuresUpdate(PendingCertifiedProductDTO listing) {
        List<CuresUpdateCriterion> criteria = listing.getCertificationCriterion().stream()
                .map(criterion -> new CuresUpdateCriterion(criterion))
                .collect(Collectors.<CuresUpdateCriterion> toList());
        return isCuresUpdate(criteria);
    }

    private Boolean isCuresUpdate(List<CuresUpdateCriterion> criteria) {
        try {
            if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
                return false;
            }
            if (!meetsB6RequirementForCuresUpdate(criteria)) {
                return false;
            }
            if (!passRevisedCriteriaRequirement(criteria)) {
                return false;
            }
            if (meetsD12D13RequirementForCuresUpdate(criteria)) {
                return meetsG8RequirementForCuresUpdate(criteria);
            } else {
                if (hasNoCuresCriteria(criteria)) {
                    return passesOnlyDependentCriteriaRule(criteria);
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Invalid state - " + e.getMessage());
        }
        return false;
    }

    private Boolean meetsB6RequirementForCuresUpdate(List<CuresUpdateCriterion> criteria) throws Exception {
        if (hasB6Criterion(criteria)) {
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

    private Boolean hasB6Criterion(List<CuresUpdateCriterion> criteria) {
        return criteria.stream()
                .filter(criterion -> criterion.getCuresNumber()
                        .equalsIgnoreCase(criteriaService.get(Criteria2015.B_6).getNumber()))
                .findFirst().get().getSuccess();
    }

    private Boolean passRevisedCriteriaRequirement(List<CuresUpdateCriterion> criteria) throws Exception {
        if (hasRevisedCriteria(criteria)) {
            if (isPast24Months()) {
                throw new Exception("Listing is not valid; has revised criteria past 24 months");
            } else {
                return false;
            }
        }
        return true;
    }

    private Boolean hasRevisedCriteria(List<CuresUpdateCriterion> criteria) {
        return criteria.stream()
                .filter(criterion -> criterion.getSuccess() && revisedCriteriaNumbers.contains(criterion.getCuresNumber()))
                .findAny()
                .isPresent();
    }

    private Boolean meetsD12D13RequirementForCuresUpdate(List<CuresUpdateCriterion> criteria) {
        if (hasD12D13Criteria(criteria)) {
            return true;
        }
        return false;
    }

    private Boolean hasD12D13Criteria(List<CuresUpdateCriterion> criteria) {
        return criteria.stream()
                .filter(criterion -> criterion.getSuccess())
                .filter(criterion -> {
                    return criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_12).getNumber())
                            || criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_13).getNumber());
                })
                .count() == 2;
    }

    private Boolean meetsG8RequirementForCuresUpdate(List<CuresUpdateCriterion> criteria) throws Exception {
        if (hasG8Criteria(criteria)) {
            if (isPast24Months()) {
                throw new Exception("Listing is not valid; fails g8 requirement past 24 months");
            } else {
                return false;
            }
        }
        return true;
    }

    private Boolean hasG8Criteria(List<CuresUpdateCriterion> criteria) {
        return criteria.stream()
                .filter(criterion -> criterion.getSuccess())
                .filter(criterion -> criterion.getCuresNumber()
                        .equalsIgnoreCase(criteriaService.get(Criteria2015.G_8).getNumber()))
                .count() == 1;
    }

    private Boolean hasNoCuresCriteria(List<CuresUpdateCriterion> criteria) throws Exception {
        if (hasCuresCriteria(criteria)) {
            if (isPast24Months()) {
                throw new Exception("Listing is not valid; doesn't have d12/d13 and is past 24 months");
            } else {
                return false;
            }
        }
        return true;
    }

    private Boolean hasCuresCriteria(List<CuresUpdateCriterion> criteria) {
        return criteria.stream()
                .filter(criterion -> criterion.getSuccess() && curesCriteriaNumbers.contains(criterion.getCuresNumber()))
                .findAny()
                .isPresent();

    }

    private Boolean passesOnlyDependentCriteriaRule(List<CuresUpdateCriterion> criteria) throws Exception {
        if (hasOnlyDependentCriteria(criteria)) {
            return true;
        }
        throw new Exception("In \"no\" state on last check. Results are unpredictable");
    }

    private Boolean hasOnlyDependentCriteria(List<CuresUpdateCriterion> criteria) {
        return criteria.stream()
                .filter(criterion -> criterion.getSuccess())
                .filter(criterion -> {
                    return !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.B_10).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_1).getNumber())
                            && !criterion.getCuresNumber()
                                    .equalsIgnoreCase(criteriaService.get(Criteria2015.D_2_REVISED).getNumber())
                            && !criterion.getCuresNumber()
                                    .equalsIgnoreCase(criteriaService.get(Criteria2015.D_3_REVISED).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_4).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_5).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_6).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_7).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_8).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_9).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_10).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.D_11).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.G_1).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.G_2).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.G_3).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.G_4).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.G_5).getNumber())
                            && !criterion.getCuresNumber()
                                    .equalsIgnoreCase(criteriaService.get(Criteria2015.G_6_REVISED).getNumber())
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(10) (Cures Update)") // maybe
                            && !criterion.getCuresNumber().equalsIgnoreCase(criteriaService.get(Criteria2015.G_10).getNumber()); // maybe
                })
                .count() == 0;
    }

    private Boolean isPast24Months() {
        return false;
    }

    private Boolean isPast36Months() {
        return false;
    }

    @Data
    private class CuresUpdateCriterion {
        private String curesNumber;
        private Boolean success;

        CuresUpdateCriterion(CertificationResult result) {
            this.curesNumber = Util.formatCriteriaNumber(result.getCriterion());
            this.success = result.isSuccess();
        }

        CuresUpdateCriterion(PendingCertificationResultDTO result) {
            this.curesNumber = Util.formatCriteriaNumber(result.getCriterion());
            this.success = result.getMeetsCriteria();
        }
    }
}
