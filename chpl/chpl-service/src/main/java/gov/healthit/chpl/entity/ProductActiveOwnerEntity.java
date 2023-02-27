package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.time.LocalDate;
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

import org.hibernate.annotations.DynamicUpdate;

import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "product_active_owner_history_map")
public class ProductActiveOwnerEntity implements Serializable {
    private static final long serialVersionUID = -8325348768063869639L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "vendor_id")
    private Long developerId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", unique = true, nullable = true, insertable = false, updatable = false)
    private DeveloperEntity developer;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "transfer_date")
    private LocalDate transferDay;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public ProductOwner toDomain() {
        return ProductOwner.builder()
                .id(this.getId())
                .developer(this.getDeveloper() != null ? this.getDeveloper().toDomain() : null)
                .transferDay(this.getTransferDay())
                .transferDate(DateUtil.toDate(this.getTransferDay()).getTime())
                .build();
    }
}
