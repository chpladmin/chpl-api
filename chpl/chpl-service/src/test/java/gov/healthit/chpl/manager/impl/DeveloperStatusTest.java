package gov.healthit.chpl.manager.impl;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.developer.DeveloperCreationValidator;
import gov.healthit.chpl.validation.developer.DeveloperUpdateValidator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class DeveloperStatusTest {
    private static final String MISSING_REASON_ERROR = "A reason must be given for marking this developer as banned on %s.";
    private static final String NO_ADMIN_NO_STATUS_CHANGE_ERROR = "User cannot change developer status to %s without ROLE_ADMIN";

    private JWTAuthenticatedUser adminUser;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private CertificationBodyDAO certificationBodyDao;

    @Autowired
    private CertifiedProductDAO certifiedProductDao;

    @Autowired
    private ChplProductNumberUtil chplProductNumberUtil;

    @Spy
    private DeveloperDAO devDao;
    @Spy
    private CertificationBodyManager acbManager;
    @Spy
    private CertifiedProductManager cpManager;
    @Spy
    private CertifiedProductDetailsManager cpdManager;
    @Spy
    private ActivityManager activityManager;
    @Spy
    private DeveloperCreationValidator creationValidator;
    @Spy
    private DeveloperUpdateValidator updateValidator;
    @Spy
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    @Mock
    private ResourcePermissions permissionChecker;

    @InjectMocks
    private DeveloperManagerImpl developerManager;

    @Before
    public void setup() {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Test Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        MockitoAnnotations.initMocks(this);
        developerManager = new DeveloperManagerImpl(devDao, productManager, acbManager, cpManager,
                cpdManager, certificationBodyDao, certifiedProductDao, chplProductNumberUtil, activityManager,
                creationValidator, updateValidator, msgUtil, permissionChecker);

        Mockito.when(permissionChecker.getAllAcbsForCurrentUser()).thenReturn(new ArrayList<CertificationBodyDTO>());
        Mockito.when(acbManager.getAll()).thenReturn(new ArrayList<CertificationBodyDTO>());
        Mockito.doReturn(MISSING_REASON_ERROR).when(msgUtil)
                .getMessage(ArgumentMatchers.eq("developer.missingReasonForBan"), ArgumentMatchers.anyString());
        Mockito.doReturn(NO_ADMIN_NO_STATUS_CHANGE_ERROR).when(msgUtil)
            .getMessage(ArgumentMatchers.eq("developer.statusChangeNotAllowedWithoutAdmin"),
                    ArgumentMatchers.eq(DeveloperStatusType.UnderCertificationBanByOnc.toString()));
    }

    @Test
    public void testDeveloperStatusChange_ActiveToSuspended_NoReasonRequired() 
            throws EntityCreationException, EntityRetrievalException,
        JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        DeveloperDTO activeDeveloper = createDeveloper(1L, "0001", "Test Developer");
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong())).thenReturn(activeDeveloper);
        } catch (EntityRetrievalException ex) {
        }
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(activeDeveloper);
        } catch (EntityRetrievalException ex) {
        }

        DeveloperDTO activeToSuspendedDeveloper = createDeveloper(1L, "0001", "Test Developer");
        activeToSuspendedDeveloper.getStatusEvents().add(createStatusEvent(2L, activeToSuspendedDeveloper.getId(),
                DeveloperStatusType.SuspendedByOnc, new Date(), null));

        DeveloperDTO updatedDeveloper =
                developerManager.update(activeToSuspendedDeveloper, false);
        //the update was allowed
        assertNotNull(updatedDeveloper);
    }

    @Test(expected = MissingReasonException.class)
    public void testDeveloperStatusChange_ActiveToBannedNullReason_ThrowsException() 
            throws EntityCreationException, EntityRetrievalException,
            JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        DeveloperDTO activeDeveloper = createDeveloper(1L, "0001", "Test Developer");
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong())).thenReturn(activeDeveloper);
        } catch (EntityRetrievalException ex) {
        }
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(activeDeveloper);
        } catch (EntityRetrievalException ex) {
        }

        DeveloperDTO activeToBannedDeveloper = createDeveloper(1L, "0001", "Test Developer");
        activeToBannedDeveloper.getStatusEvents().add(createStatusEvent(2L, activeToBannedDeveloper.getId(),
                DeveloperStatusType.UnderCertificationBanByOnc, new Date(), null));

        developerManager.update(activeToBannedDeveloper, false);
    }

    @Test(expected = MissingReasonException.class)
    public void testDeveloperWithHistoryChange_ActiveToBannedNullReason_ThrowsException()
            throws EntityCreationException, EntityRetrievalException,
            JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        DeveloperDTO activeDeveloperWithStatusHistory = createDeveloper(1L, "0001", "Test Developer");
        activeDeveloperWithStatusHistory.getStatusEvents().add(createStatusEvent(2L,
                activeDeveloperWithStatusHistory.getId(), DeveloperStatusType.SuspendedByOnc, new Date(), "Reason!"));
        activeDeveloperWithStatusHistory.getStatusEvents()
                .add(createStatusEvent(2L, activeDeveloperWithStatusHistory.getId(),
                        DeveloperStatusType.UnderCertificationBanByOnc, new Date(), null));
        activeDeveloperWithStatusHistory.getStatusEvents().add(createStatusEvent(2L,
                activeDeveloperWithStatusHistory.getId(), DeveloperStatusType.Active, new Date(), null));
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong())).thenReturn(activeDeveloperWithStatusHistory);
        } catch (EntityRetrievalException ex) {
        }
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(activeDeveloperWithStatusHistory);
        } catch (EntityRetrievalException ex) {
        }

        activeDeveloperWithStatusHistory.setName("New Name");
        developerManager.update(activeDeveloperWithStatusHistory, false);
    }

    @Test(expected = MissingReasonException.class)
    public void testDeveloperStatusChange_ActiveToBannedEmptyReason_ThrowsException()
            throws EntityCreationException, EntityRetrievalException,
            JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        DeveloperDTO activeDeveloper = createDeveloper(1L, "0001", "Test Developer");
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong())).thenReturn(activeDeveloper);
        } catch (EntityRetrievalException ex) {
        }

        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(activeDeveloper);
        } catch (EntityRetrievalException ex) {
        }

        DeveloperDTO activeToBannedDeveloper = createDeveloper(1L, "0001", "Test Developer");
        activeToBannedDeveloper.getStatusEvents().add(createStatusEvent(2L, activeToBannedDeveloper.getId(),
                DeveloperStatusType.UnderCertificationBanByOnc, new Date(), ""));

        developerManager.update(activeToBannedDeveloper, false);
    }

    @Test
    public void testDeveloperStatusChange_ActiveToSuspendedWithReason_Allowed()
        throws EntityCreationException, EntityRetrievalException,
        JsonProcessingException, MissingReasonException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        DeveloperDTO activeDeveloper = createDeveloper(1L, "0001", "Test Developer");
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong())).thenReturn(activeDeveloper);
        } catch (EntityRetrievalException ex) {
        }

        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(activeDeveloper);
        } catch (EntityRetrievalException ex) {
        }

        DeveloperDTO activeToSuspendedDeveloper = createDeveloper(1L, "0001", "Test Developer");
        activeToSuspendedDeveloper.getStatusEvents().add(createStatusEvent(2L, activeToSuspendedDeveloper.getId(),
                DeveloperStatusType.UnderCertificationBanByOnc, new Date(), "A Reason"));

        DeveloperDTO updatedDeveloper =
                developerManager.update(activeToSuspendedDeveloper, false);
        //the update was allowed
        assertNotNull(updatedDeveloper);
    }

    private DeveloperDTO createDeveloper(Long id, String code, String name) {
        Date devDate = new Date();
        DeveloperDTO dev = new DeveloperDTO();
        dev.setId(id);
        dev.setAddress(createAddress());
        dev.setContact(createContact());
        dev.setCreationDate(devDate);
        dev.setDeveloperCode(code);
        dev.setLastModifiedDate(devDate);
        dev.setLastModifiedUser(-1L);
        dev.setName(name);
        DeveloperStatusEventDTO activeStatus = createStatusEvent(1L, id, DeveloperStatusType.Active, devDate, null);
        dev.getStatusEvents().add(activeStatus);
        dev.setDeleted(Boolean.FALSE);
        return dev;
    }

    private DeveloperStatusEventDTO createStatusEvent(Long eventId, Long developerId, DeveloperStatusType type,
            Date statusDate, String reason) {
        DeveloperStatusEventDTO event = new DeveloperStatusEventDTO();
        event.setId(eventId);
        event.setDeveloperId(developerId);
        event.setReason(reason);
        event.setStatusDate(statusDate);
        event.setDeleted(false);
        DeveloperStatusDTO status = new DeveloperStatusDTO();
        status.setId(1L);
        status.setStatusName(type.toString());
        event.setStatus(status);
        return event;
    }

    private AddressDTO createAddress() {
        AddressDTO addr = new AddressDTO();
        addr.setId(1L);
        addr.setStreetLineOne("111 Test Road");
        addr.setStreetLineTwo(null);
        addr.setCity("City");
        addr.setState("MA");
        addr.setZipcode("10001");
        addr.setCountry("US");
        return addr;
    }

    private ContactDTO createContact() {
        ContactDTO contact = new ContactDTO();
        contact.setId(1L);
        contact.setFullName("First Last");
        contact.setFriendlyName("First");
        contact.setPhoneNumber("111-222-3333");
        contact.setSignatureDate(new Date());
        contact.setEmail("first@last.com");
        contact.setTitle("Mr.");
        return contact;
    }
}
