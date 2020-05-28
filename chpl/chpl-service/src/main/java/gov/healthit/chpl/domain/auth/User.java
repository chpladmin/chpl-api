package gov.healthit.chpl.domain.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.domain.Organization;
import gov.healthit.chpl.dto.OrganizationDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
        this.setPasswordResetRequired(dto.isPasswordResetRequired());
        this.setLastLoggedInDate(dto.getLastLoggedInDate());

        for (OrganizationDTO orgDTO : dto.getOrganizations()) {
            this.getOrganizations().add(new Organization(orgDTO.getId(), orgDTO.getName()));
        }
    }
}
