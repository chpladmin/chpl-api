package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.changerequest.builders.CertificationBodyBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusTypeBuilder;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class CertificationBodyRequiredValidationTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private CertificationBodyRequiredValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isValid_UserNotAnAdmin() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        assertTrue(validator.isValid(new ChangeRequestValidationContext(null, null)));
    }

    @Test
    public void isValid_UserIsAdminNoCertBody() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(2l)
                                .build())
                        .build())
                .build();

        assertFalse(validator.isValid(new ChangeRequestValidationContext(cr, null)));
    }

    @Test
    public void isValid_UserIsAdminNoCertBodyId() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(2l)
                                .build())
                        .withCertificationBody(new CertificationBodyBuilder().build())
                        .build())
                .build();

        assertFalse(validator.isValid(new ChangeRequestValidationContext(cr, null)));
    }

    @Test
    public void isValid_UserIsAdminWithCertBody() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(2l)
                                .build())
                        .withCertificationBody(new CertificationBodyBuilder()
                                .withId(3l)
                                .build())
                        .build())
                .build();

        assertTrue(validator.isValid(new ChangeRequestValidationContext(cr, null)));
    }

    @Test
    public void isValid_UserIsOnceNoCertBody() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(2l)
                                .build())
                        .build())
                .build();

        assertFalse(validator.isValid(new ChangeRequestValidationContext(cr, null)));
    }

    @Test
    public void isValid_UserIsOncNoCertBodyId() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(2l)
                                .build())
                        .withCertificationBody(new CertificationBodyBuilder().build())
                        .build())
                .build();

        assertFalse(validator.isValid(new ChangeRequestValidationContext(cr, null)));
    }

    @Test
    public void isValid_UserIsOncWithCertBody() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(2l)
                                .build())
                        .withCertificationBody(new CertificationBodyBuilder()
                                .withId(3l)
                                .build())
                        .build())
                .build();

        assertTrue(validator.isValid(new ChangeRequestValidationContext(cr, null)));
    }

}
