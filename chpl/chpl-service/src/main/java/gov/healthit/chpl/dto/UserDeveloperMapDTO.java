package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.UserDeveloperMapEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserDeveloperMapDTO implements Serializable {
    private static final long serialVersionUID = -8936439652132642011L;

    private Long id;
    private UserDTO user;
    private Developer developer;

    public UserDeveloperMapDTO(UserDeveloperMapEntity entity) {
        this.id = entity.getId();
        this.developer = entity.getDeveloper().toDomain();
    }
}
