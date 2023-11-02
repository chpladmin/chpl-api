package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class AtlDuplicateReviewerTest {
    private static final String ERR_MSG =
            "Listing contains duplicate Testing Lab: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private AtlDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateTestingLab"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), ""));
        reviewer = new AtlDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductTestingLab atl1 = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(1L)
                        .name("Atl1")
                        .build())
                .build();
        CertifiedProductTestingLab atl2 = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(1L)
                        .name("Atl1")
                        .build())
                .build();
        listing.getTestingLabs().add(atl1);
        listing.getTestingLabs().add(atl2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Atl1")))
                .count());
        assertEquals(1, listing.getTestingLabs().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductTestingLab atl1 = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(1L)
                        .name("Atl1")
                        .build())
                .build();

        CertifiedProductTestingLab atl2 = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(2L)
                        .name("Atl2")
                        .build())
                .build();

        listing.getTestingLabs().add(atl1);
        listing.getTestingLabs().add(atl2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getTestingLabs().size());
    }

    @Test
    public void review_emptyTestingLabs_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getTestingLabs().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getTestingLabs().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductTestingLab atl1 = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(1L)
                        .name("Atl1")
                        .build())
                .build();

        CertifiedProductTestingLab atl2 = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(2L)
                        .name("Atl2")
                        .build())
                .build();

        CertifiedProductTestingLab atl3 = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(1L)
                        .name("Atl1")
                        .build())
                .build();

        CertifiedProductTestingLab atl4 = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(3L)
                        .name("Atl3")
                        .build())
                .build();

        listing.getTestingLabs().add(atl1);
        listing.getTestingLabs().add(atl2);
        listing.getTestingLabs().add(atl3);
        listing.getTestingLabs().add(atl4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Atl1")))
                .count());
        assertEquals(3, listing.getTestingLabs().size());
    }
}