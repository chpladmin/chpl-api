package gov.healthit.chpl.manager.rules.complaints;

import gov.healthit.chpl.domain.complaint.ComplaintListingMap;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ComplaintListingValidation extends ValidationRule<ComplaintValidationContext> {

    @Override
    public boolean getErrorMessages(ComplaintValidationContext context) {
        for (ComplaintListingMap complaintListingMap : context.getComplaint().getListings()) {
            // Make sure the listing exists
            try {
                CertifiedProductDTO listing = context.getCertifiedProductDAO()
                        .getById(complaintListingMap.getListingId());
                if (listing == null) {
                    getMessages().add(context.getErrorMessageUtil().getMessage("complaints.listingId.notFound",
                            complaintListingMap.getListingId()));
                    return false;
                }

                // Make sure the listing's ACB matches the complaint ACB
                if (context.getComplaint().getCertificationBody().getId() != listing.getCertificationBodyId()) {
                    getMessages().add(context.getErrorMessageUtil().getMessage("complaints.listingId.doesNotMatchAcb",
                            context.getChplProductNumberUtil().generate(complaintListingMap.getListingId())));
                    return false;
                }
            } catch (EntityRetrievalException e) {
                getMessages().add(context.getErrorMessageUtil().getMessage("complaints.listingId.notFound",
                        complaintListingMap.getListingId()));
                return false;
            }
        }
        return true;
    }
}
