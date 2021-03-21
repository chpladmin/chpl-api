package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestingLabCodeReviewerTest {
    private static final String ATL_99 = "There is more than one Testing Lab but the ATL code is not '99'.";
    private static final String ATL_NOT_99 = "There is only one Testing Lab but the ATL code is '99'.";
    private static final String ATL_MISMATCH = "The ONC-ATL code from the CHPL Product Number %s does not match the code of the responsible ONC-ATL %s.";
    private static final String ATL_INVALID_CODE = "The ONC-ATL code from the CHPL Product Number %s does not match any ONC-ATL code in the system.";

    private ErrorMessageUtil errorMessageUtil;
    private TestingLabCodeReviewer reviewer;

    @Before
    public void setup() {
        ChplProductNumberUtil chplProductNumberUtil = new ChplProductNumberUtil();
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new TestingLabCodeReviewer(chplProductNumberUtil, errorMessageUtil);
    }

    @Test
    public void review_emptyAtlValidCodeInChplProductNumber_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.invalidTestingLabCode"), ArgumentMatchers.anyString()))
        .thenAnswer(i -> String.format(ATL_INVALID_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(ATL_INVALID_CODE, "04", "")));
    }

    @Test
    public void review_nullAtlValidCodeInChplProductNumber_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.invalidTestingLabCode"), ArgumentMatchers.anyString()))
        .thenAnswer(i -> String.format(ATL_INVALID_CODE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .build();
        listing.setTestingLabs(null);
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(ATL_INVALID_CODE, "04", "")));
    }

    @Test
    public void review_nullAtlEmptyCodeInChplProductNumber_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15..04.2526.WEBe.06.00.1.210101")
                .build();
        listing.setTestingLabs(null);
        reviewer.review(listing);

        assertNull(listing.getTestingLabs());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyAtlEmptyCodeInChplProductNumber_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15..04.2526.WEBe.06.00.1.210101")
                .testingLabs(new ArrayList<CertifiedProductTestingLab>())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_legacyChplProductNumberWithAtl_noError() {
        CertifiedProductTestingLab atl = new CertifiedProductTestingLab();
        atl.setTestingLabCode(null);
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(atl);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .testingLabs(atls)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_legacyChplProductNumberNullAtl_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_atlCode99OneTestingLab_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("atl.shouldNotBe99")))
            .thenReturn(ATL_NOT_99);

        CertifiedProductTestingLab atl = new CertifiedProductTestingLab();
        atl.setTestingLabCode("01");
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(atl);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.99.04.2526.WEBe.06.00.1.210101")
                .testingLabs(atls)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(ATL_NOT_99));
    }

    @Test
    public void review_atlCode99NoTestingLabs_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("atl.shouldNotBe99")))
            .thenReturn(ATL_NOT_99);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.99.04.2526.WEBe.06.00.1.210101")
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(ATL_NOT_99));
    }

    @Test
    public void review_atlCodeNot99MultipleTestingLabs_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.atl.codeIsNotForMultiple")))
            .thenReturn(ATL_99);

        CertifiedProductTestingLab atl = new CertifiedProductTestingLab();
        atl.setTestingLabCode("01");
        CertifiedProductTestingLab atl2 = new CertifiedProductTestingLab();
        atl2.setTestingLabCode("05");
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(atl);
        atls.add(atl2);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.01.04.2526.WEBe.06.00.1.210101")
                .testingLabs(atls)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(ATL_99));
    }

    @Test
    public void review_mismatchedAtlCode_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.testingLabMismatch"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(ATL_MISMATCH, i.getArgument(1), i.getArgument(2)));

        CertifiedProductTestingLab atl = new CertifiedProductTestingLab();
        atl.setTestingLabCode("01");
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(atl);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .testingLabs(atls)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(ATL_MISMATCH, "04", "01")));
    }

    @Test
    public void review_validAtlCodeWithMatchingSingleAtl_noError() {
        CertifiedProductTestingLab atl = new CertifiedProductTestingLab();
        atl.setTestingLabCode("04");
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(atl);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .testingLabs(atls)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_99AtlCodeWithMultipleAtls_noError() {
        CertifiedProductTestingLab atl = new CertifiedProductTestingLab();
        atl.setTestingLabCode("01");
        CertifiedProductTestingLab atl2 = new CertifiedProductTestingLab();
        atl2.setTestingLabCode("05");
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(atl);
        atls.add(atl2);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.99.04.2526.WEBe.06.00.1.210101")
                .testingLabs(atls)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
