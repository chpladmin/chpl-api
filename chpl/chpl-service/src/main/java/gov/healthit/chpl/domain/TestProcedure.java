package gov.healthit.chpl.domain;

import java.io.Serializable;

public class TestProcedure extends KeyValueModel implements Serializable {
    private static final long serialVersionUID = -3763885258251744916L;

    public TestProcedure() {
        super();
    }

    public TestProcedure(Long id, String name) {
        super(id, name);
    }
}
