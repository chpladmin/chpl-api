package gov.healthit.chpl.attribute;

import gov.healthit.chpl.standard.Standard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class StandardUpToDate extends AttributeUpToDate {
    private Standard standard;
}
