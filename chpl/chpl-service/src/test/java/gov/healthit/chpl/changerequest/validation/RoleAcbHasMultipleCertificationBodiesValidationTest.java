package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;

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
                Arrays.asList(CertificationBodyDTO.builder().build()));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .resourcePermissions(resourcePermissions)
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_UserIsNotAcb_ReturnsTrue() {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(
                Arrays.asList(CertificationBodyDTO.builder().build()));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .resourcePermissions(resourcePermissions)
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);

    }

    @Test
    public void isValid_UserIsAcbAndOnlyAccesstoTwoAcbs_ReturnsFalse() {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(
                Arrays.asList(CertificationBodyDTO.builder().build(),
                        CertificationBodyDTO.builder().build()));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .resourcePermissions(resourcePermissions)
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }
}
