package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Optional;

import lombok.Getter;

@Getter
public enum NonconformityType implements Serializable {
    K1("170.523 (k)(1)", false),
    K2("170.523 (k)(2)", true),
    L("170.523 (l)", false),
    ANNUAL_RWT_PLAN("Annual Real World Testing Plan", false),
    ANNUAL_RWT_RESULTS("Annual Real World Testing Results", false),
    OTHER("Other Non-Conformity", false);

    private String name;
    private Boolean removed;

    NonconformityType(String name, Boolean removed) {
        this.name = name;
        this.removed = removed;
    }

    public static Optional<NonconformityType> getByName(String name) {
        for (NonconformityType ncType : NonconformityType.values()) {
            if (name.equals(ncType.getName())) {
                return Optional.of(ncType);
            }
        }
        return Optional.empty();
    }
}
