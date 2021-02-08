package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.QmsStandardDTO;

@Component
public class QmsStandardNormalizer {
    private QmsStandardDAO qmsStandardDao;

    @Autowired
    public QmsStandardNormalizer(QmsStandardDAO qmsStandardDao) {
        this.qmsStandardDao = qmsStandardDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getQmsStandards() != null && listing.getQmsStandards().size() > 0) {
            listing.getQmsStandards().stream()
                .forEach(qmsStandard -> populateQmsStandardId(qmsStandard));
        }
    }

    private void populateQmsStandardId(CertifiedProductQmsStandard qmsStandard) {
        if (!StringUtils.isEmpty(qmsStandard.getQmsStandardName())) {
            QmsStandardDTO qmsStdDto =
                    qmsStandardDao.getByName(qmsStandard.getQmsStandardName());
            if (qmsStdDto != null) {
                qmsStandard.setQmsStandardId(qmsStdDto.getId());
            }
        }
    }
}
