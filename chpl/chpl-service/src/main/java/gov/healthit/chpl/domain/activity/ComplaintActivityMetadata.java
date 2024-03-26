package gov.healthit.chpl.domain.activity;

import gov.healthit.chpl.domain.CertificationBody;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ComplaintActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = -3877401017233923058L;

    private CertificationBody certificationBody;

}
