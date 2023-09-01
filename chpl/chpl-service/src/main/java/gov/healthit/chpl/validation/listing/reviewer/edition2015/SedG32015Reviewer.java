package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("sedG32015Reviewer")
public class SedG32015Reviewer extends PermissionBasedReviewer {
    private CertificationCriterion g3;

    @Autowired
    public SedG32015Reviewer(CertificationCriterionService criteriaService,
            ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        g3 = criteriaService.get(Criteria2015.G_3);
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationResult> presentCriteriaWithSed = listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess())
                        && certResult.isSed() != null && certResult.isSed().equals(Boolean.TRUE)
                        && certResult.getCriterion().getRemoved() != null
                        && certResult.getCriterion().getRemoved().equals(Boolean.FALSE))
                .collect(Collectors.<CertificationResult> toList());

        List<CertificationResult> removedCriteriaWithSed = listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess())
                        && certResult.isSed() != null && certResult.isSed().equals(Boolean.TRUE)
                        && certResult.getCriterion().getRemoved() != null
                        && certResult.getCriterion().getRemoved().equals(Boolean.TRUE))
                .collect(Collectors.<CertificationResult> toList());

        Optional<CertificationResult> g3CertificationResult = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion() != null
                        && certResult.getCriterion().getId().equals(g3.getId())
                        && BooleanUtils.isTrue(certResult.isSuccess()))
                .findFirst();

        // cases where the listing has at least one sed criteria but has not attested to g3
        if (hasPresentSedCriteria(presentCriteriaWithSed)
                && !attestsToG3(g3CertificationResult)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.foundSedCriteriaWithoutAttestingSed"));
        }

        // cases where the listing has attested to g3 but has no sed criteria
        if (!hasRemovedSedCriteria(removedCriteriaWithSed) && !hasPresentSedCriteria(presentCriteriaWithSed)
                && attestsToG3(g3CertificationResult)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.foundNoSedCriteriaButAttestingSed"));
        }
    }

    private boolean hasRemovedSedCriteria(List<CertificationResult> removedCriteriaWithSed) {
        return removedCriteriaWithSed != null && removedCriteriaWithSed.size() > 0;
    }

    private boolean hasPresentSedCriteria(List<CertificationResult> presentCriteriaWithSed) {
        return presentCriteriaWithSed != null && presentCriteriaWithSed.size() > 0;
    }

    private boolean attestsToG3(Optional<CertificationResult> g3CertificationResult) {
        return g3CertificationResult.isPresent();
    }
}
