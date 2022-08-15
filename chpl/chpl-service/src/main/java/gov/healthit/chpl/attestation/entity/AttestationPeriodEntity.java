package gov.healthit.chpl.attestation.entity;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.form.entity.FormEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "attestation_period")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttestationPeriodEntity {

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

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

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
