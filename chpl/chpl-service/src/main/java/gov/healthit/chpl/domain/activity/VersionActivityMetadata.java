package gov.healthit.chpl.domain.activity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class VersionActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117087924463180L;

    private String developerName;
    private String productName;
    private String version;
}
