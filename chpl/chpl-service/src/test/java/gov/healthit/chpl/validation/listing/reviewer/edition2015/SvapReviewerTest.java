package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.SvapReviewer;

public class SvapReviewerTest {
    private static final String INVALID_EDITION_ERROR_KEY = "listing.criteria.svap.invalidEdition";
    private static final String INVALID_SVAP_CRITERIA_ERROR_KEY = "listing.criteria.svap.invalidCriteria";

    private SvapDAO svapDao;
    private ErrorMessageUtil errorMessageUtil;
    private SvapReviewer svapReviewer;

    @Before
    public void before() throws EntityRetrievalException {
        svapDao = Mockito.mock(SvapDAO.class);
        Mockito.when(svapDao.getAllSvapCriteriaMap())
                .thenReturn(getSvapCriteriaMaps());

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(INVALID_EDITION_ERROR_KEY))
                .thenReturn("Test Error Message 1");
        Mockito.when(errorMessageUtil.getMessage(INVALID_SVAP_CRITERIA_ERROR_KEY))
                .thenReturn("Test Error Message 2");

        svapReviewer = new SvapReviewer(svapDao, errorMessageUtil);
        svapReviewer.init();

    }

    @Test
    public void review_validSvapAndValidCriterion_NoErrors() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .svap(CertificationResultSvap.builder()
                                .svapId(1L)
                                .regulatoryTextCitation("reg1")
                                .approvedStandardVersion("ver1")
                                .build())
                        .build())
                .certificationEdition(certEdition)
                .build();

        svapReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_invalidSvapCriterionCombination_ErrorMessageExists() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(2)")
                                .id(2L)
                                .build())
                        .svap(CertificationResultSvap.builder()
                                .svapId(1L)
                                .regulatoryTextCitation("reg1")
                                .approvedStandardVersion("ver1")
                                .build())
                        .build())
                .certificationEdition(certEdition)
                .build();

        svapReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
    }

    @Test
    public void review_invalidEdition_ErrorMessageExists() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 2L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2014");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.314 (a)(1)")
                                .id(1L)
                                .build())
                        .svap(CertificationResultSvap.builder()
                                .svapId(1L)
                                .regulatoryTextCitation("reg1")
                                .approvedStandardVersion("ver1")
                                .build())
                        .build())
                .certificationEdition(certEdition)
                .build();

        svapReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
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
                        .build())
                .build());

        return map;
    }
}
