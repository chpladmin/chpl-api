package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ListingUrlChangeRequestTypeAndListingInProcessValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        try {
            Long listingId = getListingId(context.getNewChangeRequest());
            CertifiedProductSearchDetails existingListing = context.getCpdManager().getCertifiedProductDetails(listingId);
            List<ChangeRequest> crs = context.getValidationDAOs().getChangeRequestDAO().getByDeveloper(context.getNewChangeRequest().getDeveloper().getId(), true).stream()
                    .filter(cr -> cr.getChangeRequestType().getId().equals(context.getNewChangeRequest().getChangeRequestType().getId()))
                    .filter(cr -> getListingId(cr).equals(existingListing.getId()))
                    .filter(cr -> getInProcessStatuses(context).stream()
                            .anyMatch(status -> cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(status)))
                    .collect(Collectors.toList());
            if (crs.size() > 0) {
                getMessages().add(context.getMsgUtil().getMessage("changeRequest.listingInProcess", existingListing.getChplProductNumber()));
                return false;
            }
        } catch (EntityRetrievalException e) {
            // Not sure what happened here, but we'll assume that another
            // validator catches it
            return true;
        }
        return true;
    }

    private Long getListingId(ChangeRequest crWithRawDetails) {
        if (crWithRawDetails.getDetails() instanceof ChangeRequestListingUrl) {
            return ((ChangeRequestListingUrl) crWithRawDetails.getDetails()).getListing().getId();
        }
        return null;
    }

    private List<Long> getInProcessStatuses(ChangeRequestValidationContext context) {
        return new ArrayList<Long>(Arrays.asList(
                context.getChangeRequestStatusIds().getPendingAcbActionStatus(),
                context.getChangeRequestStatusIds().getPendingDeveloperActionStatus()));
    }

}
