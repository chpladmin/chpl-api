package gov.healthit.chpl.manager.rules.complaints;

import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ComplaintListingMapDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ComplaintListingValidation extends ValidationRule<ComplaintValidationContext> {

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        for (ComplaintListingMapDTO complaintListingMapDTO : context.getComplaintDTO().getListings()) {
            // Make sure the listing exists
            try {
                CertifiedProductDTO listing = context.getCertifiedProductDAO()
                        .getById(complaintListingMapDTO.getListingId());
                if (listing == null) {
                    getMessages().add(context.getErrorMessageUtil().getMessage("complaints.listingId.notFound",
                            complaintListingMapDTO.getListingId()));
                    return false;
                }

                // Make sure the listing's ACB matches the complaint ACB
                if (context.getComplaintDTO().getCertificationBody().getId() != listing.getCertificationBodyId()) {
                    getMessages().add(context.getErrorMessageUtil().getMessage("complaints.listingId.doesNotMatchAcb",
                            context.getChplProductNumberUtil().generate(complaintListingMapDTO.getListingId())));
                    return false;
                }
            } catch (EntityRetrievalException e) {
                getMessages().add(context.getErrorMessageUtil().getMessage("complaints.listingId.notFound",
                        complaintListingMapDTO.getListingId()));
                return false;
            }
        }
        return true;
    }
}
