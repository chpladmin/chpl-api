package gov.healthit.chpl.domain.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.domain.Organization;
import gov.healthit.chpl.dto.OrganizationDTO;
import gov.healthit.chpl.dto.auth.UserDTO;

public class User implements Serializable {
    private static final long serialVersionUID = 8408154701107113148L;

    private Long userId;
    private String role;
    private String subjectName;
    private String fullName;
    private String friendlyName;
    private String email;
    private String phoneNumber;
    private String title;
    private Boolean accountLocked;
    private Boolean accountEnabled;
    private Boolean credentialsExpired;
    private Boolean passwordResetRequired;
    private Date lastLoggedInDate;
    private List<Organization> organizations = new ArrayList<Organization>();
    private String hash;

    /** Default constructor. */
    public User() {
    }

    /**
     * Constructed from DTO.
     *
     * @param dto
     *            the dto
     */
    public User(final UserDTO dto) {
        this.setUserId(dto.getId());
        if (dto.getPermission() != null) {
            this.setRole(dto.getPermission().getAuthority());
        }
        this.setSubjectName(dto.getSubjectName());
        this.setFullName(dto.getFullName());
        this.setFriendlyName(dto.getFriendlyName());
        this.setEmail(dto.getEmail());
        this.setPhoneNumber(dto.getPhoneNumber());
        this.setTitle(dto.getTitle());
        this.setAccountLocked(dto.isAccountLocked());
        this.setAccountEnabled(dto.isAccountEnabled());
        this.setCredentialsExpired(dto.isCredentialsExpired());
        this.setPasswordResetRequired(dto.getPasswordResetRequired());
        this.setLastLoggedInDate(dto.getLastLoggedInDate());

        for (OrganizationDTO orgDTO : dto.getOrganizations()) {
            this.getOrganizations().add(new Organization(orgDTO.getId(), orgDTO.getName()));
        }
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

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public Date getLastLoggedInDate() {
        return lastLoggedInDate;
    }

    public void setLastLoggedInDate(Date lastLoggedInDate) {
        this.lastLoggedInDate = lastLoggedInDate;
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }
}
