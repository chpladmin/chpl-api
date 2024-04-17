package gov.healthit.chpl.domain.activity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CertificationBodyActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117087278163180L;

    private Long acbId;
    private String acbName;
}
