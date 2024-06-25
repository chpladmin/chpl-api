package gov.healthit.chpl.user.cognito;

import java.util.UUID;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Basic(optional = false)
    @Column(name = "group_name")
    private String groupName;

    @Basic(optional = false)
    @Column(name = "organization_id")
    private Long organizationId;

    public CognitoUserInvitation toDomain() {
        return CognitoUserInvitation.builder()
                .id(id)
                .email(email)
                .invitationToken(token)
                .groupName(groupName)
                .organizationId(organizationId)
                .lastModifiedDate(getLastModifiedDate())
                .creationDate(getCreationDate())
                .lasModifiedSsoUser(getLastModifiedSsoUser())
                .lastModifiedUserId(getLastModifiedUser())
                .build();
    }
}
