package gov.healthit.chpl.validation.listing;

import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;

/**
 * Helper class for unit tests.
 * Has methods designed to generate mock objects to avoid having to put them in the test database.
 */
public final class CertifiedProductValidationTestHelper {

    /** Database ID for 2015 Edition. */
    public static final Long EDITION_2015_ID = 3L;
    /** Database ID for 2014 Edition. */
    public static final Long EDITION_2014_ID = 2L;

    private CertifiedProductValidationTestHelper() {}

    /**
     * Create a mock pending certified product.
     * @param year the edition of the listing
     * @return a mock pending certified product
     */
    public static PendingCertifiedProductDTO createPendingListing(final String year) {
        PendingCertifiedProductDTO pendingListing = new PendingCertifiedProductDTO();
        String certDateString = "11-09-2016";
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date inputDate = dateFormat.parse(certDateString);
            pendingListing.setCertificationDate(inputDate);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
        pendingListing.setId(1L);
        pendingListing.setIcs(false);
        pendingListing.setCertificationEdition(year);
        if (year.equals("2015")) {
            pendingListing.setCertificationEditionId(EDITION_2015_ID);
            pendingListing.setUniqueId("15.07.07.2642.IC04.36.00.1.160402");
        } else if (year.equals("2014")) {
            pendingListing.setCertificationEditionId(EDITION_2014_ID);
            pendingListing.setUniqueId("14.07.07.2642.IC04.36.00.1.160402");
            pendingListing.setPracticeType("Ambulatory");
            pendingListing.setProductClassificationName("Modular EHR");
        }
        return pendingListing;
    }

    /**
     * Create a pending certification result.
     * @param number the certified criteria number
     * @return a mock pending certification result
     */
    public static PendingCertificationResultDTO createPendingCertResult(final String number) {
        PendingCertificationResultDTO pendingCertResult = new PendingCertificationResultDTO();
        pendingCertResult.setPendingCertifiedProductId(1L);
        pendingCertResult.setId(1L);
        pendingCertResult.setAdditionalSoftware(null);
        pendingCertResult.setApiDocumentation(null);
        pendingCertResult.setG1Success(false);
        pendingCertResult.setG2Success(false);
        pendingCertResult.setGap(null);
        pendingCertResult.getCriterion().setNumber(number);
        pendingCertResult.setPrivacySecurityFramework("Approach 1 Approach 2");
        pendingCertResult.setSed(null);
        pendingCertResult.setG1Success(false);
        pendingCertResult.setG2Success(false);
        pendingCertResult.setTestData(null);
        pendingCertResult.setTestFunctionality(null);
        pendingCertResult.setTestProcedures(null);
        pendingCertResult.setTestStandards(null);
        pendingCertResult.setTestTasks(null);
        pendingCertResult.setMeetsCriteria(true);
        return pendingCertResult;
    }

    /**
     * Create a mock certified product.
     * @param year the edition of the listing
     * @param legacy true iff it should be a legacy style listing
     * @return the mock certified product
     */
    public static CertifiedProductSearchDetails createListing(final String year, final boolean legacy) {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        String certDateString = "11-09-2016";
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date inputDate = dateFormat.parse(certDateString);
            listing.setCertificationDate(inputDate.getTime());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
        listing.setId(1L);
        if (year.equals("2015")) {
            listing.getCertificationEdition().put("name", "2015");
            listing.getCertificationEdition().put("id", "3");
            listing.setChplProductNumber("15.07.07.2642.IC04.36.00.1.160402");
            listing.setPracticeType(null);
        } else if (year.equals("2014")) {
            listing.getCertificationEdition().put("name", "2014");
            listing.getCertificationEdition().put("id", "2");
            listing.getPracticeType().put("name", "Ambulatory");
            listing.getClassificationType().put("name", "Modular EHR");
            if (!legacy) {
                listing.setChplProductNumber("14.07.07.2642.IC04.36.00.1.160402");
            } else {
                listing.setChplProductNumber("CHP-008119");
            }
        }
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.FALSE);
        listing.setIcs(ics);
        return listing;
    }

    /**
     * Create a non-legacy mock certified product.
     * @param year the edition of the listing
     * @return the mock certified product
     */
    public static CertifiedProductSearchDetails createListing(final String year) {
        return createListing(year, false);
    }

    /**
     * Create a mock certification result.
     * @param number the certified criteria number
     * @return a pending certification result
     */
    public static CertificationResult createCertResult(final String number) {
        CertificationResult certResult = new CertificationResult();
        certResult.setId(1L);
        certResult.setAdditionalSoftware(null);
        certResult.setApiDocumentation(null);
        certResult.setG1Success(false);
        certResult.setG2Success(false);
        certResult.setGap(null);
        certResult.setNumber(number);
        certResult.setPrivacySecurityFramework("Approach 1 Approach 2");
        certResult.setSed(false);
        certResult.setG1Success(false);
        certResult.setG2Success(false);
        certResult.setTestDataUsed(null);
        certResult.setTestFunctionality(null);
        certResult.setTestProcedures(null);
        certResult.setTestStandards(null);
        certResult.setSuccess(true);
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(number);
        certResult.setCriterion(criterion);
        return certResult;
    }
}
