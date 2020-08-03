package gov.healthit.chpl.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dao.CuresUpdateEventDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.MeaningfulUseUserDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestParticipantDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestTaskDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.TransparencyAttestation;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CuresUpdateService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;


public class CertifiedProductManagerTest {

    private ErrorMessageUtil msgUtil;
    private CertifiedProductDAO cpDao;
    private CertifiedProductSearchDAO searchDao;
    private CertificationResultDAO certDao;
    private CertificationCriterionDAO certCriterionDao;
    private QmsStandardDAO qmsDao;
    private TargetedUserDAO targetedUserDao;
    private AccessibilityStandardDAO asDao;
    private CertifiedProductQmsStandardDAO cpQmsDao;
    private CertifiedProductTestingLabDAO cpTestingLabDao;
    private CertifiedProductTargetedUserDAO cpTargetedUserDao;
    private CertifiedProductAccessibilityStandardDAO cpAccStdDao;
    private CQMResultDAO cqmResultDAO;
    private CQMCriterionDAO cqmCriterionDao;
    private TestingLabDAO atlDao;
    private DeveloperDAO developerDao;
    private DeveloperStatusDAO devStatusDao;
    private DeveloperManager developerManager;
    private ProductManager productManager;
    private ProductVersionManager versionManager;
    private CertificationStatusEventDAO statusEventDao;
    private CuresUpdateEventDAO curesUpdateDao;
    private MeaningfulUseUserDAO muuDao;
    private CertificationResultManager certResultManager;
    private TestToolDAO testToolDao;
    private TestStandardDAO testStandardDao;
    private TestProcedureDAO testProcDao;
    private TestDataDAO testDataDao;
    private TestFunctionalityDAO testFuncDao;
    private UcdProcessDAO ucdDao;
    private TestParticipantDAO testParticipantDao;
    private TestTaskDAO testTaskDao;
    private CertificationStatusDAO certStatusDao;
    private ListingGraphDAO listingGraphDao;
    private FuzzyChoicesDAO fuzzyChoicesDao;
    private ResourcePermissions resourcePermissions;
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private ActivityManager activityManager;
    private ListingValidatorFactory validatorFactory;
    private CuresUpdateService curesUpdateService;
    private FF4j ff4j;

    private CertifiedProductManager certifiedProductManager;

    @Before
    public void before() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        cpDao = Mockito.mock(CertifiedProductDAO.class);
        searchDao = Mockito.mock(CertifiedProductSearchDAO.class);
        certDao = Mockito.mock(CertificationResultDAO.class);
        certCriterionDao = Mockito.mock(CertificationCriterionDAO.class);
        qmsDao = Mockito.mock(QmsStandardDAO.class);
        targetedUserDao = Mockito.mock(TargetedUserDAO.class);
        asDao = Mockito.mock(AccessibilityStandardDAO.class);
        cpQmsDao = Mockito.mock(CertifiedProductQmsStandardDAO.class);
        cpTestingLabDao = Mockito.mock(CertifiedProductTestingLabDAO.class);
        cpTargetedUserDao = Mockito.mock(CertifiedProductTargetedUserDAO.class);
        cpAccStdDao = Mockito.mock(CertifiedProductAccessibilityStandardDAO.class);
        cqmResultDAO = Mockito.mock(CQMResultDAO.class);
        cqmCriterionDao = Mockito.mock(CQMCriterionDAO.class);
        atlDao = Mockito.mock(TestingLabDAO.class);
        developerDao = Mockito.mock(DeveloperDAO.class);
        devStatusDao = Mockito.mock(DeveloperStatusDAO.class);
        developerManager = Mockito.mock(DeveloperManager.class);
        productManager = Mockito.mock(ProductManager.class);
        versionManager = Mockito.mock(ProductVersionManager.class);
        statusEventDao = Mockito.mock(CertificationStatusEventDAO.class);
        curesUpdateDao = Mockito.mock(CuresUpdateEventDAO.class);
        muuDao = Mockito.mock(MeaningfulUseUserDAO.class);
        certResultManager = Mockito.mock(CertificationResultManager.class);
        testToolDao = Mockito.mock(TestToolDAO.class);
        testStandardDao = Mockito.mock(TestStandardDAO.class);
        testProcDao = Mockito.mock(TestProcedureDAO.class);
        testDataDao = Mockito.mock(TestDataDAO.class);
        testFuncDao = Mockito.mock(TestFunctionalityDAO.class);
        ucdDao = Mockito.mock(UcdProcessDAO.class);
        testParticipantDao = Mockito.mock(TestParticipantDAO.class);
        testTaskDao = Mockito.mock(TestTaskDAO.class);
        certStatusDao = Mockito.mock(CertificationStatusDAO.class);
        listingGraphDao = Mockito.mock(ListingGraphDAO.class);
        fuzzyChoicesDao = Mockito.mock(FuzzyChoicesDAO.class);
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        certifiedProductSearchResultDAO = Mockito.mock(CertifiedProductSearchResultDAO.class);
        certifiedProductDetailsManager = Mockito.mock(CertifiedProductDetailsManager.class);
        activityManager = Mockito.mock(ActivityManager.class);
        validatorFactory = Mockito.mock(ListingValidatorFactory.class);
        curesUpdateService = Mockito.mock(CuresUpdateService.class);
        ff4j = Mockito.mock(FF4j.class);

        certifiedProductManager = new  CertifiedProductManager(msgUtil, cpDao,  searchDao, certDao,
                certCriterionDao, qmsDao,  targetedUserDao, asDao,  cpQmsDao, cpTestingLabDao,
                cpTargetedUserDao, cpAccStdDao,  cqmResultDAO, cqmCriterionDao,  atlDao,
                developerDao,  devStatusDao, developerManager,  productManager, versionManager,
                statusEventDao, curesUpdateDao, muuDao,  certResultManager, testToolDao,  testStandardDao,
                testProcDao,  testDataDao, testFuncDao,  ucdDao, testParticipantDao,  testTaskDao, certStatusDao,
                listingGraphDao, fuzzyChoicesDao,  resourcePermissions, certifiedProductSearchResultDAO,
                certifiedProductDetailsManager, activityManager,  validatorFactory, curesUpdateService,  ff4j);
    }

    @Test
    public void update_HasValidationWarningsAndNoAck_ThrowsValidationException() throws EntityRetrievalException {
        Mockito.when(certifiedProductDetailsManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
        .thenReturn(new CertifiedProductSearchDetails());


    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails() {
        return CertifiedProductSearchDetails.builder()
                .id(10048L)
                .acbCertificationId("Cert ID")
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .id(931L)
                        .accessibilityStandardId(8L)
                        .accessibilityStandardName("None")
                        .build())
                .certificationDate(1558065600000L)
                .certificationEdition(getCertificationEdition())
                .certificationEvent(CertificationStatusEvent.builder()
                        .eventDate(1558065600000L)
                        .id(18267L)
                        .status(CertificationStatus.builder()
                                .id(1l)
                                .name("Active")
                                .build())
                        .build())
                .certifyingBody(getCertifyingBody())
                .chplProductNumber("15.04.04.3046.Acel.11.01.0.190517")
                .curesUpdate(false)
                .countCerts(14)
                .countClosedNonconformities(0)
                .countClosedSurveillance(0)
                .countCqms(0)
                .countOpenNonconformities(0)
                .countSurveillance(0)
                .developer(Developer.builder()
                        .developerId(2047L)
                        .address(Address.builder()
                                .addressId(546L)
                                .city("Westport")
                                .country("US")
                                .line1("49 Richmondville, Ste 307")
                                .state("CT")
                                .zipcode("68800")
                                .build())
                        .contact(Contact.builder()
                                .contactId(1270L)
                                .email("fake@email.com")
                                .fullName("Chris Ulisse")
                                .phoneNumber("555-555-5555")
                                .build())
                        .developerCode("3046")
                        .name("Acelis Connected Health Technologies")
                        .status(DeveloperStatus.builder()
                                .id(1L)
                                .status("Active")
                                .build())
                        .statusEvent(DeveloperStatusEvent.builder()
                                .developerId(2047L)
                                .id(1L)
                                .status(DeveloperStatus.builder()
                                        .id(1L)
                                        .status("Active")
                                        .build())
                                .statusDate(new Date(1562005630961L))
                                .build())
                        .build())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .parent(CertifiedProduct.builder()
                                .certificationDate(1517720400000L)
                                .chplProductNumber("15.04.04.2853.Aler.10.00.0.180204")
                                .edition("2015")
                                .id(9445L)
                                .build())
                        .build())
                .product(Product.builder()
                        .productId(3086L)
                        .name("Acelis Connected Health eSuite")
                        .owner(Developer.builder()
                                .build())
                        .build())
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(2246L)
                        .applicableCriteria("a5,a7,a8,a11,d1,d2,d3,d4,d5,d6,d7")
                        .qmsModification("")
                        .qmsStandardId(3L)
                        .qmsStandardName("ISO 14971")
                        .build())
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(2247L)
                        .applicableCriteria("a5,a7,a8,a11,d1,d2,d3,d4,d5,d6,d7")
                        .qmsModification("")
                        .qmsStandardId(1L)
                        .qmsStandardName("21 CFR Part 820")
                        .build())
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(2248L)
                        .applicableCriteria("a5,a7,a8,a11,d1,d2,d3,d4,d5,d6,d7")
                        .qmsModification("")
                        .qmsStandardId(4L)
                        .qmsStandardName("ISO 13485")
                        .build())
                .qmsStandard(CertifiedProductQmsStandard.builder()
                        .id(2249L)
                        .applicableCriteria("a5,a7,a8,a11,d1,d2,d3,d4,d5,d6,d7")
                        .qmsModification("")
                        .qmsStandardId(5L)
                        .qmsStandardName("IEC 62304")
                        .build())
                .sed(CertifiedProductSed.builder()
                        .testTask(TestTask.builder()
                                .id(15163L)
                                .criterion(CertificationCriterion.builder()
                                        .id(5L)
                                        .certificationEdition("2015")
                                        .certificationEditionId(3L)
                                        .number("170.315 (a)(5)")
                                        .title("Demographics")
                                        .build())
                                .description("Search and Select a Patient")
                                .taskErrors(0F)
                                .taskErrorsStddev(0F)
                                .taskPathDeviationObserved(3)
                                .taskPathDeviationOptimal(4)
                                .taskRating(5F)
                                .taskRatingScale("Likert")
                                .taskRatingStddev(0F)
                                .taskSuccessAverage(100F)
                                .taskSuccessStddev(0F)
                                .taskTimeAvg(7L)
                                .taskTimeDeviationObservedAvg(5)
                                .taskTimeDeviationOptimalAvg(5)
                                .testParticipant(TestParticipant.builder()
                                        .id(112432L)
                                        .ageRange("40-49")
                                        .ageRangeId(5L)
                                        .assistiveTechnologyNeeds("No")
                                        .computerExperienceMonths(240)
                                        .educationTypeId(7L)
                                        .educationTypeName("Master's Degree")
                                        .gender("Female")
                                        .occupation("RN")
                                        .productExperienceMonths(144)
                                        .professionalExperienceMonths(240)
                                        .build())
                                .testParticipant(TestParticipant.builder()
                                        .id(112433L)
                                        .ageRange("50-59")
                                        .ageRangeId(6L)
                                        .assistiveTechnologyNeeds("No")
                                        .computerExperienceMonths(240)
                                        .educationTypeId(9L)
                                        .educationTypeName("Doctorate degree (e.g., MD, DNP, DMD, PhD)")
                                        .gender("Male")
                                        .occupation("PharmD")
                                        .productExperienceMonths(120)
                                        .professionalExperienceMonths(340)
                                        .build())
                                .testParticipant(TestParticipant.builder()
                                        .id(112434L)
                                        .ageRange("40-49")
                                        .ageRangeId(5L)
                                        .assistiveTechnologyNeeds("No")
                                        .computerExperienceMonths(240)
                                        .educationTypeId(5L)
                                        .educationTypeName("Associate Degree")
                                        .gender("Female")
                                        .occupation("Clinical Pharmacist")
                                        .productExperienceMonths(60)
                                        .professionalExperienceMonths(240)
                                        .build())
                                .testParticipant(TestParticipant.builder()
                                        .id(12435L)
                                        .ageRange("60-69")
                                        .ageRangeId(7L)
                                        .assistiveTechnologyNeeds("No")
                                        .computerExperienceMonths(240)
                                        .educationTypeId(6L)
                                        .educationTypeName("Bachelor's Degree")
                                        .gender("Female")
                                        .occupation("Clinical Specialist")
                                        .productExperienceMonths(66)
                                        .professionalExperienceMonths(480)
                                        .build())
                                .testParticipant(TestParticipant.builder()
                                        .id(12436L)
                                        .ageRange("50-59")
                                        .ageRangeId(6L)
                                        .assistiveTechnologyNeeds("No")
                                        .computerExperienceMonths(240)
                                        .educationTypeId(6L)
                                        .educationTypeName("Bachelor's Degree")
                                        .gender("Female")
                                        .occupation("Pharmacy Manager")
                                        .productExperienceMonths(144)
                                        .professionalExperienceMonths(360)
                                        .build())
                                .testParticipant(TestParticipant.builder()
                                        .id(12437L)
                                        .ageRange("40-49")
                                        .ageRangeId(5L)
                                        .assistiveTechnologyNeeds("No")
                                        .computerExperienceMonths(240)
                                        .educationTypeId(9L)
                                        .educationTypeName("Doctorate degree (e.g., MD, DNP, DMD, PhD)")
                                        .gender("Male")
                                        .occupation("PharmD")
                                        .productExperienceMonths(72)
                                        .professionalExperienceMonths(240)
                                        .build())
                                .testParticipant(TestParticipant.builder()
                                        .id(12438L)
                                        .ageRange("50-59")
                                        .ageRangeId(6L)
                                        .assistiveTechnologyNeeds("No")
                                        .computerExperienceMonths(240)
                                        .educationTypeId(7L)
                                        .educationTypeName("Master's degree")
                                        .gender("Female")
                                        .occupation("RN")
                                        .productExperienceMonths(48)
                                        .professionalExperienceMonths(360)
                                        .build())
                                .testParticipant(TestParticipant.builder()
                                        .id(12439L)
                                        .ageRange("30-39")
                                        .ageRangeId(4L)
                                        .assistiveTechnologyNeeds("No")
                                        .computerExperienceMonths(180)
                                        .educationTypeId(6L)
                                        .educationTypeName("Bachelor's degree")
                                        .gender("Female")
                                        .occupation("Operations Analyst")
                                        .productExperienceMonths(42)
                                        .professionalExperienceMonths(120)
                                        .build())
                                .testParticipant(TestParticipant.builder()
                                        .id(12430L)
                                        .ageRange("50-59")
                                        .ageRangeId(6L)
                                        .assistiveTechnologyNeeds("No")
                                        .computerExperienceMonths(240)
                                        .educationTypeId(6L)
                                        .educationTypeName("Bachelor's degree")
                                        .gender("Male")
                                        .occupation("Clinic Manager")
                                        .productExperienceMonths(120)
                                        .professionalExperienceMonths(360)
                                        .build())
                                .testParticipant(TestParticipant.builder()
                                        .id(12431L)
                                        .ageRange("30-39")
                                        .ageRangeId(4L)
                                        .assistiveTechnologyNeeds("No")
                                        .computerExperienceMonths(180)
                                        .educationTypeId(7L)
                                        .educationTypeName("Master's degree")
                                        .gender("Female")
                                        .occupation("RN")
                                        .productExperienceMonths(24)
                                        .professionalExperienceMonths(120)
                                        .build())
                                .build())
                        .ucdProcess(UcdProcess.builder()
                                .id(6L)
                                .criterion(CertificationCriterion.builder()
                                        .id(5L)
                                        .certificationEdition("2015")
                                        .certificationEditionId(3L)
                                        .number("170.315 (a)(5)")
                                        .title("Demographics")
                                        .build())
                                .criterion(CertificationCriterion.builder()
                                        .id(7L)
                                        .certificationEdition("2015")
                                        .certificationEditionId(3L)
                                        .number("170.315 (a)(7)")
                                        .title("Medication List")
                                        .build())
                                .criterion(CertificationCriterion.builder()
                                        .id(8L)
                                        .certificationEdition("2015")
                                        .certificationEditionId(3L)
                                        .number("170.315 (a)(8)")
                                        .title("Medication Allergy List")
                                        .build())
                                .details("NISTIR 7741 was used.")
                                .name("NISTIR 7741")
                                .build())
                        .build())
                .sedIntendedUserDescription("Outpatient Clinic")
                .sedReportFileLocation("https://drummondgroup.com/wp-content/uploads/2018/01/NISTIR-7742-Usability-Test-Report-Standing-Stone-FINAL.pdf")
                .sedTestingEndDate(new Date(1516597200000L))
                .targetedUser(CertifiedProductTargetedUser.builder()
                        .id(711L)
                        .targetedUserId(5L)
                        .targetedUserName("Ambulatory")
                        .build())
                .testingLab(CertifiedProductTestingLab.builder()
                        .id(2342L)
                        .testingLabCode("04")
                        .testingLabId(1L)
                        .testingLabName("Drummond Group")
                        .build())
                .transparencyAttestation(TransparencyAttestation.builder()
                        .removed(true)
                        .transparencyAttestation("http://www.standingstoneinc.com/")
                        .build())
                .version(ProductVersion.builder()
                        .versionId(7801L)
                        .version("11.3")
                        .build())
                .build();
    }

    private Map<String, Object> getCertificationEdition() {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put("name", "2015");
        edition.put("id", 3l);
        return edition;
    }

    private Map<String, Object> getCertifyingBody() {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put("id", 3L);
        acb.put("acbCode", "O4");
        acb.put("name", "Drummond Group");
        return acb;
    }

}
