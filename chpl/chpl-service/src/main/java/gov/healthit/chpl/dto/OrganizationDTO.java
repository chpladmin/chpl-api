package gov.healthit.chpl.dto;

import java.io.Serializable;

public class OrganizationDTO implements Serializable {
    private static final long serialVersionUID = 665443336578862611L;

    private Long id;
    private String name;

    public OrganizationDTO(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
