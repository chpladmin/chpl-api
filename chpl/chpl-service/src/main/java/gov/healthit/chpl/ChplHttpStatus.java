package gov.healthit.chpl;

import org.springframework.lang.Nullable;

public enum ChplHttpStatus {
    RESENT_USER_CONFIRMATION_EMAIL(461, "Account's Email Not Confirmed"),
    COGNITO_AUTHENTICATION_CHALLENGE(470, "The user is required to respond to the described challenge."),
    COGNITO_FORCE_PASSWORD_CHANGE(471, "The user is required to change their password.");

    private int value;

    private String reasonPhrase;

    ChplHttpStatus(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    public int value() {
        return this.value;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    @Override
    public String toString() {
        return this.value + " " + name();
    }

    public static ChplHttpStatus valueOf(int statusCode) {
        ChplHttpStatus status = resolve(statusCode);
        if (status == null) {
            throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
        }
        return status;
    }

    @Nullable
    public static ChplHttpStatus resolve(int statusCode) {
        for (ChplHttpStatus status : values()) {
            if (status.value == statusCode) {
                return status;
            }
        }
        return null;
    }
}
