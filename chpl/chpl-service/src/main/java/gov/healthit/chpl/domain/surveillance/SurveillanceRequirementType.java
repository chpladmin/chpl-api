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
public class SurveillanceRequirementType implements Serializable {
    private static final long serialVersionUID = -5865384642096284604L;
    public static final String CERTIFIED_CAPABILITY = "Certified Capability";
    public static final String TRANS_DISCLOSURE_REQ = "Transparency or Disclosure Requirement";
    public static final String OTHER = "Other Requirement";

    /**
     * Surveillance requirement type internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Surveillance requirement type name
     */
    @XmlElement(required = true)
    private String name;

    /**
     * Checks the id and name fields to determine if the two
     * requirement type fields are the same.
     * Expect one or both fields to be filled in always.
     * @param anotherType
     * @return whether the two objects are the same
     */
    public boolean matches(final SurveillanceRequirementType anotherType) {
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
