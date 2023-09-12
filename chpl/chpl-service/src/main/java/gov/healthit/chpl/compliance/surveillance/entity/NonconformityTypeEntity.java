package gov.healthit.chpl.compliance.surveillance.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.entity.CertificationEditionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "nonconformity_type")
public class NonconformityTypeEntity implements Serializable {
    private static final long serialVersionUID = 7042222696641931650L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @OneToOne(optional = true)
    @JoinColumn(name = "certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity certificationEdition;

    @Column(name = "number")
    private String number;

    @Column(name = "title")
    private String title;

    @Basic(optional = true)
    @Column(name = "start_day")
    private LocalDate startDay;

    @Basic(optional = true)
    @Column(name = "end_day")
    private LocalDate endDay;

    @Column(name = "classification")
    private String classification;

    public NonconformityType toDomain() {
        return NonconformityType.builder()
                .id(this.getId())
                .certificationEdition(this.getCertificationEdition() != null ? this.getCertificationEdition().toDomain() : null)
                .number(this.getNumber())
                .startDay(this.getStartDay())
                .endDay(this.getEndDay())
                .title(this.getTitle())
                .classification(NonconformityClassification.valueOf(this.classification))
                .build();
    }
}
