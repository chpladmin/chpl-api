package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("pendingTestToolReviewer")
public class TestToolReviewer extends PermissionBasedReviewer {
    private TestToolDAO testToolDao;
    private ChplProductNumberUtil productNumUtil;
    private List<TestToolCriteriaMap> testToolCriteriaMap;

    @Autowired
    public TestToolReviewer(TestToolDAO testToolDAO, ErrorMessageUtil msgUtil,
            ChplProductNumberUtil chplProductNumberUtil, ResourcePermissions resourcePermissions) throws EntityRetrievalException {
        super(msgUtil, resourcePermissions);
        this.testToolDao = testToolDAO;
        this.productNumUtil = chplProductNumberUtil;
        testToolCriteriaMap = testToolDao.getAllTestToolCriteriaMap();
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        listing.getCertificationCriterion().stream()
                .filter(cert -> isCriteriaAttestedTo(cert)
                        && doesCertificationResultHaveTestTools(cert))
                .forEach(cert -> validateTestTools(listing, cert));
    }

    private Boolean isCriteriaAttestedTo(PendingCertificationResultDTO cert) {
        return cert.getMeetsCriteria() != null && cert.getMeetsCriteria().equals(Boolean.TRUE);
    }

    private Boolean doesCertificationResultHaveTestTools(PendingCertificationResultDTO cert) {
        return cert.getTestTools() != null && cert.getTestTools().size() > 0;
    }

    private Boolean hasIcs(final PendingCertifiedProductDTO listing) {
        Integer icsCodeInteger = productNumUtil.getIcsCode(listing.getUniqueId());
        return listing.getIcs() && !icsCodeInteger.equals(0);
    }

    private Boolean hasIcsMismatch(final PendingCertifiedProductDTO listing) {
        return productNumUtil.hasIcsConflict(listing.getUniqueId(), listing.getIcs());
    }

    private void validateTestTools(PendingCertifiedProductDTO listing, PendingCertificationResultDTO cert)  {
        Iterator<PendingCertificationResultTestToolDTO> testToolIter = cert.getTestTools().iterator();
        while (testToolIter.hasNext()) {
            PendingCertificationResultTestToolDTO testTool = testToolIter.next();
            if (StringUtils.isEmpty(testTool.getName())) {
                addErrorOrWarningByPermission(listing, cert,
                        "listing.criteria.missingTestToolName",
                        Util.formatCriteriaNumber(cert.getCriterion()));
            } else {
                TestToolDTO foundTestTool = testToolDao.getByName(testTool.getName());
                if (foundTestTool != null) {
                    // retired tools aren't allowed if there is ICS or an ICS Mismatch
                    if (foundTestTool.isRetired()) {
                        if (!hasIcs(listing) || hasIcsMismatch(listing)) {
                            addErrorOrWarningByPermission(listing, cert,
                                    "listing.criteria.retiredTestToolNoIcsNotAllowed",
                                    testTool.getName(),
                                    Util.formatCriteriaNumber(cert.getCriterion()));
                        }
                    }

                    if (!isTestToolValidForCriteria(cert, foundTestTool)) {
                        listing.getErrorMessages()
                                .add(msgUtil.getMessage("listing.criteria.testToolCriterionMismatch",
                                        foundTestTool.getName(), Util.formatCriteriaNumber(cert.getCriterion())));
                    }
                } else {
                    addErrorOrWarningByPermission(listing, cert, "listing.criteria.testToolNotFoundAndRemoved",
                            Util.formatCriteriaNumber(cert.getCriterion()),
                            testTool.getName());
                    testToolIter.remove();
                }
            }
        }
    }

    private Boolean isTestToolValidForCriteria(PendingCertificationResultDTO criterion, TestToolDTO testTool) {
        return testToolCriteriaMap.stream()
                .filter(ttcm -> ttcm.getCriterion().getId().equals(criterion.getId())
                        && ttcm.getTestTool().getId().equals(testTool.getId()))
                .findAny()
                .isPresent();
    }
}
