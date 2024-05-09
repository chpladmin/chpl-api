package gov.healthit.chpl.attestation.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.form.entity.FormEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attestation_period")
public class AttestationPeriodEntity extends EntityAudit {
    private static final long serialVersionUID = -3165977425400423424L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = true, insertable = false, updatable = false)
    private FormEntity form;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "submission_start")
    private LocalDate submissionStart;

    @Column(name = "submission_end")
    private LocalDate submissionEnd;

    @Column(name = "description")
    private String description;

    public AttestationPeriod toDomain() {
        return AttestationPeriod.builder()
                .id(this.getId())
                .form(form != null ? form.toDomain() : null)
                .periodStart(this.getPeriodStart())
                .periodEnd(this.getPeriodEnd())
                .submissionStart(this.getSubmissionStart())
                .submissionEnd(this.getSubmissionEnd())
                .description(this.getDescription())
                .build();
    }

}
