package gov.healthit.chpl.entity.auth;

import java.util.Date;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "`user`")
// Setting dynamic update makes the hql engine generate new sql for any update
// call
// and will exclude any unmodified columns from the update.
// We need this because of the user_soft_delete trigger which is getting called
// whenever the delete column is updated. We don't want to un-delete
// associations
// for the user that were already marked deleted any time the user "delete"
// column is
// included in an update statement (even if its value hasn't changed).
@org.hibernate.annotations.DynamicUpdate
public class UserEntity extends EntityAudit {
    private static final long serialVersionUID = -5792083881155731413L;

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", unique = true)
    private String subjectName;

    @Column(name = "user_permission_id")
    private Long userPermissionId;

    @Column(name = "account_expired")
    private boolean accountExpired;

    @Column(name = "account_locked")
    private boolean accountLocked;

    @Column(name = "credentials_expired")
    private boolean credentialsExpired;

    @Column(name = "account_enabled")
    private boolean accountEnabled;

    @Column(name = "password_reset_required")
    private boolean passwordResetRequired;

    @Column(name = "failed_login_count")
    private int failedLoginCount;

    @Column(name = "last_logged_in_date")
    private Date lastLoggedInDate;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_permission_id", insertable = false, updatable = false)
    private UserPermissionEntity permission;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_id", unique = true, nullable = false)
    private UserContactEntity contact;

    public User toDomain() {
        User user = new User();
        user.setUserId(this.getId());
        if (this.getPermission() != null) {
            user.setRole(this.getPermission().getAuthority());
        }
        user.setSubjectName(this.getSubjectName());
        user.setFullName(this.getContact().getFullName());
        user.setEmail(this.getContact().getEmail());
        user.setPhoneNumber(this.getContact().getPhoneNumber());
        user.setAccountLocked(this.isAccountLocked());
        user.setAccountEnabled(this.isAccountEnabled());
        user.setCredentialsExpired(this.isCredentialsExpired());
        user.setPasswordResetRequired(this.isPasswordResetRequired());
        user.setLastLoggedInDate(this.getLastLoggedInDate());
        return user;
    }
}
