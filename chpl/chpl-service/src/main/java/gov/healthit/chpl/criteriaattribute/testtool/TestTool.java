package gov.healthit.chpl.criteriaattribute.testtool;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TestTool extends CriteriaAttribute implements Serializable {
    private static final long serialVersionUID = -3761135258251736516L;

    //TODO: OCD-4242
    @Deprecated
    private String nanme;

    //TODO: OCD-4242
    @Deprecated
    private String description;

    //TODO: OCD-4242
    @Deprecated
    @JsonIgnore
    public String getName() {
        return this.getValue();
    }
}
