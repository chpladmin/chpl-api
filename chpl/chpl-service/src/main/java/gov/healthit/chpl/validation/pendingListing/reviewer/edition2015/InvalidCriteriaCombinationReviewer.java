package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingInvalidCriteriaCombinationReviewer")
public class InvalidCriteriaCombinationReviewer implements Reviewer {

    private static final String CURES_UPDATE_IN_TITLE = "(Cures Update)";

    @Value("${criterion.170_315_b_6}")
    private Integer criteriaB6Id;

    @Value("${criterion.170_315_b_10}")
    private Integer criteriaB10Id;

    @Value("${criterion.170_315_g_8}")
    private Integer criteriaG8Id;

    @Value("${criterion.170_315_g_10}")
    private Integer criteriaG10Id;

    @Value("${criterion.170_315_b_1}")
    private Integer criteriaB1Id;
    @Value("${criterion.170_315_b_1_revised}")
    private Integer criteriaB1RevisedId;

    @Value("${criterion.170_315_b_2}")
    private Integer criteriaB2Id;
    @Value("${criterion.170_315_b_2_revised}")
    private Integer criteriaB2RevisedId;

    @Value("${criterion.170_315_b_3}")
    private Integer criteriaB3Id;
    @Value("${criterion.170_315_b_3_revised}")
    private Integer criteriaB3RevisedId;

    @Value("${criterion.170_315_b_7}")
    private Integer criteriaB7Id;
    @Value("${criterion.170_315_b_7_revised}")
    private Integer criteriaB7RevisedId;

    @Value("${criterion.170_315_b_8}")
    private Integer criteriaB8Id;
    @Value("${criterion.170_315_b_8_revised}")
    private Integer criteriaB8RevisedId;

    @Value("${criterion.170_315_b_9}")
    private Integer criteriaB9Id;
    @Value("${criterion.170_315_b_9_revised}")
    private Integer criteriaB9RevisedId;

    @Value("${criterion.170_315_c_3}")
    private Integer criteriaC3Id;
    @Value("${criterion.170_315_c_3_revised}")
    private Integer criteriaC3RevisedId;

    @Value("${criterion.170_315_e_1}")
    private Integer criteriaE1Id;
    @Value("${criterion.170_315_e_1_revised}")
    private Integer criteriaE1RevisedId;

    @Value("${criterion.170_315_f_5}")
    private Integer criteriaF5Id;
    @Value("${criterion.170_315_f_5_revised}")
    private Integer criteriaF5RevisedId;

    @Value("${criterion.170_315_g_6}")
    private Integer criteriaG6Id;
    @Value("${criterion.170_315_g_6_revised}")
    private Integer criteriaG6RevisedId;

    @Value("${criterion.170_315_g_9}")
    private Integer criteriaG9Id;
    @Value("${criterion.170_315_g_9_revised}")
    private Integer criteriaG9RevisedId;

    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    private List<Integer> oldCriteriaIds;
    private List<Integer> newCriteriaIds;

    @Autowired
    public InvalidCriteriaCombinationReviewer(ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            checkForInvalidCriteriaCombination(listing, criteriaB6Id, criteriaB10Id);
            checkForInvalidCriteriaCombination(listing, criteriaG8Id, criteriaG10Id);

            oldCriteriaIds = new ArrayList<Integer>(
                    Arrays.asList(criteriaB1Id, criteriaB2Id, criteriaB3Id, criteriaB7Id, criteriaB8Id, criteriaB9Id,
                            criteriaC3Id, criteriaE1Id, criteriaF5Id, criteriaG6Id, criteriaG9Id));
            newCriteriaIds = new ArrayList<Integer>(
                    Arrays.asList(criteriaB1RevisedId, criteriaB2RevisedId, criteriaB3RevisedId, criteriaB7RevisedId,
                            criteriaB8RevisedId, criteriaB9RevisedId, criteriaC3RevisedId, criteriaE1RevisedId,
                            criteriaF5RevisedId, criteriaG6RevisedId, criteriaG9RevisedId));
            Iterator<Integer> oldCriteriaIdsIter = oldCriteriaIds.iterator();
            Iterator<Integer> newCriteriaIdsIter = newCriteriaIds.iterator();
            while (oldCriteriaIdsIter.hasNext() && newCriteriaIdsIter.hasNext()) {
                checkForInvalidCriteriaCombination(listing, oldCriteriaIdsIter.next(), newCriteriaIdsIter.next());
            }
        }
    }

    private void checkForInvalidCriteriaCombination(PendingCertifiedProductDTO listing, Integer criteriaIdA,
            Integer criteriaIdB) {
        Optional<PendingCertificationResultDTO> certResultA = findCertificationResult(listing, criteriaIdA);
        Optional<PendingCertificationResultDTO> certResultB = findCertificationResult(listing, criteriaIdB);

        if (certResultA.isPresent() && certResultB.isPresent()) {
            final CertificationCriterionDTO critA = certResultA.get().getCriterion();
            final CertificationCriterionDTO critB = certResultB.get().getCriterion();
            listing.getErrorMessages()
                    .add(msgUtil.getMessage("listing.criteria.invalidCombination",
                            critA.getTitle().contains(CURES_UPDATE_IN_TITLE)
                                    ? critA.getNumber() + " " + CURES_UPDATE_IN_TITLE
                                    : critA.getNumber(),
                            critB.getTitle().contains(CURES_UPDATE_IN_TITLE)
                                    ? critB.getNumber() + " " + CURES_UPDATE_IN_TITLE
                                    : critB.getNumber()));
        }
    }

    private Optional<PendingCertificationResultDTO> findCertificationResult(PendingCertifiedProductDTO listing,
            Integer criteriaId) {
        return listing.getCertificationCriterion().stream()
                .filter(cr -> cr.getCriterion().getId().equals(Long.valueOf(criteriaId)) && cr.getMeetsCriteria())
                .findFirst();
    }
}
