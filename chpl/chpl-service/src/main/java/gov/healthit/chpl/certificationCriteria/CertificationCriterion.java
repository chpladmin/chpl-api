package gov.healthit.chpl.certificationCriteria;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.criteriaattribute.rule.Rule;
import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CertificationCriterion implements Serializable {
    private static final long serialVersionUID = 5732322243572571895L;

    @XmlElement(required = false, nillable = true)
    private Long id;

    @XmlElement(required = true)
    private String number;

    @XmlElement(required = false, nillable = true)
    private String title;

    @XmlElement(required = false, nillable = true)
    private Long certificationEditionId;

    @XmlElement(required = false, nillable = true)
    private String certificationEdition;

    /**
     * A date value representing the date by which the Criteria Attribute became available.
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate startDay;

    /**
     * A date value representing the date by which the Criteria Attribute can no longer be used.
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate endDay;

    @XmlElement(required = false, nillable = true)
    private String description;

    @Deprecated
    @DeprecatedResponseField(message = "This property will be removed. It can be derived based on the endDay.",
        removalDate = "2024-01-01")
    @XmlElement(required = true, nillable = false)
    private Boolean removed;

    /**
     * The rule which this criterion is associated with.
     */
    @XmlElement(required = false, nillable = true)
    private Rule rule;

    @Override
    public int hashCode() {
        return Objects.hash(certificationEdition, certificationEditionId, description, id, number, removed, title);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CertificationCriterion other = (CertificationCriterion) obj;
        return Objects.equals(certificationEdition, other.certificationEdition)
                && Objects.equals(certificationEditionId, other.certificationEditionId)
                && Objects.equals(description, other.description) && Objects.equals(id, other.id)
                && Objects.equals(number, other.number) && Objects.equals(removed, other.removed)
                && Objects.equals(title, other.title);
    }
}
