package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.util.ErrorMessageUtil;

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

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(new InheritedCertificationStatus(true))
                .certificationDate(beforeBoth.getTime())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_B_3)
                                .number("170.315 (b)(3)")
                                .title("not cures")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_2)
                                .number("170.315 (d)(2)")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_3)
                                .number("170.315 (d)(3)")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_10)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_WithOldCriteriaWithoutIcsBeforeDates_NoMessages() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(new InheritedCertificationStatus(false))
                .certificationDate(beforeBoth.getTime())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_B_3)
                                .number("170.315 (b)(3)")
                                .title("not cures")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_2)
                                .number("170.315 (d)(2)")
                                .title("not cures")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_3)
                                .number("170.315 (d)(3)")
                                .title("not cures")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_10)
                                .number("170.315 (d)(10)")
                                .title("not cures")
                                .build())
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_WithOldCriteriaWithoutIcsBetweenDates_OneMessage() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(new InheritedCertificationStatus(false))
                .certificationDate(betweenBoth.getTime())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_B_3)
                                .number("170.315 (b)(3)")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_2)
                                .number("170.315 (d)(2)")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_3)
                                .number("170.315 (d)(3)")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_10)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void review_WithOldCriteriaWithoutIcsAfterDates_AllMessages() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(new InheritedCertificationStatus(false))
                .certificationDate(afterBoth.getTime())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_B_3)
                                .number("170.315 (b)(3)")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_2)
                                .number("170.315 (d)(2)")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_3)
                                .number("170.315 (d)(3)")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_D_10)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(4, listing.getErrorMessages().size());
    }
}
