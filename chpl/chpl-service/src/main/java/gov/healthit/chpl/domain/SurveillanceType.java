package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveillanceType implements Serializable {
    private static final long serialVersionUID = 5788880200952752783L;

    /**
     * Surveillance type internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Surveillance type name (randomized, reactive)
     */
    @XmlElement(required = true)
    private String name;

    public SurveillanceType() {
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
