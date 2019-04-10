package gov.healthit.chpl.dto.auth;

import java.util.Date;

import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.entity.auth.UserResetTokenEntity;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserResetToken() {
        return userResetToken;
    }

    public void setUserResetToken(String userResetToken) {
        this.userResetToken = userResetToken;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
