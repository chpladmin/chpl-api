package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("testTool2015Reviewer")
public class TestTool2015Reviewer extends PermissionBasedReviewer{

    @Autowired
    public TestTool2015Reviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
    }

    @Override
    public void review(final CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (BooleanUtils.isTrue(cert.isSuccess())
                    && cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                for (CertificationResultTestTool testTool : cert.getTestToolsUsed()) {
                    if (!StringUtils.isEmpty(testTool.getTestToolName())
                            && StringUtils.isEmpty(testTool.getTestToolVersion())) {
                        // require test tool version if a test tool name was entered
                        addCriterionError(listing, cert, "listing.criteria.missingTestToolVersion",
                                testTool.getTestToolName(), Util.formatCriteriaNumber(cert.getCriterion()));
                    }
                }
            }
        }
    }
}
