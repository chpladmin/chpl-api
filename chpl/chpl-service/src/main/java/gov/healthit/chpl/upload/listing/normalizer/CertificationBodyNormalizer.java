package gov.healthit.chpl.upload.listing.normalizer;

import java.util.HashMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
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
        if (doesListingHaveAcbName(listing)) {
            updateListingFromAcbName(listing);
        } else if (doesListingHaveAcbCode(listing)) {
            updateListingFromAcbCode(listing);
        } else if (doesListingHaveAcbId(listing)) {
            updateListingFromAcbId(listing);
        } else if (isAcbPortionOfChplProductNumberValid(listing)) {
            updateAcbFromChplProductNumber(listing);
        }
    }

    private boolean doesListingHaveAcbName(CertifiedProductSearchDetails listing) {
        Long acbId = MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY);
        String acbName = MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY);
        return listing.getCertifyingBody() != null && acbId == null && !StringUtils.isEmpty(acbName);
    }

    private boolean doesListingHaveAcbCode(CertifiedProductSearchDetails listing) {
        Long acbId = MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY);
        String acbCode = MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY);
        return listing.getCertifyingBody() != null && acbId == null && !StringUtils.isEmpty(acbCode);
    }

    private boolean doesListingHaveAcbId(CertifiedProductSearchDetails listing) {
        Long acbId = MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY);
        String acbName = MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY);
        String acbCode = MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY);
        return listing.getCertifyingBody() != null && acbId != null
                && (StringUtils.isEmpty(acbCode) || StringUtils.isEmpty(acbName));
    }

    private void updateListingFromAcbName(CertifiedProductSearchDetails listing) {
        String acbName = MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY);
        CertificationBody foundAcb = acbDao.getByName(acbName);
        updateListingAcbFromFoundAcb(listing, foundAcb);
    }

    private void updateListingFromAcbCode(CertifiedProductSearchDetails listing) {
        String acbCode = MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_CODE_KEY);
        CertificationBody foundAcb = acbDao.getByCode(acbCode);
        updateListingAcbFromFoundAcb(listing, foundAcb);
    }

    private void updateListingFromAcbId(CertifiedProductSearchDetails listing) {
        Long acbId = MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY);
        CertificationBody foundAcb = null;
        try {
            foundAcb = acbDao.getById(acbId);
        } catch (EntityRetrievalException ex) {
            LOGGER.warn("No ACB found with ID " + acbId);
        }
        updateListingAcbFromFoundAcb(listing, foundAcb);
    }

    private boolean isAcbPortionOfChplProductNumberValid(CertifiedProductSearchDetails listing) {
        return !StringUtils.isEmpty(listing.getChplProductNumber())
                && validationUtils.chplNumberPartIsPresentAndValid(listing.getChplProductNumber(),
                    ChplProductNumberUtil.ACB_CODE_INDEX,
                    ChplProductNumberUtil.ACB_CODE_REGEX);
    }

    private void updateAcbFromChplProductNumber(CertifiedProductSearchDetails listing) {
        String acbCodeFromChplProductNumber = chplProductNumberUtil.getAcbCode(listing.getChplProductNumber());
        CertificationBody foundAcb = acbDao.getByCode(acbCodeFromChplProductNumber);
        if (foundAcb != null) {
            listing.setCertifyingBody(new HashMap<String, Object>());
            updateListingAcbFromFoundAcb(listing, foundAcb);
        }
    }

    private void updateListingAcbFromFoundAcb(CertifiedProductSearchDetails listing, CertificationBody foundAcb) {
        if (foundAcb != null) {
            if (listing.getCertifyingBody() == null) {
                listing.setCertifyingBody(new HashMap<String, Object>());
            }
            listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_ID_KEY, foundAcb.getId());
            listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_CODE_KEY, foundAcb.getAcbCode());
            listing.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_NAME_KEY, foundAcb.getName());
        }
    }
}
