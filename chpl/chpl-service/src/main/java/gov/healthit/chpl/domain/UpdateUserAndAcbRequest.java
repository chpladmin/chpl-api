package gov.healthit.chpl.domain;

import java.io.Serializable;

public class UpdateUserAndAcbRequest implements Serializable {
    private static final long serialVersionUID = 5248770762031781183L;
    private Long acbId;
    private Long userId;
    private ChplPermission authority;

    public Long getAcbId() {
        return acbId;
    }

    public void setAcbId(final Long acbId) {
        this.acbId = acbId;
    }

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

}
