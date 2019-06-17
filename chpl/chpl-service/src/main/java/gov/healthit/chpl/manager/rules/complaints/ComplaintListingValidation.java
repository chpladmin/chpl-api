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
                    getMessages()
                            .add("Certified Product {" + complaintListingMapDTO.getListingId() + "} is not valid.");
                    return false;
                }

                // Make sure the listing's ACB matches the complaint ACB
            } catch (EntityRetrievalException e) {
                getMessages().add("Certified Product {" + complaintListingMapDTO.getListingId() + "} is not valid.");
                return false;
            }

        }

    }
}
