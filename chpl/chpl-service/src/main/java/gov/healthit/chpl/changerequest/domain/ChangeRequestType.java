package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

public class ChangeRequestType implements Serializable {
    private static final long serialVersionUID = -4282000227446957351L;

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

    @Override
    public String toString() {
        return "ChangeRequestType [id=" + id + ", name=" + name + "]";
    }
}
