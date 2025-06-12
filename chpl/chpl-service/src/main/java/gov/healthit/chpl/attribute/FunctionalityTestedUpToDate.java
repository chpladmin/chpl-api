package gov.healthit.chpl.attribute;

import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class FunctionalityTestedUpToDate extends AttributeUpToDate {
    private FunctionalityTested functionalityTested;
}
