package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
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
import gov.healthit.chpl.util.ValidationUtils;

public class ChangeRequestWebsiteValidationTest {
    @Test
    public void validateSelfDeveloper_ValidData_ReturnsTrue() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);

        ValidationUtils validationUtils = Mockito.mock(ValidationUtils.class);
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString())).thenReturn(true);

        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com", resourcePermissions);
        context.setValidationUtils(validationUtils);

        WebsiteValidation crWebsiteValidator = new WebsiteValidation();

        boolean result = crWebsiteValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, crWebsiteValidator.getMessages().size());
    }

    @Test
    public void validateSelfDeveloper_InvalidUrl_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);

        ValidationUtils validationUtils = Mockito.mock(ValidationUtils.class);
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString())).thenReturn(false);

        ChangeRequestValidationContext context = getValidationContext("www.abc", resourcePermissions);
        context.setValidationUtils(validationUtils);

        WebsiteValidation crWebsiteValidator = new WebsiteValidation();

        boolean result = crWebsiteValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, crWebsiteValidator.getMessages().size());
    }

    @Test
    public void validateSelfDeveloper_MissingData_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);

        ChangeRequestValidationContext context = getValidationContext(null, resourcePermissions);
        WebsiteValidation crWebsiteValidator = new WebsiteValidation();

        boolean result = crWebsiteValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, crWebsiteValidator.getMessages().size());
    }

    private ChangeRequest getChangeRequest(String website) {
        return ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .developerId(Long.valueOf(20L))
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
                .details(buildChangeRequestDetails(website))
                .build();
    }

    private ChangeRequestDeveloperDemographics buildChangeRequestDetails(String website) {
        return ChangeRequestDeveloperDemographics.builder()
                .website(website)
                .build();
    }

    private ChangeRequestValidationContext getValidationContext(String website, ResourcePermissions resourcePermissions) {
        return new ChangeRequestValidationContext(null,
                        getChangeRequest(website),
                        null,
                        null,
                        resourcePermissions,
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
