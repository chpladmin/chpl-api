package gov.healthit.chpl.validation.listing.reviewer.edition2015;

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
 * Makes sure the version is included with a test tool.
 *
 * @author kekey
 *
 */
@Component("testTool2015Reviewer")
public class TestTool2015Reviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestTool2015Reviewer(final ErrorMessageUtil msgUtil) {
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
                            // require test tool version if a test tool name was entered
                            if (StringUtils.isEmpty(testTool.getTestToolVersion())) {
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
