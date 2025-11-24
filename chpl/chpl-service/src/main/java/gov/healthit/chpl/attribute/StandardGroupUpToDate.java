package gov.healthit.chpl.attribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class StandardGroupUpToDate extends AttributeUpToDate {
    private String standardGroupName;
}
