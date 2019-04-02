package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Check that a listing has a valid ICS family tree.
 * @author kekey
 *
 */
@Component("pendingIcsReviewer")
public class InheritedCertificationStatusReviewer implements Reviewer {
    @Autowired private CertifiedProductSearchDAO searchDao;
    @Autowired private ListingGraphDAO inheritanceDao;
    @Autowired private CertificationEditionDAO certEditionDao;
    @Autowired private ChplProductNumberUtil productNumUtil;
    @Autowired private ErrorMessageUtil msgUtil;

    public void review(PendingCertifiedProductDTO listing) {
        Integer icsCodeInteger = productNumUtil.getIcsCode(listing.getUniqueId());
        if (listing.getIcs() != null) {
            if (listing.getIcs().booleanValue()
                    && (listing.getIcsParents() == null || listing.getIcsParents().size() == 0)) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.icsTrueAndNoParentsFound"));
            } else if (!listing.getIcs().booleanValue() && listing.getIcsParents() != null
                    && listing.getIcsParents().size() > 0) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.icsFalseAndParentsFound"));
            }
        } else {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingIcs"));
        }

        //if parents exist make sure they are valid
        if (listing.getIcsParents() != null && listing.getIcsParents().size() > 0) {
            // parents are non-empty - check inheritance rules
            // certification edition must be the same as this listings
            List<Long> parentIds = new ArrayList<Long>();
            for (CertifiedProductDetailsDTO potentialParent : listing.getIcsParents()) {
                //the id might be null if the user changed it in the UI
                //even though it's a valid CHPL product number
                if (potentialParent.getId() == null) {
                    try {
                        CertifiedProduct found = searchDao.getByChplProductNumber(potentialParent.getChplProductNumber());
                        if (found != null) {
                            potentialParent.setId(found.getId());
                        }
                    } catch (Exception ignore) { }
                }

                //if the ID is still null after trying to look it up, that's a problem
                if (potentialParent.getId() == null) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.icsUniqueIdNotFound",
                                    potentialParent.getChplProductNumber()));
                } else if (potentialParent.getId().toString().equals(listing.getId().toString())) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.icsSelfInheritance"));
                } else {
                    parentIds.add(potentialParent.getId());
                }
            }

            if (parentIds != null && parentIds.size() > 0) {
                List<CertificationEditionDTO> parentEditions = certEditionDao.getEditions(parentIds);
                for (CertificationEditionDTO parentEdition : parentEditions) {
                    if (!listing.getCertificationEdition().equals(parentEdition.getYear())) {
                        listing.getErrorMessages().add(
                                msgUtil.getMessage("listing.icsEditionMismatch", parentEdition.getYear()));
                    }
                }

                // this listing's ICS code must be greater than the max of
                // parent ICS codes
                Integer largestIcs = inheritanceDao.getLargestIcs(parentIds);
                if (largestIcs != null && icsCodeInteger != null
                        && icsCodeInteger.intValue() != (largestIcs.intValue() + 1)) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.icsNotLargestCode", icsCodeInteger, largestIcs));
                }
            }
        }
    }
}
