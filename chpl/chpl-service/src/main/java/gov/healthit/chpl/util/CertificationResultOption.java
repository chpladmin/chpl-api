package gov.healthit.chpl.util;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CertificationResultOption {
    private String optionName;
    private boolean canHaveOption;
}
