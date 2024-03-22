package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codeset.CertificationResultCodeSet;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

public class CodeSetAsOfCertificationDayReviewerTest {
    private static final String CODE_SETS_NOT_APPLICABLE = "Code Sets are not applicable for Criterion %s. They have been removed.";
    private static final String CODE_SETS_NOT_FOUND = "Code Set %s for Criterion %s does not exist. It has been removed.";
    private static final String CODE_SET_NOT_AVAILABLE_BY_START_DATE = "Code Set %s for Criterion %s has a Start Day of %s and is not available. It has been removed.";
    private static final String CODE_SET_NOT_VALID_FOR_CRITERION = "Code Set %s is not valid for Criterion %s. It has been removed.";
    private static final String CODE_SET_REQUIRED_NOT_FOUND = "Code Set %s is required for Criterion %s beginning on %s but was not found.";

    private CodeSetDAO codeSetDao;
    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private CodeSetAsOfCertificationDayReviewer reviewer;
    private CertificationCriterion a1, a2, a5;
    private CodeSet a1a2CodeSet, a5CodeSet;

    @Before
    public void before() throws EntityRetrievalException {
        a1 = CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .certificationEdition("2015")
                .build();
        a2 = CertificationCriterion.builder()
                .id(2L)
                .number("170.315 (a)(2)")
                .certificationEdition("2015")
                .build();
        a5 = CertificationCriterion.builder()
                .id(5L)
                .number("170.315 (a)(5)")
                .certificationEdition("2015")
                .build();
        a5CodeSet = CodeSet.builder()
                .id(2L)
                .startDay(LocalDate.parse("2024-03-11"))
                .requiredDay(LocalDate.parse("2024-03-12"))
                .build();
        a1a2CodeSet = CodeSet.builder()
                .id(1L)
                .startDay(LocalDate.parse("2025-03-11"))
                //setting this way in the future so the CHPL is long gone by the time any tests would start failing
                .requiredDay(LocalDate.parse("3000-12-31"))
                .build();

        codeSetDao = Mockito.mock(CodeSetDAO.class);
        Mockito.when(codeSetDao.getCodeSetCriteriaMaps()).thenReturn(buildCodeSetCriteriaMaps());
        Mockito.when(codeSetDao.findAll()).thenReturn(Stream.of(a1a2CodeSet, a5CodeSet).collect(Collectors.toList()));

        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.CODE_SET)))
            .thenReturn(true);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.codeSetNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CODE_SETS_NOT_APPLICABLE, i.getArgument(1), ""));

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("codeSet.doesNotExist"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CODE_SETS_NOT_FOUND, i.getArgument(1), i.getArgument(2)));

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("codeSet.notAvailableBasedOnStartDay"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CODE_SET_NOT_AVAILABLE_BY_START_DATE, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("codeSet.notAllowedForCriteria"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CODE_SET_NOT_VALID_FOR_CRITERION, i.getArgument(1), i.getArgument(2)));

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.codeSetRequired"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CODE_SET_REQUIRED_NOT_FOUND, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        reviewer = new CodeSetAsOfCertificationDayReviewer(certResultRules,
                codeSetDao,
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                msgUtil);
    }

    private Map<Long, List<CodeSet>> buildCodeSetCriteriaMaps() {
        Map<Long, List<CodeSet>> maps = new LinkedHashMap<Long, List<CodeSet>>();
        maps.put(a1.getId(), List.of(a1a2CodeSet));
        maps.put(a2.getId(), List.of(a1a2CodeSet));
        maps.put(a5.getId(), List.of(a5CodeSet));
        return maps;
    }

    @Test
    public void review_nullCodeSetsOnCriteriaWithoutCodeSets_noError() {
        List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
        statusEvents.add(CertificationStatusEvent.builder()
                        .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-03-13")))
                        .status(CertificationStatus.builder()
                                .id(1L)
                                .name(CertificationStatusType.Active.getName())
                                .build())
                        .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setCodeSets(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyCodeSetsOnCriteriaWithoutCodeSets_noError() {
        List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
        statusEvents.add(CertificationStatusEvent.builder()
                        .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-03-13")))
                        .status(CertificationStatus.builder()
                                .id(1L)
                                .name(CertificationStatusType.Active.getName())
                                .build())
                        .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setCodeSets(new ArrayList<CertificationResultCodeSet>());
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_codeSetsNotApplicableToCriteria_hasWarningAndCodeSetsNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.CODE_SET)))
            .thenReturn(false);

        List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
        statusEvents.add(CertificationStatusEvent.builder()
                        .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-03-13")))
                        .status(CertificationStatus.builder()
                                .id(1L)
                                .name(CertificationStatusType.Active.getName())
                                .build())
                        .build());

        List<CertificationResultCodeSet> codeSets = new ArrayList<CertificationResultCodeSet>();
        codeSets.add(CertificationResultCodeSet.builder()
                  .codeSet(a1a2CodeSet)
                  .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .codeSets(codeSets)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CODE_SETS_NOT_APPLICABLE, Util.formatCriteriaNumber(a1))));
        assertNull(listing.getCertificationResults().get(0).getCodeSets());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_codeSetsAppliedToWrongCriteria_hasWarningAndRemovedCodeSet() {
      List<CertificationResultCodeSet> codeSets = new ArrayList<CertificationResultCodeSet>();
      codeSets.add(CertificationResultCodeSet.builder()
              .codeSet(a1a2CodeSet)
              .build());

      List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
      statusEvents.add(CertificationStatusEvent.builder()
                      .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2025-03-13")))
                      .status(CertificationStatus.builder()
                              .id(1L)
                              .name(CertificationStatusType.Active.getName())
                              .build())
                      .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(9L)
                                .number("170.315 (a)(9)")
                                .startDay(LocalDate.parse("2024-01-01"))
                                .build())
                        .success(true)
                        .codeSets(codeSets)
                        .build())
                .build();
        reviewer.review(listing);

        assertTrue(CollectionUtils.isEmpty(listing.getCertificationResults().get(0).getCodeSets()));
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CODE_SET_NOT_VALID_FOR_CRITERION,
                a1a2CodeSet.getName(),
                "170.315 (a)(9)")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_invalidCodeSetsAppliedToRemovedCriteria_noErrorsOrWarnings() {
      List<CertificationResultCodeSet> codeSets = new ArrayList<CertificationResultCodeSet>();
      codeSets.add(CertificationResultCodeSet.builder()
                  .codeSet(a1a2CodeSet)
                  .build());
      codeSets.add(CertificationResultCodeSet.builder()
                  .codeSet(a5CodeSet)
                  .build());

      List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
      statusEvents.add(CertificationStatusEvent.builder()
                      .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-03-13")))
                      .status(CertificationStatus.builder()
                              .id(1L)
                              .name(CertificationStatusType.Active.getName())
                              .build())
                      .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(10L)
                                .number("170.315 (a)(10)")
                                .startDay(LocalDate.parse("2020-06-30"))
                                .endDay(LocalDate.parse("2021-01-01"))
                                .build())
                        .success(true)
                        .codeSets(codeSets)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_codeSetWithoutId_hasWarningAndCodeSetsRemoved() {
      List<CertificationResultCodeSet> codeSets = new ArrayList<CertificationResultCodeSet>();
      codeSets.add(CertificationResultCodeSet.builder()
                  .codeSet(CodeSet.builder()
                          .startDay(LocalDate.parse("2020-01-01"))
                          .requiredDay(LocalDate.parse("2024-01-01"))
                          .build())
                  .build());

      List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
      statusEvents.add(CertificationStatusEvent.builder()
                      .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-03-13")))
                      .status(CertificationStatus.builder()
                              .id(1L)
                              .name(CertificationStatusType.Active.getName())
                              .build())
                      .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .codeSets(codeSets)
                        .build())
                .build();
        reviewer.review(listing);

        assertTrue(CollectionUtils.isEmpty(listing.getCertificationResults().get(0).getCodeSets()));
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CODE_SETS_NOT_FOUND,
                "Jan 2024",
                Util.formatCriteriaNumber(a1))));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_codeSetWithOnlyUserEnteredName_hasWarningAndCodeSetsRemoved() {
      List<CertificationResultCodeSet> codeSets = new ArrayList<CertificationResultCodeSet>();
      codeSets.add(CertificationResultCodeSet.builder()
                  .codeSet(CodeSet.builder()
                          .userEnteredName("Bad CodeSet")
                          .build())
                  .build());

      List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
      statusEvents.add(CertificationStatusEvent.builder()
                      .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-03-13")))
                      .status(CertificationStatus.builder()
                              .id(1L)
                              .name(CertificationStatusType.Active.getName())
                              .build())
                      .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .codeSets(codeSets)
                        .build())
                .build();
        reviewer.review(listing);

        assertTrue(CollectionUtils.isEmpty(listing.getCertificationResults().get(0).getCodeSets()));
        assertTrue(listing.getWarningMessages().contains(String.format(CODE_SETS_NOT_FOUND,
                "Bad CodeSet",
                Util.formatCriteriaNumber(a1))));
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_codeSetWithoutIdForRemovedCriteria_noWarningOrError() {
      List<CertificationResultCodeSet> codeSets = new ArrayList<CertificationResultCodeSet>();
      codeSets.add(CertificationResultCodeSet.builder()
                  .codeSet(CodeSet.builder()
                          .startDay(LocalDate.parse("2020-01-01"))
                          .requiredDay(LocalDate.parse("2024-01-01"))
                          .build())
                  .build());

      List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
      statusEvents.add(CertificationStatusEvent.builder()
                      .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-03-13")))
                      .status(CertificationStatus.builder()
                              .id(1L)
                              .name(CertificationStatusType.Active.getName())
                              .build())
                      .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(10L)
                                .number("170.315 (a)(10)")
                                .startDay(LocalDate.parse("2020-06-30"))
                                .endDay(LocalDate.parse("2021-01-01"))
                                .build())
                        .success(true)
                        .codeSets(codeSets)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_codeSetPastRequiredDayButNotPresent_hasError() {
      List<CertificationResultCodeSet> codeSets = new ArrayList<CertificationResultCodeSet>();
      codeSets.add(CertificationResultCodeSet.builder()
              .codeSet(a5CodeSet)
              .build());

      List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
      statusEvents.add(CertificationStatusEvent.builder()
                      .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-03-13")))
                      .status(CertificationStatus.builder()
                              .id(1L)
                              .name(CertificationStatusType.Active.getName())
                              .build())
                      .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(a5)
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CODE_SET_REQUIRED_NOT_FOUND,
                a5CodeSet.getName(),
                Util.formatCriteriaNumber(a5),
                a5CodeSet.getRequiredDay().toString())));
    }

    @Test
    public void review_codeSetsActiveAfterCertificationDay_hasWarningAndCodeSetsRemoved() {
      List<CertificationResultCodeSet> a1a2CodeSets = new ArrayList<CertificationResultCodeSet>();
      a1a2CodeSets.add(CertificationResultCodeSet.builder()
              .codeSet(a1a2CodeSet)
              .build());

      List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
      statusEvents.add(CertificationStatusEvent.builder()
                      .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-03-13")))
                      .status(CertificationStatus.builder()
                              .id(1L)
                              .name(CertificationStatusType.Active.getName())
                              .build())
                      .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .codeSets(a1a2CodeSets)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(a2)
                        .success(true)
                        .codeSets(a1a2CodeSets)
                        .build())
                .build();
        reviewer.review(listing);

        assertTrue(CollectionUtils.isEmpty(listing.getCertificationResults().get(0).getCodeSets()));
        assertTrue(CollectionUtils.isEmpty(listing.getCertificationResults().get(1).getCodeSets()));
        assertEquals(2, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CODE_SET_NOT_AVAILABLE_BY_START_DATE,
                a1a2CodeSet.getName(),
                Util.formatCriteriaNumber(a1),
                a1a2CodeSet.getStartDay().toString())));
        assertTrue(listing.getWarningMessages().contains(String.format(CODE_SET_NOT_AVAILABLE_BY_START_DATE,
                a1a2CodeSet.getName(),
                Util.formatCriteriaNumber(a2),
                a1a2CodeSet.getStartDay().toString())));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_validCodeSets_noErrorNoWarning() {
      List<CertificationResultCodeSet> a5CodeSets = new ArrayList<CertificationResultCodeSet>();
      a5CodeSets.add(CertificationResultCodeSet.builder()
              .codeSet(a5CodeSet)
              .build());

      List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
      statusEvents.add(CertificationStatusEvent.builder()
                      .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-03-13")))
                      .status(CertificationStatus.builder()
                              .id(1L)
                              .name(CertificationStatusType.Active.getName())
                              .build())
                      .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEvents(statusEvents)
                .certificationResult(CertificationResult.builder()
                        .criterion(a5)
                        .success(true)
                        .codeSets(a5CodeSets)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
