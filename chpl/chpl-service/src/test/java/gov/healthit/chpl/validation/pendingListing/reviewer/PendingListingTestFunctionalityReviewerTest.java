package gov.healthit.chpl.validation.pendingListing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.manager.TestingFunctionalityManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.TestFunctionality2014Reviewer;

/**
 * Test for pending test functionality.
 * @author kekey
 *
 */
public class PendingListingTestFunctionalityReviewerTest {
    private static final Long EDITION_2015_ID = 3L;
    private static final Long EDITION_2014_ID = 2L;
    private static final String INVALID_TEST_FUNC_ERROR =
            "Criteria 170.314 (a)(6) contains an invalid test functionality"
                    + " 'Bad test functionality name'. It has been removed from the pending listing.";

    @Autowired
    private MessageSource messageSource;

    @Spy
    private TestFunctionalityDAO testFunctionalityDAO;

    @Spy
    private CertificationCriterionDAO certificationCriterionDAO;

    @Spy
    private PracticeTypeDAO practiceTypeDAO;

    @Spy
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    @Spy
    private CertificationEditionDAO certificationEditionDAO;

    @Spy
    private TestingFunctionalityManager testFunctionalityManager;

    private TestFunctionality2014Reviewer pendingTfReviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        pendingTfReviewer = new TestFunctionality2014Reviewer(testFunctionalityDAO, testFunctionalityManager,
                certificationEditionDAO, msgUtil);

        Mockito.doReturn("In Criteria 170.314 (a)(6), Test Functionality (a)(6)(11) is for "
                + "Criteria other and is not valid for Criteria 170.314 (a)(6).")
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.testFunctionalityCriterionMismatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.doReturn("In Criteria 170.314 (a)(6), Test Functionality (a)(6)(11) is for "
                + "other Settings and is not valid for Practice Type Ambulatory.")
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.testFunctionalityPracticeTypeMismatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.doReturn(INVALID_TEST_FUNC_ERROR)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.testFunctionalityNotFoundAndRemoved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.when(certificationEditionDAO.findAll()).thenReturn(getEditions());

        Mockito.when(testFunctionalityManager.getTestFunctionalityCriteriaMap2014())
        .thenReturn(getTestFunctionalityCriteriaMap2014());
    }

    //Case 1: A valid test functionality
    @Test
    public void validatePendingCertifiedProductTestFunctionality() {
        Mockito.when(testFunctionalityDAO.getByNumberAndEdition(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyLong()))
        .thenReturn(getTestFunctionalityId_7());
        Mockito.when(certificationCriterionDAO.getByNumberAndTitle(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
        .thenReturn(getCertificationCriterion_a6());
        Mockito.when(practiceTypeDAO.getByName(ArgumentMatchers.anyString()))
        .thenReturn(getPracticeType_Ambulatory());

        PendingCertifiedProductDTO listing = createPendingListing("2014");
        PendingCertificationResultDTO certResult = createPendingCertResult("170.314 (a)(6)");
        PendingCertificationResultTestFunctionalityDTO crtf = new PendingCertificationResultTestFunctionalityDTO();
        crtf.setId(1L);
        crtf.setNumber("(a)(6)(i)");
        crtf.setTestFunctionalityId(7L);
        certResult.setTestFunctionality(new ArrayList<PendingCertificationResultTestFunctionalityDTO>());
        certResult.getTestFunctionality().add(crtf);
        listing.getCertificationCriterion().add(certResult);

        pendingTfReviewer.onApplicationEvent(null);
        pendingTfReviewer.review(listing);

        assertFalse(doesTestFunctionalityPracticeTypeErrorMessageExist(listing.getErrorMessages()));
        assertFalse(doesTestFunctionalityCriterionMessageExist(listing.getWarningMessages()));
    }

    //Case 2: An invalid test functionality based on practice type
    @Test
    public void validatePendingCertifiedProductTestFunctionalityPracticeTypeMismatch() {
        Mockito.when(testFunctionalityDAO.getByNumberAndEdition(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyLong()))
        .thenReturn(getTestFunctionalityId_18());
        Mockito.when(certificationCriterionDAO.getByNumberAndTitle(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
        .thenReturn(getCertificationCriterion_a6());
        Mockito.when(practiceTypeDAO.getByName(ArgumentMatchers.anyString()))
        .thenReturn(getPracticeType_Ambulatory());

        PendingCertifiedProductDTO listing = createPendingListing("2014");
        PendingCertificationResultDTO certResult = createPendingCertResult("170.314 (a)(6)");
        PendingCertificationResultTestFunctionalityDTO crtf = new PendingCertificationResultTestFunctionalityDTO();
        crtf.setId(1L);
        crtf.setNumber("(a)(6)(ii)");
        crtf.setTestFunctionalityId(18L);
        certResult.setTestFunctionality(new ArrayList<PendingCertificationResultTestFunctionalityDTO>());
        certResult.getTestFunctionality().add(crtf);
        listing.getCertificationCriterion().add(certResult);

        pendingTfReviewer.onApplicationEvent(null);
        pendingTfReviewer.review(listing);

        assertTrue(doesTestFunctionalityPracticeTypeErrorMessageExist(listing.getErrorMessages()));
    }


    //Case 3: An invalid test functionality based on certification criterion
    @Test
    public void validatePendingCertifiedProductTestFunctionalityCertificationCriterionMismatch() {
        Mockito.when(testFunctionalityDAO.getByNumberAndEdition(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyLong()))
        .thenReturn(getTestFunctionalityId_27());
        Mockito.when(certificationCriterionDAO.getByNumberAndTitle(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
        .thenReturn(getCertificationCriterion_a7());
        Mockito.when(practiceTypeDAO.getByName(ArgumentMatchers.anyString()))
        .thenReturn(getPracticeType_Ambulatory());

        PendingCertifiedProductDTO listing = createPendingListing("2014");
        PendingCertificationResultDTO certResult = createPendingCertResult("170.314 (a)(6)");
        PendingCertificationResultTestFunctionalityDTO crtf = new PendingCertificationResultTestFunctionalityDTO();
        crtf.setId(1L);
        crtf.setNumber("(a)(f)(3)(ii)");
        crtf.setTestFunctionalityId(27L);
        certResult.setTestFunctionality(new ArrayList<PendingCertificationResultTestFunctionalityDTO>());
        certResult.getTestFunctionality().add(crtf);
        listing.getCertificationCriterion().add(certResult);

        pendingTfReviewer.onApplicationEvent(null);
        pendingTfReviewer.review(listing);

        assertTrue(doesTestFunctionalityCriterionMessageExist(listing.getWarningMessages()));
    }

    //A test functionality name that does not exist
    @Test
    public void validatePendingCertifiedProductInvalidTestFunctionalityCatchesException() {
        String invalidTestFuncName = "Bad test functionality name";
        Mockito.when(testFunctionalityDAO.getByNumberAndEdition(ArgumentMatchers.eq(invalidTestFuncName),
                ArgumentMatchers.anyLong()))
        .thenReturn(null);
        Mockito.when(certificationCriterionDAO.getByNumberAndTitle(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
        .thenReturn(getCertificationCriterion_a6());
        Mockito.when(practiceTypeDAO.getByName(ArgumentMatchers.anyString()))
        .thenReturn(getPracticeType_Ambulatory());

        PendingCertifiedProductDTO listing = createPendingListing("2014");
        PendingCertificationResultDTO certResult = createPendingCertResult("170.314 (a)(6)");
        PendingCertificationResultTestFunctionalityDTO crtf = new PendingCertificationResultTestFunctionalityDTO();
        crtf.setId(1L);
        crtf.setNumber(invalidTestFuncName);
        crtf.setTestFunctionalityId(null);
        certResult.setTestFunctionality(new ArrayList<PendingCertificationResultTestFunctionalityDTO>());
        certResult.getTestFunctionality().add(crtf);
        listing.getCertificationCriterion().add(certResult);

        pendingTfReviewer.onApplicationEvent(null);
        pendingTfReviewer.review(listing);

        assertTrue(doesInvalidTestFunctionalityErrorMessageExist(listing.getErrorMessages()));
    }

    private Boolean doesTestFunctionalityPracticeTypeErrorMessageExist(final Set<String> errorMessages) {
        for (String error : errorMessages) {
            if (error.contains("In Criteria")
                    && error.contains("Test Functionality")
                    && error.contains("is for")
                    && error.contains("Settings and is not valid for Practice Type")) {
                return true;
            }
        }
        return false;
    }

    private Boolean doesTestFunctionalityCriterionMessageExist(final Set<String> messages) {
        for (String error : messages) {
            if (error.contains("In Criteria")
                    && error.contains("Test Functionality")
                    && error.contains("is for Criteria")
                    && error.contains("and is not valid for Criteria")) {
                return true;
            }
        }
        return false;
    }

    private Boolean doesInvalidTestFunctionalityErrorMessageExist(final Set<String> errorMessages) {
        for (String error : errorMessages) {
            if (error.equals(INVALID_TEST_FUNC_ERROR)) {
                return true;
            }
        }
        return false;
    }

    private PendingCertifiedProductDTO createPendingListing(final String year) {
        PendingCertifiedProductDTO pendingListing = new PendingCertifiedProductDTO();
        String certDateString = "11-09-2016";
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date inputDate = dateFormat.parse(certDateString);
            pendingListing.setCertificationDate(inputDate);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
        pendingListing.setId(1L);
        pendingListing.setIcs(false);
        pendingListing.setCertificationEdition(year);
        if (year.equals("2015")) {
            pendingListing.setCertificationEditionId(EDITION_2015_ID);
            pendingListing.setUniqueId("15.07.07.2642.IC04.36.00.1.160402");
        } else if (year.equals("2014")) {
            pendingListing.setCertificationEditionId(EDITION_2014_ID);
            pendingListing.setUniqueId("14.07.07.2642.IC04.36.00.1.160402");
            pendingListing.setPracticeType("Ambulatory");
            pendingListing.setProductClassificationName("Modular EHR");
        }
        pendingListing.setPracticeTypeId(1L);
        return pendingListing;
    }

    private PendingCertificationResultDTO createPendingCertResult(final String number) {
        PendingCertificationResultDTO pendingCertResult = new PendingCertificationResultDTO();
        pendingCertResult.setPendingCertifiedProductId(1L);
        pendingCertResult.setId(1L);
        pendingCertResult.setAdditionalSoftware(null);
        pendingCertResult.setApiDocumentation(null);
        pendingCertResult.setG1Success(false);
        pendingCertResult.setG2Success(false);
        pendingCertResult.setGap(null);
        pendingCertResult.getCriterion().setNumber(number);
        pendingCertResult.setPrivacySecurityFramework("Approach 1 Approach 2");
        pendingCertResult.setSed(null);
        pendingCertResult.setG1Success(false);
        pendingCertResult.setG2Success(false);
        pendingCertResult.setTestData(null);
        pendingCertResult.setTestFunctionality(null);
        pendingCertResult.setTestProcedures(null);
        pendingCertResult.setTestStandards(null);
        pendingCertResult.setTestTasks(null);
        pendingCertResult.setMeetsCriteria(true);
        return pendingCertResult;
    }

    private CertificationCriterionDTO getCertificationCriterion_a6() {
        CertificationCriterionDTO cc = new CertificationCriterionDTO();
        cc.setCertificationEdition("2014");
        cc.setCertificationEditionId(2L);
        cc.setDeleted(false);
        cc.setDescription("Medication list");
        cc.setId(66L);
        cc.setNumber("170.314 (a)(6)");
        cc.setTitle("Medication list");

        return cc;
    }

    private CertificationCriterionDTO getCertificationCriterion_a7() {
        CertificationCriterionDTO cc = new CertificationCriterionDTO();
        cc.setCertificationEdition("2014");
        cc.setCertificationEditionId(2L);
        cc.setDeleted(false);
        cc.setDescription("Medication allergy list");
        cc.setId(67L);
        cc.setNumber("170.314 (a)(7)");
        cc.setTitle("Medication allergy list");

        return cc;
    }

    private TestFunctionalityDTO getTestFunctionalityId_18() {
        TestFunctionalityDTO tf = new TestFunctionalityDTO();
        tf.setId(18L);
        tf.setName("(a)(6)(ii)");
        tf.setYear("2014");

        PracticeTypeDTO pt = new PracticeTypeDTO();
        pt.setDeleted(false);
        pt.setId(2L);
        pt.setName("Inpatient");
        pt.setDescription("Inpatient");

        tf.setPracticeType(pt);

        return tf;
    }

    private TestFunctionalityDTO getTestFunctionalityId_7() {
        TestFunctionalityDTO tf = new TestFunctionalityDTO();
        tf.setId(7L);
        tf.setName("(a)(6)(i)");
        tf.setYear("2014");

        PracticeTypeDTO pt = new PracticeTypeDTO();
        pt.setDeleted(false);
        pt.setId(1L);
        pt.setName("Ambulatory");
        pt.setDescription("Ambulatory");

        tf.setPracticeType(pt);

        return tf;
    }

    private TestFunctionalityDTO getTestFunctionalityId_27() {
        TestFunctionalityDTO tf = new TestFunctionalityDTO();
        tf.setId(27L);
        tf.setName("(f)(3)(ii)");
        tf.setYear("2014");

        PracticeTypeDTO pt = new PracticeTypeDTO();
        pt.setDeleted(false);
        pt.setId(1L);
        pt.setName("Ambulatory");
        pt.setDescription("Ambulatory");

        tf.setPracticeType(pt);

        return tf;
    }

    private PracticeTypeDTO getPracticeType_Ambulatory() {
        PracticeTypeDTO pt = new PracticeTypeDTO();
        pt.setDescription("Ambulatory");
        pt.setId(1L);
        pt.setName("Ambulatory");
        return pt;
    }

    private List<CertificationEditionDTO> getEditions() {
        List<CertificationEditionDTO> editions = new ArrayList<CertificationEditionDTO>();
        CertificationEditionDTO edition2011 = new CertificationEditionDTO();
        edition2011.setId(1L);
        edition2011.setYear("2011");
        editions.add(edition2011);

        CertificationEditionDTO edition2014 = new CertificationEditionDTO();
        edition2014.setId(2L);
        edition2014.setYear("2014");
        editions.add(edition2014);

        CertificationEditionDTO edition2015 = new CertificationEditionDTO();
        edition2015.setId(3L);
        edition2015.setYear("2015");
        editions.add(edition2015);

        return editions;
    }

    private Map<String, List<TestFunctionalityDTO>> getTestFunctionalityCriteriaMap2014() {
        Map<String, List<TestFunctionalityDTO>> map = new HashMap<String, List<TestFunctionalityDTO>>();

        List<TestFunctionalityDTO> tfs = new ArrayList<TestFunctionalityDTO>();
        tfs.add(getTestFunctionalityId_18());
        tfs.add(getTestFunctionalityId_7());

        map.put("170.314 (a)(6)", tfs);

        return map;
    }
}

