package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;

public class RwtChangeRequestValidationTest {
    @Test
    public void validateValidRwtChangeRequest_returnsTrue() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com", resourcePermissionsFactory);
        RwtChangeRequestValidation rwtValidator = new RwtChangeRequestValidation();

        boolean result = rwtValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, rwtValidator.getMessages().size());
    }

    @Test
    public void validateRwtChangeRequestWithNullDetails_returnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com", resourcePermissionsFactory);
        context.getNewChangeRequest().setDetails(null);
        RwtChangeRequestValidation rwtValidator = new RwtChangeRequestValidation();

        boolean result = rwtValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, rwtValidator.getMessages().size());
    }

    @Test
    public void validateRwtChangeRequestWithNullUrl_returnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com", resourcePermissionsFactory);
        ChangeRequestListingUrl details = (ChangeRequestListingUrl) context.getNewChangeRequest().getDetails();
        details.setUrl(null);
        RwtChangeRequestValidation rwtValidator = new RwtChangeRequestValidation();

        boolean result = rwtValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, rwtValidator.getMessages().size());
    }

    @Test
    public void validateRwtChangeRequestWithNullListing_returnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com", resourcePermissionsFactory);
        ChangeRequestListingUrl details = (ChangeRequestListingUrl) context.getNewChangeRequest().getDetails();
        details.setListing(null);
        RwtChangeRequestValidation rwtValidator = new RwtChangeRequestValidation();

        boolean result = rwtValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, rwtValidator.getMessages().size());
    }

    private ChangeRequest getChangeRequest(String rwt) {
        return ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .id(Long.valueOf(20L))
                        .developerCode("1234")
                        .name("Dev 1")
                        .build())
                .changeRequestType(ChangeRequestType.builder()
                        .id(3L)
                        .name("Service Base URL List Change Request")
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
                .details(buildChangeRequestDetails(rwt))
                .build();
    }

    private ChangeRequestListingUrl buildChangeRequestDetails(String rwt) {
        return ChangeRequestListingUrl.builder()
                .listing(getListingWithRwt(rwt))
                .url(rwt)
                .build();
    }

    private CertifiedProductSearchDetails getListingWithRwt(String rwt) {
        return CertifiedProductSearchDetails.builder()
            .id(1L)
            .rwtPlansUrl(rwt)
            .build();
    }

    private ChangeRequestValidationContext getValidationContext(String rwt, ResourcePermissionsFactory resourcePermissionsFactory) {
        return new ChangeRequestValidationContext(null,
                        getChangeRequest(rwt),
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
