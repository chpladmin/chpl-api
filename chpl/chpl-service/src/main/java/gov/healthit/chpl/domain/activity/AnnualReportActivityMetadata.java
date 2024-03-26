package gov.healthit.chpl.domain.activity;

import gov.healthit.chpl.domain.CertificationBody;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AnnualReportActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = -3877462517237563058L;

    private CertificationBody acb;
    private Integer year;
}
