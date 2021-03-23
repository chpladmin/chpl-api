package gov.healthit.chpl.upload.listing.normalizer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CertificationBodyNormalizer {
    private CertificationBodyDAO acbDao;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ValidationUtils validationUtils;

    @Autowired
    public CertificationBodyNormalizer(CertificationBodyDAO acbDao, ChplProductNumberUtil chplProductNumberUtil,
            ValidationUtils validationUtils) {
        this.acbDao = acbDao;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.validationUtils = validationUtils;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        Long acbId = MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY);
        String acbName = MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY);
        String acbCode = MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY);

        if (listing.getCertifyingBody() != null && acbId == null && !StringUtils.isEmpty(acbName)) {
            CertificationBodyDTO foundAcb = acbDao.getByName(acbName);
            if (foundAcb != null) {
                listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_ID_KEY, foundAcb.getId());
                listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_CODE_KEY, foundAcb.getAcbCode());
            }
        } else if (listing.getCertifyingBody() != null && acbId == null && !StringUtils.isEmpty(acbCode)) {
            CertificationBodyDTO foundAcb = acbDao.getByCode(acbCode);
            if (foundAcb != null) {
                listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_ID_KEY, foundAcb.getId());
                listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_NAME_KEY, foundAcb.getName());
            }
        } else if (listing.getCertifyingBody() != null && acbId != null
                && (StringUtils.isEmpty(acbCode) || StringUtils.isEmpty(acbName))) {
            CertificationBodyDTO foundAcb = null;
            try {
                foundAcb = acbDao.getById(acbId);
            } catch (EntityRetrievalException ex) {
                LOGGER.warn("No ACB found with ID " + acbId);
            }
            if (foundAcb != null) {
                listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_CODE_KEY, foundAcb.getAcbCode());
                listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_NAME_KEY, foundAcb.getName());
            }
        } else if (!StringUtils.isEmpty(listing.getChplProductNumber())) {
            if (validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                            ChplProductNumberUtil.ACB_CODE_INDEX,
                            ChplProductNumberUtil.ACB_CODE_REGEX)) {
                String acbCodeFromChplProductNumber = chplProductNumberUtil.getAcbCode(listing.getChplProductNumber());
                CertificationBodyDTO foundAcb = acbDao.getByCode(acbCodeFromChplProductNumber);
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
