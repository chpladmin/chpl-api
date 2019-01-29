package gov.healthit.chpl.auth.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import gov.healthit.chpl.auth.dto.UserPermissionDTO;

/**
 * Entity for user.
 */
@Entity
@Table(name = "`user`")
public class UserEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy  =  GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", unique = true)
    private String subjectName;

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

    @Column(name = "compliance_signature")
    private Date complianceSignature;

    @Column(name = "password_reset_required")
    private boolean passwordResetRequired;

    @Column(name = "failed_login_count")
    private int failedLoginCount;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "deleted")
    private Boolean deleted;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserPermissionUserMappingEntity> permissionMappings;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_id", unique = true, nullable = false)
    private UserContactEntity contact;
    
    /** Default constructor. */
    public UserEntity() {
        this.subjectName = null;
        this.password = null;
        this.accountExpired = false;
        this.accountLocked = false;
        this.credentialsExpired = false;
        this.accountEnabled = true;
        this.passwordResetRequired = false;

        this.contact = new UserContactEntity();
    }

    /**
     * Constructor with subjectName.
     * @param subjectName the user's subjectname / username
     */
    public UserEntity(final String subjectName) {
        this.subjectName = subjectName;
        this.password = null;
        this.accountExpired = false;
        this.accountLocked = false;
        this.credentialsExpired = false;
        this.accountEnabled = true;
        this.passwordResetRequired = false;

        this.contact = new UserContactEntity();
    }

    /**
     * Constructor with un/pw.
     * @param subjectName the username
     * @param encodedPassword the password
     */
    public UserEntity(final String subjectName, final String encodedPassword) {
        this.subjectName = subjectName;
        this.password = encodedPassword;
        this.accountExpired = false;
        this.accountLocked = false;
        this.credentialsExpired = false;
        this.accountEnabled = true;
        this.passwordResetRequired = false;

        this.contact = new UserContactEntity();
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(final String subject) {
        this.subjectName = subject;
    }

    public String getFullName() {
        return contact.getFullName();
    }

    /**
     * Set the user's contact's full name value.
     * @param fullName the new value
     */
    public void setFullName(final String fullName) {
        contact.setFullName(fullName);
    }

    public String getFriendlyName() {
        return contact.getFriendlyName();
    }

    /**
     * Set the user's contact's friendly name value.
     * @param friendlyName the new value
     */
    public void setFriendlyName(final String friendlyName) {
        contact.setFriendlyName(friendlyName);
    }

    /**
     * Retrieve the set of permissions the user has.
     * @return that set
     */
    public Set<UserPermissionDTO> getPermissions() {

        Set<UserPermissionDTO> permissions = new HashSet<UserPermissionDTO>();

        for (UserPermissionUserMappingEntity mapping : permissionMappings) {

            permissions.add(new UserPermissionDTO(mapping.getPermission()));
        }
        return permissions;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String encodedPassword) {
        this.password = encodedPassword;
    }

    public String getUsername() {
        return subjectName;
    }

    public boolean isAccountNonExpired() {
        return !accountExpired;
    }

    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    public boolean isEnabled() {
        return accountEnabled;
    }

    public Long getId() {
        return id;
    }

    public UserContactEntity getContact() {
        return contact;
    }

    public void setContact(final UserContactEntity contact) {
        this.contact = contact;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public void setAccountExpired(final boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public void setAccountLocked(final boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public void setCredentialsExpired(final boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public void setAccountEnabled(final boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }


    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getComplianceSignature() {
        return complianceSignature;
    }

    public void setComplianceSignature(final Date complianceSignature) {
        this.complianceSignature = complianceSignature;
    }

    public int getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(final int failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    public Set<UserPermissionUserMappingEntity> getPermissionMappings() {
        return permissionMappings;
    }

    public void setPermissionMappings(final Set<UserPermissionUserMappingEntity> permissionMappings) {
        this.permissionMappings = permissionMappings;
    }

    public boolean isAccountExpired() {
        return accountExpired;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    public boolean isAccountEnabled() {
        return accountEnabled;
    }

    public Boolean getPasswordResetRequired() {
        return passwordResetRequired;
    }

    public void setPasswordResetRequired(final Boolean passwordResetRequired) {
        this.passwordResetRequired = passwordResetRequired;
    }

    @Override
    public String toString() {
        String ret = "[UserEntity: "
                + "[id: " + this.id + "]"
                + "[subjectName: " + this.subjectName + "]"
                + "[contact: " + this.contact + "]"
                + "[failedLoginCount: " + this.failedLoginCount + "]"
                + "[accountExpired: " + this.accountExpired + "]"
                + "[accountLocked: " + this.accountLocked + "]"
                + "[credentialsExpired: " + this.credentialsExpired + "]"
                + "[accountEnabled: " + this.accountEnabled + "]"
                + "[passwordResetRequired: " + this.passwordResetRequired + "]]";
        return ret;
    }
}