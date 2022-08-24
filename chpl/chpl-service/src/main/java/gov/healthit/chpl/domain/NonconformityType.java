package gov.healthit.chpl.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class NonconformityType extends CertificationCriterion implements Serializable {
    private static final long serialVersionUID = -7437221753188417890L;

    @JsonIgnore
    private String classification;
    //public NonconformityType(Long id, String number, String title, Long certificationEditionId, String description, Boolean removed) {
    //    super(id, number, title,certificationEditionId, null, description, removed);
    //}
}
