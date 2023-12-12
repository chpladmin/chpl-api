package gov.healthit.chpl.entity.auth;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "invited_user")
public class InvitationEntity extends EntityAudit {
    private static final long serialVersionUID = -56212582485414978L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invited_user_id", nullable = false)
    private Long id;

    @Column(name = "email", unique = true)
    private String emailAddress;

    @Column(name = "user_permission_id")
    private Long userPermissionId;

    @Column(name = "permission_object_id")
    private Long permissionObjectId;

    @Column(name = "invite_token", unique = true)
    private String inviteToken;

    @Column(name = "confirm_token", unique = true)
    private String confirmToken;

    @Column(name = "created_user_id", unique = true)
    private Long createdUserId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_permission_id", insertable = false, updatable = false)
    private UserPermissionEntity permission;

    public UserInvitation toDomain() {
        return UserInvitation.builder()
                .id(this.getId())
                .hash(this.getInviteToken())
                .emailAddress(this.getEmailAddress())
                .permissionObjectId(this.getPermissionObjectId())
                .role(this.getPermission().getAuthority())
                .permission(this.getPermission().toDomain())
                .invitationToken(this.getInviteToken())
                .confirmationToken(this.getConfirmToken())
                .createdUserId(this.getCreatedUserId())
                .lastModifiedDate(this.getLastModifiedDate())
                .creationDate(this.getCreationDate())
                .lastModifiedUserId(this.getLastModifiedUser())
                .build();
    }

}
