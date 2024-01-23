package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.permissions.ChplResourcePermissions;

public class RoleAcbHasMultipleCertificationBodiesValidationTest {
    private RoleAcbHasMultipleCertificationBodiesValidation validator;

    @Before
    public void setup() {
        validator = new RoleAcbHasMultipleCertificationBodiesValidation();
    }

    @Test
    public void isValid_UserIsAcbAndOnlyAccesstoOneAcb_ReturnsTrue() {
        ChplResourcePermissions resourcePermissions = Mockito.mock(ChplResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(
                Arrays.asList(CertificationBody.builder().build()));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .resourcePermissions(resourcePermissions)
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_UserIsNotAcb_ReturnsTrue() {
        ChplResourcePermissions resourcePermissions = Mockito.mock(ChplResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(
                Arrays.asList(CertificationBody.builder().build()));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .resourcePermissions(resourcePermissions)
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);

    }

    @Test
    public void isValid_UserIsAcbAndOnlyAccesstoTwoAcbs_ReturnsFalse() {
        ChplResourcePermissions resourcePermissions = Mockito.mock(ChplResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(
                Arrays.asList(CertificationBody.builder().build(),
                        CertificationBody.builder().build()));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .resourcePermissions(resourcePermissions)
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }
}
