package gov.healthit.chpl.entity.listing;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.entity.TestingLabEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "certified_product_testing_lab_map")
public class CertifiedProductTestingLabMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;

    @Basic(optional = false)
    @Column(name = "testing_lab_id", nullable = false)
    private Long testingLabId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "testing_lab_id", unique = true, nullable = true, insertable = false, updatable = false)
    private TestingLabEntity testingLab;

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public CertifiedProductTestingLab toDomain() {
        return CertifiedProductTestingLab.builder()
                .id(id)
                .testingLabId(testingLabId)
                .testingLabName(testingLab.getName())
                .testingLabCode(testingLab.getTestingLabCode())
                .testingLab(testingLab.toDomain())
                .build();
    }
}
