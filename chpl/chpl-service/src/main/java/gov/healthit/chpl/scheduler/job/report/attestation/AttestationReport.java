package gov.healthit.chpl.scheduler.job.report.attestation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttestationReport {
    private Long id;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate reportDate;

    private CertificationBody certificationBody;
    private AttestationPeriod attestationPeriod;

    @Builder.Default
    private Long developerCount = 0L;

    @Builder.Default
    private Long approvedCount = 0L;

    @Builder.Default
    private Long pendingAcbActionCount = 0L;

    @Builder.Default
    private Long pendingDeveloperActionCount = 0L;

    @Builder.Default
    private Long noSubmissionCount = 0L;

    @JsonIgnore
    @Builder.Default
    private List<Developer> developersWithApprovedAttestations = new ArrayList<Developer>();

    @JsonIgnore
    @Builder.Default
    private List<Developer> developersWithPendingAcbActionAttestations = new ArrayList<Developer>();

    @JsonIgnore
    @Builder.Default
    private List<Developer> developerWithPendingDeveloperActionAttestations = new ArrayList<Developer>();

    @JsonIgnore
    @Builder.Default
    private List<Developer> developersWithNoSubmissionAttestations = new ArrayList<Developer>();
}
