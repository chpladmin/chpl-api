package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.UserDeveloperMapEntity;

public class UserDeveloperMapDTO implements Serializable {
    private static final long serialVersionUID = -8936439652132642011L;

    private Long id;
    private UserDTO user;
    private DeveloperDTO developer;

    public UserDeveloperMapDTO(final UserDeveloperMapEntity entity) {
        this.id = entity.getId();
        this.developer = new DeveloperDTO(entity.getDeveloper());
        this.user = new UserDTO(entity.getUser());
    }

    public UserDeveloperMapDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(final UserDTO user) {
        this.user = user;
    }

    public DeveloperDTO getDeveloper() {
        return developer;
    }

    public void setDeveloper(final DeveloperDTO developer) {
        this.developer = developer;
    }

    @Override
    public String toString() {
        return "UserDeveloperMapDTO [id=" + id + ", user=" + user + ", developer=" + developer + "]";
    }
}
