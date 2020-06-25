package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("pendingTestStandardReviewer")
public class TestStandardReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;
    private TestStandardDAO testStandardDao;

    @Autowired
    public TestStandardReviewer(TestStandardDAO testStandardDao, ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
        this.testStandardDao = testStandardDao;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        List<PendingCertificationResultDTO> attestedCriteriaWithTestStandards =
                listing.getCertificationCriterion().stream()
                    .filter(cert -> (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().equals(Boolean.TRUE)))
                    .filter(cert -> (cert.getTestStandards() != null && cert.getTestStandards().size() > 0))
                    .collect(Collectors.<PendingCertificationResultDTO>toList());

        for (PendingCertificationResultDTO criterion : attestedCriteriaWithTestStandards) {
            criterion.getTestStandards().stream()
                .forEach(testStandard -> reviewTestStandard(listing, criterion, testStandard));
        }
    }

    private void reviewTestStandard(PendingCertifiedProductDTO listing, PendingCertificationResultDTO certResult,
            PendingCertificationResultTestStandardDTO testStandard) {
        if (StringUtils.isEmpty(testStandard.getName())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestStandardName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else {
            TestStandardDTO foundTestStandard =
                    testStandardDao.getByNumberAndEdition(testStandard.getName(), listing.getCertificationEditionId());
            if (foundTestStandard == null) {
                listing.getWarningMessages().add(
                        msgUtil.getMessage("listing.criteria.testStandardNotFound",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        testStandard.getName(),
                        listing.getCertificationEdition()));
            }
        }
    }
}
