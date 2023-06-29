package gov.healthit.chpl.criteriaattribute.testtool;

import java.io.Serializable;

import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class TestTool extends CriteriaAttribute implements Serializable {
    private static final long serialVersionUID = -3761135258251736516L;

    //TODO: OCD-4242
    @Deprecated
    public String getName() {
        return this.getValue();
    }
}
