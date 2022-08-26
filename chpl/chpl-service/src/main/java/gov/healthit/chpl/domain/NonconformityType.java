package gov.healthit.chpl.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
public class NonconformityType extends CertificationCriterion implements Serializable {
    private static final long serialVersionUID = -7437221753188417890L;

    @JsonIgnore
    private NonconformityClassification classification;
}
