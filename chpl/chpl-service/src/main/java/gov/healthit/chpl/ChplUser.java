package gov.healthit.chpl;

import java.util.List;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class ChplUser {
    private User user;

    private List<CertificationBodyDTO> certificationBodies;

    public ChplUser(User user) {
        this.user = user;
    }

    public List<CertificationBodyDTO> getCertificationBodies() {
        return certificationBodies;
    }

    public void setCertificationBodies(List<CertificationBodyDTO> certificationBodies) {
        this.certificationBodies = certificationBodies;
    }
}
