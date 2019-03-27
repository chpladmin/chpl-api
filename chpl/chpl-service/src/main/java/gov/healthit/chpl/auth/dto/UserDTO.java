package gov.healthit.chpl.auth.dto;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import gov.healthit.chpl.auth.entity.UserEntity;

/**
 * User data transfer object.
 */
public class UserDTO implements UserDetails {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String subjectName;
    private String fullName;
    private String friendlyName;
    private String email;
    private String phoneNumber;
    private String title;
    private Date signatureDate;
    private UserDTO impersonatedBy;


    private int failedLoginCount;
    private boolean accountExpired;
    private boolean accountLocked;
    private boolean credentialsExpired;
    private boolean accountEnabled;
    private boolean passwordResetRequired;

    /**
     * Default constructor.
     */
    public UserDTO(){}

    /**
     * Constructed from an entity.
     * @param entity the entity
     */
    public UserDTO(final UserEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.subjectName = entity.getSubjectName();
            this.fullName = entity.getFullName();
            this.friendlyName = entity.getFriendlyName();
            this.email = entity.getContact().getEmail();
            this.phoneNumber = entity.getContact().getPhoneNumber();
            this.title = entity.getContact().getTitle();
            this.signatureDate = entity.getContact().getSignatureDate();
            this.failedLoginCount = entity.getFailedLoginCount();
            this.accountExpired = !entity.isAccountNonExpired();
            this.accountLocked = !entity.isAccountNonLocked();
            this.accountEnabled = entity.isEnabled();
            this.credentialsExpired = entity.isCredentialsExpired();
            this.passwordResetRequired = entity.getPasswordResetRequired();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(final String subject) {
        this.subjectName = subject;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(final String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * We return null rather than returning authorities here because we
     * don't actually want the DTO to have granted permissions (those
     * come from the JWT token).
     * @return a null collection
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return subjectName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return accountEnabled;
    }

    @Override
    public String getPassword() {
        return null;
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

    public void setCredentialsExpired(final boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public boolean isAccountEnabled() {
        return accountEnabled;
    }

    public void setAccountEnabled(final boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    public Date getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(final Date signatureDate) {
        this.signatureDate = signatureDate;
    }

    public UserDTO getImpersonatedBy() {
        return impersonatedBy;
    }

    public void setImpersonatedBy(UserDTO impersonatedBy) {
        this.impersonatedBy = impersonatedBy;
    }

  public int getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(final int failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    public boolean getPasswordResetRequired() {
        return passwordResetRequired;
    }

    public void setPasswordResetRequired(final boolean passwordResetRequired) {
        this.passwordResetRequired = passwordResetRequired;
    }

    @Override
    public String toString() {
        String ret = "[UserDTO: "
                + "[id: " + this.id + "]"
                + "[subjectName: " + this.subjectName + "]"
                + "[fullName: " + this.fullName + "]"
                + "[friendlyName: " + this.friendlyName + "]"
                + "[email: " + this.email + "]"
                + "[phoneNumber: " + this.phoneNumber + "]"
                + "[title: " + this.title + "]"
                + "[signatureDate: " + this.signatureDate + "]"
                + "[failedLoginCount: " + this.failedLoginCount + "]"
                + "[accountExpired: " + this.accountExpired + "]"
                + "[accountLocked: " + this.accountLocked + "]"
                + "[credentialsExpired: " + this.credentialsExpired + "]"
                + "[accountEnabled: " + this.accountEnabled + "]"
                + "[passwordResetRequired: " + this.passwordResetRequired + "]]";
        return ret;
    }
}
