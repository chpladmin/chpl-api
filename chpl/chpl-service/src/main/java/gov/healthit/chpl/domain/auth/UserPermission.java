package gov.healthit.chpl.domain.auth;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermission implements Serializable {
    private static final long serialVersionUID = -394648579362879816L;

    private Long id;
    private String authority;
    private String name;
    private String description;

    public GrantedPermission getGrantedPermission() {
        return new GrantedPermission(authority);
    }
}
