package gov.healthit.chpl.complaint.rules;

import gov.healthit.chpl.complaint.domain.ComplaintListingMap;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ComplaintListingValidation extends ValidationRule<ComplaintValidationContext> {

    @Override
    public boolean isValid(ComplaintValidationContext context) {
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
