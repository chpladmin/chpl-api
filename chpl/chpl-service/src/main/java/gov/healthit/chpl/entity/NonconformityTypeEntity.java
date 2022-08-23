package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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

    @Column(length = 15)
    private String number;

    @Column(length = 250)
    private String title;

    @Column(name = "removed")
    private Boolean removed;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

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
