package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.PermissionBasedReviewer;

@Component("pendingTestTool2015Reviewer")
public class TestTool2015Reviewer extends PermissionBasedReviewer {

    @Autowired
    public TestTool2015Reviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
    }

    public void review(final PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().equals(Boolean.TRUE)
                    && cert.getTestTools() != null && cert.getTestTools().size() > 0) {
                for (PendingCertificationResultTestToolDTO testTool : cert.getTestTools()) {
                        if (!StringUtils.isEmpty(testTool.getName()) && StringUtils.isEmpty(testTool.getVersion())) {
                            addErrorOrWarningByPermission(listing, cert, "listing.criteria.missingTestToolVersion",
                                testTool.getName(), cert.getCriterion().getNumber());
                    }
                }
            }
        }
    }
}

