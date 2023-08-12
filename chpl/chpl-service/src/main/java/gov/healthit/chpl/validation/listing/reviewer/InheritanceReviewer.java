package gov.healthit.chpl.validation.listing.reviewer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertifiedProductUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("inheritanceReviewer")
@Log4j2
public class InheritanceReviewer implements Reviewer {
    private CertifiedProductUtil cpUtil;
    private ListingGraphDAO inheritanceDao;
    private CertificationEditionDAO certEditionDao;
    private ChplProductNumberUtil productNumUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public InheritanceReviewer(CertifiedProductUtil cpUtil, ListingGraphDAO inheritanceDao,
            CertificationEditionDAO certEditionDao, ChplProductNumberUtil productNumUtil, ErrorMessageUtil msgUtil) {
        this.cpUtil = cpUtil;
        this.inheritanceDao = inheritanceDao;
        this.certEditionDao = certEditionDao;
        this.productNumUtil = productNumUtil;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getIcs() == null) {
            return;
        }

        if (!inherits(listing) && listing.getIcs().getParents() != null
                && listing.getIcs().getParents().size() > 0) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.icsFalseAndParentsFound"));
        } else if (inherits(listing) && (listing.getIcs().getParents() == null
                || listing.getIcs().getParents().size() == 0)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.icsTrueAndNoParentsFound"));
        }

        if (listing.getIcs().getParents() != null && listing.getIcs().getParents().size() > 0) {
            List<Long> parentListingIds = new ArrayList<Long>();
            for (CertifiedProduct icsParent : listing.getIcs().getParents()) {
                lookupListingId(icsParent);

                // if the ID is still null after trying to look it up, that's a problem
                if (icsParent.getId() == null) {
                    listing.addDataErrorMessage(msgUtil.getMessage("listing.icsUniqueIdNotFound", icsParent.getChplProductNumber()));
                } else if (icsParent.getId().equals(listing.getId())) {
                    listing.addDataErrorMessage(msgUtil.getMessage("listing.icsSelfInheritance"));
                } else {
                    parentListingIds.add(icsParent.getId());
                }
            }

            if (parentListingIds != null && parentListingIds.size() > 0) {
                reviewListingParentsHaveSameEditionAsListing(parentListingIds, listing);
                reviewListingIcsCodeIsOneGreaterThanParents(parentListingIds, listing);
            }
        }
    }

    private boolean inherits(CertifiedProductSearchDetails listing) {
        return listing.getIcs() != null && listing.getIcs().getInherits() != null
                && listing.getIcs().getInherits();
    }

    private void lookupListingId(CertifiedProduct certifiedProduct) {
        if (certifiedProduct.getId() == null) {
            try {
                CertifiedProduct listing = cpUtil.getListing(certifiedProduct.getChplProductNumber());
                if (listing != null) {
                    certifiedProduct.setId(listing.getId());
                }
            } catch (Exception ex) {
                LOGGER.catching(ex);
            }
        }
    }

    private void reviewListingParentsHaveSameEditionAsListing(List<Long> parentIds, CertifiedProductSearchDetails listing) {
        List<CertificationEdition> parentEditions = certEditionDao.getEditions(parentIds);
        parentEditions.stream()
                .filter(parentEdition -> listing.getEdition() != null && !listing.getEdition().getId().equals(parentEdition.getId()))
                .forEach(parentEdition -> listing.addBusinessErrorMessage(
                        msgUtil.getMessage("listing.icsEditionMismatch", parentEdition.getName())));
    }

    private void reviewListingIcsCodeIsOneGreaterThanParents(List<Long> parentIds, CertifiedProductSearchDetails listing) {
        Integer largestIcs = inheritanceDao.getLargestIcs(parentIds);
        Integer icsCodeInteger = null;
        try {
            icsCodeInteger = productNumUtil.getIcsCode(listing.getChplProductNumber());
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }

        if (largestIcs != null && icsCodeInteger != null
                && icsCodeInteger.intValue() != (largestIcs.intValue() + 1)) {
            listing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.icsNotLargestCode", icsCodeInteger, largestIcs));
        }
    }
}
