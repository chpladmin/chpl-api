package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

public enum RequirementTypeEnum implements Serializable {
    K1("170.523 (k)(1)"),
    K2("170.523 (k)(2)"),
    L("170.523 (l)"),
    ANNUAL_RWT_PLAN("Annual Real World Testing Plan"),
    ANNUAL_RWT_RESULTS("Annual Real World Testing Results");

    private String name;

    RequirementTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
