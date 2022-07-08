package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.attestation.manager.AttestationManager;

public class AttestationValidationTest {

    private AttestationValidation validator;
    private AttestationManager attestationManager;

    /**************
    @Before
    public void setup() throws EntityRetrievalException {
        attestationManager = Mockito.mock(AttestationManager.class);
        Mockito.when(attestationManager.getAttestationForm()).thenReturn(getAttestationForm());
        Mockito.when(attestationManager.canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong())).thenReturn(true);

        validator = new AttestationValidation();
    }

    @Test
    public void isValid_AttestationSubmissionIsValid_ReturnsTrue() {
        ChangeRequest cr = ChangeRequest.builder()
                .developer(Developer.builder()
                        .id(1l)
                        .build())
                .details(createChangeRequestAttestationSubmission())
                .build();

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(cr)
                .domainManagers(new DomainManagers(attestationManager))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_AttestationSubmissionIsMissingSignature_ReturnsFalse() {
        ChangeRequestAttestationSubmission details = createChangeRequestAttestationSubmission();
        details.setSignature(null);

        ChangeRequest cr = ChangeRequest.builder()
                .developer(Developer.builder()
                        .id(1l)
                        .build())
                .details(details)
                .build();

        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(cr)
                .domainManagers(new DomainManagers(attestationManager))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_AttestationSubmissionIsSignatureDoesNotMatch_ReturnsFalse() {
        ChangeRequestAttestationSubmission details = createChangeRequestAttestationSubmission();
        details.setSignature("Another User");

        ChangeRequest cr = ChangeRequest.builder()
                .developer(Developer.builder()
                        .id(1l)
                        .build())
                .details(details)
                .build();


        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(cr)
                .domainManagers(new DomainManagers(attestationManager))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_AttestationSubmissionHasMissingAttestation_ReturnsFalse() {
        ChangeRequestAttestationSubmission details = createChangeRequestAttestationSubmission();
        details.setAttestationResponses(new ArrayList<AttestationSubmittedResponse>(details.getAttestationResponses()));
        details.getAttestationResponses().remove(0);

        ChangeRequest cr = ChangeRequest.builder()
                .developer(Developer.builder()
                        .id(1l)
                        .build())
                .details(details)
                .build();


        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(cr)
                .domainManagers(new DomainManagers(attestationManager))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    @Test
    public void isValid_AttestationSubmissionHasInvalidResponse_ReturnsFalse() {
        ChangeRequestAttestationSubmission details = createChangeRequestAttestationSubmission();
        details.setAttestationResponses(new ArrayList<AttestationSubmittedResponse>(details.getAttestationResponses()));
        details.getAttestationResponses().get(0).setResponse(ValidResponse.builder().id(5L).build());

        ChangeRequest cr = ChangeRequest.builder()
                .developer(Developer.builder()
                        .id(1l)
                        .build())
                .details(details)
                .build();


        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .currentUser(new CurrentUser())
                .newChangeRequest(cr)
                .domainManagers(new DomainManagers(attestationManager))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
    }

    private ChangeRequestAttestationSubmission createChangeRequestAttestationSubmission() {
        return ChangeRequestAttestationSubmission.builder()
                .signature("Test User")
                .attestationResponse(AttestationSubmittedResponse.builder()
                                .attestation(Attestation.builder()
                                        .id(1L)
                                        .build())
                                .response(ValidResponse.builder()
                                        .id(1L)
                                        .build())
                                .build())
                .attestationResponse(AttestationSubmittedResponse.builder()
                        .attestation(Attestation.builder()
                                .id(2L)
                                .build())
                        .response(ValidResponse.builder()
                                .id(2L)
                                .build())
                        .build())
                .attestationResponse(AttestationSubmittedResponse.builder()
                        .attestation(Attestation.builder()
                                .id(3L)
                                .build())
                        .response(ValidResponse.builder()
                                .id(1L)
                                .build())
                        .build())
                .attestationResponse(AttestationSubmittedResponse.builder()
                        .attestation(Attestation.builder()
                                .id(4L)
                                .build())
                        .response(ValidResponse.builder()
                                .id(5L)
                                .build())
                        .build())
                .attestationResponse(AttestationSubmittedResponse.builder()
                        .attestation(Attestation.builder()
                                .id(5L)
                                .build())
                        .response(ValidResponse.builder()
                                .id(1L)
                                .build())
                        .build())
                .build();
    }

    private AttestationPeriodForm getAttestationForm() {
        return AttestationPeriodForm.builder()
                .attestation(Attestation.builder()
                        .id(1L)
                        .description("Attestation 1")
                        .validResponse(ValidResponse.builder().id(1L).response("respo 1").build())
                        .validResponse(ValidResponse.builder().id(4L).response("respo 4").build())
                        .build())
                .attestation(Attestation.builder()
                        .id(2L)
                        .description("Attestation 2")
                        .validResponse(ValidResponse.builder().id(2L).response("respo 2").build())
                        .validResponse(ValidResponse.builder().id(3L).response("respo 3").build())
                        .validResponse(ValidResponse.builder().id(4L).response("respo 4").build())
                        .build())
                .attestation(Attestation.builder()
                        .id(3L)
                        .description("Attestation 3")
                        .validResponse(ValidResponse.builder().id(1L).response("respo 1").build())
                        .validResponse(ValidResponse.builder().id(4L).response("respo 4").build())
                        .build())
                .attestation(Attestation.builder()
                        .id(4L)
                        .description("Attestation 4")
                        .validResponse(ValidResponse.builder().id(1L).build())
                        .validResponse(ValidResponse.builder().id(4L).response("respo 4").build())
                        .validResponse(ValidResponse.builder().id(5L).response("respo 5").build())
                        .build())
                .attestation(Attestation.builder()
                        .id(5L)
                        .description("Attestation 5")
                        .validResponse(ValidResponse.builder().id(1L).build())
                        .validResponse(ValidResponse.builder().id(4L).response("respo 4").build())
                        .validResponse(ValidResponse.builder().id(5L).response("respo 5").build())
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
    **************/
}
