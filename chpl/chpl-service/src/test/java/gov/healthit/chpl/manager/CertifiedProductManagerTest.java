package gov.healthit.chpl.manager;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.accessibilityStandard.AccessibilityStandardDAO;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dao.CuresUpdateEventDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.PromotingInteroperabilityUserDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.CertifiedProductUpdateException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.notifier.ChplTeamNotifier;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.qmsStandard.QmsStandardDAO;
import gov.healthit.chpl.service.CuresUpdateService;
import gov.healthit.chpl.sharedstore.listing.ListingIcsSharedStoreHandler;
import gov.healthit.chpl.upload.listing.normalizer.ListingDetailsNormalizer;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.Validator;


public class CertifiedProductManagerTest {
    private static final long EDITION_2015_ID = 3L;
    private static final long DRUMMOND_ACB_ID = 3L;

    private ErrorMessageUtil msgUtil;
    private CertifiedProductDAO cpDao;
    private CertificationResultDAO certDao;
    private CertificationCriterionDAO certCriterionDao;
    private QmsStandardDAO qmsDao;
    private TargetedUserDAO targetedUserDao;
    private AccessibilityStandardDAO asDao;
    private CertifiedProductQmsStandardDAO cpQmsDao;
    private ListingMeasureDAO cpMeasureDao;
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
    private PromotingInteroperabilityUserDAO piuDao;
    private CertificationResultManager certResultManager;
    private CertificationStatusDAO certStatusDao;
    private ListingGraphDAO listingGraphDao;
    private ResourcePermissions resourcePermissions;
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private ActivityManager activityManager;
    private ListingValidatorFactory validatorFactory;
    private CuresUpdateService curesUpdateService;

    private CertifiedProductManager certifiedProductManager;

    @Before
    public void before() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        cpDao = Mockito.mock(CertifiedProductDAO.class);
        certDao = Mockito.mock(CertificationResultDAO.class);
        certCriterionDao = Mockito.mock(CertificationCriterionDAO.class);
        qmsDao = Mockito.mock(QmsStandardDAO.class);
        targetedUserDao = Mockito.mock(TargetedUserDAO.class);
        asDao = Mockito.mock(AccessibilityStandardDAO.class);
        cpQmsDao = Mockito.mock(CertifiedProductQmsStandardDAO.class);
        cpMeasureDao = Mockito.mock(ListingMeasureDAO.class);
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
        piuDao = Mockito.mock(PromotingInteroperabilityUserDAO.class);
        certResultManager = Mockito.mock(CertificationResultManager.class);
        certStatusDao = Mockito.mock(CertificationStatusDAO.class);
        listingGraphDao = Mockito.mock(ListingGraphDAO.class);
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        certifiedProductSearchResultDAO = Mockito.mock(CertifiedProductSearchResultDAO.class);
        certifiedProductDetailsManager = Mockito.mock(CertifiedProductDetailsManager.class);
        activityManager = Mockito.mock(ActivityManager.class);
        validatorFactory = Mockito.mock(ListingValidatorFactory.class);
        curesUpdateService = Mockito.mock(CuresUpdateService.class);

        certifiedProductManager = new  CertifiedProductManager(msgUtil, cpDao, certDao,
                certCriterionDao, qmsDao,  targetedUserDao, asDao,  cpQmsDao, cpMeasureDao, cpTestingLabDao,
                cpTargetedUserDao, cpAccStdDao,  cqmResultDAO, cqmCriterionDao,  atlDao,
                developerDao,  devStatusDao, developerManager,  productManager, versionManager,
                statusEventDao, curesUpdateDao, piuDao,  certResultManager, certStatusDao,
                listingGraphDao, resourcePermissions, certifiedProductSearchResultDAO,
                certifiedProductDetailsManager,
                Mockito.mock(SchedulerManager.class),
                activityManager, Mockito.mock(ListingDetailsNormalizer.class),
                validatorFactory, curesUpdateService,
                Mockito.mock(ListingIcsSharedStoreHandler.class),
                Mockito.mock(ChplTeamNotifier.class),
                Mockito.mock(Environment.class),
                Mockito.mock(ChplHtmlEmailBuilder.class));
    }

    @Test(expected = ValidationException.class)
    public void update_HasValidationWarningsAndNoAck_ThrowsValidationException()
            throws EntityRetrievalException, AccessDeniedException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException, IOException, ValidationException, MissingReasonException, CertifiedProductUpdateException {

        Mockito.when(certifiedProductDetailsManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
                .thenReturn(getCertifiedProductSearchDetails());

        Validator validator = Mockito.mock(Validator.class);
        Mockito.when(validatorFactory.getValidator(ArgumentMatchers.any(CertifiedProductSearchDetails.class)))
                .thenReturn(validator);

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                CertifiedProductSearchDetails listing = (CertifiedProductSearchDetails) invocation.getArgument(1);
                listing.addWarningMessage("This is a test warning");
                return null;
            }
         }).when(validator).validate(ArgumentMatchers.any(CertifiedProductSearchDetails.class),
                 ArgumentMatchers.any(CertifiedProductSearchDetails.class));

        ListingUpdateRequest request = new ListingUpdateRequest();
        request.setAcknowledgeWarnings(false);
        request.setListing(getCertifiedProductSearchDetails());

        certifiedProductManager.update(request);
    }

    @Test
    public void update_HasValidationWarningsAndAck_ReturnsUpdatedListing()
            throws EntityRetrievalException, AccessDeniedException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException, IOException, ValidationException, MissingReasonException, CertifiedProductUpdateException {

        Mockito.when(certifiedProductDetailsManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
                .thenReturn(getCertifiedProductSearchDetails());

        Mockito.when(certifiedProductDetailsManager.getCertifiedProductDetailsNoCache(ArgumentMatchers.anyLong()))
                .thenReturn(getCertifiedProductSearchDetails());

        Validator validator = Mockito.mock(Validator.class);
        Mockito.when(validatorFactory.getValidator(ArgumentMatchers.any(CertifiedProductSearchDetails.class)))
                .thenReturn(validator);

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                CertifiedProductSearchDetails listing = (CertifiedProductSearchDetails) invocation.getArgument(1);
                listing.addWarningMessage("This is a test warning");
                return null;
            }
        }).when(validator).validate(ArgumentMatchers.any(CertifiedProductSearchDetails.class),
                ArgumentMatchers.any(CertifiedProductSearchDetails.class));

        ListingUpdateRequest request = new ListingUpdateRequest();
        request.setAcknowledgeWarnings(true);
        request.setListing(getCertifiedProductSearchDetails());

        CertifiedProductDTO dto = new CertifiedProductDTO();
        dto.setId(1L);
        Mockito.when(cpDao.update(ArgumentMatchers.any(CertifiedProductDTO.class)))
                .thenReturn(dto);

        CertifiedProductDTO listing = certifiedProductManager.update(request);

        assertNotNull(listing);
    }

    @SuppressWarnings({"checkstyle:magicnumber"}) // Used for setting dates
    private CertifiedProductSearchDetails getCertifiedProductSearchDetails() {
        Calendar cal1 = Calendar.getInstance();
        cal1.clear();
        cal1.set(2019,  Calendar.MAY, 17, 0, 0, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.clear();
        cal2.set(2019,  Calendar.JULY, 1, 0, 0, 0);

        return CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationDate(cal1.getTime().getTime())
                .certificationEdition(getCertificationEdition())
                .certificationEvent(CertificationStatusEvent.builder()
                        .eventDate(cal1.getTime().getTime())
                        .id(1L)
                        .status(CertificationStatus.builder()
                                .id(1L)
                                .name("Active")
                                .build())
                        .build())
                .certifyingBody(getCertifyingBody())
                .chplProductNumber("15.04.04.3046.Acel.11.01.0.190517")
                .cqmResults(new ArrayList<CQMResultDetails>())
                .developer(Developer.builder()
                        .id(1L)
                        .address(Address.builder()
                                .addressId(1L)
                                .city("Westport")
                                .country("US")
                                .line1("49 Richmondville, Ste 307")
                                .state("CT")
                                .zipcode("68800")
                                .build())
                        .contact(PointOfContact.builder()
                                .contactId(1L)
                                .email("fake@email.com")
                                .fullName("Chris Ulisse")
                                .phoneNumber("555-555-5555")
                                .build())
                        .developerCode("3046")
                        .name("Acelis Connected Health Technologies")
                        .statusEvents(List.of(DeveloperStatusEvent.builder()
                                .developerId(1L)
                                .id(1L)
                                .status(DeveloperStatus.builder()
                                        .id(1L)
                                        .status("Active")
                                        .build())
                                .statusDate(cal2.getTime())
                                .build()))
                        .build())
                .product(Product.builder()
                        .id(1L)
                        .name("Acelis Connected Health eSuite")
                        .owner(Developer.builder()
                                .build())
                        .build())
                .testingLab(CertifiedProductTestingLab.builder()
                        .id(1L)
                        .testingLabCode("04")
                        .testingLabId(1L)
                        .testingLabName("Drummond Group")
                        .build())
                .version(ProductVersion.builder()
                        .id(1L)
                        .version("11.3")
                        .build())
                .build();
    }

    private Map<String, Object> getCertificationEdition() {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put("name", "2015");
        edition.put("id", EDITION_2015_ID);
        return edition;
    }

    private Map<String, Object> getCertifyingBody() {
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put("id", DRUMMOND_ACB_ID);
        acb.put("acbCode", "O4");
        acb.put("name", "Drummond Group");
        return acb;
    }

}
