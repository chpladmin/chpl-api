package gov.healthit.chpl.validation.listing.reviewer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("icsReviewer")
public class InheritedCertificationStatusReviewer implements Reviewer {
    private CertifiedProductSearchDAO searchDao;
    private ListingGraphDAO inheritanceDao;
    private CertificationEditionDAO certEditionDao;
    private ChplProductNumberUtil productNumUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public InheritedCertificationStatusReviewer(CertifiedProductSearchDAO searchDao, ListingGraphDAO inheritanceDao,
            CertificationEditionDAO certEditionDao, ChplProductNumberUtil productNumUtil, ErrorMessageUtil msgUtil) {
        this.searchDao = searchDao;
        this.inheritanceDao = inheritanceDao;
        this.certEditionDao = certEditionDao;
        this.productNumUtil = productNumUtil;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getIcs() == null || listing.getIcs().getInherits() == null) {
            return;
        }

        Integer icsCodeInteger = productNumUtil.getIcsCode(listing.getChplProductNumber());
        if (listing.getIcs().getInherits().equals(Boolean.TRUE) && icsCodeInteger.intValue() > 0) {
            // if ICS is nonzero, and no parents are found, give error
            if (listing.getIcs().getParents() == null
                    || listing.getIcs().getParents().size() == 0) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.icsTrueAndNoParentsFound"));
            } else {
                // parents are non-empty - check inheritance rules
                // certification edition must be the same as this listings
                List<Long> parentIds = new ArrayList<Long>();
                for (CertifiedProduct potentialParent : listing.getIcs().getParents()) {
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
                                msgUtil.getMessage("listing.icsUniqueIdNotFound", potentialParent.getChplProductNumber()));
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
                        if (!listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString()
                                .equals(parentEdition.getId().toString())) {
                            listing.getErrorMessages().add(
                                    msgUtil.getMessage("listing.icsEditionMismatch", parentEdition.getYear()));
                        }
                    }

                    // this listing's ICS code must be greater than the max of
                    // parent ICS codes
                    Integer largestIcs = inheritanceDao.getLargestIcs(parentIds);

                    //Findbugs says this cannot be null since it used above - an NPE would have been thrown
                    //if (largestIcs != null && icsCodeInteger != null
                    //        && icsCodeInteger.intValue() != (largestIcs.intValue() + 1)) {
                    if (largestIcs != null && icsCodeInteger.intValue() != (largestIcs.intValue() + 1)) {
                        listing.getErrorMessages().add(
                                   msgUtil.getMessage("listing.icsNotLargestCode", icsCodeInteger, largestIcs));
                    }
                }
            }
        }
    }
}
