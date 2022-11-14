package gov.healthit.chpl.entity.surveillance;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.dto.CertificationEditionDTO;
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

    @Column(name = "removed")
    private Boolean removed;

    @Column(name = "classification")
    private String classification;

    public NonconformityType toDomain() {
        return NonconformityType.builder()
                .id(this.getId())
                .certificationEdition(this.getCertificationEdition() != null ? new CertificationEdition(new CertificationEditionDTO(this.getCertificationEdition())) : null)
                .number(this.getNumber())
                .removed(this.getRemoved())
                .title(this.getTitle())
                .classification(NonconformityClassification.valueOf(this.classification))
                .build();
    }
}
