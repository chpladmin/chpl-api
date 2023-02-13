package gov.healthit.chpl.scheduler.job.ics.reviewer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;

public class GapWithoutIcsReviewerTest {
    private static final String ERROR_MESSAGE = "Listing uses GAP Without ICS";

    private SimpleDateFormat sdf;
    private SpecialProperties specialProperties;
    private GapWithoutIcsReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date erd = createDate("06/01/2020");
        specialProperties = Mockito.mock(SpecialProperties.class);
        Mockito.when(specialProperties.getEffectiveRuleDate()).thenReturn(erd);
        reviewer = new GapWithoutIcsReviewer(specialProperties, ERROR_MESSAGE);
    }

    @Test
    public void review_listingWithIcs_noCertResults_certDateAfterErd_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationDate(createDate("02/19/2022").getTime())
                .chplProductNumber("15.02.05.1439.A111.01.00.1.220219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithoutIcs_attestedCertResultWithoutGap_certDateAfterErd_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.220219")
                .certificationDate(createDate("02/19/2022").getTime())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .gap(false)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithoutIcs_unattestedCertResultWithoutGap_certDateAfterErd_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.220219")
                .certificationDate(createDate("02/19/2022").getTime())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .gap(false)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithoutIcs_attestedCertResultWithGap_certDateBeforeErd_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.190219")
                .certificationDate(createDate("02/19/2019").getTime())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .gap(false)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithoutIcs_unattestedCertResultWithGap_certDateBeforeErd_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.190219")
                .certificationDate(createDate("02/19/2019").getTime())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .gap(false)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithoutIcs_attestedCertResultWithGap_certDateAfterErd_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.220219")
                .certificationDate(createDate("02/19/2022").getTime())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .gap(true)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNotNull(errorMessage);
        assertEquals(ERROR_MESSAGE, errorMessage);
    }

    @Test
    public void review_listingWithoutIcs_unattestedCertResultWithGap_certDateAfterErd_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.220219")
                .certificationDate(createDate("02/19/2022").getTime())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .gap(true)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    private Date createDate(String dateStr) {
        Date erd = null;
        try {
            erd = sdf.parse("06/01/2020");
        } catch (ParseException e) {
            fail("Could not generate ERD");
        }
        return erd;
    }
}
