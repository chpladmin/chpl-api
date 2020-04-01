package gov.healthit.chpl.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsFactory;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsService;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationFactory;
import gov.healthit.chpl.changerequest.validation.WebsiteValidation;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class ChangeRequestManagerTest {
    @Test
    public void getChangeRequest_ValidCrId_ReturnsValidObject() throws EntityRetrievalException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(getBasicChangeRequest());

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(changeRequestDAO,
                null, null, null, null, null, null, null, null, null, null, null);

        // Run
        ChangeRequest cr = changeRequestManager.getChangeRequest(1l);

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

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(changeRequestDAO,
                null, null, null, null, null, null, null, null, null, null, null);

        // Run
        changeRequestManager.getChangeRequest(11l);

        // Check
        fail("Exception was not thrown");
    }

    @Test
    public void getAllChangeRequestsForUser_None_ReturnsValidList() throws EntityRetrievalException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.getAll())
                .thenReturn(Arrays.asList(getBasicChangeRequest(), getBasicChangeRequest(), getBasicChangeRequest()));

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(changeRequestDAO,
                null, null, null, null, null, null, null, null, null, null, null);

        // Run
        List<ChangeRequest> crs = changeRequestManager.getAllChangeRequestsForUser();

        // Check
        assertEquals(3l, crs.size());
    }

    @Test
    public void updateChangeRequest_ValidCr_ReturnsUpdatedCr()
            throws EntityRetrievalException, ValidationException, EntityCreationException, JsonProcessingException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong())).thenReturn(getBasicChangeRequest());

        ChangeRequestValidationFactory crValidationFactory = Mockito.mock(ChangeRequestValidationFactory.class);
        Mockito.when(crValidationFactory.getRule(ArgumentMatchers.anyString())).thenReturn(null);

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);

        ChangeRequestDetailsFactory crDetailsFactory = Mockito.mock(ChangeRequestDetailsFactory.class);
        ChangeRequestDetailsService detailsService = Mockito.mock(ChangeRequestDetailsService.class);
        // Return what was passed in...
        Mockito.when(detailsService.update(ArgumentMatchers.any(ChangeRequest.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(crDetailsFactory.get(ArgumentMatchers.anyLong())).thenReturn(detailsService);

        ChangeRequestStatusService crStatusService = Mockito.mock(ChangeRequestStatusService.class);
        Mockito.when(crStatusService.updateChangeRequestStatus(ArgumentMatchers.any(ChangeRequest.class)))
                .thenAnswer(i -> i.getArgument(0));

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(changeRequestDAO,
                null,
                null,
                null,
                null,
                crStatusService,
                crValidationFactory,
                crDetailsFactory,
                null,
                null,
                resourcePermissions,
                null);

        // Run
        changeRequestManager.updateChangeRequest(getBasicChangeRequest());

        // Check
        Mockito.verify(detailsService, Mockito.times(1)).update(ArgumentMatchers.any());
        Mockito.verify(crStatusService, Mockito.times(1)).updateChangeRequestStatus(ArgumentMatchers.any());
    }

    @Test(expected = ValidationException.class)
    public void updateChangeRequest_InvalidData_ThrowsException()
            throws EntityRetrievalException, ValidationException, EntityCreationException, JsonProcessingException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong())).thenReturn(getBasicChangeRequest());

        ChangeRequestValidationFactory crValidationFactory = Mockito.mock(ChangeRequestValidationFactory.class);
        WebsiteValidation websiteValidation = Mockito.mock(WebsiteValidation.class);
        Mockito.when(websiteValidation.isValid(ArgumentMatchers.any())).thenReturn(false);
        Mockito.when(websiteValidation.getMessages()).thenReturn(Arrays.asList("Error Message"));
        Mockito.when(crValidationFactory.getRule(ArgumentMatchers.anyString())).thenReturn(websiteValidation);

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(changeRequestDAO,
                null,
                null,
                null,
                null,
                null,
                crValidationFactory,
                null,
                null,
                null,
                null,
                null);

        // Run
        changeRequestManager.updateChangeRequest(getBasicChangeRequest());

        // Check
        fail("Exception was not thrown");
    }

    @Test
    public void updateChangeRequest_UserIsNotDeveloper_CrDetailsAreNotUpdate()
            throws EntityRetrievalException, ValidationException, EntityCreationException, JsonProcessingException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong())).thenReturn(getBasicChangeRequest());

        ChangeRequestValidationFactory crValidationFactory = Mockito.mock(ChangeRequestValidationFactory.class);
        Mockito.when(crValidationFactory.getRule(ArgumentMatchers.anyString())).thenReturn(null);

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(false);

        ChangeRequestDetailsFactory crDetailsFactory = Mockito.mock(ChangeRequestDetailsFactory.class);
        ChangeRequestDetailsService detailsService = Mockito.mock(ChangeRequestDetailsService.class);
        // Return what was passed in...
        Mockito.when(detailsService.update(ArgumentMatchers.any(ChangeRequest.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(crDetailsFactory.get(ArgumentMatchers.anyLong())).thenReturn(detailsService);

        ChangeRequestStatusService crStatusService = Mockito.mock(ChangeRequestStatusService.class);
        Mockito.when(crStatusService.updateChangeRequestStatus(ArgumentMatchers.any(ChangeRequest.class)))
                .thenAnswer(i -> i.getArgument(0));

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(changeRequestDAO,
                null,
                null,
                null,
                null,
                crStatusService,
                crValidationFactory,
                crDetailsFactory,
                null,
                null,
                resourcePermissions,
                null);

        // Run
        changeRequestManager.updateChangeRequest(getBasicChangeRequest());

        // Check
        Mockito.verify(detailsService, Mockito.times(0)).update(ArgumentMatchers.any());
        Mockito.verify(crStatusService, Mockito.times(1)).updateChangeRequestStatus(ArgumentMatchers.any());
    }

    @Test
    public void updateChangeRequest_CrStatusIsNull_CrStatusIsNotUpdated()
            throws EntityRetrievalException, ValidationException, EntityCreationException, JsonProcessingException {
        // Setup
        ChangeRequestDAO changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        Mockito.when(changeRequestDAO.get(ArgumentMatchers.anyLong())).thenReturn(getBasicChangeRequest());

        ChangeRequestValidationFactory crValidationFactory = Mockito.mock(ChangeRequestValidationFactory.class);
        Mockito.when(crValidationFactory.getRule(ArgumentMatchers.anyString())).thenReturn(null);

        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);

        ChangeRequestDetailsFactory crDetailsFactory = Mockito.mock(ChangeRequestDetailsFactory.class);
        ChangeRequestDetailsService detailsService = Mockito.mock(ChangeRequestDetailsService.class);
        // Return what was passed in...
        Mockito.when(detailsService.update(ArgumentMatchers.any(ChangeRequest.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(crDetailsFactory.get(ArgumentMatchers.anyLong())).thenReturn(detailsService);

        ChangeRequestStatusService crStatusService = Mockito.mock(ChangeRequestStatusService.class);
        Mockito.when(crStatusService.updateChangeRequestStatus(ArgumentMatchers.any(ChangeRequest.class)))
                .thenAnswer(i -> i.getArgument(0));

        ChangeRequestManager changeRequestManager = new ChangeRequestManager(changeRequestDAO,
                null,
                null,
                null,
                null,
                crStatusService,
                crValidationFactory,
                crDetailsFactory,
                null,
                null,
                resourcePermissions,
                null);

        // Run
        ChangeRequest cr = getBasicChangeRequest();
        cr.setCurrentStatus(null);
        changeRequestManager.updateChangeRequest(cr);

        // Check
        Mockito.verify(detailsService, Mockito.times(1)).update(ArgumentMatchers.any());
        Mockito.verify(crStatusService, Mockito.times(0)).updateChangeRequestStatus(ArgumentMatchers.any());
    }

    private ChangeRequest getBasicChangeRequest() {
        // return new ChangeRequestBuilder()
        // .withId(1l)
        // .withDeveloper(new DeveloperBuilder().withId(20l).withCode("1234").withName("Dev 1").build())
        // .withChangeRequestType(
        // new ChangeRequestTypeBuilder().withId(1l).withName("Website Change Request").build())
        // .withCurrentStatus(new ChangeRequestStatusBuilder()
        // .withId(8l)
        // .withComment("Comment")
        // .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
        // .withId(1l)
        // .withName("Pending ONC-ACB Action")
        // .build())
        // .build())
        // .addChangeRequestStatus(new ChangeRequestStatusBuilder()
        // .withId(8l)
        // .withComment("Comment")
        // .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
        // .withId(1l)
        // .withName("Pending ONC-ACB Action")
        // .build())
        // .build())
        // .addCertificationBody(new CertificationBodyBuilder()
        // .withId(1l)
        // .withCode("1234")
        // .withName("ACB 1234")
        // .build())
        // .withDetails(new ChangeRequestWebsiteBuilder()
        // .withId(2l)
        // .withWebsite("http://www.abc.com")
        // .build())
        // .build();

        return ChangeRequest.builder()
                .id(1l)
                .developer(Developer.builder()
                        .developerId(Long.valueOf(20l))
                        .developerCode("1234")
                        .name("Dev 1")
                        .build())
                .changeRequestType(ChangeRequestType.builder()
                        .id(1l)
                        .name("Website Change Request")
                        .build())
                .currentStatus(ChangeRequestStatus.builder()
                        .id(Long.valueOf(8l))
                        .comment("Comment")
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(1l)
                                .name("Pending ONC-ACB Action")
                                .build())
                        .build())
                .certificationBody(CertificationBody.builder()
                        .id(1l)
                        .acbCode("1234")
                        .name("ACB 1234")
                        .build())
                .details(ChangeRequestWebsite.builder()
                        .id(2l)
                        .website("http://www.abc.com")
                        .build())
                .build();
    }

}
