package gov.healthit.chpl.upload.listing.normalizer;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class IcsNormalizer {
    private CertifiedProductSearchDAO cpSearchDao;

    @Autowired
    public IcsNormalizer(CertifiedProductSearchDAO cpSearchDao) {
        this.cpSearchDao = cpSearchDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getIcs() != null && listing.getIcs().getParents() != null && listing.getIcs().getParents().size() > 0) {
            listing.getIcs().getParents().stream()
                .forEach(icsParent -> lookupParentId(icsParent));
        }
    }

    private void lookupParentId(CertifiedProduct parent) {
        if (!StringUtils.isEmpty(parent.getChplProductNumber())) {
            try {
                CertifiedProduct foundListing = cpSearchDao.getByChplProductNumber(parent.getChplProductNumber());
                if (foundListing != null) {
                    parent.setId(foundListing.getId());
                }
            } catch (EntityNotFoundException ex) {
                LOGGER.error("Listing uploaded with invalid ICS source " + parent.getChplProductNumber());
            }
        }
    }
}
