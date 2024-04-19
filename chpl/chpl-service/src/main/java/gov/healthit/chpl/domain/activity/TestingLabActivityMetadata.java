package gov.healthit.chpl.domain.activity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TestingLabActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069109187278163180L;

    private Long atlId;
    private String atlName;

}
