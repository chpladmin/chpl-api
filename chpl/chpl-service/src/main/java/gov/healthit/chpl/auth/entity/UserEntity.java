package gov.healthit.chpl.auth.entity;

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

/**
 * Entity for user.
 */
@Entity
@Table(name = "`user`")
public class UserEntity {
    private static final long serialVersionUID = -5792083881155731413L;

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy  =  GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", unique = true)
    private String subjectName;

    @Column(name = "user_permission_id")
    private Long userPermissionId;

    @Column(name = "password")
    private String password  =  null;

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

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_permission_id", insertable = false, updatable = false)
    private UserPermissionEntity permission;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_id", unique = true, nullable = false)
    private UserContactEntity contact;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(final String subjectName) {
        this.subjectName = subjectName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public boolean isAccountExpired() {
        return accountExpired;
    }

    public void setAccountExpired(final boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(final boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    public void setCredentialsExpired(final boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public boolean isAccountEnabled() {
        return accountEnabled;
    }

    public void setAccountEnabled(final boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    public boolean isPasswordResetRequired() {
        return passwordResetRequired;
    }

    public void setPasswordResetRequired(final boolean passwordResetRequired) {
        this.passwordResetRequired = passwordResetRequired;
    }

    public int getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(final int failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public UserPermissionEntity getPermission() {
        return permission;
    }

    public void setPermission(final UserPermissionEntity permission) {
        this.permission = permission;
    }

    public UserContactEntity getContact() {
        return contact;
    }

    public void setContact(final UserContactEntity contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "UserEntity [id=" + id + ", subjectName=" + subjectName + ", password=" + password + ", accountExpired="
                + accountExpired + ", accountLocked=" + accountLocked + ", credentialsExpired=" + credentialsExpired
                + ", accountEnabled=" + accountEnabled + ", passwordResetRequired=" + passwordResetRequired
                + ", failedLoginCount=" + failedLoginCount + ", deleted=" + deleted + ", lastModifiedUser="
                + lastModifiedUser + ", creationDate=" + creationDate + ", lastModifiedDate=" + lastModifiedDate
                + ", permission=" + permission + ", contact=" + contact + "]";
    }

    public Long getUserPermissionId() {
        return userPermissionId;
    }

    public void setUserPermissionId(Long userPermissionId) {
        this.userPermissionId = userPermissionId;
    }
}
