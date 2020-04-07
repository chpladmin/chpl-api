package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("attestedCriteriaCqmReviewer")
public class AttestedCriteriaCqmReviewer implements Reviewer {
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private CertificationCriterionDAO criteriaDao;

    private static final String[] CRITERIA_REQUIRING_CQM = {
            "170.315 (c)(1)", "170.315 (c)(2)", "170.315 (c)(3)", "170.315 (c)(4)"
    };

    @Autowired
    public AttestedCriteriaCqmReviewer(ValidationUtils validationUtils, CertificationCriterionDAO criteriaDao,
            ErrorMessageUtil msgUtil) {
        this.validationUtils = validationUtils;
        this.criteriaDao = criteriaDao;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> cqmEligibleCritera = new ArrayList<CertificationCriterion>();
        for (String criterionNumber : CRITERIA_REQUIRING_CQM) {
            List<CertificationCriterionDTO> criteriaDtos = criteriaDao.getAllByNumber(criterionNumber);
            cqmEligibleCritera.addAll(criteriaDtos.stream()
                    .map(dto -> new CertificationCriterion(dto))
                    .collect(Collectors.<CertificationCriterion>toList()));
        }

        //any attested criteria that is eligible for CQM must have a CQM that references it
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        for (CertificationCriterion criterion : attestedCriteria) {
            if (isCriteriaEligibleForCqm(criterion, cqmEligibleCritera)) {
                //is there a cqm with this criterion?
                if (!isCriterionIncludedInCqms(criterion, listing.getCqmResults())) {
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingCqmForCriteria",
                            Util.formatCriteriaNumber(criterion)));
                }
            }
        }
    }

    private boolean isCriteriaEligibleForCqm(CertificationCriterion criterion, List<CertificationCriterion> eligibleCqmCriteria) {
        Optional<CertificationCriterion> foundCriterion = eligibleCqmCriteria.stream()
            .filter(eligibleCqmCriterion -> eligibleCqmCriterion.getId().equals(criterion.getId()))
            .findFirst();
        return foundCriterion.isPresent();
    }

    private boolean isCriterionIncludedInCqms(CertificationCriterion criterion, List<CQMResultDetails> cqms) {
        Optional<CQMResultDetails> foundCqm = cqms.stream()
            .filter(cqm -> isCriterionIncludedInCqmCerts(criterion, cqm.getCriteria()))
            .findFirst();
        return foundCqm.isPresent();
    }

    private boolean isCriterionIncludedInCqmCerts(CertificationCriterion criterion,
            List<CQMResultCertification> cqmCerts) {
        Optional<CQMResultCertification> foundCqmCert = cqmCerts.stream()
                .filter(cqmResult -> cqmResult.getCertificationNumber().equals(criterion.getNumber()))
                .findFirst();
        return foundCqmCert.isPresent();
    }
}
