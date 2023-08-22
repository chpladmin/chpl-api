package gov.healthit.chpl.criteriaattribute.functionalitytested;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import gov.healthit.chpl.domain.PracticeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


// TODO OCD-4288 - NEED TEXT TO DESCRIBE THIS OBJECT
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FunctionalityTested extends CriteriaAttribute implements Serializable {
    private static final long serialVersionUID = 620315627813874301L;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found value",
            removalDate = "2024-01-01")
    @XmlTransient
    private String name;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found regulatoryTextCitation",
            removalDate = "2024-01-01")
    @XmlTransient
    private String description;

    /*
     * TODO: OCD-4288 NEED THIS TEXT
     */
    @XmlElement(required = false)
    private PracticeType practiceType;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        FunctionalityTested functionalityTested = (FunctionalityTested) o;
        return Objects.equals(getId(), functionalityTested.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
