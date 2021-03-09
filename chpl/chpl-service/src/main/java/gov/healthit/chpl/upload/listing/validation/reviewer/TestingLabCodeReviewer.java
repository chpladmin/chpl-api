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
import lombok.extern.log4j.Log4j2;

@Component("testingLabCodeReviewer")
@Log4j2
public class TestingLabCodeReviewer  {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestingLabCodeReviewer(ChplProductNumberUtil chplProductNumberUtil,
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

        String atlCode = null;
        try {
            atlCode = chplProductNumberUtil.getAtlCode(chplProductNumber);
        } catch (Exception ex) {
            LOGGER.warn("Cannot find ATL code in " + chplProductNumber);
        }

        List<CertifiedProductTestingLab> testingLabs = listing.getTestingLabs();
        if (testingLabs != null && testingLabs.size() > 0) {
            if (!StringUtils.isEmpty(atlCode) && testingLabs.size() > 1
                    && !atlCode.equals(TestingLab.MULTIPLE_TESTING_LABS_CODE)) {
                listing.getWarningMessages().add(msgUtil.getMessage("atl.shouldBe99"));
                //TODO: should it actually do the code fix in here?
            } else if (!StringUtils.isEmpty(atlCode) && testingLabs.size() == 1
                    && atlCode.equals(TestingLab.MULTIPLE_TESTING_LABS_CODE)) {
                listing.getErrorMessages().add(msgUtil.getMessage("atl.shouldNotBe99"));
            } else if (!StringUtils.isEmpty(atlCode) && testingLabs.size() == 1) {
                CertifiedProductTestingLab atl = testingLabs.get(0);
                if (!StringUtils.isEmpty(atl.getTestingLabCode())
                        && !atl.getTestingLabCode().equals(atlCode)) {
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.testingLabMismatch", atlCode, atl.getTestingLabCode()));
                } else if (StringUtils.isEmpty(atl.getTestingLabCode())) {
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.missingTestingLabCode"));
                }
            }
        }
    }
}
