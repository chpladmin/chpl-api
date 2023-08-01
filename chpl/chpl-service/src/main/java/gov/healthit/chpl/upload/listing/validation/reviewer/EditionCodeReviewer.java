package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("editionCodeReviewer")
@Log4j2
public class EditionCodeReviewer implements Reviewer {
    private static final String[] EDITION_CODES = {
            "15"
    };
    private ChplProductNumberUtil chplProductNumberUtil;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public EditionCodeReviewer(ChplProductNumberUtil chplProductNumberUtil,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        String chplProductNumber = listing.getChplProductNumber();
        if (StringUtils.isEmpty(chplProductNumber)
                || chplProductNumberUtil.isLegacyChplProductNumberStyle(chplProductNumber)
                || !chplProductNumberUtil.isCurrentChplProductNumberStyle(chplProductNumber)) {
            return;
        }

        String editionCode = null;
        try {
            editionCode = chplProductNumberUtil.getCertificationEditionCode(chplProductNumber);
        } catch (Exception ex) {
            LOGGER.warn("Cannot find edition code in " + chplProductNumber);
        }

        if (isValidEditionCode(listing.getChplProductNumber()) && !Arrays.asList(EDITION_CODES).contains(editionCode)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.invalidEditionCode",
                    editionCode, Stream.of(EDITION_CODES).collect(Collectors.joining(","))));
        }

        String editionYear = listing.getEdition() == null ? null : listing.getEdition().getName();
        if (isValidEditionCode(listing.getChplProductNumber()) && !StringUtils.isEmpty(editionYear)
                && !convertEditionCodeToYear(editionCode).equals(editionYear)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.certificationEditionMismatch", editionCode, editionYear));
        }
    }

    private String convertEditionCodeToYear(String editionCode) {
        String year = editionCode;
        if (editionCode.length() == 2) {
            year = "20" + editionCode;
        }
        return year;
    }

    private boolean isValidEditionCode(String chplProductNumber) {
        return validationUtils.chplNumberPartIsValid(chplProductNumber,
                ChplProductNumberUtil.EDITION_CODE_INDEX,
                ChplProductNumberUtil.EDITION_CODE_REGEX);
    }
}
