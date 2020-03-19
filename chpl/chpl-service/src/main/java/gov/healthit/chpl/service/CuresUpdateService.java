package gov.healthit.chpl.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.Util;
import lombok.Data;

@Component
public class CuresUpdateService {
    private static final Logger LOGGER = LogManager.getLogger(CuresUpdateService.class);

    public CuresUpdateService() {
    }

    public Boolean isCuresUpdate(CertifiedProductSearchDetails listing) {
        List<CuresUpdateCriterion> criteria = listing.getCertificationResults().stream()
                .map(criterion -> new CuresUpdateCriterion(criterion))
                .collect(Collectors.<CuresUpdateCriterion>toList());
        return isCuresUpdate(criteria);
    }

    public Boolean isCuresUpdate(PendingCertifiedProductDTO listing) {
        List<CuresUpdateCriterion> criteria = listing.getCertificationCriterion().stream()
                .map(criterion -> new CuresUpdateCriterion(criterion))
                .collect(Collectors.<CuresUpdateCriterion>toList());
        return isCuresUpdate(criteria);
    }

    private Boolean isCuresUpdate(List<CuresUpdateCriterion> criteria) {
        try {
            // add flag check for ERD
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
                    throw new Exception("Listing is not valid");
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    private Boolean hasB6Criterion(List<CuresUpdateCriterion> criteria) {
        return criteria.stream()
                .filter(criterion -> criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(6)"))
                .findFirst().get().getSuccess();
    }

    private Boolean passRevisedCriteriaRequirement(List<CuresUpdateCriterion> criteria) throws Exception {
        if (hasRevisedCriteria(criteria)) {
            if (isPast24Months()) {
                throw new Exception("Listing is not valid");
            } else {
                return false;
            }
        }
        return true;
    }

    private Boolean hasRevisedCriteria(List<CuresUpdateCriterion> criteria) {
        return criteria.stream()
                .filter(criterion -> criterion.getSuccess())
                .filter(criterion -> {
                    return criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(1)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(2)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(3)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(7)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(8)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(9)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (c)(3)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(2)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(3)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(10)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (e)(1)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (f)(5)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(6)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(9)");
                })
                .count() > 0;
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
                    return criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(12)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(13)");
                })
                .count() == 2;
    }

    private Boolean meetsG8RequirementForCuresUpdate(List<CuresUpdateCriterion> criteria) throws Exception {
        if (hasG8Criteria(criteria)) {
            if (isPast24Months()) {
                throw new Exception("Listing is not valid");
            } else {
                return false;
            }
        }
        return true;
    }

    private Boolean hasG8Criteria(List<CuresUpdateCriterion> criteria) {
        return criteria.stream()
                .filter(criterion -> criterion.getSuccess())
                .filter(criterion -> criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(8)"))
                .count() == 1;
    }


    private Boolean hasNoCuresCriteria(List<CuresUpdateCriterion> criteria) throws Exception {
        if (hasCuresCriteria(criteria)) {
            if (isPast24Months()) {
                throw new Exception("Listing is not valid");
            } else {
                return false;
            }
        }
        return true;
    }

    private Boolean hasCuresCriteria(List<CuresUpdateCriterion> criteria) {
        return criteria.stream()
                .filter(criterion -> criterion.getSuccess())
                .filter(criterion -> {
                    return criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(1)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(2)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(3)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(4)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(5)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(9)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(10)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(12)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(13)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(14)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (a)(15)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(1) Cures Update")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(2) Cures Update")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(3) Cures Update")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(7) Cures Update")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(8) Cures Update")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(9) Cures Update")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (c)(1)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (c)(2)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (c)(3) Cures Update")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (c)(4)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (e)(1) Cures Update")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (e)(2)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (e)(3)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (f)(1)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (f)(2)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (f)(3)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (f)(4)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (f)(5) Cures Update")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (f)(6)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (f)(7)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(7)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(8)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(9) Cures Update")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (h)(1)")
                            || criterion.getCuresNumber().equalsIgnoreCase("170.315 (h)(2)");
                })
                .count() > 0;
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
                    return !criterion.getCuresNumber().equalsIgnoreCase("170.315 (b)(10)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(1)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(2) Cures Update")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(3) Cures Update")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(4)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(5)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(6)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(7)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(8)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(9)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(10)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(11)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(1)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(2)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(3)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(4)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(5)")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(6) Cures Update")
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (d)(10) Cures Update") // not sure about this one
                            && !criterion.getCuresNumber().equalsIgnoreCase("170.315 (g)(10)"); // not sure about this one
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
