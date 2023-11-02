package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("testingLabReviewer")
public class TestingLabReviewer implements Reviewer {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestingLabReviewer(ChplProductNumberUtil chplProductNumberUtil,
            ErrorMessageUtil msgUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        String chplProductNumber = listing.getChplProductNumber();
        if (chplProductNumberUtil.isLegacyChplProductNumberStyle(chplProductNumber)) {
            // testing labs are not required for legacy listings
            return;
        } else {
            List<CertifiedProductTestingLab> atls = listing.getTestingLabs();
            if (atls == null || atls.size() == 0) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.missingTestingLab"));
                return;
            }
        }

        listing.getTestingLabs().stream()
                .forEach(atl -> reviewValidTestingLab(listing, atl));
    }

    private void reviewValidTestingLab(CertifiedProductSearchDetails listing, CertifiedProductTestingLab atl) {
        if (StringUtils.isEmpty(atl.getTestingLab().getName())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.missingTestingLabName"));
        }

        if (atl.getTestingLab().getId() == null && !StringUtils.isEmpty(atl.getTestingLab().getName())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.invalidTestingLab", atl.getTestingLab().getName()));
        }
    }
}
