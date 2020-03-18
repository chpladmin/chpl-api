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
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

/**
 * Makes sure a valid test tool was entered by the user - otherwise removes it and includes an error.
 * Makes sure the version is included with a test tool.
 * Checks that retired test tools are not used if not appropriate.
 *
 * @author kekey
 *
 */
@Component("testToolReviewer")
public class TestToolReviewer extends PermissionBasedReviewer {
    private TestToolDAO testToolDao;

    @Autowired
    public TestToolReviewer(TestToolDAO testToolDAO, ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.testToolDao = testToolDAO;
    }

    @Override
    public void review(final CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess().equals(Boolean.TRUE)) {
                if (cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                    Iterator<CertificationResultTestTool> testToolIter = cert.getTestToolsUsed().iterator();
                    while (testToolIter.hasNext()) {
                        CertificationResultTestTool testTool = testToolIter.next();
                        if (StringUtils.isEmpty(testTool.getTestToolName())) {
                            addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingTestToolName",
                                    Util.formatCriteriaNumber(cert.getCriterion()));
                        } else {
                            TestToolDTO tt = testToolDao.getByName(testTool.getTestToolName());
                            if (tt != null && tt.isRetired()) {
                                listing.getWarningMessages()
                                        .add(msgUtil.getMessage("listing.criteria.retiredTestToolNotAllowed",
                                                testTool.getTestToolName(), Util.formatCriteriaNumber(cert.getCriterion())));
                            } else if (tt == null) {
                                addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.testToolNotFound",
                                        Util.formatCriteriaNumber(cert.getCriterion()), testTool.getTestToolName());
                            }
                        }
                    }
                }
            }
        }
    }
}
