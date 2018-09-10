package gov.healthit.chpl.auth.json;

import gov.healthit.chpl.auth.dto.UserDTO;

public class User {
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
    private String hash;

    public User() {}
    public User(UserDTO dto) {
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
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getFriendlyName() {
        return friendlyName;
    }
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Boolean getAccountLocked() {
        return accountLocked;
    }
    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }
    public Boolean getAccountEnabled() {
        return accountEnabled;
    }
    public void setAccountEnabled(Boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }
    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }
    public Boolean getComplianceTermsAccepted() {
        return complianceTermsAccepted;
    }
    public void setComplianceTermsAccepted(Boolean complianceTermsAccepted) {
        this.complianceTermsAccepted = complianceTermsAccepted;
    }
}
