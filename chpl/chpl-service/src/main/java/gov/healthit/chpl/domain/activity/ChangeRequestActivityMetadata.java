package gov.healthit.chpl.domain.activity;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ChangeRequestActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = -1866753136959514275L;

    private Developer developer;
    private List<CertificationBody> certificationBodies = new ArrayList<CertificationBody>();
}
