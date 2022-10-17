package gov.healthit.chpl.validation.listing.reviewer.edition2014;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

/**
 * Some 2014 listings reference test standards that are only mapped to 2015 edition.
 * Rather than create a validation error for this existing data, this reviewer
 * only checks for newly added test standards and gives an error if any of those newly added
 * ones are not mapped to 2014 edition.
 * If the data gets fixed in the future then this reviewer could be deleted and replaced
 * with the TestStandardReviewer in all 2014 listing validators.
 */
@Component("testStandardComparisonReviewer")
public class TestStandardComparisonReviewer implements ComparisonReviewer {
    private TestStandardDAO testStandardDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestStandardComparisonReviewer(TestStandardDAO testStandardDao, ErrorMessageUtil msgUtil) {
        this.testStandardDao = testStandardDao;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        List<CertificationResult> updatedCertResultsWithTestStandards = updatedListing.getCertificationResults().stream()
                .filter(updatedCertResult -> hasTestStandards(updatedCertResult))
                .collect(Collectors.toList());

        for (CertificationResult updatedCr : updatedCertResultsWithTestStandards) {
            Optional<CertificationResult> existingCr = findCertificationResult(existingListing, updatedCr.getId());
            if (existingCr.isPresent()) {
                List<CertificationResultTestStandard> newlyAddedTestStandards
                    = getNewlyAddedTestStandards(updatedCr.getTestStandards(), existingCr.get().getTestStandards());
                newlyAddedTestStandards.stream()
                    .forEach(testStandard -> reviewNewlyAddedTestStandard(updatedListing, updatedCr, testStandard));
            } else {
                updatedCr.getTestStandards().stream()
                    .forEach(testStandard -> reviewNewlyAddedTestStandard(updatedListing, updatedCr, testStandard));
            }
        }
    }

    private boolean hasTestStandards(CertificationResult certResult) {
        return certResult.getTestStandards() != null && certResult.getTestStandards().size() > 0;
    }

    private Optional<CertificationResult> findCertificationResult(CertifiedProductSearchDetails listing,
            Long certificationResultId) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getId().equals(certificationResultId))
                .findFirst();
    }

    private List<CertificationResultTestStandard> getNewlyAddedTestStandards(List<CertificationResultTestStandard> updatedTestStandards,
            List<CertificationResultTestStandard> existingTestStandards) {
        return updatedTestStandards.stream()
            .filter(updatedTestStandard -> !hasTestStandardWithId(existingTestStandards, updatedTestStandard.getTestStandardId()))
            .collect(Collectors.toList());
    }

    private boolean hasTestStandardWithId(List<CertificationResultTestStandard> testStandards, Long id) {
        return testStandards.stream()
                .filter(testStandard -> testStandard.getTestStandardId().equals(id))
                .findAny().isPresent();
    }

    private void reviewNewlyAddedTestStandard(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultTestStandard testStandard) {
        String testStandardName = testStandard.getTestStandardName();
        Long editionId = MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY);

        if (StringUtils.isEmpty(testStandardName)) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestStandardName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else {
            TestStandardDTO foundTestStandard = testStandardDao.getByNumberAndEdition(testStandardName, editionId);
            if (foundTestStandard == null) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.criteria.testStandardNotFound",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        testStandardName,
                        MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY)));
            }
        }
    }
}
