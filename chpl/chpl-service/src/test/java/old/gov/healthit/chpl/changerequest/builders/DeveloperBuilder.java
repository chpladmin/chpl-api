package old.gov.healthit.chpl.changerequest.builders;

import gov.healthit.chpl.domain.Developer;

public class DeveloperBuilder {
    private Long id;
    private String code;
    private String name;

    public DeveloperBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public DeveloperBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    public DeveloperBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public Developer build() {
        Developer dev = new Developer();
        dev.setDeveloperId(id);
        dev.setDeveloperCode(code);
        dev.setName(name);
        return dev;
    }
}
