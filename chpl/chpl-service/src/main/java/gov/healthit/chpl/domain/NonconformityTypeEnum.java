package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Optional;

import lombok.Getter;

@Getter
@Deprecated
public enum NonconformityTypeEnum implements Serializable {
    K1("170.523 (k)(1)", false),
    K2("170.523 (k)(2)", true),
    L("170.523 (l)", false),
    ANNUAL_RWT_PLAN("Annual Real World Testing Plan", false),
    ANNUAL_RWT_RESULTS("Annual Real World Testing Results", false),
    SEMIANNUAL_ATTESTATIONS_SUBMISSION("Semiannual Attestations Submission", false),
    OTHER("Other Non-Conformity", false);

    private String name;
    private Boolean removed;

    NonconformityTypeEnum(String name, Boolean removed) {
        this.name = name;
        this.removed = removed;
    }

    public static Optional<NonconformityTypeEnum> getByName(String name) {
        for (NonconformityTypeEnum ncType : NonconformityTypeEnum.values()) {
            if (name.equals(ncType.getName())) {
                return Optional.of(ncType);
            }
        }
        return Optional.empty();
    }

}
