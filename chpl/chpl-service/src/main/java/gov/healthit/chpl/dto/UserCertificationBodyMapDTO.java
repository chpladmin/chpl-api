package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.UserCertificationBodyMapEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserCertificationBodyMapDTO implements Serializable {
    private static final long serialVersionUID = 1316069925338614100L;

    private Long id;
    private UserDTO user;
    private CertificationBody certificationBody;
    private Boolean retired;

    public UserCertificationBodyMapDTO(UserCertificationBodyMapEntity entity) {
        this.id = entity.getId();
        this.certificationBody = entity.getCertificationBody().toDomain();
        this.retired = entity.getRetired();
    }
}
