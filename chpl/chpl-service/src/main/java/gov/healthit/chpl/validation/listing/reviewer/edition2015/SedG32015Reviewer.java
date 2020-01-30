package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("sedG32015Reviewer")
public class SedG32015Reviewer extends PermissionBasedReviewer {
    private static final String G3_2015 = "170.315 (g)(3)";

    @Autowired
    public SedG32015Reviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationResult> existingCriteriaWithSed = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.isSuccess() != null && certResult.isSuccess().equals(Boolean.TRUE)
                        && certResult.isSed() != null && certResult.isSed().equals(Boolean.TRUE)
                        && (certResult.getCriterion().getRemoved() == null
                        && certResult.getCriterion().getRemoved().equals(Boolean.FALSE)))
                .collect(Collectors.<CertificationResult>toList());

        List<CertificationResult> removedCriteriaWithSed = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.isSuccess() != null && certResult.isSuccess().equals(Boolean.TRUE)
                        && certResult.isSed() != null && certResult.isSed().equals(Boolean.TRUE)
                        && certResult.getCriterion().getRemoved() != null
                        && certResult.getCriterion().getRemoved().equals(Boolean.TRUE))
                .collect(Collectors.<CertificationResult>toList());

        Optional<CertificationResult> g3CertificationResult = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion() != null
                    && certResult.getCriterion().getNumber().equals(G3_2015)
                    && certResult.isSuccess() != null && certResult.isSuccess().equals(Boolean.TRUE))
                .findFirst();


        //cases where the listing has at least one sed criteria but has not attested to g3
        if (!hasRemovedSedCriteria(removedCriteriaWithSed) && hasExistingSedCriteria(existingCriteriaWithSed)
                && !attestsToG3(g3CertificationResult)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.foundSedCriteriaWithoutAttestingSed"));
        }
        if (hasRemovedSedCriteria(removedCriteriaWithSed) && !hasExistingSedCriteria(existingCriteriaWithSed)
                && !attestsToG3(g3CertificationResult)) {
            //add warning if onc/admin, acb sees nothing
            addListingWarningByPermission(listing, msgUtil.getMessage("listing.criteria.foundSedCriteriaWithoutAttestingSed"));
        }

        //cases where the listing has attested to g3 but has no sed criteria
        if (!hasRemovedSedCriteria(removedCriteriaWithSed) && !hasExistingSedCriteria(existingCriteriaWithSed)
                && attestsToG3(g3CertificationResult)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.foundNoSedCriteriaButAttestingSed"));
        }
        if (hasRemovedSedCriteria(removedCriteriaWithSed) && !hasExistingSedCriteria(existingCriteriaWithSed)
                && attestsToG3(g3CertificationResult)) {
            //add warning if onc/admin, acb sees nothing
            addListingWarningByPermission(listing, msgUtil.getMessage("listing.criteria.foundNoSedCriteriaButAttestingSed"));
        }
    }

    private boolean hasRemovedSedCriteria(List<CertificationResult> removedCriteriaWithSed) {
        return removedCriteriaWithSed != null && removedCriteriaWithSed.size() > 0;
    }

    private boolean hasExistingSedCriteria(List<CertificationResult> existingCriteriaWithSed) {
        return existingCriteriaWithSed != null && existingCriteriaWithSed.size() > 0;
    }

    private boolean attestsToG3(Optional<CertificationResult> g3CertificationResult) {
        return g3CertificationResult.isPresent();
    }
}
