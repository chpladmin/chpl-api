package gov.healthit.chpl.entity.listing.pending;

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

import gov.healthit.chpl.optionalStandard.entity.OptionalStandardEntity;
import lombok.Data;

@Data
@Entity
@Table(name = "pending_certification_result_optional_standard")
public class PendingCertificationResultOptionalStandardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_certification_result_optional_standard_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Long pendingCertificationResultId;

    @Column(name = "optional_standard_id")
    private Long optionalStandardId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "optional_standard_id", unique = true, nullable = true, insertable = false, updatable = false)
    private OptionalStandardEntity optionalStandard;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}
