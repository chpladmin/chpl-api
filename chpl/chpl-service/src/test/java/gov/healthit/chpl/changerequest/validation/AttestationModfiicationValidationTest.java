package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.attestation.domain.Attestation;
import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationSubmittedResponse;
import gov.healthit.chpl.attestation.domain.AttestationValidResponse;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ChangeRequestStatusIds;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.DomainManagers;

public class AttestationModfiicationValidationTest {
    private static final Long PENDING_ACB_ACTION = 1L;
    private static final Long PENDING_DEVELOPER_ACTION = 2L;
    private static final Long ACCEPTED = 3L;
    private static final Long REJECTED = 4L;
    private static final Long CANCELLED_BY_REQUESTER = 5L;

    private AttestationModificationValidation validator;
    private AttestationManager attestationManager;

    @Before
    public void setup() {
        attestationManager = Mockito.mock(AttestationManager.class);
        Mockito.when(attestationManager.getAttestationForm()).thenReturn(getAttestationForm());
        validator = new AttestationModificationValidation();
    }

    @Test
    public void isValid_AttestationUpdateDetailsMatch_ReturnsTrue() {
        ChangeRequest cr = ChangeRequest.builder()
                .details(convertToMap(createChangeRequestAttestationSubmission()))
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(cr)
                .origChangeRequest(cr)
                .domainManagers(new DomainManagers(attestationManager))
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);
        assertTrue(isValid);
    }

    @Test
    public void isValid_AttestationUpdateStatusDiffersDetailsMatch_ReturnsTrue() {
        ChangeRequest crOrig = ChangeRequest.builder()
                .details(convertToMap(createChangeRequestAttestationSubmission()))
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();
        ChangeRequest crNew = ChangeRequest.builder()
                .details(convertToMap(createChangeRequestAttestationSubmission()))
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(ACCEPTED)
                                .build())
                        .build())
                .build();

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(crNew)
                .origChangeRequest(crOrig)
                .domainManagers(new DomainManagers(attestationManager))
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);
        assertTrue(isValid);
    }

    @Test
    public void isValid_AttestationUpdateStatusMatchesDetailsSignatureDiffers_ReturnsFalse() {
        ChangeRequest crOrig = ChangeRequest.builder()
                .details(convertToMap(createChangeRequestAttestationSubmission()))
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();
        ChangeRequest crNew = ChangeRequest.builder()
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();
        ChangeRequestAttestationSubmission details = createChangeRequestAttestationSubmission();
        details.setSignature("Something Different");
        crNew.setDetails(convertToMap(details));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(crNew)
                .origChangeRequest(crOrig)
                .domainManagers(new DomainManagers(attestationManager))
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);
        assertFalse(isValid);
    }

    @Test
    public void isValid_AttestationUpdateStatusMatchesDetailsSignatureEmailDiffers_ReturnsFalse() {
        ChangeRequest crOrig = ChangeRequest.builder()
                .details(convertToMap(createChangeRequestAttestationSubmission()))
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();
        ChangeRequest crNew = ChangeRequest.builder()
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();
        ChangeRequestAttestationSubmission details = createChangeRequestAttestationSubmission();
        details.setSignatureEmail("different@email.com");
        crNew.setDetails(convertToMap(details));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(crNew)
                .origChangeRequest(crOrig)
                .domainManagers(new DomainManagers(attestationManager))
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);
        assertFalse(isValid);
    }

    @Test
    public void isValid_AttestationUpdateStatusMatchesDetailsAttestationResponseDiffers_ReturnsFalse() {
        ChangeRequest crOrig = ChangeRequest.builder()
                .details(convertToMap(createChangeRequestAttestationSubmission()))
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();
        ChangeRequest crNew = ChangeRequest.builder()
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();
        ChangeRequestAttestationSubmission details = createChangeRequestAttestationSubmission();
        details.setAttestationResponses(Stream.of(AttestationSubmittedResponse.builder()
                                .attestation(Attestation.builder()
                                        .id(1L)
                                        .build())
                                .response(AttestationValidResponse.builder()
                                        .id(1L)
                                        .build())
                                .build()).collect(Collectors.toList()));
        crNew.setDetails(convertToMap(details));

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(crNew)
                .origChangeRequest(crOrig)
                .domainManagers(new DomainManagers(attestationManager))
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);
        assertFalse(isValid);
    }

    @Test
    public void isValid_AttestationUpdateStatusMatchesNewDetailsNull_ReturnsFalse() {
        ChangeRequest crOrig = ChangeRequest.builder()
                .details(convertToMap(createChangeRequestAttestationSubmission()))
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();
        ChangeRequest crNew = ChangeRequest.builder()
                .details(null)
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(crNew)
                .origChangeRequest(crOrig)
                .domainManagers(new DomainManagers(attestationManager))
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);
        assertFalse(isValid);
    }

    @Test
    public void isValid_AttestationUpdateStatusMatchesOrigDetailsNull_ReturnsFalse() {
        ChangeRequest crOrig = ChangeRequest.builder()
                .details(null)
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();
        ChangeRequest crNew = ChangeRequest.builder()
                .details(convertToMap(createChangeRequestAttestationSubmission()))
                .currentStatus(ChangeRequestStatus.builder()
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(PENDING_ACB_ACTION)
                                .build())
                        .build())
                .build();

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(crNew)
                .origChangeRequest(crOrig)
                .domainManagers(new DomainManagers(attestationManager))
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);
        assertFalse(isValid);
    }

    private Map<String, Object> convertToMap(ChangeRequestAttestationSubmission attestationSubmission) {
        ObjectMapper mapper = new ObjectMapper();
        return  mapper.convertValue(attestationSubmission, Map.class);
    }


    private ChangeRequestAttestationSubmission createChangeRequestAttestationSubmission() {
        return ChangeRequestAttestationSubmission.builder()
                .signature("Test User")
                .attestationResponse(AttestationSubmittedResponse.builder()
                                .attestation(Attestation.builder()
                                        .id(1L)
                                        .build())
                                .response(AttestationValidResponse.builder()
                                        .id(1L)
                                        .build())
                                .build())
                .attestationResponse(AttestationSubmittedResponse.builder()
                        .attestation(Attestation.builder()
                                .id(2L)
                                .build())
                        .response(AttestationValidResponse.builder()
                                .id(2L)
                                .build())
                        .build())
                .attestationResponse(AttestationSubmittedResponse.builder()
                        .attestation(Attestation.builder()
                                .id(3L)
                                .build())
                        .response(AttestationValidResponse.builder()
                                .id(1L)
                                .build())
                        .build())
                .attestationResponse(AttestationSubmittedResponse.builder()
                        .attestation(Attestation.builder()
                                .id(4L)
                                .build())
                        .response(AttestationValidResponse.builder()
                                .id(5L)
                                .build())
                        .build())
                .attestationResponse(AttestationSubmittedResponse.builder()
                        .attestation(Attestation.builder()
                                .id(5L)
                                .build())
                        .response(AttestationValidResponse.builder()
                                .id(1L)
                                .build())
                        .build())
                .build();
    }

    private AttestationForm getAttestationForm() {
        return AttestationForm.builder()
                .attestation(Attestation.builder()
                        .id(1L)
                        .description("Attestation 1")
                        .validResponse(AttestationValidResponse.builder().id(1L).response("respo 1").build())
                        .validResponse(AttestationValidResponse.builder().id(4L).response("respo 4").build())
                        .build())
                .attestation(Attestation.builder()
                        .id(2L)
                        .description("Attestation 2")
                        .validResponse(AttestationValidResponse.builder().id(2L).response("respo 2").build())
                        .validResponse(AttestationValidResponse.builder().id(3L).response("respo 3").build())
                        .validResponse(AttestationValidResponse.builder().id(4L).response("respo 4").build())
                        .build())
                .attestation(Attestation.builder()
                        .id(3L)
                        .description("Attestation 3")
                        .validResponse(AttestationValidResponse.builder().id(1L).response("respo 1").build())
                        .validResponse(AttestationValidResponse.builder().id(4L).response("respo 4").build())
                        .build())
                .attestation(Attestation.builder()
                        .id(4L)
                        .description("Attestation 4")
                        .validResponse(AttestationValidResponse.builder().id(1L).build())
                        .validResponse(AttestationValidResponse.builder().id(4L).response("respo 4").build())
                        .validResponse(AttestationValidResponse.builder().id(5L).response("respo 5").build())
                        .build())
                .attestation(Attestation.builder()
                        .id(5L)
                        .description("Attestation 5")
                        .validResponse(AttestationValidResponse.builder().id(1L).build())
                        .validResponse(AttestationValidResponse.builder().id(4L).response("respo 4").build())
                        .validResponse(AttestationValidResponse.builder().id(5L).response("respo 5").build())
                        .build())
                .build();
    }

    class CurrentUser implements gov.healthit.chpl.auth.user.User {

        @Override
        public Long getId() {
            return null;
        }

        @Override
        public String getSubjectName() {
            return null;
        }

        @Override
        public void setSubjectName(String subject) {

        }

        @Override
        public void setFullName(String fullName) {

        }

        @Override
        public String getFullName() {

            return "Test User";
        }

        @Override
        public void setFriendlyName(String friendlyName) {

        }

        @Override
        public String getFriendlyName() {
            return null;
        }

        @Override
        public boolean getPasswordResetRequired() {
            return false;
        }

        @Override
        public void setPasswordResetRequired(boolean setPasswordResetRequired) {

        }

        @Override
        public Set<GrantedPermission> getPermissions() {
            return null;
        }

        @Override
        public void addPermission(GrantedPermission permission) {

        }

        @Override
        public void removePermission(String permissionValue) {

        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getUsername() {
            return null;
        }

        @Override
        public boolean isAccountNonExpired() {
            return false;
        }

        @Override
        public boolean isAccountNonLocked() {
            return false;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return null;
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public void setAuthenticated(boolean arg0) throws IllegalArgumentException {

        }

        @Override
        public String getName() {
            return null;
        }

    }
}
