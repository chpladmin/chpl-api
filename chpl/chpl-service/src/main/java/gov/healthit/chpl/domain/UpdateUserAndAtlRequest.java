package gov.healthit.chpl.domain;

import java.io.Serializable;

public class UpdateUserAndAtlRequest implements Serializable {
    private static final long serialVersionUID = 1961898850619311406L;
    private Long atlId;
    private Long userId;
    private ChplPermission authority;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    public ChplPermission getAuthority() {
        return authority;
    }

    public void setAuthority(final ChplPermission authority) {
        this.authority = authority;
    }

    public Long getAtlId() {
        return atlId;
    }

    public void setAtlId(final Long atlId) {
        this.atlId = atlId;
    }

}
