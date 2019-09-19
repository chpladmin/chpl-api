package gov.healthit.chpl.domain.activity;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;

public class ChangeRequestActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = -1866753136959514275L;

    private Developer developer;

    private List<CertificationBody> certificationBodies = new ArrayList<CertificationBody>();

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(final Developer developer) {
        this.developer = developer;
    }

    public List<CertificationBody> getCertificationBodies() {
        return certificationBodies;
    }

    public void setCertificationBodies(List<CertificationBody> certificationBodies) {
        this.certificationBodies = certificationBodies;
    }

}
