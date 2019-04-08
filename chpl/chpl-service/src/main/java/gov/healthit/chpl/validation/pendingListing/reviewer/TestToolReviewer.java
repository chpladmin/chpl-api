package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Checks test tool related requirements.
 * Test tool name must match a test tool in the database.
 * Test tool version is required
 * Listings shouldn't use retired test tools.
 * 
 * @author kekey
 *
 */
@Component("pendingTestToolReviewer")
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
    public void review(final PendingCertifiedProductDTO listing) {
        Integer icsCodeInteger = productNumUtil.getIcsCode(listing.getUniqueId());

        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria()) {
                if (cert.getTestTools() != null && cert.getTestTools().size() > 0) {
                    Iterator<PendingCertificationResultTestToolDTO> testToolIter = cert.getTestTools().iterator();
                    while (testToolIter.hasNext()) {
                        PendingCertificationResultTestToolDTO testTool = testToolIter.next();
                        if (StringUtils.isEmpty(testTool.getName())) {
                            listing.getErrorMessages()
                                    .add(msgUtil.getMessage("listing.criteria.missingTestToolName", cert.getNumber()));
                        } else {
                            // require test tool version
                            if (StringUtils.isEmpty(testTool.getVersion())) {
                                listing.getErrorMessages()
                                        .add(msgUtil.getMessage("listing.criteria.missingTestToolVersion",
                                                testTool.getName(), cert.getNumber()));
                            }

                            TestToolDTO foundTestTool = testToolDao.getByName(testTool.getName());
                            if (foundTestTool != null) {
                                // retired tools aren't allowed if there is ICS or an ICS Mismatch
                                if (foundTestTool.isRetired()) {
                                    if (!hasIcs(listing) || hasIcsMismatch(listing)) {
                                        listing.getErrorMessages()
                                                .add(msgUtil.getMessage(
                                                        "listing.criteria.retiredTestToolNoIcsNotAllowed",
                                                        testTool.getName(), cert.getNumber()));
                                    }
                                }
                            } else {
                                listing.getErrorMessages()
                                        .add(msgUtil.getMessage("listing.criteria.testToolNotFoundAndRemoved",
                                                cert.getNumber(), testTool.getName()));
                                testToolIter.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    private Boolean hasIcs(final PendingCertifiedProductDTO listing) {
        Integer icsCodeInteger = productNumUtil.getIcsCode(listing.getUniqueId());
        return listing.getIcs() && !icsCodeInteger.equals(0);
    }

    private Boolean hasIcsMismatch(final PendingCertifiedProductDTO listing) {
        return productNumUtil.hasIcsConflict(listing.getUniqueId(), listing.getIcs());
    }
}
