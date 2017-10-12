package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.QmsStandardEntity;

public class QmsStandardDTO implements Serializable {
    private static final long serialVersionUID = 5091557483274894084L;
    private Long id;
    private String name;

    public QmsStandardDTO() {
    }

    public QmsStandardDTO(QmsStandardEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }

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
