package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import lombok.Data;

@Entity
@Table(name = "promoting_interoperability_user")
@Data
public class PromotingInteroperabilityUserEntity implements Serializable {
    private static final long serialVersionUID = -1463562876433665214L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "certified_product_id")
    private Long listingId;

    @Basic(optional = false)
    @Column(name = "user_count")
    private Long userCount;

    @Basic(optional = false)
    @Column(name = "user_count_date")
    private LocalDate userCountDate;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public PromotingInteroperabilityUser toDomain() {
        return PromotingInteroperabilityUser.builder()
                .id(this.getId())
                .userCount(this.getUserCount())
                .userCountDate(this.getUserCountDate())
        .build();
    }
}
