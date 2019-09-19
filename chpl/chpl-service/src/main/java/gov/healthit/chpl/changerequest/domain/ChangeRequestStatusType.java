package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

public class ChangeRequestStatusType implements Serializable {
    private static final long serialVersionUID = -3309062067247912001L;

    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
