package gov.healthit.chpl.upload.listing.normalizer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.util.ChplProductNumberUtil;

@Component
public class CertificationBodyNormalizer {
    private CertificationBodyDAO acbDao;
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public CertificationBodyNormalizer(CertificationBodyDAO acbDao, ChplProductNumberUtil chplProductNumberUtil) {
        this.acbDao = acbDao;
        this.chplProductNumberUtil = chplProductNumberUtil;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertifyingBody() != null
                && listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY) == null
                && listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY) != null) {
            String acbName = listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString();
            CertificationBodyDTO foundAcb = acbDao.getByName(acbName);
            if (foundAcb != null) {
                listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_ID_KEY, foundAcb.getId());
                listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_CODE_KEY, foundAcb.getAcbCode());
            }
        } else if (!StringUtils.isEmpty(listing.getChplProductNumber())) {
            String acbCode = chplProductNumberUtil.getAcbCode(listing.getChplProductNumber());
            if (!StringUtils.isEmpty(acbCode)) {
                CertificationBodyDTO foundAcb = acbDao.getByCode(acbCode);
                if (foundAcb != null) {
                    Map<String, Object> certifyingBody = new HashMap<String, Object>();
                    certifyingBody.put(CertifiedProductSearchDetails.ACB_ID_KEY, foundAcb.getId());
                    certifyingBody.put(CertifiedProductSearchDetails.ACB_NAME_KEY, foundAcb.getName());
                    certifyingBody.put(CertifiedProductSearchDetails.ACB_CODE_KEY, foundAcb.getAcbCode());
                    listing.setCertifyingBody(certifyingBody);
                }
            }
        }
    }
}
