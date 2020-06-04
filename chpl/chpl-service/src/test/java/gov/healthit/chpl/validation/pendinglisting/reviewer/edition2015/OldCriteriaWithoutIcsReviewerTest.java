package gov.healthit.chpl.validation.pendinglisting.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.OldCriteriaWithoutIcsReviewer;

public class OldCriteriaWithoutIcsReviewerTest {

    private Environment env;
    private ErrorMessageUtil msgUtil;

    private OldCriteriaWithoutIcsReviewer reviewer;

    private static final long EDITION_2015_B_3 = 1L;
    private static final long EDITION_2015_D_2 = 2L;
    private static final long EDITION_2015_D_3 = 3L;
    private static final long EDITION_2015_D_10 = 4L;
    private static final int BEFORE_YEAR = 2006;
    private static final int RELEVANT_YEAR = 2020;
    private static final int MIDDLE_MONTH = 3;
    private static final int AFTER_MONTH = 9;

    private Date beforeBoth;
    private Date betweenBoth;
    private Date afterBoth;

    @Before
    public void before() {
        env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty(ArgumentMatchers.eq("questionableActivity.b3ChangeDate"))).thenReturn("01/01/2020");
        Mockito.when(env.getProperty(ArgumentMatchers.eq("cures.ruleEffectiveDate"))).thenReturn("07/07/2020");

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(
                ArgumentMatchers.eq("listing.criteria.hasOldVersionOfCriteria"),
                ArgumentMatchers.any())).thenAnswer(i -> i.getArguments()[1]);

        reviewer = new OldCriteriaWithoutIcsReviewer(env, msgUtil);
        beforeBoth = Date.from(LocalDate.of(BEFORE_YEAR, MIDDLE_MONTH, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        betweenBoth = Date.from(LocalDate.of(RELEVANT_YEAR, MIDDLE_MONTH, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        afterBoth = Date.from(LocalDate.of(RELEVANT_YEAR, AFTER_MONTH, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    public void review_WithIcs_NoMessages() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .ics(true)
                .certificationDate(beforeBoth)
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_B_3)
                                .number("170.315 (b)(3)")
                                .title("not cures")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_2)
                                .number("170.315 (d)(2)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_3)
                                .number("170.315 (d)(3)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_10)
                                .number("170.315 (d)(10)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_WithoutIcsWithoutCriteria_NoMessages() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .ics(false)
                .certificationDate(beforeBoth)
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_B_3)
                                .number("170.315 (b)(3)")
                                .title("not cures")
                                .build())
                        .meetsCriteria(false)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_2)
                                .number("170.315 (d)(2)")
                                .build())
                        .meetsCriteria(false)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_3)
                                .number("170.315 (d)(3)")
                                .build())
                        .meetsCriteria(false)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_10)
                                .number("170.315 (d)(10)")
                                .build())
                        .meetsCriteria(false)
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_WithOldCriteriaWithoutIcsBeforeDates_NoMessages() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .ics(false)
                .certificationDate(beforeBoth)
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_B_3)
                                .number("170.315 (b)(3)")
                                .title("not cures")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_2)
                                .number("170.315 (d)(2)")
                                .title("not cures")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_3)
                                .number("170.315 (d)(3)")
                                .title("not cures")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_10)
                                .number("170.315 (d)(10)")
                                .title("not cures")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_WithOldCriteriaWithoutIcsBetweenDates_OneMessage() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .ics(false)
                .certificationDate(betweenBoth)
                .errorMessages(new HashSet<String>())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_B_3)
                                .number("170.315 (b)(3)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_2)
                                .number("170.315 (d)(2)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_3)
                                .number("170.315 (d)(3)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_10)
                                .number("170.315 (d)(10)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void review_WithOldCriteriaWithoutIcsAfterDates_AllMessages() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .ics(false)
                .certificationDate(afterBoth)
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_B_3)
                                .number("170.315 (b)(3)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_2)
                                .number("170.315 (d)(2)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_3)
                                .number("170.315 (d)(3)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(EDITION_2015_D_10)
                                .number("170.315 (d)(10)")
                                .build())
                        .meetsCriteria(true)
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        reviewer.review(listing);

        assertEquals(4, listing.getErrorMessages().size());
    }
}
