package gov.healthit.chpl.domain.auth;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermission {
    private Long id;
    private String authority;
    private String name;
    private String description;

    public GrantedPermission getGrantedPermission() {
        return new GrantedPermission(authority);
    }
}
