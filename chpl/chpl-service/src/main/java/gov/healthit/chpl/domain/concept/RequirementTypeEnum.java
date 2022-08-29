package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

import lombok.Getter;

@Getter
@Deprecated
public enum RequirementTypeEnum implements Serializable {
    K1("170.523 (k)(1)", false),
    K2("170.523 (k)(2)", true),
    L("170.523 (l)", false),
    ANNUAL_RWT_PLAN("Annual Real World Testing Plan", false),
    ANNUAL_RWT_RESULTS("Annual Real World Testing Results", false),
    SEMIANNUAL_ATTESTATIONS_SUBMISSION("Semiannual Attestations Submission", false);

    private String name;
    private Boolean removed;

    RequirementTypeEnum(String name, Boolean removed) {
        this.name = name;
        this.removed = removed;
    }
}
