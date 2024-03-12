package gov.healthit.chpl.permissions;

public class ChplResourcePermissionsTest {
    private ChplResourcePermissions resourcePermissions;

    /*
    @Before
    public void setup() {
        resourcePermissions = Mockito.mock(ChplResourcePermissions.class);
    }

    @Test
    public void hasPermissionOnUser_AcbHasAbilityToEditDeveloper_True() {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());

        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.getRoleByUser(ArgumentMatchers.any(User.class)))
                .thenReturn(UserPermission.builder()
                        .authority(Authority.ROLE_DEVELOPER)
                        .build());
        Mockito.when(resourcePermissions.hasPermissionOnUser(ArgumentMatchers.any(User.class))).thenCallRealMethod();

        User userToCheck = User.builder()
                .userId(1L)
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
    */
}
