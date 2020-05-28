package gov.healthit.chpl.dto.auth;

import java.util.Date;

import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.entity.auth.UserResetTokenEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserResetTokenDTO {

    private Long id;
    private String userResetToken;
    private Long userId;
    private UserEntity user;
    private Date creationDate;
    private boolean deleted;

    public UserResetTokenDTO(final UserResetTokenEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.userResetToken = entity.getUserResetToken();
            this.userId = entity.getUserId();
            this.user = entity.getUser();
            this.creationDate = entity.getCreationDate();
            this.deleted = entity.getDeleted();
        }
    }
}
