package gov.healthit.chpl.domain;

import java.io.Serializable;

public enum NonconformityType implements Serializable {
    K1("170.523 (k)(1)"), K2("170.523 (k)(2)"), L("170.523 (l)"), OTHER("Other Non-Conformity");

    private String name;

    private NonconformityType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
