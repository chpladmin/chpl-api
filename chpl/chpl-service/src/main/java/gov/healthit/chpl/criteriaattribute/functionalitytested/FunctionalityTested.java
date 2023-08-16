package gov.healthit.chpl.criteriaattribute.functionalitytested;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.PracticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

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


    private PracticeType practiceType;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.",
            removalDate = "2024-01-01")
    @XmlTransient
    @Builder.Default
    private List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();

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
