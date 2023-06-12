package gov.healthit.chpl.permissions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.dto.auth.UserDTO;

public class ResourcePermissionsTest {
    private ResourcePermissions resourcePermissions;

    @Before
    public void setup() {
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
    }

    @Test
    public void hasPermissionOnUser_AcbHasAbilityToEditDeveloper_True() {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.getRoleByUserId(ArgumentMatchers.anyLong()))
                .thenReturn(UserPermission.builder()
                        .authority(Authority.ROLE_DEVELOPER)
                        .build());
        Mockito.when(resourcePermissions.hasPermissionOnUser(ArgumentMatchers.any(UserDTO.class))).thenCallRealMethod();

        UserDTO userToCheck = UserDTO.builder()
                .id(1L)
                .permission(UserPermission.builder()
                        .authority(Authority.ROLE_DEVELOPER)
                        .build())
                .build();

        Boolean hasPermission = resourcePermissions.hasPermissionOnUser(userToCheck);

        assertEquals(true, hasPermission);
    }

    private JWTAuthenticatedUser getAcbUser() {
        JWTAuthenticatedUser acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
        return acbUser;
    }
}
