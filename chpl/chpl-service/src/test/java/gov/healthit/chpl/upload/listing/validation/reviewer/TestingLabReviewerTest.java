package gov.healthit.chpl.upload.listing.validation.reviewer;

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
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestingLabReviewerTest {
    private static final String MISSING_ATL = "Testing Lab is required but not found.";
    private static final String MISSING_ATL_NAME = "Testing Lab name is required but was not found.";
    private static final String MISSING_ATL_CODE = "Testing Lab code is required but was not found.";
    private static final String INVALID_ATL = "The ONC-ATL %s is not valid.";
    private static final String NOT_FOUND = "Testing lab not found.";

    private ErrorMessageUtil errorMessageUtil;
    private TestingLabReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.invalidTestingLab"), ArgumentMatchers.anyString()))
        .thenAnswer(i -> String.format(INVALID_ATL, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage("listing.missingTestingLab"))
            .thenReturn(MISSING_ATL);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.missingTestingLabName")))
            .thenReturn(MISSING_ATL_NAME);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.missingTestingLabCode")))
            .thenReturn(MISSING_ATL_CODE);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("atl.notFound")))
            .thenReturn(NOT_FOUND);
        reviewer = new TestingLabReviewer(errorMessageUtil);
    }

    @Test
    public void review_nullAtl_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ATL));
    }

    @Test
    public void review_atlObjectWithNullValues_hasErrors() {
        List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductTestingLab atl = new CertifiedProductTestingLab();
        testingLabs.add(atl);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(testingLabs)
                .build();

        reviewer.review(listing);

        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ATL_CODE));
        assertTrue(listing.getErrorMessages().contains(MISSING_ATL_NAME));
    }

    @Test
    public void review_atlObjectWithEmptyValues_hasErrors() {
        List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                .testingLabCode("")
                .testingLabName("")
                .build();
        testingLabs.add(atl);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(testingLabs)
                .build();

        reviewer.review(listing);

        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ATL_CODE));
        assertTrue(listing.getErrorMessages().contains(MISSING_ATL_NAME));
    }

    @Test
    public void review_atlObjectWithNullNameValidId_hasError() {
        List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                .testingLabCode("04")
                .testingLabName("")
                .testingLabId(1L)
                .build();
        testingLabs.add(atl);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(testingLabs)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ATL_NAME));
    }

    @Test
    public void review_atlObjectWithValidNameNullId_hasError() {
        List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                .testingLabCode("05")
                .testingLabName("My ATL")
                .testingLabId(null)
                .build();
        testingLabs.add(atl);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(testingLabs)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_ATL, "My ATL", "")));
    }

    @Test
    public void review_atlObjectWithValidNameAndId_noErrors() {
        List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                .testingLabCode("05")
                .testingLabName("My ATL")
                .testingLabId(2L)
                .build();
        testingLabs.add(atl);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(testingLabs)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
