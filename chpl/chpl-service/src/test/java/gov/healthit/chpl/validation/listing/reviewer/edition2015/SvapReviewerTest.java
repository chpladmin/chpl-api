package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.SvapReviewer;

public class SvapReviewerTest {
    private static final String INVALID_EDITION_ERROR_KEY = "listing.criteria.svap.invalidEdition";
    private static final String INVALID_SVAP_CRITERIA_ERROR_KEY = "listing.criteria.svap.invalidCriteria";
    private static final String REMOVED_SVAP_WARNING_KEY = "listing.criteria.svap.removed";
    private static final String INVALID_URL_KEY = "listing.svap.url.invalid";
    private static final String SVAP_REPLACED = "listing.criteria.svap.replaced";

    private SvapDAO svapDao;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil errorMessageUtil;
    private SvapReviewer svapReviewer;
    private CertificationEdition edition2015;

    @Before
    public void before() throws EntityRetrievalException {
        edition2015 = CertificationEdition.builder()
                .id(3L)
                .name("2015")
                .build();

        svapDao = Mockito.mock(SvapDAO.class);
        Mockito.when(svapDao.getAllSvapCriteriaMap())
                .thenReturn(getSvapCriteriaMaps());

        validationUtils = Mockito.mock(ValidationUtils.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(INVALID_EDITION_ERROR_KEY), ArgumentMatchers.any()))
                .thenReturn("Test Error Message 1");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(INVALID_SVAP_CRITERIA_ERROR_KEY), ArgumentMatchers.any()))
                .thenReturn("Test Error Message 2");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(INVALID_URL_KEY), ArgumentMatchers.any()))
                .thenReturn("Test Error Message 3");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(SVAP_REPLACED), ArgumentMatchers.any()))
        .thenReturn("Test Error Message 4");

        svapReviewer = new SvapReviewer(svapDao, validationUtils, errorMessageUtil);
    }

    @Test
    public void review_validSvapAndValidCriterion_NoErrors() {
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails origlisting = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .edition(edition2015)
                .build();

        CertifiedProductSearchDetails updatedlisting = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .svaps(Stream.of(CertificationResultSvap.builder()
                                .svapId(1L)
                                .regulatoryTextCitation("reg1")
                                .approvedStandardVersion("ver1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .edition(edition2015)
                .build();

        svapReviewer.review(origlisting, updatedlisting);

        assertEquals(0, updatedlisting.getErrorMessages().size());
    }

    @Test
    public void review_invalidNotificationUrl_ErrorMessageExists() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .edition(edition2015)
                .svapNoticeUrl("http://www.example.com")
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .svaps(Stream.of(CertificationResultSvap.builder()
                                .svapId(1L)
                                .regulatoryTextCitation("reg1")
                                .approvedStandardVersion("ver1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .edition(edition2015)
                .svapNoticeUrl("bad")
                .build();

        svapReviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_invalidSvapCriterionCombination_ErrorMessageExists() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(2)")
                                .id(2L)
                                .build())
                        .build())
                .edition(edition2015)
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(2)")
                                .id(2L)
                                .build())
                        .svaps(Stream.of(CertificationResultSvap.builder()
                                .svapId(1L)
                                .regulatoryTextCitation("reg1")
                                .approvedStandardVersion("ver1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .edition(edition2015)
                .build();

        svapReviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_invalidEdition_ErrorMessageExists() {
        CertificationEdition edition2014 = CertificationEdition.builder()
                .id(2L)
                .name("2014")
                .build();
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.314 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .edition(edition2014)
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.314 (a)(1)")
                                .id(1L)
                                .build())
                        .svaps(Stream.of(CertificationResultSvap.builder()
                                .svapId(1L)
                                .regulatoryTextCitation("reg1")
                                .approvedStandardVersion("ver1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .edition(edition2014)
                .build();

        svapReviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_addedReplacedSvap_WarningMessageExists() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(2)")
                                .id(2L)
                                .build())
                        .build())
                .edition(edition2015)
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(2)")
                                .id(2L)
                                .build())
                        .svaps(Stream.of(CertificationResultSvap.builder()
                                .svapId(1L)
                                .regulatoryTextCitation("reg1")
                                .approvedStandardVersion("ver1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .edition(edition2015)
                .build();

        svapReviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getWarningMessages().size());

    }

    private List<SvapCriteriaMap> getSvapCriteriaMaps() {
        List<SvapCriteriaMap> map = new ArrayList<SvapCriteriaMap>();

        map.add(SvapCriteriaMap.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .certificationEdition("2015")
                        .certificationEditionId(3L)
                        .number("170.315 (a)(1)")
                        .removed(false)
                        .build())
                .svap(Svap.builder()
                        .svapId(1L)
                        .regulatoryTextCitation("reg1")
                        .approvedStandardVersion("ver1")
                        .replaced(true)
                        .build())
                .build());

        map.add(SvapCriteriaMap.builder()
                .id(2L)
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .certificationEdition("2015")
                        .certificationEditionId(3L)
                        .number("170.315 (a)(1)")
                        .removed(false)
                        .build())
                .svap(Svap.builder()
                        .svapId(2L)
                        .regulatoryTextCitation("reg2")
                        .approvedStandardVersion("ver2")
                        .replaced(false)
                        .build())
                .build());

        map.add(SvapCriteriaMap.builder()
                .id(3L)
                .criterion(CertificationCriterion.builder()
                        .id(2L)
                        .certificationEdition("2015")
                        .certificationEditionId(3L)
                        .number("170.315 (a)(2)")
                        .removed(false)
                        .build())
                .svap(Svap.builder()
                        .svapId(2L)
                        .regulatoryTextCitation("reg2")
                        .approvedStandardVersion("ver2")
                        .replaced(false)
                        .build())
                .build());

        return map;
    }
}
