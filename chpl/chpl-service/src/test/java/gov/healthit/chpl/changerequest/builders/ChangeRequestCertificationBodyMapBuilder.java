package gov.healthit.chpl.changerequest.builders;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.domain.CertificationBody;

public class ChangeRequestCertificationBodyMapBuilder {

    private Long id;
    private ChangeRequest changeRequest;
    private CertificationBody certificationBody;

    public ChangeRequestCertificationBodyMapBuilder withId(final Long id) {
        this.id = id;
        return this;
    }

    public ChangeRequestCertificationBodyMapBuilder withChangeRequest(final ChangeRequest cr) {
        this.changeRequest = cr;
        return this;
    }

    public ChangeRequestCertificationBodyMapBuilder withCertificationBody(final CertificationBody acb) {
        this.certificationBody = acb;
        return this;
    }

    public ChangeRequestCertificationBodyMap build() {
        ChangeRequestCertificationBodyMap map = new ChangeRequestCertificationBodyMap();
        map.setId(id);;
        map.setCertificationBody(certificationBody);
        map.setChangeRequest(changeRequest);
        return map;
    }
}
