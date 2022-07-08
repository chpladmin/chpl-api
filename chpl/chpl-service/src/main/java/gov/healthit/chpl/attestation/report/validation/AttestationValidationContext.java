package gov.healthit.chpl.attestation.report.validation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class AttestationValidationContext {
    private Developer developer;

    @Singular
    private List<ListingSearchResult> listings;

    private List<CertificationCriterion> realWorldTestingCriteria;

    private List<CertificationCriterion> assuranceCriteria;

    private List<CertificationCriterion> apiCriteria;

    private final List<String> activeStatuses = Stream.of(CertificationStatusType.Active.getName(),
            CertificationStatusType.SuspendedByAcb.getName(),
            CertificationStatusType.SuspendedByOnc.getName())
            .collect(Collectors.toList());
}
