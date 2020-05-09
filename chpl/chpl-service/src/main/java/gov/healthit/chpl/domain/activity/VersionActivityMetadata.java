package gov.healthit.chpl.domain.activity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class VersionActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117087924463180L;

    private String developerName;
    private String productName;
    private String version;
}
