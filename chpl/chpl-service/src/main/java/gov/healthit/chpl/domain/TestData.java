package gov.healthit.chpl.domain;

import java.io.Serializable;

public class TestData extends KeyValueModel implements Serializable {
    private static final long serialVersionUID = -3763885258251736516L;

    public TestData() {
        super();
    }

    public TestData(Long id, String name) {
        super(id, name);
    }
}
