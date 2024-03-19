package gov.healthit.chpl.user.cognito;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_invitation")
@Data
public class CognitoUserInvitationEntity extends EntityAudit {

    private static final long serialVersionUID = -3646722799251141040L;

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


    public CognitoUserInvitation toDomain() {
        return CognitoUserInvitation.builder()
                .id(id)
                .email(email)
                .invitationToken(token)
                .lastModifiedDate(getLastModifiedDate())
                .creationDate(getCreationDate())
                .lasModifiedSsoUser(getLastModifiedSsoUser())
                .lastModifiedUserId(getLastModifiedUser())
                .build();
    }
}
