package gov.healthit.chpl.web.controller.results;

import java.util.List;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttestationPeriodResults {
    private List<AttestationPeriod> results;
    
    public AttestationPeriodResults(List<AttestationPeriod> periods) {
        this.results = periods;
    }
}
