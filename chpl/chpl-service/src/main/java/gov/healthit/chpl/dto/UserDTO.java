package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.auth.entity.UserEntity;

public class UserDTO implements Serializable {
    private static final long serialVersionUID = -7764344555859600883L;

    private Long userId;

    public UserDTO(UserEntity entity) {
        this.userId = entity.getId();
    }

    public UserDTO() {

    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
