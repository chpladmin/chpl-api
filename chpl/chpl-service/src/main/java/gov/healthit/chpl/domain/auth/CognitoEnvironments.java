package gov.healthit.chpl.domain.auth;

import lombok.Getter;

public enum CognitoEnvironments {
    DEV("chpl-dev-env"),
    QA("chpl-qa-env"),
    STG("chpl-stg-env"),
    PROD("chpl-prod-env");

    @Getter
    private String name;

    CognitoEnvironments(String name) {
        this.name = name;
    }
}
