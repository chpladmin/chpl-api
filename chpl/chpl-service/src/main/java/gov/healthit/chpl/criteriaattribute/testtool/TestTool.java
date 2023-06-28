package gov.healthit.chpl.criteriaattribute.testtool;

import java.io.Serializable;
import java.time.LocalDate;

import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class TestTool extends CriteriaAttribute implements Serializable {
    private static final long serialVersionUID = -3761135258251736516L;

    public String getName() {
        return this.getValue();
    }

    public Boolean isRetired() {
        LocalDate start = this.getStartDay() != null ? this.getStartDay() : LocalDate.MIN;
        LocalDate end = this.getEndDay() != null ? this.getEndDay() : LocalDate.MAX;
        return LocalDate.now().compareTo(start) >= 0 && LocalDate.now().compareTo(end) <= 0;
    }
}
