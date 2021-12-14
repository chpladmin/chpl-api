package gov.healthit.chpl.dto.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.dto.OrganizationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements UserDetails {
    private static final long serialVersionUID = -5792083881606731413L;

    private Long id;
    private UserPermission permission;
    private String subjectName;
    private String fullName;
    private String friendlyName;
    private String email;
    private String phoneNumber;
    private String title;
    private Date signatureDate;
    private Date lastLoggedInDate;

    @Builder.Default
    private List<OrganizationDTO> organizations = new ArrayList<OrganizationDTO>();

    private UserDTO impersonatedBy;
    private int failedLoginCount;
    private boolean accountExpired;
    private boolean accountLocked;
    private boolean credentialsExpired;
    private boolean accountEnabled;
    private boolean passwordResetRequired;

    /**
     * We return null rather than returning authorities here because we don't actually want the DTO to have granted
     * permissions (those come from the JWT token).
     *
     * @return a null collection
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        //TODO: replace this with "email" when eventually removing user_name column.
        return StringUtils.isEmpty(subjectName) ? email : subjectName;
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
}
