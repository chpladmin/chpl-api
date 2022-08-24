package gov.healthit.chpl.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.NonconformityType;
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

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity certificationEdition;

    @Column(length = 15)
    private String number;

    @Column(length = 250)
    private String title;

    @Column(name = "removed")
    private Boolean removed;

    public NonconformityType toDomain() {
        return NonconformityType.builder()
                .id(this.getId())
                .certificationEditionId(this.getCertificationEditionId())
                .number(this.getNumber())
                .removed(this.getRemoved())
                .title(this.getTitle())
                .build();
    }
}
