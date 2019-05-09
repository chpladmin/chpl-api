package gov.healthit.chpl.domain.activity;

/**
 * ATL specific activity summary fields.
 * @author kekey
 *
 */
public class TestingLabActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069109187278163180L;

    private Long atlId;
    private String atlName;

    public TestingLabActivityMetadata() {
    }

    public Long getAtlId() {
        return atlId;
    }

    public void setAtlId(final Long atlId) {
        this.atlId = atlId;
    }

    public String getAtlName() {
        return atlName;
    }

    public void setAtlName(final String atlName) {
        this.atlName = atlName;
    }

}
