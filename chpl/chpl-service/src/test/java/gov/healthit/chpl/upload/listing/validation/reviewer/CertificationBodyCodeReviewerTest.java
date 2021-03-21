package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CertificationBodyCodeReviewerTest {
    private static final String MISSING_ACB_CODE = "A Certification Body code must be supplied with the listing.";
    private static final String MISMATCHED_ACBS = "The ONC-ACB code from the CHPL Product Number %s does not match the code of the responsible ONC-ACB %s.";

    private ErrorMessageUtil errorMessageUtil;
    private CertificationBodyCodeReviewer reviewer;

    @Before
    public void setup() {
        ChplProductNumberUtil chplProductNumberUtil = new ChplProductNumberUtil();
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new CertificationBodyCodeReviewer(chplProductNumberUtil, errorMessageUtil);
    }

    @Test
    public void review_nullAcbValidCodeInChplProductNumber_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .certifyingBody(null)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_nullAcbEmptyCodeInChplProductNumber_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(".04.04.2526.WEBe.06.00.1.210101")
                .certifyingBody(null)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_legacyChplProductNumberWithAcb_noError() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "01");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .certifyingBody(acbMap)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_legacyChplProductNumberNullAcb_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .certifyingBody(null)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_mismatchedAcbCode_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.certificationBodyMismatch"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISMATCHED_ACBS, i.getArgument(1), i.getArgument(2)));

        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "01");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .certifyingBody(acbMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        System.out.println(listing.getErrorMessages().iterator().next());
        assertTrue(listing.getErrorMessages().contains(String.format(MISMATCHED_ACBS, "04", "01")));
    }

    @Test
    public void review_goodAcbCodeWithMatchingListingAcbCode_noError() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .certifyingBody(acbMap)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
