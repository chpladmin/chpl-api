package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.UserCertificationBodyMapEntity;

public class UserCertificationBodyMapDTO implements Serializable {
    private static final long serialVersionUID = 1316069925338614100L;

    private Long id;
    private UserDTO user;
    private CertificationBodyDTO certificationBody;
    private Boolean retired;

    public UserCertificationBodyMapDTO(final UserCertificationBodyMapEntity entity) {
        this.id = entity.getId();
        this.certificationBody = new CertificationBodyDTO(entity.getCertificationBody());
        this.user = new UserDTO(entity.getUser());
        this.retired = entity.getRetired();
    }

    public UserCertificationBodyMapDTO() {

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

    public CertificationBodyDTO getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(final CertificationBodyDTO certificationBody) {
        this.certificationBody = certificationBody;
    }

    public Boolean getRetired() {
        return retired;
    }

    public void setRetired(final Boolean retired) {
        this.retired = retired;
    }
}
