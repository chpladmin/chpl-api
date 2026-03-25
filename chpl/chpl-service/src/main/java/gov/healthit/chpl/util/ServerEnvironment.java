package gov.healthit.chpl.util;

import java.util.stream.Stream;

import lombok.Getter;

public enum ServerEnvironment {
    PRODUCTION("production"),
    NON_PRODUCTION("non-production");

    @Getter
    private String name;
    ServerEnvironment(String name) {
        this.name = name;
    }

    public static ServerEnvironment getByName(String envName) {
        return Stream.of(ServerEnvironment.values())
                .filter(val -> val.getName().equalsIgnoreCase(envName))
                .findAny()
                .orElse(null);
    }
}
