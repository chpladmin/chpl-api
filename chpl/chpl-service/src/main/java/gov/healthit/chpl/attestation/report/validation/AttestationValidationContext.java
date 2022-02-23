package gov.healthit.chpl.attestation.report.validation;

import java.util.List;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.search.domain.CertifiedProductBasicSearchResult;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class AttestationValidationContext {
    private Developer developer;

    @Singular
    private List<CertifiedProductBasicSearchResult> listings;
}
