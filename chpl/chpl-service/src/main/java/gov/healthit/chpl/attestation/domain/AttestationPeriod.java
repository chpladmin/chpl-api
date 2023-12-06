package gov.healthit.chpl.attestation.domain;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AttestationPeriod implements Serializable {
    private static final long serialVersionUID = 6251042464421884050L;

    @Schema(description = "The internal ID of the attestation period.")
    private Long id;

    @Schema(description = "The starting date for which the submitted Attestations are based.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate periodStart;

    @Schema(description = "The ending date for which the submitted Attestations are based.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate periodEnd;

    @JsonIgnore
    private LocalDate submissionStart;

    @JsonIgnore
    private LocalDate submissionEnd;

    @JsonIgnore
    private Form form;

    @Schema(description = "A description of the attestation period.")
    private String description;

    public AttestationPeriod(AttestationPeriodEntity entity) {
        this.id = entity.getId();
        this.form = entity.getForm() != null ? entity.getForm().toDomain() : null;
        this.periodStart = entity.getPeriodStart();
        this.periodEnd = entity.getPeriodEnd();
        this.submissionEnd = entity.getSubmissionEnd();
        this.submissionStart = entity.getSubmissionStart();
        this.description = entity.getDescription();
    }
}
