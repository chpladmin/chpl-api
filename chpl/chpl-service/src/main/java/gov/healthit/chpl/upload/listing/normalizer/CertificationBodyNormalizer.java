package gov.healthit.chpl.upload.listing.normalizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;

@Component
public class CertificationBodyNormalizer {
    private CertificationBodyDAO acbDao;

    @Autowired
    public CertificationBodyNormalizer(CertificationBodyDAO acbDao) {
        this.acbDao = acbDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertifyingBody() != null
                && listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY) == null
                && listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY) != null) {
            String acbName = listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString();
            CertificationBodyDTO foundAcb = acbDao.getByName(acbName);
            if (foundAcb != null) {
                listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_ID_KEY, foundAcb.getId());
            }
        }
    }
}
