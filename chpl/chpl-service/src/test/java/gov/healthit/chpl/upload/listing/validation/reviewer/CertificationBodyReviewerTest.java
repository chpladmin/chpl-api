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
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CertificationBodyReviewerTest {
    private static final String MISSING_ACB = "A Certification Body is required.";
    private static final String MISSING_ACB_NAME = "A Certification Body name is required.";
    private static final String MISSING_ACB_CODE = "A Certification Body code is required.";
    private static final String INVALID_ACB = "The ONC-ACB %s is not valid.";

    private ErrorMessageUtil errorMessageUtil;
    private CertificationBodyReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.invalidCertificationBody"), ArgumentMatchers.anyString()))
        .thenAnswer(i -> String.format(INVALID_ACB, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage("listing.missingCertificationBody"))
            .thenReturn(MISSING_ACB);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.missingCertificationBodyName")))
            .thenReturn(MISSING_ACB_NAME);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.missingCertificationBodyCode")))
            .thenReturn(MISSING_ACB_CODE);
        reviewer = new CertificationBodyReviewer(errorMessageUtil);
    }

    @Test
    public void review_nullAcb_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(null)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACB));
    }

    @Test
    public void review_acbObjectWithEmptyValues_hasErrors() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "");
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "");
        acbMap.put(CertifiedProductSearchDetails.ACB_ID_KEY, "");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acbMap)
                .build();

        reviewer.review(listing);

        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACB_CODE));
        assertTrue(listing.getErrorMessages().contains(MISSING_ACB_NAME));
    }

    @Test
    public void review_acbObjectWithNullValues_hasErrors() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_NAME_KEY, null);
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, null);
        acbMap.put(CertifiedProductSearchDetails.ACB_ID_KEY, null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acbMap)
                .build();

        reviewer.review(listing);

        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACB_CODE));
        assertTrue(listing.getErrorMessages().contains(MISSING_ACB_NAME));
    }

    @Test
    public void review_acbObjectWithNoNameKey_hasError() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_ID_KEY, "");
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acbMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACB_NAME));
    }

    @Test
    public void review_acbObjectWithEmptyNameValidId_hasError() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "");
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");
        acbMap.put(CertifiedProductSearchDetails.ACB_ID_KEY, 2L);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acbMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACB_NAME));
    }

    @Test
    public void review_acbObjectWithNullNameValidId_hasError() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_NAME_KEY, null);
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");
        acbMap.put(CertifiedProductSearchDetails.ACB_ID_KEY, 2L);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acbMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACB_NAME));
    }

    @Test
    public void review_acbObjectWithValidNameEmptyId_hasError() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Test");
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");
        acbMap.put(CertifiedProductSearchDetails.ACB_ID_KEY, "");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acbMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_ACB, "Test", "")));
    }

    @Test
    public void review_acbObjectWithValidNameNullId_hasError() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Test");
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");
        acbMap.put(CertifiedProductSearchDetails.ACB_ID_KEY, null);


        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acbMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        System.out.println(listing.getErrorMessages().iterator().next());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_ACB, "Test", "")));
    }

    @Test
    public void review_acbObjectWithValidNameMissingIdKey_hasError() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Test");
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acbMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_ACB, "Test", "")));
    }

    @Test
    public void review_acbObjectWithValidNameAndId_noErrors() {
        Map<String, Object> acbMap = new HashMap<String, Object>();
        acbMap.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Test");
        acbMap.put(CertifiedProductSearchDetails.ACB_CODE_KEY, "04");
        acbMap.put(CertifiedProductSearchDetails.ACB_ID_KEY, 2L);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certifyingBody(acbMap)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
