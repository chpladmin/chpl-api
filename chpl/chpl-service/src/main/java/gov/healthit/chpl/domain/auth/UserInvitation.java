package gov.healthit.chpl.domain.auth;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserInvitation {
    private Long id;
    private String emailAddress;
    private String role;
    private Long permissionObjectId;
    private String hash;

    @JsonIgnore
    private UserPermission permission;
    @JsonIgnore
    private String invitationToken;
    @JsonIgnore
    private String confirmationToken;
    @JsonIgnore
    private Long createdUserId;
    @JsonIgnore
    private Date creationDate;
    @JsonIgnore
    private Date lastModifiedDate;
    @JsonIgnore
    private Long lastModifiedUserId;

    public boolean isOlderThan(long numDays) {
        return isOlderThanMillis(numDays * 24L * 60L * 60L * 1000L);
    }

    private boolean isOlderThanMillis(long numDaysInMillis) {
        if (this.creationDate == null || this.lastModifiedDate == null) {
            return true;
        }

        Date now = new Date();
        if ((now.getTime() - this.lastModifiedDate.getTime()) > numDaysInMillis) {
            return true;
        }
        return false;
    }
}
