package gov.healthit.chpl.auth.json;

import java.io.Serializable;

import gov.healthit.chpl.auth.dto.UserDTO;

public class User implements Serializable {
    private static final long serialVersionUID = 8408154701107113148L;

    private Long userId;
    private String subjectName;
    private String fullName;
    private String friendlyName;
    private String email;
    private String phoneNumber;
    private String title;
    private Boolean complianceTermsAccepted;
    private Boolean accountLocked;
    private Boolean accountEnabled;
    private Boolean credentialsExpired;
    private Boolean passwordResetRequired;
    private String hash;

    /** Default constructor. */
    public User() {}

    /**
     * Constructed from DTO.
     * @param dto the dto
     */
    public User(final UserDTO dto) {
        this.setUserId(dto.getId());
        this.setSubjectName(dto.getSubjectName());
        this.setFullName(dto.getFullName());
        this.setFriendlyName(dto.getFriendlyName());
        this.setEmail(dto.getEmail());
        this.setPhoneNumber(dto.getPhoneNumber());
        this.setTitle(dto.getTitle());
        this.setAccountLocked(dto.isAccountLocked());
        this.setAccountEnabled(dto.isAccountEnabled());
        this.setComplianceTermsAccepted(dto.getComplianceSignatureDate() == null ? false : true);
        this.setCredentialsExpired(dto.isCredentialsExpired());
        this.setPasswordResetRequired(dto.getPasswordResetRequired());
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(final String subjectName) {
        this.subjectName = subjectName;
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

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(final Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public Boolean getAccountEnabled() {
        return accountEnabled;
    }

    public void setAccountEnabled(final Boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public Boolean getComplianceTermsAccepted() {
        return complianceTermsAccepted;
    }

    public void setComplianceTermsAccepted(final Boolean complianceTermsAccepted) {
        this.complianceTermsAccepted = complianceTermsAccepted;
    }

    public Boolean getCredentialsExpired() {
        return credentialsExpired;
    }

    public void setCredentialsExpired(final Boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public Boolean getPasswordResetRequired() {
        return passwordResetRequired;
    }

    public void setPasswordResetRequired(final Boolean passwordResetRequired) {
        this.passwordResetRequired = passwordResetRequired;
    }
}
