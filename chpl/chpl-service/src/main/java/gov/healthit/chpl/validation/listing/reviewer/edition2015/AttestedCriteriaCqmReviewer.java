package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("attestedCriteriaCqmReviewer")
public class AttestedCriteriaCqmReviewer implements Reviewer {
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private List<CertificationCriterion> cqmEligibleCriteria;

    @Autowired
    public AttestedCriteriaCqmReviewer(ValidationUtils validationUtils,
            CertificationCriterionService criteriaService,
            ErrorMessageUtil msgUtil,
            @Value("${cqmEligibleCriteria}") String cqmCriteriaIdList) {
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;

        cqmEligibleCriteria = Arrays.asList(cqmCriteriaIdList.split(",")).stream()
                .map(id -> criteriaService.get(Long.parseLong(id)))
                .filter(criterion -> BooleanUtils.isFalse(criterion.isRemoved()))
                .collect(Collectors.toList());
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        // any attested criteria that is eligible for CQM must have a CQM that references it
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        attestedCriteria.stream()
                .filter(attestedCriterion -> isCriterionEligibleForCqm(attestedCriterion))
                .filter(attestedCqmEligibleCriterion -> !isCriterionInAnyCqm(attestedCqmEligibleCriterion, listing.getCqmResults()))
                .forEach(attestedCqmEligibleCriterionNotInCqms -> addError(listing, attestedCqmEligibleCriterionNotInCqms));
    }

    private boolean isCriterionEligibleForCqm(CertificationCriterion criterion) {
        Optional<CertificationCriterion> foundCriterion = cqmEligibleCriteria.stream()
                .filter(eligibleCqmCriterion -> eligibleCqmCriterion.getId().equals(criterion.getId()))
                .findFirst();
        return foundCriterion.isPresent();
    }

    private boolean isCriterionInAnyCqm(CertificationCriterion criterion, List<CQMResultDetails> cqms) {
        Optional<CQMResultDetails> foundCqm = cqms.stream()
                .filter(cqm -> BooleanUtils.isTrue(cqm.getSuccess()))
                .filter(cqm -> isCriterionInCqmCriteria(criterion, cqm.getCriteria()))
                .findFirst();
        return foundCqm.isPresent();
    }

    private boolean isCriterionInCqmCriteria(CertificationCriterion criterion, List<CQMResultCertification> cqmCerts) {
        Optional<CQMResultCertification> foundCqmCert = cqmCerts.stream()
                .filter(cqmResult -> cqmResult.getCertificationId().equals(criterion.getId()))
                .findFirst();
        return foundCqmCert.isPresent();
    }

    private void addError(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.missingCqmForCriteria",
                Util.formatCriteriaNumber(criterion)));
    }
}
