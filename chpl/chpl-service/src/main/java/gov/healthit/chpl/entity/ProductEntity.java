package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "product")
public class ProductEntity implements Serializable {
    private static final long serialVersionUID = -5332080900089062551L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "product_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "name")
    private String name;

    @Basic(optional = true)
    @Column(name = "contact_id")
    private Long contactId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", unique = true, nullable = true, insertable = false, updatable = false)
    private ContactEntity contact;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productId")
    @Basic(optional = false)
    @Column(name = "product_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<ProductVersionEntity> productVersions = new HashSet<ProductVersionEntity>();

    @Basic(optional = true)
    @Column(name = "report_file_location", length = 255)
    private String reportFileLocation;

    @Basic(optional = false)
    @Column(name = "vendor_id", nullable = false)
    private Long developerId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", unique = true, nullable = true, insertable = false, updatable = false)
    private DeveloperEntity developer;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @MapsId("id")
    @JoinColumn(name = "product_id", unique = true, nullable = true, insertable = false, updatable = false)
    @Where(clause = "deleted <> 'true'")
    private ProductCertificationStatusesEntity productCertificationStatuses;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productId")
    @Basic(optional = true)
    @Column(name = "product_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<ProductActiveOwnerEntity> ownerHistory = new HashSet<ProductActiveOwnerEntity>();

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public Product toDomain() {
        return Product.builder()
                .productId(this.getId())
                .id(this.getId())
                .contact(this.getContact() != null ? this.getContact().toDomain() : null)
                .lastModifiedDate(this.getLastModifiedDate().getTime() + "")
                .name(this.getName())
                .owner(this.getDeveloper() != null ? this.getDeveloper().toDomain() : null)
                .reportFileLocation(this.getReportFileLocation())
                .ownerHistory(toOwnerHistoryDomains())
                .build();
    }

    private List<ProductOwner> toOwnerHistoryDomains() {
        if (CollectionUtils.isEmpty(this.getOwnerHistory())) {
            return new ArrayList<ProductOwner>();
        }
        return this.getOwnerHistory().stream()
            .map(ownerHistoryItem -> ownerHistoryItem.toDomain())
            .collect(Collectors.toList());
    }
}
