package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("testingLabCodeReviewer")
@Log4j2
public class TestingLabCodeReviewer implements Reviewer {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestingLabCodeReviewer(ChplProductNumberUtil chplProductNumberUtil,
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

        String atlCode = null;
        try {
            atlCode = chplProductNumberUtil.getAtlCode(chplProductNumber);
        } catch (Exception ex) {
            LOGGER.warn("Cannot find ATL code in " + chplProductNumber);
        }

        List<CertifiedProductTestingLab> testingLabs = listing.getTestingLabs();
        if (testingLabs != null) {
            if (isValidAtlCode(chplProductNumber) && testingLabs.size() > 1
                    && !atlCode.equals(TestingLab.MULTIPLE_TESTING_LABS_CODE)) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.atl.codeIsNotForMultiple"));
                // should it actually do the code fix in here?
                // I think for edit it would have to because the user can't change it otherwise
                // but for newly uploaded listings it does not have to do the fix - the user can re-upload.
            } else if (isValidAtlCode(chplProductNumber) && testingLabs.size() < 2
                    && atlCode.equals(TestingLab.MULTIPLE_TESTING_LABS_CODE)) {
                listing.addDataErrorMessage(msgUtil.getMessage("atl.shouldNotBe99"));
            } else if (isValidAtlCode(chplProductNumber) && testingLabs.size() == 1) {
                CertifiedProductTestingLab atl = testingLabs.get(0);
                if (!StringUtils.isEmpty(atl.getTestingLab().getAtlCode())
                        && !atl.getTestingLab().getAtlCode().equals(atlCode)) {
                    listing.addDataErrorMessage(msgUtil.getMessage("listing.testingLabMismatch", atlCode, atl.getTestingLab().getAtlCode()));
                }
            } else if (testingLabs.size() == 0 && isValidAtlCode(chplProductNumber)) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.invalidTestingLabCode", atlCode));
            }
        } else if (testingLabs == null && isValidAtlCode(chplProductNumber)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.invalidTestingLabCode", atlCode));
        }
    }

    private boolean isValidAtlCode(String chplProductNumber) {
        return validationUtils.chplNumberPartIsValid(chplProductNumber,
                ChplProductNumberUtil.ATL_CODE_INDEX,
                ChplProductNumberUtil.ATL_CODE_REGEX);
    }
}
