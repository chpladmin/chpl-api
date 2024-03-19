package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.validation.attestation.AttestationValidation;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.form.validation.FormValidationResult;
import gov.healthit.chpl.form.validation.FormValidator;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;

public class AttestationValidationTest {

    private AttestationValidation validator = new AttestationValidation();
    private AttestationManager attestationManager;
    private AttestationPeriodService attestationPeriodService;
    private FormValidator formValidator;
    private ResourcePermissionsFactory resourcePermissionsFactory;
    private ResourcePermissions resourcePermissions;

    @Before
    public void setup() {
        attestationManager = Mockito.mock(AttestationManager.class);
        attestationPeriodService = Mockito.mock(AttestationPeriodService.class);
        formValidator = Mockito.mock(FormValidator.class);
        resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);
    }

    @Test
    public void isValid_ProvidedAttestationPeriodIsNull_MessageIsGenerated() throws EntityRetrievalException {
        Mockito.when(attestationManager.canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleAdmin()).thenReturn(false);
        AttestationPeriod submittablePeriod = AttestationPeriod.builder()
                .id(1L)
                .build();
        Mockito.when(attestationPeriodService.getSubmittableAttestationPeriod(ArgumentMatchers.anyLong()))
            .thenReturn(submittablePeriod);
        Mockito.when(formValidator.validate(ArgumentMatchers.any(Form.class))).thenReturn(
                FormValidationResult.builder()
                        .errorMessages(new ArrayList<String>())
                        .valid(true)
                        .build());
        Mockito.when(formValidator.removePhantomAndDuplicateResponses(ArgumentMatchers.any(Form.class)))
            .thenAnswer(i -> i.getArgument(0));

        JWTAuthenticatedUser currentUser = new JWTAuthenticatedUser();
        currentUser.setFullName("User A");

        validator.isValid(ChangeRequestValidationContext.builder()
                .attestationPeriodService(attestationPeriodService)
                .domainManagers(ChangeRequestValidationContext.DomainManagers.builder()
                        .attestationManager(attestationManager)
                        .build())
                .formValidator(formValidator)
                .currentUser(currentUser)
                .newChangeRequest(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .details(ChangeRequestAttestationSubmission.builder()
                                .attestationPeriod(null)
                                .form(Form.builder()
                                        .build())
                                .signature("User A")
                                .build())
                        .build())
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build());

        assertEquals(1, validator.getMessages().size());
    }

    @Test
    public void isValid_ProvidedAttestationPeriodIsNotSubmittable_MessageIsGenerated() throws EntityRetrievalException {
        Mockito.when(attestationManager.canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleAdmin()).thenReturn(false);
        AttestationPeriod submittablePeriod = AttestationPeriod.builder()
                .id(1L)
                .build();
        Mockito.when(attestationPeriodService.getSubmittableAttestationPeriod(ArgumentMatchers.anyLong()))
            .thenReturn(submittablePeriod);
        Mockito.when(formValidator.validate(ArgumentMatchers.any(Form.class))).thenReturn(
                FormValidationResult.builder()
                        .errorMessages(new ArrayList<String>())
                        .valid(true)
                        .build());
        Mockito.when(formValidator.removePhantomAndDuplicateResponses(ArgumentMatchers.any(Form.class)))
            .thenAnswer(i -> i.getArgument(0));

        JWTAuthenticatedUser currentUser = new JWTAuthenticatedUser();
        currentUser.setFullName("User A");

        validator.isValid(ChangeRequestValidationContext.builder()
                .attestationPeriodService(attestationPeriodService)
                .domainManagers(ChangeRequestValidationContext.DomainManagers.builder()
                        .attestationManager(attestationManager)
                        .build())
                .formValidator(formValidator)
                .currentUser(currentUser)
                .newChangeRequest(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .details(ChangeRequestAttestationSubmission.builder()
                                .attestationPeriod(AttestationPeriod.builder()
                                        .id(2L)
                                        .build())
                                .form(Form.builder()
                                        .build())
                                .signature("User A")
                                .build())
                        .build())
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build());

        assertEquals(1, validator.getMessages().size());
    }

    @Test
    public void isValid_ProvidedAttestationPeriodIsNotSubmittable_OncUser_NoErrors() throws EntityRetrievalException {
        Mockito.when(attestationManager.canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleAdmin()).thenReturn(false);
        AttestationPeriod submittablePeriod = AttestationPeriod.builder()
                .id(1L)
                .build();
        Mockito.when(attestationPeriodService.getSubmittableAttestationPeriod(ArgumentMatchers.anyLong()))
            .thenReturn(submittablePeriod);
        Mockito.when(formValidator.validate(ArgumentMatchers.any(Form.class))).thenReturn(
                FormValidationResult.builder()
                        .errorMessages(new ArrayList<String>())
                        .valid(true)
                        .build());
        Mockito.when(formValidator.removePhantomAndDuplicateResponses(ArgumentMatchers.any(Form.class)))
            .thenAnswer(i -> i.getArgument(0));

        JWTAuthenticatedUser currentUser = new JWTAuthenticatedUser();
        currentUser.setFullName("User A");

        validator.isValid(ChangeRequestValidationContext.builder()
                .attestationPeriodService(attestationPeriodService)
                .domainManagers(ChangeRequestValidationContext.DomainManagers.builder()
                        .attestationManager(attestationManager)
                        .build())
                .formValidator(formValidator)
                .currentUser(currentUser)
                .newChangeRequest(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .details(ChangeRequestAttestationSubmission.builder()
                                .attestationPeriod(AttestationPeriod.builder()
                                        .id(2L)
                                        .build())
                                .form(Form.builder()
                                        .build())
                                .signature("User A")
                                .build())
                        .build())
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build());

        assertEquals(0, validator.getMessages().size());
    }

    @Test
    public void isValid_DeveloperHasNoSubmittableAttestationPeriod_MessageIsGenerated() throws EntityRetrievalException {
        Mockito.when(attestationManager.canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleAdmin()).thenReturn(false);
        Mockito.when(attestationPeriodService.getSubmittableAttestationPeriod(ArgumentMatchers.anyLong()))
            .thenReturn(null);
        Mockito.when(formValidator.validate(ArgumentMatchers.any(Form.class))).thenReturn(
                FormValidationResult.builder()
                        .errorMessages(new ArrayList<String>())
                        .valid(true)
                        .build());
        Mockito.when(formValidator.removePhantomAndDuplicateResponses(ArgumentMatchers.any(Form.class)))
            .thenAnswer(i -> i.getArgument(0));

        JWTAuthenticatedUser currentUser = new JWTAuthenticatedUser();
        currentUser.setFullName("User A");

        validator.isValid(ChangeRequestValidationContext.builder()
                .attestationPeriodService(attestationPeriodService)
                .domainManagers(ChangeRequestValidationContext.DomainManagers.builder()
                        .attestationManager(attestationManager)
                        .build())
                .formValidator(formValidator)
                .currentUser(currentUser)
                .newChangeRequest(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .details(ChangeRequestAttestationSubmission.builder()
                                .attestationPeriod(AttestationPeriod.builder()
                                        .id(2L)
                                        .build())
                                .form(Form.builder()
                                        .build())
                                .signature("User A")
                                .build())
                        .build())
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build());

        assertEquals(1, validator.getMessages().size());
    }

    @Test
    public void isValid_DeveloperHasNoSubmittableAttestationPeriod_OncUser_NoErrors() throws EntityRetrievalException {
        Mockito.when(attestationManager.canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleAdmin()).thenReturn(false);
        Mockito.when(attestationPeriodService.getSubmittableAttestationPeriod(ArgumentMatchers.anyLong()))
            .thenReturn(null);
        Mockito.when(formValidator.validate(ArgumentMatchers.any(Form.class))).thenReturn(
                FormValidationResult.builder()
                        .errorMessages(new ArrayList<String>())
                        .valid(true)
                        .build());
        Mockito.when(formValidator.removePhantomAndDuplicateResponses(ArgumentMatchers.any(Form.class)))
            .thenAnswer(i -> i.getArgument(0));

        JWTAuthenticatedUser currentUser = new JWTAuthenticatedUser();
        currentUser.setFullName("User A");

        validator.isValid(ChangeRequestValidationContext.builder()
                .attestationPeriodService(attestationPeriodService)
                .domainManagers(ChangeRequestValidationContext.DomainManagers.builder()
                        .attestationManager(attestationManager)
                        .build())
                .formValidator(formValidator)
                .currentUser(currentUser)
                .newChangeRequest(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .details(ChangeRequestAttestationSubmission.builder()
                                .attestationPeriod(AttestationPeriod.builder()
                                        .id(2L)
                                        .build())
                                .form(Form.builder()
                                        .build())
                                .signature("User A")
                                .build())
                        .build())
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build());

        assertEquals(0, validator.getMessages().size());
    }

    @Test
    public void isValid_DeveloperCannotSubmitAttestationCr_MessageIsGenerated() throws EntityRetrievalException {
        Mockito.when(attestationManager.canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong())).thenReturn(false);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleAdmin()).thenReturn(false);
        AttestationPeriod submittablePeriod = AttestationPeriod.builder()
                .id(1L)
                .build();
        Mockito.when(attestationPeriodService.getSubmittableAttestationPeriod(ArgumentMatchers.anyLong()))
            .thenReturn(submittablePeriod);
        Mockito.when(formValidator.validate(ArgumentMatchers.any(Form.class))).thenReturn(
                FormValidationResult.builder()
                        .errorMessages(new ArrayList<String>())
                        .valid(true)
                        .build());
        Mockito.when(formValidator.removePhantomAndDuplicateResponses(ArgumentMatchers.any(Form.class)))
            .thenAnswer(i -> i.getArgument(0));

        JWTAuthenticatedUser currentUser = new JWTAuthenticatedUser();
        currentUser.setFullName("User A");

        validator.isValid(ChangeRequestValidationContext.builder()
                .attestationPeriodService(attestationPeriodService)
                .domainManagers(ChangeRequestValidationContext.DomainManagers.builder()
                        .attestationManager(attestationManager)
                        .build())
                .formValidator(formValidator)
                .currentUser(currentUser)
                .newChangeRequest(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .details(ChangeRequestAttestationSubmission.builder()
                                .attestationPeriod(submittablePeriod)
                                .form(Form.builder()
                                        .build())
                                .signature("User A")
                                .build())
                        .build())
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build());

        assertEquals(1, validator.getMessages().size());
    }

    @Test
    public void isValid_SignatureNotValid_MessageIsGenerated() throws EntityRetrievalException {
        Mockito.when(attestationManager.canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleAdmin()).thenReturn(false);
        AttestationPeriod submittablePeriod = AttestationPeriod.builder()
                .id(1L)
                .build();
        Mockito.when(attestationPeriodService.getSubmittableAttestationPeriod(ArgumentMatchers.anyLong()))
            .thenReturn(submittablePeriod);
        Mockito.when(formValidator.validate(ArgumentMatchers.any(Form.class))).thenReturn(
                FormValidationResult.builder()
                        .errorMessages(new ArrayList<String>())
                        .valid(true)
                        .build());

        JWTAuthenticatedUser currentUser = new JWTAuthenticatedUser();
        currentUser.setFullName("User A");

        validator.isValid(ChangeRequestValidationContext.builder()
                .attestationPeriodService(attestationPeriodService)
                .domainManagers(ChangeRequestValidationContext.DomainManagers.builder()
                        .attestationManager(attestationManager)
                        .build())
                .formValidator(formValidator)
                .currentUser(currentUser)
                .newChangeRequest(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .details(ChangeRequestAttestationSubmission.builder()
                                .attestationPeriod(submittablePeriod)
                                .form(Form.builder()
                                        .build())
                                .signature("User B")
                                .build())
                        .build())
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build());

        assertEquals(1, validator.getMessages().size());
    }

    @Test
    public void isValid_FormValidationFails_MessageIsGenerated() throws EntityRetrievalException {
        Mockito.when(attestationManager.canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleAdmin()).thenReturn(false);
        AttestationPeriod submittablePeriod = AttestationPeriod.builder()
                .id(1L)
                .build();
        Mockito.when(attestationPeriodService.getSubmittableAttestationPeriod(ArgumentMatchers.anyLong()))
            .thenReturn(submittablePeriod);
        Mockito.when(formValidator.validate(ArgumentMatchers.any(Form.class))).thenReturn(
                FormValidationResult.builder()
                        .errorMessages(List.of("Form validation error message"))
                        .valid(false)
                        .build());

        JWTAuthenticatedUser currentUser = new JWTAuthenticatedUser();
        currentUser.setFullName("User A");

        validator.isValid(ChangeRequestValidationContext.builder()
                .attestationPeriodService(attestationPeriodService)
                .domainManagers(ChangeRequestValidationContext.DomainManagers.builder()
                        .attestationManager(attestationManager)
                        .build())
                .formValidator(formValidator)
                .currentUser(currentUser)
                .newChangeRequest(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .details(ChangeRequestAttestationSubmission.builder()
                                .attestationPeriod(submittablePeriod)
                                .form(Form.builder()
                                        .build())
                                .signature("User A")
                                .build())
                        .build())
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build());

        assertEquals(1, validator.getMessages().size());
    }

    @Test
    public void isValid_ValidAttestationRequest_NoErrors() throws EntityRetrievalException {
        Mockito.when(attestationManager.canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissionsFactory.get().isUserRoleAdmin()).thenReturn(false);
        AttestationPeriod submittablePeriod = AttestationPeriod.builder()
                .id(1L)
                .build();
        Mockito.when(attestationPeriodService.getSubmittableAttestationPeriod(ArgumentMatchers.anyLong()))
            .thenReturn(submittablePeriod);
        Mockito.when(formValidator.validate(ArgumentMatchers.any(Form.class))).thenReturn(
                FormValidationResult.builder()
                        .errorMessages(new ArrayList<String>())
                        .valid(true)
                        .build());
        Mockito.when(formValidator.removePhantomAndDuplicateResponses(ArgumentMatchers.any(Form.class)))
            .thenAnswer(i -> i.getArgument(0));

        JWTAuthenticatedUser currentUser = new JWTAuthenticatedUser();
        currentUser.setFullName("User A");

        validator.isValid(ChangeRequestValidationContext.builder()
                .attestationPeriodService(attestationPeriodService)
                .domainManagers(ChangeRequestValidationContext.DomainManagers.builder()
                        .attestationManager(attestationManager)
                        .build())
                .formValidator(formValidator)
                .currentUser(currentUser)
                .newChangeRequest(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .details(ChangeRequestAttestationSubmission.builder()
                                .attestationPeriod(submittablePeriod)
                                .form(Form.builder()
                                        .build())
                                .signature("User A")
                                .build())
                        .build())
                .resourcePermissionsFactory(resourcePermissionsFactory)
                .build());

        assertEquals(0, validator.getMessages().size());
    }
}
