package gov.healthit.chpl.attribute;

import gov.healthit.chpl.codeset.CodeSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CodeSetUpToDate extends AttributeUpToDate {
    private CodeSet codeSet;
}
