package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;

public class ChangeRequestSelfDeveloperValidationTest {

    @Test
    public void validateSelfDeveloper_ValidData_ReturnsTrue() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestValidationContext context = getValidationContext(true, resourcePermissionsFactory);
        SelfDeveloperValidation crSelfDevValidator = new SelfDeveloperValidation();

        boolean result = crSelfDevValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, crSelfDevValidator.getMessages().size());
    }

    @Test
    public void validateSelfDeveloper_ValidDataFalse_ReturnsTrue() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);


        ChangeRequestValidationContext context = getValidationContext(false, resourcePermissionsFactory);
        SelfDeveloperValidation crSelfDevValidator = new SelfDeveloperValidation();

        boolean result = crSelfDevValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, crSelfDevValidator.getMessages().size());
    }

    @Test
    public void validateSelfDeveloper_MissingData_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestValidationContext context = getValidationContext(null, resourcePermissionsFactory);
        SelfDeveloperValidation crSelfDevValidator = new SelfDeveloperValidation();

        boolean result = crSelfDevValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, crSelfDevValidator.getMessages().size());
    }

    private ChangeRequest getChangeRequestSelfDeveloper(Boolean selfDeveloper) {
        return ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .id(Long.valueOf(20L))
                        .developerCode("1234")
                        .name("Dev 1")
                        .build())
                .changeRequestType(ChangeRequestType.builder()
                        .id(2L)
                        .name("Developer Details Request")
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
                .details(buildChangeRequestDetails(selfDeveloper))
                .build();
    }

    private ChangeRequestDeveloperDemographics buildChangeRequestDetails(Boolean selfDeveloper) {
        return ChangeRequestDeveloperDemographics.builder()
                .selfDeveloper(selfDeveloper)
                .build();
    }

    private ChangeRequestValidationContext getValidationContext(Boolean  selfDeveloperValue, ResourcePermissionsFactory resourcePermissionsFactory) {
        return new ChangeRequestValidationContext(null,
                        getChangeRequestSelfDeveloper(selfDeveloperValue),
                        null,
                        null,
                        null,
                        null,
                        null,
                        resourcePermissionsFactory,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }
}
