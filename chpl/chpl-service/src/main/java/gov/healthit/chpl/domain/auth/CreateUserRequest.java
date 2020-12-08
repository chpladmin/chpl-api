package gov.healthit.chpl.domain.auth;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreateUserRequest implements Serializable {
    private static final long serialVersionUID = -8036620754066927881L;

    private String role;
    private String fullName;
    private String friendlyName;
    private String email;
    private String phoneNumber;
    private String title = null;
    private String password = null;
    private Boolean complianceTermsAccepted = Boolean.FALSE;
}
