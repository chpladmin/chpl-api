package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

/**
 * Makes sure the version is included with a test tool.
 *
 * @author kekey
 *
 */
@Component("testTool2015Reviewer")
public class TestTool2015Reviewer extends PermissionBasedReviewer{

    @Autowired
    public TestTool2015Reviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
    }

    @Override
    public void review(final CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess().equals(Boolean.TRUE)
                    && cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                for (CertificationResultTestTool testTool : cert.getTestToolsUsed()) {
                    if (!StringUtils.isEmpty(testTool.getTestToolName())
                            && StringUtils.isEmpty(testTool.getTestToolVersion())) {
                        // require test tool version if a test tool name was entered
                        addCriterionErrorOrWarningByPermission(listing, cert, "listing.criteria.missingTestToolVersion",
                                testTool.getTestToolName(), cert.getNumber());
                    }
                }
            }
        }
    }
}
