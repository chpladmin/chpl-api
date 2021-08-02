package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class PromotingInteroperabilityUserCountReviewerTest {
    private static final String ERR_MSG = "Listing contains duplicate Promoting Interoperability entries for %s. Please remove the duplicates.";
    private static final String WARN_MSG = "Listing contains duplicate Promoting Interoperability entires for user count %s and date %s. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private PromotingInteroperabilityUserCountReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicatePromotingInteroperabilityDate"), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicatePromotingInteroperability"), ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(WARN_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new PromotingInteroperabilityUserCountReviewer(msgUtil);
    }

    @Test
    public void review_duplicateDateExists_errorFound() {
        List<PromotingInteroperabilityUser> piuHistory = new ArrayList<PromotingInteroperabilityUser>();
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(1L)
                .userCountDate(LocalDate.parse("2021-01-01"))
                .build());
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(2L)
                .userCountDate(LocalDate.parse("2021-01-01"))
                .build());
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setPromotingInteroperabilityUserHistory(piuHistory);
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(ERR_MSG, "2021-01-01")));
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getPromotingInteroperabilityUserHistory().size());
    }

    @Test
    public void review_duplicateCountAndDateExists_warningFoundAndDuplicateRemoved() {
        List<PromotingInteroperabilityUser> piuHistory = new ArrayList<PromotingInteroperabilityUser>();
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(1L)
                .userCountDate(LocalDate.parse("2021-01-01"))
                .build());
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(1L)
                .userCountDate(LocalDate.parse("2021-01-01"))
                .build());
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setPromotingInteroperabilityUserHistory(piuHistory);
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(WARN_MSG, "1", "2021-01-01")));
        assertEquals(1, listing.getPromotingInteroperabilityUserHistory().size());
    }

    @Test
    public void review_noDuplicates_noWarningOrError() {
        List<PromotingInteroperabilityUser> piuHistory = new ArrayList<PromotingInteroperabilityUser>();
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(1L)
                .userCountDate(LocalDate.parse("2021-01-01"))
                .build());
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(2L)
                .userCountDate(LocalDate.parse("2021-02-01"))
                .build());
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setPromotingInteroperabilityUserHistory(piuHistory);
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getPromotingInteroperabilityUserHistory().size());
    }

    @Test
    public void review_emptyPromotingInteroperabilityUserHistory_noWarningOrError() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getPromotingInteroperabilityUserHistory().clear();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getPromotingInteroperabilityUserHistory().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        List<PromotingInteroperabilityUser> piuHistory = new ArrayList<PromotingInteroperabilityUser>();
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(1L)
                .userCountDate(LocalDate.parse("2021-01-01"))
                .build());
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(2L)
                .userCountDate(LocalDate.parse("2021-02-01"))
                .build());
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(3L)
                .userCountDate(LocalDate.parse("2021-03-01"))
                .build());
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(2L)
                .userCountDate(LocalDate.parse("2021-02-01"))
                .build());
        piuHistory.add(PromotingInteroperabilityUser.builder()
                .userCount(4L)
                .userCountDate(LocalDate.parse("2021-04-01"))
                .build());
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setPromotingInteroperabilityUserHistory(piuHistory);
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(WARN_MSG, "2", "2021-02-01")));
        assertEquals(4, listing.getPromotingInteroperabilityUserHistory().size());
    }
}