package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("testStandardReviewer")
public class TestStandardReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;
    private TestStandardDAO testStandardDao;

    @Autowired
    public TestStandardReviewer(TestStandardDAO testStandardDao, ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
        this.testStandardDao = testStandardDao;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(cert -> (cert.getTestStandards() != null && cert.getTestStandards().size() > 0))
                .forEach(certResult -> certResult.getTestStandards().stream()
                        .forEach(testStandard -> reviewTestStandard(listing, certResult, testStandard)));
    }

    private void reviewTestStandard(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultTestStandard testStandard) {
        String testStandardName = testStandard.getTestStandardName();
        Long editionId = listing.getEdition().getId();
        if (testStandard.getTestStandardId() != null) {
            TestStandard foundTestStandard = testStandardDao.getByIdAndEdition(testStandard.getTestStandardId(), editionId);
            if (foundTestStandard == null) {
                listing.addDataErrorMessage(
                        msgUtil.getMessage("listing.criteria.testStandardIdNotFound",
                                Util.formatCriteriaNumber(certResult.getCriterion()),
                                testStandard.getTestStandardId(),
                                listing.getEdition().getName()));
            }
        } else if (!StringUtils.isEmpty(testStandardName)) {
            TestStandard foundTestStandard = testStandardDao.getByNumberAndEdition(testStandardName, editionId);
            if (foundTestStandard == null) {
                listing.addDataErrorMessage(
                        msgUtil.getMessage("listing.criteria.testStandardNotFound",
                                Util.formatCriteriaNumber(certResult.getCriterion()),
                                testStandardName,
                                listing.getEdition().getName()));
            }
        } else {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.missingTestStandardName",
                            Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }
}
