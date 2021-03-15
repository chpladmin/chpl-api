package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CertificationEditionNormalizer {

    private CertificationEditionDAO editionDao;

    @Autowired
    public CertificationEditionNormalizer(CertificationEditionDAO editionDao) {
        this.editionDao = editionDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationEdition() == null) {
            return;
        }
        Long editionId = MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY);
        String year = MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY);

        if (editionId == null && !StringUtils.isEmpty(year)) {
            CertificationEditionDTO foundEdition = editionDao.getByYear(year);
            if (foundEdition != null) {
                listing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_ID_KEY, foundEdition.getId());
            }
        } else if (editionId != null && StringUtils.isEmpty(year)) {
            CertificationEditionDTO foundEdition = null;
            try {
                foundEdition = editionDao.getById(editionId);
            } catch (Exception ex) {
                LOGGER.warn("No certification edition found with ID " + editionId);
            }
            if (foundEdition != null) {
                listing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, foundEdition.getYear());
            }
        }
    }
}
