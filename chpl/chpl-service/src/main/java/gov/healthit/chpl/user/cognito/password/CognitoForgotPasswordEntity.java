package gov.healthit.chpl.user.cognito.password;

import java.util.UUID;

import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "forgot_password")
@Data
public class CognitoForgotPasswordEntity extends EntityAudit {
    private static final long serialVersionUID = 7119092555583820361L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "email")
    private String email;

    @Basic(optional = false)
    @Column(name = "token")
    private UUID token;

    public CognitoForgotPassword toDomain() {
        return CognitoForgotPassword.builder()
                .id(id)
                .email(email)
                .token(token)
                .lastModifiedDate(getLastModifiedDate())
                .creationDate(getCreationDate())
                .lasModifiedSsoUser(getLastModifiedSsoUser())
                .lastModifiedUserId(getLastModifiedUser())
                .build();
    }

}
