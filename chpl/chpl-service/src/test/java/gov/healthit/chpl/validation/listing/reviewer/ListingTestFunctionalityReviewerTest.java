package gov.healthit.chpl.validation.listing.reviewer;

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
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.listing.ListingMockUtil;
import gov.healthit.chpl.manager.TestingFunctionalityManager;
import gov.healthit.chpl.manager.impl.TestingFunctionalityManagerImpl;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.TestFunctionality2014Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.TestFunctionality2015Reviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class ListingTestFunctionalityReviewerTest {
    @Autowired
    private ListingMockUtil listingMockUtil;

    @Autowired
    private MessageSource messageSource;

    @Spy
    private TestFunctionalityDAO testFunctionalityDAO;

    @Spy
    private CertificationCriterionDAO certificationCriterionDAO;

    @Spy
    private PracticeTypeDAO practiceTypeDAO;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    @Spy
    private CertificationEditionDAO certificationEditionDAO;

    @Spy
    private TestingFunctionalityManager testFunctionalityManager;

    private TestFunctionality2014Reviewer tfReviewer;

    private TestFunctionality2015Reviewer tfReviewer2015;

    @Before
    public void setup() {
        testFunctionalityManager = new TestingFunctionalityManagerImpl(testFunctionalityDAO);
        MockitoAnnotations.initMocks(this);

        tfReviewer = new TestFunctionality2014Reviewer(testFunctionalityDAO, testFunctionalityManager,
                certificationEditionDAO, msgUtil);

        tfReviewer2015 = new TestFunctionality2015Reviewer(testFunctionalityDAO, testFunctionalityManager,
                certificationEditionDAO, msgUtil);

        Mockito.doReturn("In Criteria 170.314 (a)(6), Test Functionality (a)(6)(11) is for "
                + "other Settings and is not valid for Practice Type Ambulatory.")
            .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.testFunctionalityPracticeTypeMismatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.when(certificationEditionDAO.findAll()).thenReturn(getEditions());

        Mockito.when(testFunctionalityManager.getTestFunctionalityCriteriaMap2014()).thenReturn(getTestFunctionalityCriteriaMap2014());

        Mockito.when(testFunctionalityManager.getTestFunctionalityCriteriaMap2015()).thenReturn(getTestFunctionalityCriteriaMap2015());

        Mockito.when(testFunctionalityDAO.getTestFunctionalityCritieriaMaps()).thenReturn(xxxx());

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "In Criteria %s, Test Functionality %s is for Criteria %s and is not valid for Criteria %s.";
                Object[] args = invocation.getArguments();
                return String.format(message, (String) args[1], (String) args[2], (String) args[3], (String) args[4]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.testFunctionalityCriterionMismatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
    }

    //Case 1: A valid test functionality
    @Test
    public void validateCertifiedProductTestFunctionality() {
        Mockito.when(testFunctionalityDAO.getByNumberAndEdition(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong()))
                .thenReturn(getTestFunctionalityId_7());
        Mockito.when(certificationCriterionDAO.getByNameAndYear(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(getCertificationCriterion_a6());

        CertifiedProductSearchDetails listing = createListing("2014");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = createCertResult("170.314 (a)(6)");
        CertificationResultTestFunctionality crtf = new CertificationResultTestFunctionality();
        crtf.setDescription("No description required");
        crtf.setId(1L);
        crtf.setName("(a)(6)(i)");
        crtf.setTestFunctionalityId(7L);
        crtf.setYear("2014");
        certResult.setTestFunctionality(new ArrayList<CertificationResultTestFunctionality>());
        certResult.getTestFunctionality().add(crtf);
        certResults.add(certResult);
        listing.getCertificationResults().add(certResult);

        tfReviewer.onApplicationEvent(null);
        tfReviewer.review(listing);

        assertFalse(doesTestFunctionalityPracticeTypeErrorMessageExist(listing.getErrorMessages()));
        assertFalse(doesTestFunctionalityCriterionErrorMessageExist(listing.getErrorMessages()));
    }

    //Case 2: An invalid test functionality based on practice type
    @Test
    public void validateCertifiedProductTestFunctionalityPracticeTypeMismatch() throws Exception {
        Mockito.when(testFunctionalityDAO.getByNumberAndEdition(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong()))
                .thenReturn(getTestFunctionalityId_18());
        Mockito.when(certificationCriterionDAO.getByNameAndYear(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(getCertificationCriterion_a6());

        CertifiedProductSearchDetails listing = createListing("2014");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = createCertResult("170.314 (a)(6)");
        CertificationResultTestFunctionality crtf = new CertificationResultTestFunctionality();
        crtf.setDescription("No description required");
        crtf.setId(1L);
        crtf.setName("(a)(6)(ii)");
        crtf.setTestFunctionalityId(18L);
        crtf.setYear("2104");
        certResult.setTestFunctionality(new ArrayList<CertificationResultTestFunctionality>());
        certResult.getTestFunctionality().add(crtf);
        certResults.add(certResult);
        listing.getCertificationResults().add(certResult);

        tfReviewer.onApplicationEvent(null);
        tfReviewer.review(listing);

        assertTrue(doesTestFunctionalityPracticeTypeErrorMessageExist(listing.getErrorMessages()));
    }

    //Case 3: An invalid test functionality based on certifcation criterion
    @Test
    public void validateCertifiedProductTestFunctionalityCertificationCriterionMismatch() {
        Mockito.when(testFunctionalityDAO.getByNumberAndEdition(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong()))
                .thenReturn(getTestFunctionalityId_7());

        Mockito.when(certificationCriterionDAO.getByNameAndYear(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(getCertificationCriterion_a6());

        CertifiedProductSearchDetails listing = createListing("2014");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = createCertResult("170.314 (a)(7)");
        CertificationResultTestFunctionality crtf = new CertificationResultTestFunctionality();
        crtf.setDescription("No description required");
        crtf.setId(1L);
        crtf.setName("(a)(6)(i)");
        crtf.setTestFunctionalityId(18L);
        crtf.setYear("2104");
        certResult.setTestFunctionality(new ArrayList<CertificationResultTestFunctionality>());
        certResult.getTestFunctionality().add(crtf);
        certResults.add(certResult);
        listing.getCertificationResults().add(certResult);

        tfReviewer.onApplicationEvent(null);
        tfReviewer.review(listing);

        assertTrue(doesTestFunctionalityCriterionErrorMessageExist(listing.getErrorMessages()));
    }

    @Test
    public void validate2015ListingTestFunctionality_Valid() {
        Mockito.when(testFunctionalityDAO.getByNumberAndEdition(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong()))
        .thenReturn(getTestFunctionality(34L, "(b)(1)(ii)(A)(5)(1)", "2015"));

        CertifiedProductSearchDetails listing = createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = createCertResult("170.315 (b)(1)");
        CertificationResultTestFunctionality crtf = new CertificationResultTestFunctionality();
        crtf.setDescription("No description required");
        crtf.setId(1L);
        crtf.setName("(b)(1)(ii)(A)(5)");
        crtf.setTestFunctionalityId(34L);
        crtf.setYear("2105");
        certResult.setTestFunctionality(new ArrayList<CertificationResultTestFunctionality>());
        certResult.getTestFunctionality().add(crtf);
        certResults.add(certResult);
        listing.getCertificationResults().add(certResult);

        tfReviewer2015.init();
        tfReviewer2015.review(listing);

        assertFalse(doesTestFunctionalityCriterionErrorMessageExist(listing.getErrorMessages()));
    }

    @Test
    public void validate2015ListingTestFunctionality_Invalid() {
        Mockito.when(testFunctionalityDAO.getByNumberAndEdition(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong()))
        .thenReturn(getTestFunctionality(33L, "(a)(14)(iii)(A)(2)", "2015"));

        CertifiedProductSearchDetails listing = createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = createCertResult("170.315 (b)(1)");
        CertificationResultTestFunctionality crtf = new CertificationResultTestFunctionality();
        crtf.setDescription("No description required");
        crtf.setId(1L);
        crtf.setName("(a)(14)(iii)(A)(2)");
        crtf.setTestFunctionalityId(33L);
        crtf.setYear("2105");
        certResult.setTestFunctionality(new ArrayList<CertificationResultTestFunctionality>());
        certResult.getTestFunctionality().add(crtf);
        certResults.add(certResult);
        listing.getCertificationResults().add(certResult);

        tfReviewer2015.init();
        tfReviewer2015.review(listing);

        assertTrue(doesTestFunctionalityCriterionErrorMessageExist(listing.getErrorMessages()));
    }

    private Boolean doesTestFunctionalityPracticeTypeErrorMessageExist(Set<String> errorMessages) {
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

    private Boolean doesTestFunctionalityCriterionErrorMessageExist(Set<String> errorMessages) {
        for (String error : errorMessages) {
            if (error.contains("In Criteria")
                    && error.contains("Test Functionality")
                    && error.contains("is for Criteria")
                    && error.contains("and is not valid for Criteria")) {
                return true;
            }
        }
        return false;
    }

    private CertifiedProductSearchDetails createListing(final String year) {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        String certDateString = "11-09-2016";
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date inputDate = dateFormat.parse(certDateString);
            listing.setCertificationDate(inputDate.getTime());
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
        listing.setId(1L);
        if (year.equals("2015")) {
            listing.getCertificationEdition().put("name", "2015");
            listing.getCertificationEdition().put("id", "3");
            listing.setChplProductNumber("15.07.07.2642.IC04.36.00.1.160402");
            listing.setPracticeType(null);
        } else if (year.equals("2014")) {
            listing.getCertificationEdition().put("name", "2014");
            listing.getCertificationEdition().put("id", "2");
            listing.setChplProductNumber("14.07.07.2642.IC04.36.00.1.160402");
            listing.getPracticeType().put("name", "Ambulatory");
            listing.getPracticeType().put("id", "1");
            listing.getClassificationType().put("name", "Modular EHR");
        }
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.FALSE);
        listing.setIcs(ics);
        return listing;
    }

    private CertificationResult createCertResult(final String number) {
        CertificationResult certResult = new CertificationResult();
        certResult.setId(1L);
        certResult.setAdditionalSoftware(null);
        certResult.setApiDocumentation(null);
        certResult.setG1Success(false);
        certResult.setG2Success(false);
        certResult.setGap(null);
        certResult.setNumber(number);
        certResult.setPrivacySecurityFramework("Approach 1 Approach 2");
        certResult.setSed(null);
        certResult.setG1Success(false);
        certResult.setG2Success(false);
        certResult.setTestDataUsed(null);
        certResult.setTestFunctionality(null);
        certResult.setTestProcedures(null);
        certResult.setTestStandards(null);
        certResult.setSuccess(true);
        return certResult;
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

        CertificationCriterionDTO cc = new CertificationCriterionDTO();
        cc.setDeleted(false);
        cc.setId(66L);
        cc.setNumber("170.314 (a)(6)");
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

        CertificationCriterionDTO cc = new CertificationCriterionDTO();
        cc.setDeleted(false);
        cc.setId(66L);
        cc.setNumber("170.314 (a)(6)");

        tf.setPracticeType(pt);

        return tf;
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

    private Map<String, List<TestFunctionalityDTO>> getTestFunctionalityCriteriaMap2015() {
        Map<String, List<TestFunctionalityDTO>> map = new HashMap<String, List<TestFunctionalityDTO>>();

        List<TestFunctionalityDTO> tfs = new ArrayList<TestFunctionalityDTO>();
        tfs.add(getTestFunctionality(34L, "(b)(1)(ii)(A)(5)(i)", "2015"));
        tfs.add(getTestFunctionality(35L, "(b)(1)(ii)(A)(5)(ii)", "2015"));
        tfs.add(getTestFunctionality(53L, "(b)(1)(iii)(G)(1)(ii)", "2015"));
        tfs.add(getTestFunctionality(73L, "(b)(1)(iii)(F)", "2015"));
        tfs.add(getTestFunctionality(63L, "(b)(1)(iii)(E)", "2015"));
        tfs.add(getTestFunctionality(57L, "170.102(13)(ii)(C)", "2015"));
        tfs.add(getTestFunctionality(58L, "170.102(19)(i)", "2015"));
        tfs.add(getTestFunctionality(58L, "170.102(19)(ii)", "2015"));

        map.put("170.315 (b)(1)", tfs);

        return map;
    }

    private List<TestFunctionalityCriteriaMapDTO> xxxx() {
        List<TestFunctionalityCriteriaMapDTO> maps = new ArrayList<TestFunctionalityCriteriaMapDTO>();
        Long id = 1L;

        for (List<TestFunctionalityDTO> dtos : getTestFunctionalityCriteriaMap2015().values()) {
            for (TestFunctionalityDTO dto : dtos) {
                TestFunctionalityCriteriaMapDTO map = new TestFunctionalityCriteriaMapDTO();
                CertificationCriterionDTO cc = new CertificationCriterionDTO();
                cc.setDeleted(false);
                cc.setId(16L);
                cc.setNumber("170.315 (b)(1)");
                cc.setCertificationEdition("2015");

                map.setCriteria(cc);
                map.setId(id);
                map.setTestFunctionality(dto);

                id++;

                maps.add(map);
            }
        }
        return maps;
    }

    private TestFunctionalityDTO getTestFunctionality(Long id, String number, String edition) {
        TestFunctionalityDTO tf = new TestFunctionalityDTO();
        tf.setId(id);
        tf.setName(number);
        tf.setYear(edition);

        return tf;
    }
}
