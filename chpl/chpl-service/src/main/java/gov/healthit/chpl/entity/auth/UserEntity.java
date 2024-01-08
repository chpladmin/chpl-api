package gov.healthit.chpl.entity.auth;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

    @Column(name = "password")
    private String password = null;

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
}
