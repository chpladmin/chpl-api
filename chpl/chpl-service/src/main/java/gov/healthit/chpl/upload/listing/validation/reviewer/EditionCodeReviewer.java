package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("editionCodeReviewer")
@Log4j2
public class EditionCodeReviewer implements Reviewer {
    private static final String[] EDITION_CODES = {"15"};
    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public EditionCodeReviewer(ChplProductNumberUtil chplProductNumberUtil,
            ErrorMessageUtil msgUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.msgUtil = msgUtil;
    }

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

        if (!StringUtils.isEmpty(editionCode) && !Arrays.asList(EDITION_CODES).contains(editionCode)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.invalidEditionCode",
                    editionCode, Stream.of(EDITION_CODES).collect(Collectors.joining(","))));
        }

        Map<String, Object> certEditionMap = listing.getCertificationEdition();
        if (certEditionMap != null) {
            String editionYear = certEditionMap.get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString();
            if (StringUtils.isNoneEmpty(editionCode, editionYear)
                    && !convertEditionCodeToYear(editionCode).equals(editionYear)) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.certificationEditionMismatch", editionCode, editionYear));
            }
        }
    }

    private String convertEditionCodeToYear(String editionCode) {
        String year = editionCode;
        if (editionCode.length() == 2) {
            year = "20" + editionCode;
        }
        return year;
    }
}
