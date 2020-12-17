package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("requiredAndRelatedCriteriaReviewer")
public class RequiredAndRelatedCriteriaReviewer implements Reviewer {
    private Environment env;
    private ErrorMessageUtil msgUtil;
    private CertificationCriterionService criterionService;
    private ValidationUtils validationUtils;

    private CertificationCriterion g4;
    private CertificationCriterion g5;

    private List<CertificationCriterion> bCriteriaWithDependencies = new ArrayList<CertificationCriterion>();

    private CertificationCriterion g10;
    private CertificationCriterion d1;
    private CertificationCriterion d2;
    private CertificationCriterion d2Cures;
    private CertificationCriterion d3;
    private CertificationCriterion d3Cures;
    private CertificationCriterion d5;
    private CertificationCriterion d6;
    private CertificationCriterion d7;
    private CertificationCriterion d8;
    private CertificationCriterion d9;
    private List<CertificationCriterion> d2Ord10 = new ArrayList<CertificationCriterion>();

    @PostConstruct
    public void postConstruct() {
        g4 = criterionService.get(new Long(env.getProperty("criterion.170_315_g_4")));
        g5 = criterionService.get(new Long(env.getProperty("criterion.170_315_g_5")));

        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_1"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_1_cures"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_2"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_2_cures"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_3"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_3_cures"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_4"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_5"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_6"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_7"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_7_cures"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_8"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_8_cures"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_9"))));
        bCriteriaWithDependencies.add(criterionService.get(new Long(env.getProperty("criterion.170_315_b_9_cures"))));

        g10 = criterionService.get(new Long(env.getProperty("criterion.170_315_g_10")));
        d1 = criterionService.get(new Long(env.getProperty("criterion.170_315_d_1")));
        d2 = criterionService.get(new Long(env.getProperty("criterion.170_315_d_2")));
        d2Cures = criterionService.get(new Long(env.getProperty("criterion.170_315_d_2_cures")));
        d3 = criterionService.get(new Long(env.getProperty("criterion.170_315_d_3")));
        d3Cures = criterionService.get(new Long(env.getProperty("criterion.170_315_d_3_cures")));
        d5 = criterionService.get(new Long(env.getProperty("criterion.170_315_d_5")));
        d6 = criterionService.get(new Long(env.getProperty("criterion.170_315_d_6")));
        d7 = criterionService.get(new Long(env.getProperty("criterion.170_315_d_7")));
        d8 = criterionService.get(new Long(env.getProperty("criterion.170_315_d_8")));
        d9 = criterionService.get(new Long(env.getProperty("criterion.170_315_d_9")));
        d2Ord10.add(d2);
        d2Ord10.add(d2Cures);
        CertificationCriterion d10 = criterionService.get(new Long(env.getProperty("criterion.170_315_d_10")));
        CertificationCriterion d10Cures = criterionService.get(new Long(env.getProperty("criterion.170_315_d_10_cures")));
        d2Ord10.add(d10);
        d2Ord10.add(d10Cures);
    }

    @Autowired
    public RequiredAndRelatedCriteriaReviewer(CertificationCriterionService criterionService,
            Environment env, ErrorMessageUtil msgUtil, ValidationUtils validationUtils) {
        this.criterionService = criterionService;
        this.env = env;
        this.msgUtil = msgUtil;
        this.validationUtils = validationUtils;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);

        checkAlwaysRequiredCriteria(listing, attestedCriteria);
        checkG10RequiredDependencies(listing, attestedCriteria);
        checkBCriteriaRequiredDependencies(listing, attestedCriteria);
    }

    private void checkAlwaysRequiredCriteria(CertifiedProductSearchDetails listing,
            List<CertificationCriterion> attestedCriteria) {
        if (!validationUtils.hasCriterion(g4, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g4)));
        }
        if (!validationUtils.hasCriterion(g5, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g5)));
        }
    }

    private void checkBCriteriaRequiredDependencies(CertifiedProductSearchDetails listing,
            List<CertificationCriterion> attestedCriteria) {
        if (!validationUtils.hasAnyCriteria(bCriteriaWithDependencies, attestedCriteria)) {
            return;
        }
        if (!validationUtils.hasCriterion(d1, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    "170.315 (b)",
                    Util.formatCriteriaNumber(d1)));
        }
        List<CertificationCriterion> d2Criteria = Stream.of(d2, d2Cures).collect(Collectors.toList());
        if (!validationUtils.hasAnyCriteria(d2Criteria, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    "170.315 (b)",
                    d2Criteria.stream().map(criterion -> Util.formatCriteriaNumber(criterion))
                        .collect(Collectors.joining(" or "))));
        }
        List<CertificationCriterion> d3Criteria = Stream.of(d3, d3Cures).collect(Collectors.toList());
        if (!validationUtils.hasAnyCriteria(d3Criteria, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    "170.315 (b)",
                    d3Criteria.stream().map(criterion -> Util.formatCriteriaNumber(criterion))
                        .collect(Collectors.joining(" or "))));
        }
        if (!validationUtils.hasCriterion(d5, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    "170.315 (b)",
                    Util.formatCriteriaNumber(d5)));
        }
        if (!validationUtils.hasCriterion(d6, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    "170.315 (b)",
                    Util.formatCriteriaNumber(d6)));
        }
        if (!validationUtils.hasCriterion(d7, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    "170.315 (b)",
                    Util.formatCriteriaNumber(d7)));
        }
        if (!validationUtils.hasCriterion(d8, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    "170.315 (b)",
                    Util.formatCriteriaNumber(d8)));
        }
    }

    private void checkG10RequiredDependencies(CertifiedProductSearchDetails listing,
            List<CertificationCriterion> attestedCriteria) {
        if (!validationUtils.hasCriterion(g10, attestedCriteria)) {
            return;
        }
        if (!validationUtils.hasCriterion(d1, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    Util.formatCriteriaNumber(g10),
                    Util.formatCriteriaNumber(d1)));
        }
        if (!validationUtils.hasCriterion(d9, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    Util.formatCriteriaNumber(g10),
                    Util.formatCriteriaNumber(d9)));
        }
        if (!validationUtils.hasAnyCriteria(d2Ord10, attestedCriteria)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                    Util.formatCriteriaNumber(g10),
                    d2Ord10.stream().map(criterion -> Util.formatCriteriaNumber(criterion))
                        .collect(Collectors.joining(" or "))));
        }
    }
}
