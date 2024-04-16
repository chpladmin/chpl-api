package gov.healthit.chpl.domain.activity;

import gov.healthit.chpl.domain.CertificationBody;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class QuarterlyReportActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = -3877401017237563058L;

    private CertificationBody acb;
    private String quarterName;
    private Integer year;
}
