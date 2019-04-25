package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveillanceNonconformityStatus implements Serializable {
    private static final long serialVersionUID = -411041849666278903L;
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";

    /**
     * Nonconformity status internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Nonconformity status name. Open or Closed.
     */
    @XmlElement(required = true)
    private String name;

    public SurveillanceNonconformityStatus() {
    }

    /**
     * Checks the id and name fields to determine if the two
     * status fields are the same.
     * Expect one or both fields to be filled in always.
     * @param anotherStatus
     * @return whether the two objects are the same
     */
    public boolean matches(final SurveillanceNonconformityStatus anotherStatus) {
        if (this.id != null && anotherStatus.id != null
                && this.id.longValue() == anotherStatus.id.longValue()) {
            return true;
        } else if (!StringUtils.isEmpty(this.name) && !StringUtils.isEmpty(anotherStatus.name)
                && this.name.equalsIgnoreCase(anotherStatus.name)) {
            return true;
        }
        return false;
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
