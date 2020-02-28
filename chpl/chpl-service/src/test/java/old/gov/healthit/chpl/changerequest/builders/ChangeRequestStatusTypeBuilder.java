package old.gov.healthit.chpl.changerequest.builders;

import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;

public class ChangeRequestStatusTypeBuilder {

    private Long id;
    private String name;

    public ChangeRequestStatusTypeBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ChangeRequestStatusTypeBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ChangeRequestStatusType build() {
        ChangeRequestStatusType crType = new ChangeRequestStatusType();
        crType.setId(id);
        crType.setName(name);
        return crType;
    }
}
