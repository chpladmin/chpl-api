package gov.healthit.chpl.manager.rules.complaints;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import gov.healthit.chpl.domain.complaint.ComplaintListingMap;
import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ComplaintSurveillanceValidation extends ValidationRule<ComplaintValidationContext> {

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        Set<ComplaintSurveillanceMap> complaintSurveillanceMaps = context.getComplaint().getSurveillances();
        if (CollectionUtils.isEmpty(complaintSurveillanceMaps)) {
            return true;
        }

        int currErrorMessagesSize = getMessages().size();
        complaintSurveillanceMaps.stream()
            .forEach(surv -> validateSurveillanceHasAssociatedListing(surv.getSurveillance(), context));

        return getMessages().size() == currErrorMessagesSize;
    }

    private void validateSurveillanceHasAssociatedListing(SurveillanceBasic surv, ComplaintValidationContext context) {
        if (CollectionUtils.isEmpty(context.getComplaint().getListings())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("complaints.surveillance.noListingMatch", surv.getFriendlyId(), surv.getChplProductNumber()));
        }

        Optional<ComplaintListingMap> listingForSurveillance = context.getComplaint().getListings().stream()
                .filter(listing -> listing.getListingId().equals(surv.getCertifiedProductId()))
                .findAny();
        if (listingForSurveillance.isEmpty()) {
            getMessages().add(context.getErrorMessageUtil().getMessage("complaints.surveillance.noListingMatch", surv.getFriendlyId(), surv.getChplProductNumber()));
        }
    }
}
