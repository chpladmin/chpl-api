package gov.healthit.chpl.upload.listing.normalizer;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class IcsNormalizer {
    private CertifiedProductSearchDAO cpSearchDao;
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public IcsNormalizer(CertifiedProductSearchDAO cpSearchDao,
            ChplProductNumberUtil chplProductNumberUtil) {
        this.cpSearchDao = cpSearchDao;
        this.chplProductNumberUtil = chplProductNumberUtil;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getIcs() != null && listing.getIcs().getParents() != null && listing.getIcs().getParents().size() > 0) {
            listing.getIcs().getParents().stream()
                .forEach(icsParent -> populateParent(icsParent));
        } else if (listing.getIcs() == null || listing.getIcs().getInherits() == null) {
            populateIcsBooleanFromChplProductNumber(listing);
        }
    }

    private void populateParent(CertifiedProduct parent) {
        if (parent == null) {
            return;
        }

        if (parent.getId() == null && !StringUtils.isEmpty(parent.getChplProductNumber())) {
            try {
                CertifiedProduct foundListing = cpSearchDao.getByChplProductNumber(parent.getChplProductNumber());
                if (foundListing != null) {
                    parent.setId(foundListing.getId());
                    parent.setCertificationDate(foundListing.getCertificationDate());
                    parent.setCertificationStatus(foundListing.getCertificationStatus());
                    parent.setCuresUpdate(foundListing.getCuresUpdate());
                    parent.setEdition(foundListing.getEdition());
                    parent.setLastModifiedDate(foundListing.getLastModifiedDate());
                }
            } catch (EntityNotFoundException ex) {
                LOGGER.error("Listing uploaded with invalid ICS source " + parent.getChplProductNumber());
            }
        }
    }

    private void populateIcsBooleanFromChplProductNumber(CertifiedProductSearchDetails listing) {
        if (StringUtils.isEmpty(listing.getChplProductNumber())) {
            return;
        }

        Boolean icsBoolean = null;
        Integer icsCodeFromChplProductNumber = null;
        try {
            icsCodeFromChplProductNumber = chplProductNumberUtil.getIcsCode(listing.getChplProductNumber());
        } catch (Exception ex) {
            LOGGER.error("Could not parse ICS code from " + listing.getChplProductNumber());
        }

        if (icsCodeFromChplProductNumber != null) {
            if (icsCodeFromChplProductNumber.equals(0)) {
                icsBoolean = Boolean.FALSE;
            } else if (icsCodeFromChplProductNumber > 0) {
                icsBoolean = Boolean.TRUE;
            }
        }

        if (icsBoolean != null) {
            if (listing.getIcs() == null) {
                listing.setIcs(InheritedCertificationStatus.builder()
                        .inherits(icsBoolean)
                        .build());
            } else {
                listing.getIcs().setInherits(icsBoolean);
            }
        }
    }
}
