package gov.healthit.chpl.auth.user;

import java.util.UUID;

public final class CognitoSystemUsers {
    public static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private CognitoSystemUsers() {}
}
