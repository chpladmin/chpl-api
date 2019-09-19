package gov.healthit.chpl.changerequest.builders;

import gov.healthit.chpl.domain.CertificationBody;

public class CertificationBodyBuilder {

    private Long id;
    private String code;
    private String name;

    public CertificationBodyBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CertificationBodyBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    public CertificationBodyBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CertificationBody build() {
        CertificationBody acb = new CertificationBody();
        acb.setId(id);
        acb.setAcbCode(code);
        acb.setName(name);
        return acb;
    }
}
