package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;

public class RoleAcbHasMultipleCertificationBodiesValidationTest {
    private RoleAcbHasMultipleCertificationBodiesValidation validator;

    @Before
    public void setup() {
        validator = new RoleAcbHasMultipleCertificationBodiesValidation();
    }

    @Test
    public void isValid_UserIsAcbAndOnlyAccesstoOneAcb_ReturnsTrue() {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(
                Arrays.asList(CertificationBody.builder().build()));
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);


        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_UserIsNotAcb_ReturnsTrue() {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(
                Arrays.asList(CertificationBody.builder().build()));
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);

    }

    @Test
    public void isValid_UserIsAcbAndOnlyAccesstoTwoAcbs_ReturnsFalse() {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(
                Arrays.asList(CertificationBody.builder().build(),
                        CertificationBody.builder().build()));
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }
}
