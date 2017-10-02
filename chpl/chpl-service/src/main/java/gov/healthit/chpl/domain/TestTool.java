package gov.healthit.chpl.domain;

import java.io.Serializable;

public class TestTool extends KeyValueModel implements Serializable {
    private static final long serialVersionUID = -3761135258251736516L;
    private boolean retired;

    public TestTool() {
        super();
    }

    public TestTool(Long id, String name) {
        super(id, name);
    }

    public TestTool(Long id, String name, String description) {
        super(id, name, description);
    }

    public boolean getRetired() {
        return retired;
    }

    public void setRetired(final boolean retired) {
        this.retired = retired;
    }

}
