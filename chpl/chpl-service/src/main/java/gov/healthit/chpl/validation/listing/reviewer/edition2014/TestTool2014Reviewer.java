package gov.healthit.chpl.validation.listing.reviewer.edition2014;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

/**
 * Makes sure the version is included with a test tool if ICS = false, otherwise a version is not required.
 *
 * @author kekey
 *
 */
@Component("testTool2014Reviewer")
public class TestTool2014Reviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestTool2014Reviewer(final ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(final CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess()) {
                if (cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                    Iterator<CertificationResultTestTool> testToolIter = cert.getTestToolsUsed().iterator();
                    while (testToolIter.hasNext()) {
                        CertificationResultTestTool testTool = testToolIter.next();
                        if (!StringUtils.isEmpty(testTool.getTestToolName())) {
                            // require test tool version if not ICS
                            if (StringUtils.isEmpty(testTool.getTestToolVersion())) {
                                if (listing.getIcs() != null && listing.getIcs().getInherits() != null
                                        && listing.getIcs().getInherits().booleanValue()) {
                                    listing.getWarningMessages()
                                    .add(msgUtil.getMessage("listing.criteria.missingTestToolVersion",
                                            testTool.getTestToolName(), cert.getNumber()));
                                } else {
                                    listing.getErrorMessages()
                                    .add(msgUtil.getMessage("listing.criteria.missingTestToolVersion",
                                            testTool.getTestToolName(), cert.getNumber()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
