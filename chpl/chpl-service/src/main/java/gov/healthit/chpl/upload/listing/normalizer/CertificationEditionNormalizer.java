package gov.healthit.chpl.upload.listing.normalizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationEditionDTO;

@Component
public class CertificationEditionNormalizer {

    private CertificationEditionDAO editionDao;

    @Autowired
    public CertificationEditionNormalizer(CertificationEditionDAO editionDao) {
        this.editionDao = editionDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationEdition() != null
                && listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY) == null
                && listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY) != null) {
            String year = listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString();
            CertificationEditionDTO foundEdition = editionDao.getByYear(year);
            if (foundEdition != null) {
                listing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_ID_KEY, foundEdition.getId());
            }
        }
    }
}
