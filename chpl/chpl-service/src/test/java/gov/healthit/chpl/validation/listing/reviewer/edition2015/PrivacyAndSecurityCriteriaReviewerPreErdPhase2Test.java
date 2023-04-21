package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class PrivacyAndSecurityCriteriaReviewerPreErdPhase2Test {

    private CertificationCriterionDAO certificationCriterionDao;
    private Environment env;
    private ErrorMessageUtil errorMessageUtil;
    private ValidationUtils validationUtils;

    @Before
    public void before() throws EntityRetrievalException {
        certificationCriterionDao = Mockito.mock(CertificationCriterionDAO.class);
        Mockito.when(certificationCriterionDao.getById(1L)).thenReturn(getCriterionDTO(1l, "170.315 (a)(1)", false));
        Mockito.when(certificationCriterionDao.getById(2L)).thenReturn(getCriterionDTO(2l, "170.315 (a)(2)", false));
        Mockito.when(certificationCriterionDao.getById(166L)).thenReturn(getCriterionDTO(166l, "170.315 (d)(12)", false));
        Mockito.when(certificationCriterionDao.getById(167L)).thenReturn(getCriterionDTO(167l, "170.315 (d)(13)", false));

        env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty("privacyAndSecurityCriteria")).thenReturn("1,2");
        Mockito.when(env.getProperty("privacyAndSecurityRequiredCriteria")).thenReturn("166,167");

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.dependentCriteriaRequired"), ArgumentMatchers.any()))
                .thenReturn("Test error message!");

        validationUtils = new ValidationUtils(Mockito.mock(CertificationCriterionService.class));
    }

    @Test
    public void review_NotAnActiveOrSuspendedStatus_NoErrorMessages() {
        // Updated Listing
        // 1: Certification Status Type is Retired | 2: All parent properties are non-null
        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();
        updatedListing.setCertificationEvents(getCertificationStatusEvents(CertificationStatusType.Retired.toString()));

        PrivacyAndSecurityCriteriaReviewerPreErdPhase2 reviewer = new PrivacyAndSecurityCriteriaReviewerPreErdPhase2(certificationCriterionDao,
                env, errorMessageUtil, validationUtils);
        reviewer.postConstruct();

        // Test
        reviewer.review(Mockito.mock(CertifiedProductSearchDetails.class), updatedListing);

        assertTrue(updatedListing.getErrorMessages().isEmpty());
    }

    @Test
    public void review_LE3NoCriteriaAdded_NoErrorMessages() {
        // Existing Listing
        // Attests to 170.315 (a)(1), 170.315 (a)(2)
        CertifiedProductSearchDetails existingListing = Mockito.mock(CertifiedProductSearchDetails.class);
        List<CertificationResult> existingListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(1L, true), getCertificationResult(2L, true)));
        Mockito.when(existingListing.getCertificationResults()).thenReturn(existingListingCertificationResults);

        // Updated Listing
        // 1: Certification Status Type is Active | 2: All parent properties are non-null
        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();
        updatedListing.setCertificationEvents(getCertificationStatusEvents(CertificationStatusType.Active.toString()));
        // Attests to 170.315 (a)(1), 170.315 (a)(2)
        List<CertificationResult> updatedListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(1L, true), getCertificationResult(2L, true)));
        updatedListing.setCertificationResults(updatedListingCertificationResults);

        PrivacyAndSecurityCriteriaReviewerPreErdPhase2 reviewer = new PrivacyAndSecurityCriteriaReviewerPreErdPhase2(certificationCriterionDao,
                env, errorMessageUtil, validationUtils);
        reviewer.postConstruct();

        // Test
        reviewer.review(existingListing, updatedListing);

        assertTrue(updatedListing.getErrorMessages().isEmpty());
    }

    @Test
    public void review_RequiredCriteriaAddedInGeneral_NoErrorMessages() {
        // Existing Listing
        // Attests to 170.315 (a)(1), 170.315 (a)(2)
        CertifiedProductSearchDetails existingListing = Mockito.mock(CertifiedProductSearchDetails.class);
        List<CertificationResult> existingListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(1L, true), getCertificationResult(2L, true)));
        Mockito.when(existingListing.getCertificationResults()).thenReturn(existingListingCertificationResults);

        // Updated Listing
        // 1: Certification Status Type is Active | 2: All parent properties are non-null
        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();
        updatedListing.setCertificationEvents(getCertificationStatusEvents(CertificationStatusType.Active.toString()));
        // Attests to 170.315 (a)(1), 170.315 (a)(2), 170.315 (d)(12), 170.315 (d)(13)
        List<CertificationResult> updatedListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(1L, true), getCertificationResult(2L, true),
                        getCertificationResult(166L, true), getCertificationResult(167L, true)));
        updatedListing.setCertificationResults(updatedListingCertificationResults);

        PrivacyAndSecurityCriteriaReviewerPreErdPhase2 reviewer = new PrivacyAndSecurityCriteriaReviewerPreErdPhase2(certificationCriterionDao,
                env, errorMessageUtil, validationUtils);
        reviewer.postConstruct();

        // Test
        reviewer.review(existingListing, updatedListing);

        assertTrue(updatedListing.getErrorMessages().isEmpty());
    }

    @Test
    public void review_AddsCriteriaButDoesNotHaveRequiredCriteria_HasErrorMessages() {
        // Existing Listing
        // Attests to 170.315 (a)(1)
        CertifiedProductSearchDetails existingListing = Mockito.mock(CertifiedProductSearchDetails.class);
        List<CertificationResult> existingListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(1L, true)));
        Mockito.when(existingListing.getCertificationResults()).thenReturn(existingListingCertificationResults);

        // Updated Listing
        // 1: Certification Status Type is Active | 2: All parent properties are non-null
        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();
        updatedListing.setCertificationEvents(getCertificationStatusEvents(CertificationStatusType.Active.toString()));
        // Attests to 170.315 (a)(1), 170.315 (a)(2)
        List<CertificationResult> updatedListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(1L, true), getCertificationResult(2L, true)));
        updatedListing.setCertificationResults(updatedListingCertificationResults);

        PrivacyAndSecurityCriteriaReviewerPreErdPhase2 reviewer = new PrivacyAndSecurityCriteriaReviewerPreErdPhase2(certificationCriterionDao,
                env, errorMessageUtil, validationUtils);
        reviewer.postConstruct();

        // Test
        reviewer.review(existingListing, updatedListing);

        assertFalse(updatedListing.getErrorMessages().isEmpty());
    }

    @Test
    public void review_LE1HasRequiredListCriteriaInExistingAndAddsCriteriaNotFromRequiredListButDoesNotHaveRequiredCriteria_HasErrorMessages() {
        // Existing Listing
        // Attests to 170.315 (a)(1)
        CertifiedProductSearchDetails existingListing = Mockito.mock(CertifiedProductSearchDetails.class);
        List<CertificationResult> existingListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(1L, true)));
        Mockito.when(existingListing.getCertificationResults()).thenReturn(existingListingCertificationResults);

        // Updated Listing
        // 1: Certification Status Type is Active | 2: All parent properties are non-null
        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();
        updatedListing.setCertificationEvents(getCertificationStatusEvents(CertificationStatusType.Active.toString()));
        // Attests to 170.315 (a)(1) (from required list) and adds 1 criteria NOT from the required list
        List<CertificationResult> updatedListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(1L, true), getCertificationResult(6L, true)));
        updatedListing.setCertificationResults(updatedListingCertificationResults);

        PrivacyAndSecurityCriteriaReviewerPreErdPhase2 reviewer = new PrivacyAndSecurityCriteriaReviewerPreErdPhase2(certificationCriterionDao,
                env, errorMessageUtil, validationUtils);
        reviewer.postConstruct();

        // Test
        reviewer.review(existingListing, updatedListing);

        assertFalse(updatedListing.getErrorMessages().isEmpty());
    }

    @Test
    public void review_LE2HasNonRequiredListCriteriaInExistingAndAddsCriteriaFromRequiredListButDoesNotHaveRequiredCriteria_HasErrorMessages() {
        // Existing Listing
        // Attests to 1 criteria NOT from the required list
        CertifiedProductSearchDetails existingListing = Mockito.mock(CertifiedProductSearchDetails.class);
        List<CertificationResult> existingListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(6L, true)));
        Mockito.when(existingListing.getCertificationResults()).thenReturn(existingListingCertificationResults);

        // Updated Listing
        // 1: Certification Status Type is Active | 2: All parent properties are non-null
        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();
        updatedListing.setCertificationEvents(getCertificationStatusEvents(CertificationStatusType.Active.toString()));
        // Attests to 1 criteria NOT from the required list: 6L and adds 1 criteria from the required list: 170.315 (a)(1)
        List<CertificationResult> updatedListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(6L, true), getCertificationResult(1L, true)));
        updatedListing.setCertificationResults(updatedListingCertificationResults);

        PrivacyAndSecurityCriteriaReviewerPreErdPhase2 reviewer = new PrivacyAndSecurityCriteriaReviewerPreErdPhase2(certificationCriterionDao,
                env, errorMessageUtil, validationUtils);
        reviewer.postConstruct();

        // Test
        reviewer.review(existingListing, updatedListing);

        assertFalse(updatedListing.getErrorMessages().isEmpty());
    }

    @Test
    public void review_LE4NoCriteriaAddedAndRemovedRequiredCriteria_NoErrorMessages() {
        // Existing Listing
        // Attests to 170.315 (a)(1), 170.315 (a)(2), 170.315 (d)(12), 170.315 (d)(13)
        CertifiedProductSearchDetails existingListing = Mockito.mock(CertifiedProductSearchDetails.class);
        List<CertificationResult> existingListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(1L, true), getCertificationResult(2L, true),
                        getCertificationResult(166L, true), getCertificationResult(167L, true)));
        Mockito.when(existingListing.getCertificationResults()).thenReturn(existingListingCertificationResults);

        // Updated Listing
        // 1: Certification Status Type is Active | 2: All parent properties are non-null
        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();
        updatedListing.setCertificationEvents(getCertificationStatusEvents(CertificationStatusType.Active.toString()));
        // Attests to 170.315 (a)(1), 170.315 (a)(2)
        List<CertificationResult> updatedListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(1L, true), getCertificationResult(2L, true)));
        updatedListing.setCertificationResults(updatedListingCertificationResults);

        PrivacyAndSecurityCriteriaReviewerPreErdPhase2 reviewer = new PrivacyAndSecurityCriteriaReviewerPreErdPhase2(certificationCriterionDao,
                env, errorMessageUtil, validationUtils);
        reviewer.postConstruct();

        // Test
        reviewer.review(existingListing, updatedListing);

        assertTrue(updatedListing.getErrorMessages().isEmpty());
    }

    @Test
    public void review_LE6NoRequiredCriteriaButNoCriteriaFromRequiredList_NoErrorMessages() {
        // Existing Listing
        // Attests to 1 criteria NOT from the required list
        CertifiedProductSearchDetails existingListing = Mockito.mock(CertifiedProductSearchDetails.class);
        List<CertificationResult> existingListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(6L, true)));
        Mockito.when(existingListing.getCertificationResults()).thenReturn(existingListingCertificationResults);

        // Updated Listing
        // 1: Certification Status Type is Active | 2: All parent properties are non-null
        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();
        updatedListing.setCertificationEvents(getCertificationStatusEvents(CertificationStatusType.Active.toString()));
        // Attests to 2 criteria NOT from the required list (1 new one since existing version)
        List<CertificationResult> updatedListingCertificationResults = new ArrayList<CertificationResult>(Arrays
                .asList(getCertificationResult(6L, true), getCertificationResult(7L, true)));
        updatedListing.setCertificationResults(updatedListingCertificationResults);

        PrivacyAndSecurityCriteriaReviewerPreErdPhase2 reviewer = new PrivacyAndSecurityCriteriaReviewerPreErdPhase2(certificationCriterionDao,
                env, errorMessageUtil, validationUtils);
        reviewer.postConstruct();

        // Test
        reviewer.review(existingListing, updatedListing);

        assertTrue(updatedListing.getErrorMessages().isEmpty());
    }

    private static List<CertificationStatusEvent> getCertificationStatusEvents(String typeName) {
        CertificationStatus certificationStatus = new CertificationStatus();
        certificationStatus.setName(typeName);
        CertificationStatusEvent certificationStatusEvent = new CertificationStatusEvent();
        certificationStatusEvent.setStatus(certificationStatus);
        certificationStatusEvent.setEventDate(1L);
        return new ArrayList<CertificationStatusEvent>(Arrays.asList(certificationStatusEvent));
    }

    private static CertificationResult getCertificationResult(Long id, boolean isSuccess) {
        CertificationResult certificationResult = new CertificationResult();
        certificationResult.setSuccess(isSuccess);
        certificationResult.setCriterion(getCertificationCriterion(id));
        return certificationResult;

    }

    private static CertificationCriterion getCertificationCriterion(Long id) {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setId(id);
        return criterion;
    }

    private CertificationCriterionDTO getCriterionDTO(Long id, String number, boolean removed) {
        return CertificationCriterionDTO.builder()
                .id(id)
                .number(number)
                .removed(removed)
                .build();
    }
}
