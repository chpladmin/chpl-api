package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.UserDeveloperMapEntity;
import gov.healthit.chpl.util.DeveloperMapper;
import lombok.Data;

@Data
public class UserDeveloperMapDTO implements Serializable {
    private static final long serialVersionUID = -8936439652132642011L;

    private Long id;
    private UserDTO user;
    private DeveloperDTO developer;
    private DeveloperMapper developerMapper;

    public UserDeveloperMapDTO() {
        this.developerMapper = new DeveloperMapper();
    }


    public UserDeveloperMapDTO(UserDeveloperMapEntity entity) {
        this();
        this.id = entity.getId();
        this.developer = this.developerMapper.from(entity.getDeveloper());
    }

    @Override
    public String toString() {
        return "UserDeveloperMapDTO [id=" + id + ", user=" + user + ", developer=" + developer + "]";
    }
}
