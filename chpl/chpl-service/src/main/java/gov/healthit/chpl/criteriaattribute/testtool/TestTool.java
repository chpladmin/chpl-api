package gov.healthit.chpl.criteriaattribute.testtool;

import java.io.Serializable;

import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.entity.TestToolEntity;

public class TestTool extends KeyValueModel implements Serializable {
    private static final long serialVersionUID = -3761135258251736516L;
    private boolean retired;

    public TestTool() {
        super();
    }

    public TestTool(Long id, String name) {
        super(id, name);
    }

    public TestTool(TestToolEntity entity) {
        super(entity.getId(), entity.getName());
        this.retired = entity.getRetired();
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
