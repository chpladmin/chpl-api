package old.gov.healthit.chpl.changerequest.builders;

import gov.healthit.chpl.changerequest.domain.ChangeRequestType;

public class ChangeRequestTypeBuilder {
    private Long id;
    private String name;

    public ChangeRequestTypeBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ChangeRequestTypeBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ChangeRequestType build() {
        ChangeRequestType crType = new ChangeRequestType();
        crType.setId(id);
        crType.setName(name);
        return crType;
    }
}
