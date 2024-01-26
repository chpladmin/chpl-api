package gov.healthit.chpl.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestUpdateRequest;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsFactory;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsService;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationService;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ChangeRequestManagerTest {
    private FF4j ff4j;
    private ErrorMessageUtil errorMessageUtil;


    @Before
    public void before() throws EntityRetrievalException {
        ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(FeatureList.DEMOGRAPHIC_CHANGE_REQUEST))
        .thenReturn(true);

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
                .thenReturn("Error message");
    }

    @Test
    public void getChangeRequest_ValidCrId_ReturnsValidObject() throws EntityRetrievalException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(getBasicChangeRequest());

        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(null);

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(null, null, changeRequestDAO,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                resourcePermissionsFactory, null, null, null, ff4j);

        // Run
        ChangeRequest cr = changeRequestManager.getChangeRequest(1L);

        // Check - ChangeRequest should implement 'equals' but does not.
        // This indicates there is a problem.
        // assertEquals(getBasicChangeRequest(), cr);
        assertEquals(getBasicChangeRequest().getId(), cr.getId());
    }

    @Test(expected = EntityRetrievalException.class)
    public void getChangeRequest_InvalidCrId_ThrowsException() throws EntityRetrievalException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenThrow(EntityRetrievalException.class);

        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(null);

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(null, null, changeRequestDAO,
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, resourcePermissionsFactory, null, null, null, ff4j);

        // Run
        changeRequestManager.getChangeRequest(11L);

        // Check
        fail("Exception was not thrown");
    }

    @Test
    public void updateChangeRequest_ValidCr_ReturnsUpdatedCr()
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, EmailNotSentException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong())).thenReturn(getBasicChangeRequest());

        ChangeRequestValidationService crValidationService = Mockito.mock(ChangeRequestValidationService.class);
        Mockito.when(crValidationService.getErrorMessages(ArgumentMatchers.any())).thenReturn(new ArrayList<String>());

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);

        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestDetailsFactory crDetailsFactory = Mockito.mock(ChangeRequestDetailsFactory.class);
        ChangeRequestDetailsService detailsService = Mockito.mock(ChangeRequestDetailsService.class);
        // Return what was passed in...
        Mockito.when(detailsService.update(ArgumentMatchers.any(ChangeRequest.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(crDetailsFactory.get(ArgumentMatchers.anyLong())).thenReturn(detailsService);

        ChangeRequestStatusService crStatusService = Mockito.mock(ChangeRequestStatusService.class);
        Mockito.when(crStatusService.updateChangeRequestStatus(ArgumentMatchers.any(ChangeRequest.class)))
                .thenAnswer(i -> i.getArgument(0));

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(null, null, changeRequestDAO,
                null,
                null,
                null,
                null,
                null,
                crStatusService,
                crValidationService,
                crDetailsFactory,
                null,
                null,
                null,
                null,
                null,
                null,
                resourcePermissionsFactory,
                null,
                null,
                null,
                ff4j);

        // Run
        changeRequestManager.updateChangeRequest(ChangeRequestUpdateRequest.builder()
                .changeRequest(getBasicChangeRequest())
                .acknowledgeWarnings(true)
                .build());

        // Check
        Mockito.verify(detailsService, Mockito.times(1)).update(ArgumentMatchers.any());
        Mockito.verify(crStatusService, Mockito.times(1)).updateChangeRequestStatus(ArgumentMatchers.any());
    }

    @Test(expected = ValidationException.class)
    public void updateChangeRequest_ValidationErrors_ThrowsException()
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, EmailNotSentException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong())).thenReturn(getBasicChangeRequest());

        ChangeRequestValidationService crValidationService = Mockito.mock(ChangeRequestValidationService.class);
                Mockito.when(crValidationService.getErrorMessages(ArgumentMatchers.any())).thenReturn(new ArrayList<String>(
                        Arrays.asList("This is an error.")));
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(null);

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(null, null, changeRequestDAO,
                null,
                null,
                null,
                null,
                null,
                null,
                crValidationService,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                resourcePermissionsFactory,
                null,
                null,
                null,
                ff4j);

        // Run
        changeRequestManager.updateChangeRequest(ChangeRequestUpdateRequest.builder()
                .changeRequest(getBasicChangeRequest())
                .acknowledgeWarnings(true)
                .build());
        // Check
        fail("Exception was not thrown");
    }

    @Test
    public void updateChangeRequest_UserIsNotDeveloper_CrDetailsAreNotUpdate()
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, EmailNotSentException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong())).thenReturn(getBasicChangeRequest());

        ChangeRequestValidationService crValidationService = Mockito.mock(ChangeRequestValidationService.class);
        Mockito.when(crValidationService.getErrorMessages(ArgumentMatchers.any())).thenReturn(new ArrayList<String>());

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(false);

        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestDetailsFactory crDetailsFactory = Mockito.mock(ChangeRequestDetailsFactory.class);
        ChangeRequestDetailsService detailsService = Mockito.mock(ChangeRequestDetailsService.class);
        // Return what was passed in...
        Mockito.when(detailsService.update(ArgumentMatchers.any(ChangeRequest.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(crDetailsFactory.get(ArgumentMatchers.anyLong())).thenReturn(detailsService);

        ChangeRequestStatusService crStatusService = Mockito.mock(ChangeRequestStatusService.class);
        Mockito.when(crStatusService.updateChangeRequestStatus(ArgumentMatchers.any(ChangeRequest.class)))
                .thenAnswer(i -> i.getArgument(0));

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(null, null, changeRequestDAO,
                null,
                null,
                null,
                null,
                null,
                crStatusService,
                crValidationService,
                crDetailsFactory,
                null,
                null,
                null,
                null,
                null,
                null,
                resourcePermissionsFactory,
                null,
                null,
                null,
                ff4j);

        // Run
        changeRequestManager.updateChangeRequest(ChangeRequestUpdateRequest.builder()
                .changeRequest(getBasicChangeRequest())
                .acknowledgeWarnings(true)
                .build());

        // Check
        Mockito.verify(detailsService, Mockito.times(0)).update(ArgumentMatchers.any());
        Mockito.verify(crStatusService, Mockito.times(1)).updateChangeRequestStatus(ArgumentMatchers.any());
    }

    @Test(expected = InvalidArgumentsException.class)
    public void updateChangeRequest_CrCurrentStatusIsNull_InvalidArgumentsException()
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, EmailNotSentException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong())).thenReturn(getBasicChangeRequest());

        ChangeRequestValidationService crValidationService = Mockito.mock(ChangeRequestValidationService.class);
        Mockito.when(crValidationService.getErrorMessages(ArgumentMatchers.any())).thenReturn(new ArrayList<String>());

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);

        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestDetailsFactory crDetailsFactory = Mockito.mock(ChangeRequestDetailsFactory.class);
        ChangeRequestDetailsService detailsService = Mockito.mock(ChangeRequestDetailsService.class);
        // Return what was passed in...
        Mockito.when(detailsService.update(ArgumentMatchers.any(ChangeRequest.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(crDetailsFactory.get(ArgumentMatchers.anyLong())).thenReturn(detailsService);

        ChangeRequestStatusService crStatusService = Mockito.mock(ChangeRequestStatusService.class);
        Mockito.when(crStatusService.updateChangeRequestStatus(ArgumentMatchers.any(ChangeRequest.class)))
                .thenAnswer(i -> i.getArgument(0));

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(null, null,
                changeRequestDAO,
                null,
                null,
                null,
                null,
                null,
                crStatusService,
                crValidationService,
                crDetailsFactory,
                null,
                null,
                null,
                null,
                null,
                null,
                resourcePermissionsFactory,
                errorMessageUtil,
                null,
                null,
                ff4j);

        // Run
        ChangeRequest cr = getBasicChangeRequest();
        cr.setCurrentStatus(null);
        changeRequestManager.updateChangeRequest(ChangeRequestUpdateRequest.builder()
                .changeRequest(cr)
                .acknowledgeWarnings(true)
                .build());
        // Check
    }

    private ChangeRequest getBasicChangeRequest() {
        return ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .id(Long.valueOf(20L))
                        .developerCode("1234")
                        .name("Dev 1")
                        .build())
                .changeRequestType(ChangeRequestType.builder()
                        .id(1L)
                        .name("Website Change Request")
                        .build())
                .currentStatus(ChangeRequestStatus.builder()
                        .id(Long.valueOf(8L))
                        .comment("Comment")
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(1L)
                                .name("Pending ONC-ACB Action")
                                .build())
                        .build())
                .certificationBody(CertificationBody.builder()
                        .id(1L)
                        .acbCode("1234")
                        .name("ACB 1234")
                        .build())
                .details(new HashMap<String, Object>())
                .build();
    }

}
