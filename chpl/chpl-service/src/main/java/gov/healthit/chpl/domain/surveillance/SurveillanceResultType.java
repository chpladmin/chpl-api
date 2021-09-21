package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveillanceResultType implements Serializable {
    private static final long serialVersionUID = 120064764043803388L;
    public static final String NON_CONFORMITY = "Non-Conformity";
    public static final String NO_NON_CONFORMITY = "No Non-Conformity";

    /**
     * Surveillance result type internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Surveillance result type name. Nonconformity or No Nonconformity
     */
    @XmlElement(required = true)
    private String name;

    /**
     * Checks the id and name fields to determine if the two
     * result type fields are the same.
     * Expect one or both fields to be filled in always.
     * @param anotherType
     * @return whether the two objects are the same
     */
    public boolean matches(final SurveillanceResultType anotherType) {
        if (this.id != null && anotherType.id != null
                && this.id.longValue() == anotherType.id.longValue()) {
            return true;
        } else if (!StringUtils.isEmpty(this.name) && !StringUtils.isEmpty(anotherType.name)
                && this.name.equalsIgnoreCase(anotherType.name)) {
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
