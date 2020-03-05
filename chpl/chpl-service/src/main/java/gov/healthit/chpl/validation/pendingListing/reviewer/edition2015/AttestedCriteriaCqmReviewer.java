package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCqmCertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCqmCriterionDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingAttestedCriteriaCqmReviewer")
public class AttestedCriteriaCqmReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;
    private CertificationCriterionDAO criteriaDao;

    private static final String[] CRITERIA_REQUIRING_CQM = {
            "170.315 (c)(1)", "170.315 (c)(2)", "170.315 (c)(3)", "170.315 (c)(4)"
    };

    @Autowired
    public AttestedCriteriaCqmReviewer(CertificationCriterionDAO criteriaDao, ErrorMessageUtil msgUtil) {
        this.criteriaDao = criteriaDao;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        List<CertificationCriterion> cqmEligibleCritera = new ArrayList<CertificationCriterion>();
        for (String criterionNumber : CRITERIA_REQUIRING_CQM) {
            List<CertificationCriterionDTO> criteriaDtos = criteriaDao.getAllByNumber(criterionNumber);
            cqmEligibleCritera.addAll(criteriaDtos.stream()
                    .map(dto -> new CertificationCriterion(dto))
                    .collect(Collectors.<CertificationCriterion>toList()));
        }

        //any attested criteria that is eligible for CQM must have a CQM that references it
        List<CertificationCriterion> attestedCriteria = ValidationUtils.getAttestedCriteria(listing);
        for (CertificationCriterion criterion : attestedCriteria) {
            if (isCriteriaEligibleForCqm(criterion, cqmEligibleCritera)) {
                //is there a cqm with this criterion?
                if (!isCriterionIncludedInCqms(criterion, listing.getCqmCriterion())) {
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingCqmForCriteria",
                            criterion.getNumber()));
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

    private boolean isCriterionIncludedInCqms(CertificationCriterion criterion, List<PendingCqmCriterionDTO> cqms) {
        Optional<PendingCqmCriterionDTO> foundCqm = cqms.stream()
            .filter(cqm -> isCriterionIncludedInCqmCerts(criterion, cqm.getCertifications()))
            .findFirst();
        return foundCqm.isPresent();
    }

    private boolean isCriterionIncludedInCqmCerts(CertificationCriterion criterion,
            List<PendingCqmCertificationCriterionDTO> cqmCerts) {
        Optional<PendingCqmCertificationCriterionDTO> foundCqmCert = cqmCerts.stream()
                .filter(cqmResult -> cqmResult.getCertificationCriteriaNumber().equals(criterion.getNumber()))
                .findFirst();
        return foundCqmCert.isPresent();
    }
}
