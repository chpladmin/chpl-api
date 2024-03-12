package gov.healthit.chpl.auth.user;

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
public class CognitoUserInvitation {
    private Long id;
    private String email;
    private UUID invitationToken;

    @JsonIgnore
    private Date creationDate;

    @JsonIgnore
    private Date lastModifiedDate;

    @JsonIgnore
    private Long lastModifiedUserId;

    @JsonIgnore
    private UUID lasModifiedSsoUser;

    public boolean isOlderThan(long numDays) {
        return isOlderThanMillis(numDays * 24L * 60L * 60L * 1000L);
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
