package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.CertificationCriterionServiceTest;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class OldCriteriaWithoutIcsReviewerTest {

    private Environment env;
    private ErrorMessageUtil msgUtil;
    private CertificationCriterionDAO certificationCriterionDAO;
    private CertificationCriterionService criteriaService;

    private OldCriteriaWithoutIcsReviewer reviewer;

    private static final int BEFORE_YEAR = 2006;
    private static final int RELEVANT_YEAR = 2020;
    private static final int MIDDLE_MONTH = 3;
    private static final int AFTER_MONTH = 9;

    private CertificationCriterion b3, d2, d3, d10;
    private Date beforeBoth;
    private Date betweenBoth;
    private Date afterBoth;

    @Before
    public void before() {
        b3 = CertificationCriterion.builder()
                .id(18L)
                .number("170.315 (b)(3)")
                .build();

        d2 = CertificationCriterion.builder()
                .id(30L)
                .number("170.315 (d)(2)")
                .build();

        d3 = CertificationCriterion.builder()
                .id(31L)
                .number("170.315 (d)(3)")
                .build();

        d10 = CertificationCriterion.builder()
                .id(38L)
                .number("170.315 (d)(10)")
                .build();

        env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty(ArgumentMatchers.eq("questionableActivity.b3ChangeDate"))).thenReturn("01/01/2020");
        Mockito.when(env.getProperty(ArgumentMatchers.eq("cures.ruleEffectiveDate"))).thenReturn("07/07/2020");
        Mockito.when(env.getProperty(Criteria2015.B_3_OLD)).thenReturn(String.valueOf(b3.getId()));
        Mockito.when(env.getProperty(Criteria2015.D_2_OLD)).thenReturn(String.valueOf(d2.getId()));
        Mockito.when(env.getProperty(Criteria2015.D_3_OLD)).thenReturn(String.valueOf(d3.getId()));
        Mockito.when(env.getProperty(Criteria2015.D_10_OLD)).thenReturn(String.valueOf(d10.getId()));
        Mockito.when(env.getProperty("criteria.sortOrder")).thenReturn(CertificationCriterionServiceTest.sortOrderFromProperty());

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(
                ArgumentMatchers.eq("listing.criteria.hasOldVersionOfCriteria"),
                ArgumentMatchers.any())).thenAnswer(i -> i.getArguments()[1]);

        certificationCriterionDAO = Mockito.mock(CertificationCriterionDAO.class);
        Mockito.when(certificationCriterionDAO.findAll()).thenReturn(makeAllCriteria());

        criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.B_3_OLD)))
            .thenReturn(b3);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.D_2_OLD)))
            .thenReturn(d2);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.D_3_OLD)))
            .thenReturn(d3);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.D_10_OLD)))
            .thenReturn(d10);

        reviewer = new OldCriteriaWithoutIcsReviewer(env, msgUtil, criteriaService);
        reviewer.postConstruct();

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
                        .criterion(b3)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d10)
                        .success(true)
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_WithoutIcsWithoutCriteria_NoMessages() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(new InheritedCertificationStatus(false))
                .certificationDate(beforeBoth.getTime())
                .certificationResult(CertificationResult.builder()
                        .criterion(b3)
                        .success(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2)
                        .success(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3)
                        .success(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d10)
                        .success(false)
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
                        .criterion(b3)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d10)
                        .success(true)
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
                        .criterion(b3)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d10)
                        .success(true)
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
                        .criterion(b3)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d10)
                        .success(true)
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(4, listing.getErrorMessages().size());
    }

    private ArrayList<CertificationCriterion> makeAllCriteria() {
        ArrayList<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();
        criteria.add(b3);
        criteria.add(d2);
        criteria.add(d3);
        criteria.add(d10);
        return criteria;
    }
}
