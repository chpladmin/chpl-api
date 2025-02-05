package gov.healthit.chpl.domain.auth;

import java.util.List;

public final class CognitoGroups {
    private CognitoGroups() {}
    public static final String CHPL_ADMIN = "chpl-admin";
    public static final String CHPL_ONC = "chpl-onc";
    public static final String CHPL_ACB = "chpl-onc-acb";
    public static final String CHPL_CMS_STAFF = "chpl-cms-staff";
    public static final String CHPL_DEVELOPER = "chpl-developer";
    public static final String CHPL_STARTUP = "chpl-startup";
    public static final String CHPL_SYSTEM = "chpl-system";

    public static List<String> getAll() {
        return List.of(
                CHPL_ADMIN,
                CHPL_ONC,
                CHPL_ACB,
                CHPL_CMS_STAFF,
                CHPL_DEVELOPER,
                CHPL_STARTUP,
                CHPL_SYSTEM);
    }
}
