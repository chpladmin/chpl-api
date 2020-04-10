package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("listingStatusAndUserRoleReviewer")
public class ListingStatusAndUserRoleReviewer implements ComparisonReviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil messages;

    @Autowired
    public ListingStatusAndUserRoleReviewer(ResourcePermissions resourcePermissions, ErrorMessageUtil messages) {
        this.resourcePermissions = resourcePermissions;
        this.messages = messages;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        if (!resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            if (!isListingCurrentStatusConsideredActive(existingListing)
                    || !isListingCurrentStatusConsideredActive(updatedListing)) {
                if (haveCriteriaBeenAdded(existingListing, updatedListing)
                        || haveCriteriaBeenRemoved(existingListing, updatedListing)) {
                    updatedListing.getErrorMessages().add(messages.getMessage("listing.criteria.userCannotAddOrRemove"));
                }
            }
        }
    }

    private boolean isListingCurrentStatusConsideredActive(CertifiedProductSearchDetails listing) {
        List<CertificationStatusType> activeStatuses = Arrays.asList(CertificationStatusType.Active,
                CertificationStatusType.SuspendedByAcb, CertificationStatusType.SuspendedByOnc);

        return activeStatuses.stream()
                .anyMatch(status -> status.getName().equals(listing.getCurrentStatus().getStatus().getName()));
    }

    private boolean haveCriteriaBeenRemoved(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        List<CertificationResult> origCriteria = getAttestedToCriteria(existingListing);
        List<CertificationResult> updatedCriteria = getAttestedToCriteria(updatedListing);
        return subtractLists(origCriteria, updatedCriteria).size() > 0;
    }

    private boolean haveCriteriaBeenAdded(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        List<CertificationResult> origCriteria = getAttestedToCriteria(existingListing);
        List<CertificationResult> updatedCriteria = getAttestedToCriteria(updatedListing);
        return subtractLists(updatedCriteria, origCriteria).size() > 0;
    }

    private List<CertificationResult> subtractLists(List<CertificationResult> listA, List<CertificationResult> listB) {

        Predicate<CertificationResult> notInListB = crtfFromA -> !listB.stream()
                .anyMatch(crtf -> doCriterionMatch(crtfFromA, crtf));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private List<CertificationResult> getAttestedToCriteria(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.isSuccess())
                .collect(Collectors.toList());
    }

    private boolean doCriterionMatch(CertificationResult a, CertificationResult b) {
        return a.getCriterion().getId().equals(b.getCriterion().getId());
    }
}
