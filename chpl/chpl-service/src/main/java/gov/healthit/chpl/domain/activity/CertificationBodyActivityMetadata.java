package gov.healthit.chpl.domain.activity;

/**
 * ACB specific activity summary fields.
 * @author kekey
 *
 */
public class CertificationBodyActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117087278163180L;

    private Long acbId;
    private String acbName;

    public CertificationBodyActivityMetadata() {
    }

    public String getAcbName() {
        return acbName;
    }

    public void setAcbName(final String acbName) {
        this.acbName = acbName;
    }

    public Long getAcbId() {
        return acbId;
    }

    public void setAcbId(final Long acbId) {
        this.acbId = acbId;
    }
}
