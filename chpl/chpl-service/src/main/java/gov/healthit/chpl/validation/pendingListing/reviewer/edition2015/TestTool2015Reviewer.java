package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingTestTool2015Reviewer")
public class TestTool2015Reviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestTool2015Reviewer(final ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(final PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria()) {
                if (cert.getTestTools() != null && cert.getTestTools().size() > 0) {
                    Iterator<PendingCertificationResultTestToolDTO> testToolIter = cert.getTestTools().iterator();
                    while (testToolIter.hasNext()) {
                        PendingCertificationResultTestToolDTO testTool = testToolIter.next();
                        if (!StringUtils.isEmpty(testTool.getName()) && StringUtils.isEmpty(testTool.getVersion())) {
                            listing.getErrorMessages()
                                .add(msgUtil.getMessage("listing.criteria.missingTestToolVersion",
                                        testTool.getName(), cert.getNumber()));
                        }
                    }
                }
            }
        }
    }
}

