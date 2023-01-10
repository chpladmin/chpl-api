package gov.healthit.chpl.upload.listing.normalizer;

import java.util.HashMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CertificationEditionNormalizer {
    private CertificationEditionDAO editionDao;
    private ValidationUtils validationUtils;
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public CertificationEditionNormalizer(CertificationEditionDAO editionDao,
            ValidationUtils validationUtils, ChplProductNumberUtil chplProductNumberUtil) {
        this.editionDao = editionDao;
        this.validationUtils = validationUtils;
        this.chplProductNumberUtil = chplProductNumberUtil;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (doesListingHaveEditionYear(listing)) {
            updateListingFromEditionYear(listing);
        } else if (doesListingHaveEditionId(listing)) {
            updateListingFromEditionId(listing);
        } else if (isEditionPortionOfChplProductNumberValid(listing)) {
            updateEditionFromChplProductNumber(listing);
        }
    }

    private boolean doesListingHaveEditionYear(CertifiedProductSearchDetails listing) {
        Long editionId = MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY);
        String year = MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY);
        return editionId == null && !StringUtils.isEmpty(year);
    }

    private boolean doesListingHaveEditionId(CertifiedProductSearchDetails listing) {
        Long editionId = MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY);
        String year = MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY);
        return editionId != null && StringUtils.isEmpty(year);
    }

    private void updateListingFromEditionYear(CertifiedProductSearchDetails listing) {
        String year = MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY);
        CertificationEditionDTO foundEdition = editionDao.getByYear(year);
        populateListingEditionFromDto(listing, foundEdition);
    }

    private void updateListingFromEditionId(CertifiedProductSearchDetails listing) {
        Long editionId = MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY);
        CertificationEditionDTO foundEdition = null;
        try {
            foundEdition = editionDao.getById(editionId);
        } catch (Exception ex) {
            LOGGER.warn("No certification edition found with ID " + editionId);
        }
        populateListingEditionFromDto(listing, foundEdition);
    }

    private boolean isEditionPortionOfChplProductNumberValid(CertifiedProductSearchDetails listing) {
        return !StringUtils.isEmpty(listing.getChplProductNumber())
                && validationUtils.chplNumberPartIsPresentAndValid(listing.getChplProductNumber(),
                    ChplProductNumberUtil.EDITION_CODE_INDEX,
                    ChplProductNumberUtil.EDITION_CODE_REGEX);
    }

    private void updateEditionFromChplProductNumber(CertifiedProductSearchDetails listing) {
        String editionCodeFromChplProductNumber = chplProductNumberUtil.getCertificationEditionCode(listing.getChplProductNumber());
        if (!StringUtils.isEmpty(editionCodeFromChplProductNumber)) {
            String year = "20" + editionCodeFromChplProductNumber;
            CertificationEditionDTO foundEdition = editionDao.getByYear(year);
            populateListingEditionFromDto(listing, foundEdition);
        }
    }

    private void populateListingEditionFromDto(CertifiedProductSearchDetails listing, CertificationEditionDTO editionDto) {
        if (editionDto != null) {
            if (listing.getCertificationEdition() == null) {
                listing.setCertificationEdition(new HashMap<String, Object>());
            }
            listing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_ID_KEY, editionDto.getId());
            listing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, editionDto.getYear());
        }
    }
}
