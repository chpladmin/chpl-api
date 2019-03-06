package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Makes sure a valid test tool was entered by the user - otherwise removes it and includes an error.
 * Makes sure the version is included with a test tool.
 * Checks that retired test tools are not used if not appropriate.
 * 
 * @author kekey
 *
 */
@Component("testToolReviewer")
public class TestToolReviewer implements Reviewer {
    private TestToolDAO testToolDao;
    private ErrorMessageUtil msgUtil;
    private ChplProductNumberUtil productNumUtil;

    @Autowired
    public TestToolReviewer(TestToolDAO testToolDAO, ErrorMessageUtil msgUtil,
            ChplProductNumberUtil chplProductNumberUtil) {
        this.testToolDao = testToolDAO;
        this.msgUtil = msgUtil;
        this.productNumUtil = chplProductNumberUtil;
    }

    @Override
    public void review(final CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess()) {
                if (cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                    Iterator<CertificationResultTestTool> testToolIter = cert.getTestToolsUsed().iterator();
                    while (testToolIter.hasNext()) {
                        CertificationResultTestTool testTool = testToolIter.next();
                        if (StringUtils.isEmpty(testTool.getTestToolName())) {
                            listing.getErrorMessages()
                                    .add(msgUtil.getMessage("listing.criteria.missingTestToolName", cert.getNumber()));
                        } else {
                            // require test tool version if there is a name
                            if (StringUtils.isEmpty(testTool.getTestToolVersion())) {
                                listing.getErrorMessages()
                                        .add(msgUtil.getMessage("listing.criteria.missingTestToolVersion",
                                                testTool.getTestToolName(), cert.getNumber()));
                            }

                            TestToolDTO tt = testToolDao.getByName(testTool.getTestToolName());
                            if (tt != null && tt.isRetired()) {
                                listing.getWarningMessages()
                                        .add(msgUtil.getMessage("listing.criteria.retiredTestToolNotAllowed",
                                                testTool.getTestToolName(), cert.getNumber()));
                            }
                        }
                    }
                }
            }
        }
    }
}
