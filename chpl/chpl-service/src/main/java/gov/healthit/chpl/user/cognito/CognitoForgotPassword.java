package gov.healthit.chpl.user.cognito;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CognitoForgotPassword {
    private static final Long MILLIS_IN_HOUR = 24L * 60L * 60L * 1000L;

    private Long id;
    private String email;
    private UUID token;

    @JsonIgnore
    private Date creationDate;

    @JsonIgnore
    private Date lastModifiedDate;

    @JsonIgnore
    private Long lastModifiedUserId;

    @JsonIgnore
    private UUID lasModifiedSsoUser;

    public boolean isOlderThan(long hours) {
        return isOlderThanMillis(hours * MILLIS_IN_HOUR);
    }

    private boolean isOlderThanMillis(long numDaysInMillis) {
        if (this.creationDate == null || this.lastModifiedDate == null) {
            return true;
        }

        Date now = new Date();
        if ((now.getTime() - this.creationDate.getTime()) > numDaysInMillis) {
            return true;
        }
        return false;
    }
}
