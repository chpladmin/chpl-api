package gov.healthit.chpl.domain.auth;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class AuthorizeCredentials extends LoginCredentials implements Serializable {
    private static final long serialVersionUID = 5419635752541177318L;
    private String hash;
}
