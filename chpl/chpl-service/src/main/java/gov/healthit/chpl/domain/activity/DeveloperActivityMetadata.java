package gov.healthit.chpl.domain.activity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DeveloperActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117187928313180L;

    private String developerName;
    private String developerCode;
}
