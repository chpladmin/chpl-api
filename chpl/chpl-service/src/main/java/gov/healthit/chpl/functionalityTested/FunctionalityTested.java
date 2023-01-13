package gov.healthit.chpl.functionalityTested;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.PracticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FunctionalityTested implements Serializable {
    private static final long serialVersionUID = 620315627813874301L;
    private Long id;
    private String name;
    private String description;
    private PracticeType practiceType;

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
        return Objects.equals(id, functionalityTested.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
